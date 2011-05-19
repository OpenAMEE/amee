/*
 * A script to migrate data from the ITEM_VALUE table to the DATA_ITEM_*_VALUE and PROFILE_ITEM_*_VALUE tables.
 *
 * To redirect stdout and stderr: groovy item_value_migrate.groovy 1> out.log 2> err.log
 *
 * This script requires the MySQL connector. Copy the jar to ~/.groovy/lib/
 *
 * To generate a report of problem values, use this awk script:
 * awk -F ',' '{ values[$4]++ } END { for(value in values) { print value, values[value] } }' item_value.err 
 *
 * To generate a CSV report from the above:
 * awk '{print $14}' item_value.err | tr -d '\n\' | sed 's/,$//' | sed 's/^/select p.ID as PROFILE_ID, u.NAME as USER_NAME, iv.ID, ivd.ID, ivd.VALUE_DEFINITION_ID, id.NAME AS ITEM_NAME, ivd.NAME as VAL_NAME, iv.VALUE from ITEM_VALUE iv join ITEM i on iv.ITEM_ID = i.ID join ITEM_VALUE_DEFINITION ivd on iv.ITEM_VALUE_DEFINITION_ID = ivd.ID join ITEM_DEFINITION id on ivd.ITEM_DEFINITION_ID = id.ID join PROFILE p on i.PROFILE_ID = p.ID join USER u on p.USER_ID = u.ID where iv.ID in (/;s/$/) into outfile "\/tmp\/iv.csv" FIELDS TERMINATED BY "\t";/' | mysql amee
 *
 */

import groovy.sql.Sql
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

@Grab(group='net.sf.opencsv', module='opencsv', version='2.0')
import au.com.bytecode.opencsv.CSVWriter;

def numberValueBatch = 1000
def numberValueBatchCount = 0
def textValueBatch = 1000
def textValueBatchCount = 0
def numberValueHistoryBatch = 1000
def numberValueHistoryBatchCount = 0
def textValueHistoryBatch = 1000
def textValueHistoryBatchCount = 0

def valueTypes = ["UNSPECIFIED", "TEXT", "DATE", "BOOLEAN", "INTEGER", "DECIMAL"]

// Handle command line parameters
def cli = configureCliBuilder()
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    return
}

// CSV support.
writeToCSV = opt.c ?: false
batchObjects = []
profileItemNumberValueWriter = null
profileItemTextValueWriter = null
dataItemNumberValueWriter = null
dataItemNumberValueHistoryWriter = null
dataItemTextValueWriter = null
dataItemTextValueHistoryWriter = null
if (writeToCSV) {
  profileItemNumberValueWriter = new CSVWriter(new FileWriter("PROFILE_ITEM_NUMBER_VALUE.txt"), ",".charAt(0),  "\"".charAt(0));
  profileItemTextValueWriter = new CSVWriter(new FileWriter("PROFILE_ITEM_TEXT_VALUE.txt"), ",".charAt(0),  "\"".charAt(0));
  dataItemNumberValueWriter = new CSVWriter(new FileWriter("DATA_ITEM_NUMBER_VALUE.txt"), ",".charAt(0),  "\"".charAt(0));
  dataItemNumberValueHistoryWriter = new CSVWriter(new FileWriter("DATA_ITEM_NUMBER_VALUE_HISTORY.txt"), ",".charAt(0),  "\"".charAt(0));
  dataItemTextValueWriter = new CSVWriter(new FileWriter("DATA_ITEM_TEXT_VALUE.txt"), ",".charAt(0),  "\"".charAt(0));
  dataItemTextValueHistoryWriter = new CSVWriter(new FileWriter("DATA_ITEM_TEXT_VALUE_HISTORY.txt"), ",".charAt(0),  "\"".charAt(0));
}

// Database options.
def server = opt.s ?: "localhost"
def database = opt.d ?: "amee"
def user = opt.u ?: "amee"
def password = opt.p ?: "amee"
def dryRun = opt.r ?: false

log "Started item value migration."
start = System.currentTimeMillis()

// Configure select DataSource.
sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")
sql.connection.autoCommit = false

// Configure insert DataSource.
sqlInsert = Sql.newInstance("jdbc:mysql://${server}:3306/${database}?rewriteBatchedStatements=true", user, password, "com.mysql.jdbc.Driver")
sqlInsert.connection.autoCommit = false

// Check for scrolling.
DatabaseMetaData dbmd = sql.connection.getMetaData();
int JDBCVersion = dbmd.getJDBCMajorVersion();
boolean srs = dbmd.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY);
if (JDBCVersion > 2 || srs == true) {
  // println "ResultSet scrolling is supported.";
} else {
  logError "ResultSet scrolling is NOT supported.";
  return;
}

// Get scrollable Statement.
Statement st = sql.connection.createStatement(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY);
st.setFetchSize(Integer.MIN_VALUE);

// Create a view for the profile item values
log "Creating profile_item_values view..."
sql.execute "CREATE OR REPLACE VIEW profile_item_values AS " +
    "SELECT iv.ID, iv.UID, iv.STATUS, iv.VALUE, iv.CREATED, iv.MODIFIED, i.ID as PROFILE_ITEM_ID, iv.ITEM_VALUE_DEFINITION_ID, vd.VALUE_TYPE, iv.UNIT, iv.PER_UNIT " +
    "FROM ITEM_VALUE AS iv JOIN ITEM i ON iv.ITEM_ID = i.ID " +
    "JOIN ITEM_VALUE_DEFINITION ivd ON iv.ITEM_VALUE_DEFINITION_ID = ivd.ID " +
    "JOIN VALUE_DEFINITION vd on ivd.VALUE_DEFINITION_ID = vd.ID " +
    "WHERE i.TYPE = 'PI'"

def profileItemNumberValueSql =
        "INSERT INTO PROFILE_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def profileItemNumberValueStatement = sqlInsert.connection.prepareStatement(profileItemNumberValueSql)

def profileItemTextValueSql =
        "INSERT INTO PROFILE_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
def profileItemTextValueStatement = sqlInsert.connection.prepareStatement(profileItemTextValueSql)

def rs = st.executeQuery("SELECT ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID, VALUE_TYPE, UNIT, PER_UNIT FROM profile_item_values")

while (rs.next()) {
    rowValType = rs.getInt("VALUE_TYPE")

    if (rowValType == valueTypes.indexOf("INTEGER") ||
        rowValType == valueTypes.indexOf("DECIMAL")) {

        // Handle numbers
        try {
            setBatchObject(profileItemNumberValueStatement, 1, rs.getLong("ID"))
            setBatchObject(profileItemNumberValueStatement, 2, rs.getString("UID"))
            setBatchObject(profileItemNumberValueStatement, 3, rs.getInt("STATUS"))
            def rowVal = rs.getString("VALUE")

            // Handle empty values
            if (rowVal == "" || rowVal == "-") {
                setBatchObject(profileItemNumberValueStatement, 4, null)
            } else if (Double.parseDouble(rowVal).infinite || Double.parseDouble(rowVal).naN) {

                // Technically a Double but would throw an SQLException if we tried to insert these values.
                throw new NumberFormatException()
            } else {

                // By now the value must be good
                setBatchObject(profileItemNumberValueStatement, 4, Double.parseDouble(rowVal))
            }
            setBatchObject(profileItemNumberValueStatement, 5, rs.getTimestamp("CREATED"))
            setBatchObject(profileItemNumberValueStatement, 6, rs.getTimestamp("MODIFIED"))
            setBatchObject(profileItemNumberValueStatement, 7, rs.getLong("PROFILE_ITEM_ID"))
            setBatchObject(profileItemNumberValueStatement, 8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
            def rowUnit = rs.getString("UNIT")
            setBatchObject(profileItemNumberValueStatement, 9, (rowUnit ? rowUnit : ''))
            def perUnit = rs.getString("PER_UNIT")
            setBatchObject(profileItemNumberValueStatement, 10, (perUnit ? perUnit : ''))

            addBatch(profileItemNumberValueStatement, profileItemNumberValueWriter)
            numberValueBatchCount++

            if (numberValueBatchCount >= numberValueBatch) {
                // Execute this batch.
                if (dryRun) {
                    profileItemNumberValueStatement.clearBatch()
                } else {
                    executeBatch(profileItemNumberValueStatement)
                }
                log "Created ${numberValueBatch} PROFILE_ITEM_NUMBER_VALUEs in a batch."
                numberValueBatchCount = 0
            }
        } catch (NumberFormatException e) {
            logError "Error parsing PROFILE_ITEM value as double. ITEM_VALUE.ID: ${rs.getString('ID')}, ITEM_VALUE.UID: ${rs.getString('UID')}, " +
                "ITEM_VALUE_DEFINITION_ID: ${rs.getString('ITEM_VALUE_DEFINITION_ID')}, VALUE: '${rs.getString('VALUE')}'"
            batchObjects = []
        }
    } else {
        
        // Handle text
        setBatchObject(profileItemTextValueStatement, 1, rs.getLong("ID"))
        setBatchObject(profileItemTextValueStatement, 2, rs.getString("UID"))
        setBatchObject(profileItemTextValueStatement, 3, rs.getInt("STATUS"))

        // Truncate any strings > 32767
        value = rs.getString("VALUE")
        if (value.size() > 32767) {
            logError "Truncating PROFILE_ITEM string value. ID: ${rs.getString('ID')}"
            setBatchObject(profileItemTextValueStatement, 4, value[0..32766])
        } else {
            setBatchObject(profileItemTextValueStatement, 4, value)
        }
        setBatchObject(profileItemTextValueStatement, 5, rs.getTimestamp("CREATED"))
        setBatchObject(profileItemTextValueStatement, 6, rs.getTimestamp("MODIFIED"))
        setBatchObject(profileItemTextValueStatement, 7, rs.getLong("PROFILE_ITEM_ID"))
        setBatchObject(profileItemTextValueStatement, 8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))

        addBatch(profileItemTextValueStatement, profileItemTextValueWriter)
        textValueBatchCount++

        if (textValueBatchCount >= textValueBatch) {
            // Execute this batch.
            if (dryRun) {
                profileItemTextValueStatement.clearBatch()
            } else {
                executeBatch(profileItemTextValueStatement)
            }
            log "Created ${textValueBatch} PROFILE_ITEM_TEXT_VALUEs in a batch."
            textValueBatchCount = 0
        }
    }
}

// Handle remaining Item Values in current batch.
if (numberValueBatchCount > 0) {
    if (dryRun) {
        profileItemNumberValueStatement.clearBatch()
    } else {
        profileItemNumberValueStatement.executeBatch()
    }
    log "Created ${numberValueBatchCount} PROFILE_ITEM_NUMBER_VALUEs in a batch."
    numberValueBatchCount = 0
}
if (textValueBatchCount > 0) {
    if (dryRun) {
        profileItemTextValueStatement.clearBatch()
    } else {
        profileItemTextValueStatement.executeBatch()
    }
    log "Created ${textValueBatchCount} PROFILE_ITEM_TEXT_VALUEs in a batch."
    textValueBatchCount = 0
}

// Commit the profile item data
if (dryRun) {
    sql.rollback()
    sqlInsert.rollback()
} else {
    commit()
}

// Get scrollable Statement.
st = sql.connection.createStatement(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY);
st.setFetchSize(Integer.MIN_VALUE);

// Create a view for the data item values
log "Creating data_item_values view..."
sql.execute "CREATE OR REPLACE VIEW data_item_values AS " +
    "SELECT iv.ID, iv.UID, iv.STATUS, iv.VALUE, iv.CREATED, iv.MODIFIED, i.ID as DATA_ITEM_ID, iv.ITEM_VALUE_DEFINITION_ID, vd.VALUE_TYPE, iv.UNIT, iv.PER_UNIT, iv.START_DATE " +
    "FROM ITEM_VALUE AS iv JOIN ITEM i ON iv.ITEM_ID = i.ID " +
    "JOIN ITEM_VALUE_DEFINITION ivd ON iv.ITEM_VALUE_DEFINITION_ID = ivd.ID " +
    "JOIN VALUE_DEFINITION vd on ivd.VALUE_DEFINITION_ID = vd.ID " +
    "WHERE i.TYPE = 'DI'"

def dataItemNumberValueSql =
        "INSERT INTO DATA_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, UNIT, PER_UNIT) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemNumberValueStatement = sqlInsert.connection.prepareStatement(dataItemNumberValueSql)

def dataItemTextValueSql =
        "INSERT INTO DATA_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemTextValueStatement = sqlInsert.connection.prepareStatement(dataItemTextValueSql)

def dataItemNumberValueHistorySql =
        "INSERT INTO DATA_ITEM_NUMBER_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, UNIT, PER_UNIT, START_DATE) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemNumberValueHistoryStatement = sqlInsert.connection.prepareStatement(dataItemNumberValueHistorySql)

def dataItemTextValueHistorySql =
        "INSERT INTO DATA_ITEM_TEXT_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, START_DATE) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemTextValueHistoryStatement = sqlInsert.connection.prepareStatement(dataItemTextValueHistorySql)

rs = st.executeQuery("SELECT ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, VALUE_TYPE, UNIT, PER_UNIT, START_DATE FROM data_item_values")

while (rs.next()) {
    rowValType = rs.getInt("VALUE_TYPE")

    if (rowValType == valueTypes.indexOf("INTEGER") ||
        rowValType == valueTypes.indexOf("DECIMAL")) {

        // Handle numbers
        try {

            if (rs.getTimestamp("START_DATE").toString().equals('1970-01-01 00:00:00.0')) {

                // Add to standard table
                setBatchObject(dataItemNumberValueStatement, 1, rs.getLong("ID"))
                setBatchObject(dataItemNumberValueStatement, 2, rs.getString("UID"))
                setBatchObject(dataItemNumberValueStatement, 3, rs.getInt("STATUS"))
                def rowVal = rs.getString("VALUE")

                // Handle empty values
                if (rowVal == "" || rowVal == "-") {
                    setBatchObject(dataItemNumberValueStatement, 4, null)
                } else if (Double.parseDouble(rowVal).infinite || Double.parseDouble(rowVal).naN) {

                    // Technically a Double but would throw an SQLException if we tried to insert these values.
                    throw new NumberFormatException()
                } else {

                    // By now the value must be good
                    setBatchObject(dataItemNumberValueStatement, 4, Double.parseDouble(rowVal))
                }
                setBatchObject(dataItemNumberValueStatement, 5, rs.getTimestamp("CREATED"))
                setBatchObject(dataItemNumberValueStatement, 6, rs.getTimestamp("MODIFIED"))
                setBatchObject(dataItemNumberValueStatement, 7, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                setBatchObject(dataItemNumberValueStatement, 8, rs.getLong("DATA_ITEM_ID"))
                def rowUnit = rs.getString("UNIT")
                setBatchObject(dataItemNumberValueStatement, 9, (rowUnit ? rowUnit : ''))
                def perUnit = rs.getString("PER_UNIT")
                setBatchObject(dataItemNumberValueStatement, 10, (perUnit ? perUnit : ''))

                addBatch(dataItemNumberValueStatement, dataItemNumberValueWriter)
                numberValueBatchCount++

                if (numberValueBatchCount >= numberValueBatch) {
                    // Execute this batch.
                    if (dryRun) {
                        dataItemNumberValueStatement.clearBatch()
                    } else {
                        executeBatch(dataItemNumberValueStatement)
                    }
                    log "Created ${numberValueBatch} DATA_ITEM_NUMBER_VALUEs in a batch."
                    numberValueBatchCount = 0
                }
            } else {

                // Add to history table
                setBatchObject(dataItemNumberValueHistoryStatement, 1, rs.getLong("ID"))
                setBatchObject(dataItemNumberValueHistoryStatement, 2, rs.getString("UID"))
                setBatchObject(dataItemNumberValueHistoryStatement, 3, rs.getInt("STATUS"))
                def rowVal = rs.getString("VALUE")
                setBatchObject(dataItemNumberValueHistoryStatement, 4, rowVal == "" || rowVal == "-" ? null : Double.parseDouble(rowVal))
                setBatchObject(dataItemNumberValueHistoryStatement, 5, rs.getTimestamp("CREATED"))
                setBatchObject(dataItemNumberValueHistoryStatement, 6, rs.getTimestamp("MODIFIED"))
                setBatchObject(dataItemNumberValueHistoryStatement, 7, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                setBatchObject(dataItemNumberValueHistoryStatement, 8, rs.getLong("DATA_ITEM_ID"))
                def rowUnit = rs.getString("UNIT")
                setBatchObject(dataItemNumberValueHistoryStatement, 9, (rowUnit ? rowUnit : ''))
                def perUnit = rs.getString("PER_UNIT")
                setBatchObject(dataItemNumberValueHistoryStatement, 10, (perUnit ? perUnit : ''))
                setBatchObject(dataItemNumberValueHistoryStatement, 11, rs.getTimestamp("START_DATE"))

                addBatch(dataItemNumberValueHistoryStatement, dataItemNumberValueHistoryWriter)
                numberValueHistoryBatchCount++

                if (numberValueHistoryBatchCount >= numberValueHistoryBatch) {
                    // Execute this batch.
                    if (dryRun) {
                        dataItemNumberValueHistoryStatement.clearBatch()
                    } else {
                        executeBatch(dataItemNumberValueHistoryStatement)
                    }
                    log "Created ${numberValueHistoryBatch} DATA_ITEM_NUMBER_VALUE_HISTORYs in a batch."
                    numberValueHistoryBatchCount = 0
                }
            }
        } catch (NumberFormatException e) {
            logError "Error parsing DATA_ITEM value as double. ITEM_VALUE.ID: ${rs.getLong("ID")}, ITEM_VALUE.UID: ${rs.getString("UID")}, " +
                "ITEM_VALUE_DEFINITION_ID: ${rs.getLong("ITEM_VALUE_DEFINITION_ID")}, VALUE: '${rs.getString("VALUE")}'"
            batchObjects = []
        }
    } else {

        // Handle text
        if (rs.getTimestamp("START_DATE").toString().equals('1970-01-01 00:00:00.0')) {
            setBatchObject(dataItemTextValueStatement, 1, rs.getLong("ID"))
            setBatchObject(dataItemTextValueStatement, 2, rs.getString("UID"))
            setBatchObject(dataItemTextValueStatement, 3, rs.getInt("STATUS"))

            // Truncate any strings > 32767
            value = rs.getString("VALUE")
            if (value.size() > 32767) {
                logError "Truncating DATA_ITEM string value. ID: ${rs.getLong("ID")}"
                setBatchObject(dataItemTextValueStatement, 4, value[0..32766])
            } else {
                setBatchObject(dataItemTextValueStatement, 4, value)
            }
            setBatchObject(dataItemTextValueStatement, 5, rs.getTimestamp("CREATED"))
            setBatchObject(dataItemTextValueStatement, 6, rs.getTimestamp("MODIFIED"))
            setBatchObject(dataItemTextValueStatement, 7, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
            setBatchObject(dataItemTextValueStatement, 8, rs.getLong("DATA_ITEM_ID"))

            addBatch(dataItemTextValueStatement, dataItemTextValueWriter)
            textValueBatchCount++

            if (textValueBatchCount >= textValueBatch) {
                // Execute this batch.
                if (dryRun) {
                    dataItemTextValueStatement.clearBatch()
                } else {
                    executeBatch(dataItemTextValueStatement)
                }
                log "Created ${textValueBatch} DATA_ITEM_TEXT_VALUEs in a batch."
                textValueBatchCount = 0
            }
        } else {
            setBatchObject(dataItemTextValueHistoryStatement, 1, rs.getLong("ID"))
            setBatchObject(dataItemTextValueHistoryStatement, 2, rs.getString("UID"))
            setBatchObject(dataItemTextValueHistoryStatement, 3, rs.getInt("STATUS"))

            // Truncate any strings > 32767
            value = rs.getString("VALUE")
            if (value.size() > 32767) {
                logError "Truncating DATA_ITEM string value. ID: ${rs.getLong("ID")}"
                setBatchObject(dataItemTextValueHistoryStatement, 4, value[0..32766])
            } else {
                setBatchObject(dataItemTextValueHistoryStatement, 4, value)
            }
            setBatchObject(dataItemTextValueHistoryStatement, 5, rs.getTimestamp("CREATED"))
            setBatchObject(dataItemTextValueHistoryStatement, 6, rs.getTimestamp("MODIFIED"))
            setBatchObject(dataItemTextValueHistoryStatement, 7, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
            setBatchObject(dataItemTextValueHistoryStatement, 8, rs.getLong("DATA_ITEM_ID"))
            setBatchObject(dataItemTextValueHistoryStatement, 9, rs.getTimestamp("START_DATE"))

            addBatch(dataItemTextValueHistoryStatement, dataItemTextValueHistoryWriter)
            textValueHistoryBatchCount++

            if (textValueHistoryBatchCount >= textValueHistoryBatch) {
                // Execute this batch.
                if (dryRun) {
                    dataItemTextValueHistoryStatement.clearBatch()
                } else {
                    executeBatch(dataItemTextValueHistoryStatement)
                }
                log "Created ${textValueHistoryBatch} DATA_ITEM_TEXT_VALUE_HISTORYs in a batch."
                textValueHistoryBatchCount = 0
            }
        }
    }
}

// Handle remaining Item Values in current batch.
if (numberValueBatchCount > 0) {
    if (dryRun) {
        dataItemNumberValueStatement.clearBatch()
    } else {
        dataItemNumberValueStatement.executeBatch()
    }
    log "Created ${numberValueBatchCount} DATA_ITEM_NUMBER_VALUEs in a batch."
    numberValueBatchCount = 0
}
if (textValueBatchCount > 0) {
    if (dryRun) {
        dataItemTextValueStatement.clearBatch()
    } else {
        dataItemTextValueStatement.executeBatch()
    }
    log "Created ${textValueBatchCount} DATA_ITEM_TEXT_VALUEs in a batch."
    textValueBatchCount = 0
}
if (numberValueHistoryBatchCount > 0) {
    if (dryRun) {
        dataItemNumberValueHistoryStatement.clearBatch()
    } else {
        dataItemNumberValueHistoryStatement.executeBatch()
    }
    log "Created ${numberValueHistoryBatchCount} DATA_ITEM_NUMBER_VALUE_HISTORYs in a batch."
    numberValueHistoryBatchCount = 0
}
if (textValueHistoryBatchCount > 0) {
    if (dryRun) {
        dataItemTextValueHistoryStatement.clearBatch()
    } else {
        dataItemTextValueHistoryStatement.executeBatch()
    }
    log "Created ${textValueHistoryBatchCount} DATA_ITEM_TEXT_VALUE_HISTORYs in a batch."
    textValueHistoryBatchCount = 0
}

// Commit the data item data
if (dryRun) {
    sql.rollback()
    sqlInsert.rollback()
} else {
    sql.commit()
    sqlInsert.commit()
}

// Close CSV files.
if (writeToCSV) {
    profileItemNumberValueWriter.close()
    profileItemTextValueWriter.close()
    dataItemNumberValueWriter.close()
    dataItemNumberValueHistoryWriter.close()
    dataItemTextValueWriter.close()
    dataItemTextValueHistoryWriter.close()
}

log "Finished item value migration. Took ${(System.currentTimeMillis() - start * 1000) / 60} minutes."

def configureCliBuilder() {
    def cli = new CliBuilder(usage: 'groovy item_migrate.groovy [-h] [-s server] [-d database] [-u user] [-p password] [-r] [-c]')
    cli.h(longOpt: 'help', 'usage information')
    cli.s(argName: 'servername', longOpt: 'server', args: 1, required: false, type: GString, "server name (default 'localhost')")
    cli.d(argName: 'database', longOpt: 'database', args: 1, required: false, type: GString, "database name (default 'amee')")
    cli.u(argName: 'user', longOpt: 'user', args: 1, required: false, type: GString, "username (default 'amee')")
    cli.p(argName: 'password', longOpt: 'password', args: 1, required: false, type: GString, "password (default 'amee')")
    cli.r(argName: 'dryrun', longOpt: 'dryrun', args: 0, required: false, type: GString, "dry-run (does not commit data)")
    cli.c(argName: 'csv', longOpt: 'csv', args: 0, required: false, type: GString, "write to csv (does not commit data)")
    return cli
}

def log(message) {
    println new Date().toString() + ' ' + message
}

def logError(message) {
    System.err.println new Date().toString() + ' ' + message
}

def commit() {
    if (!writeToCSV) {
        sql.commit()
        sqlInsert.commit()
    } else {
        profileItemNumberValueWriter.flush()
        profileItemTextValueWriter.flush()
        dataItemNumberValueWriter.flush()
        dataItemNumberValueHistoryWriter.flush()
        dataItemTextValueWriter.flush()
        dataItemTextValueHistoryWriter.flush()
    }
}

def executeBatch(statement) {
    if (!writeToCSV) {
        statement.executeBatch()
    }
}

def addBatch(statement, writer) {
    if (!writeToCSV) {
        statement.addBatch()
    } else {
        writer.writeNext(toStringArray(batchObjects))
        batchObjects = []
    }
}

def setBatchObject(statement, index, object) {
    if (!writeToCSV) {
        statement.setObject(index, object)
    } else {
        if (object != null) {
            batchObjects.add(object.toString())
        } else {
            batchObjects.add(object)
        }
    }
}

def String[] toStringArray(List params) {
    List<String> results = new ArrayList<String>()
    params.each { object ->
        if (object != null) {
            results.add(object.toString())
        } else {
            results.add("\\N")
        }
    }
    return results.toArray(new String[0])
}