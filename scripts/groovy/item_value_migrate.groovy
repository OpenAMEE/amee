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
 * awk '{print $8}' item_value.err | tr -d '\n\' | sed 's/,$//' | sed 's/^/select p.ID as PROFILE_ID, u.NAME as USER_NAME, iv.ID, ivd.ID, ivd.VALUE_DEFINITION_ID, id.NAME AS ITEM_NAME, ivd.NAME as VAL_NAME, iv.VALUE from ITEM_VALUE iv join ITEM i on iv.ITEM_ID = i.ID join ITEM_VALUE_DEFINITION ivd on iv.ITEM_VALUE_DEFINITION_ID = ivd.ID join ITEM_DEFINITION id on ivd.ITEM_DEFINITION_ID = id.ID join PROFILE p on i.PROFILE_ID = p.ID join USER u on p.USER_ID = u.ID where iv.ID in (/;s/$/) into outfile "\/tmp\/iv.csv" FIELDS TERMINATED BY "\t";/' | mysql amee
 *
 */

import groovy.sql.Sql
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

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

// Database options.
def server = opt.s ?: "localhost"
def target = opt.t ?: "localhost"
def database = opt.d ?: "amee"
def userRead = opt.ur ?: "amee"
def passwordRead = opt.pr ?: "amee"
def userWrite = opt.uw ?: "amee"
def passwordWrite = opt.pw ?: "amee"
def dryRun = opt.r ?: false
def from = opt.f ?: "1970-01-01 00:00:00"

// Should we use REPLACE INTO?
def replace = opt.f ? true : false

log "Migrating data with modified date >= ${from}"

// Configure reader DataSource.
def sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", userRead, passwordRead, "com.mysql.jdbc.Driver")
sql.connection.autoCommit = false

// Configure writer DataSource.
def sqlInsert = Sql.newInstance("jdbc:mysql://${target}:3306/${database}?rewriteBatchedStatements=true", userWrite, passwordWrite, "com.mysql.jdbc.Driver")
sqlInsert.connection.autoCommit = false

// Check for scolling.
DatabaseMetaData dbmd = sql.connection.getMetaData();
int JDBCVersion = dbmd.getJDBCMajorVersion();
boolean srs = dbmd.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY);
if (JDBCVersion > 2 || srs == true) {
  // println "ResultSet scrolling is supported.";
} else {
  log "ResultSet scrolling is NOT supported.";
  return;
}

// Get scrollable Statement.
Statement st = sql.connection.createStatement(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY);
st.setFetchSize(Integer.MIN_VALUE);

// Create a view for the profile item values
sql.execute "CREATE OR REPLACE VIEW profile_item_values AS " +
    "SELECT iv.ID, iv.UID, iv.STATUS, iv.VALUE, iv.CREATED, iv.MODIFIED, i.ID as PROFILE_ITEM_ID, iv.ITEM_VALUE_DEFINITION_ID, vd.VALUE_TYPE, iv.UNIT, iv.PER_UNIT " +
    "FROM ITEM_VALUE AS iv JOIN ITEM i ON iv.ITEM_ID = i.ID " +
    "JOIN ITEM_VALUE_DEFINITION ivd ON iv.ITEM_VALUE_DEFINITION_ID = ivd.ID " +
    "JOIN VALUE_DEFINITION vd on ivd.VALUE_DEFINITION_ID = vd.ID " +
    "WHERE i.TYPE = 'PI'"

def profileItemNumberValueSql
if (replace) {
    profileItemNumberValueSql =
        "INSERT INTO PROFILE_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLCATE KEY UPDATE STATUS=VALUES(STATUS), VALUE=VALUES(VALUE), MODIFIED=VALUES(MODIFIED), PROFILE_ITEM_ID=VALUES(PROFILE_ITEM_ID), " +
        "ITEM_VALUE_DEFINITION_ID=VALUES(ITEM_VALUE_DEFINITION_ID), UNIT=VALUES(UNIT), PER_UNIT=VALUES(PER_UNIT)"
} else {
    profileItemNumberValueSql =
        "INSERT INTO PROFILE_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
}
def profileItemNumberValueStatement = sqlInsert.connection.prepareStatement(profileItemNumberValueSql)

def profileItemTextValueSql
if (replace) {
    profileItemTextValueSql =
        "INSERT INTO PROFILE_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE STATUS=VALUES(STATUS), VALUE=VALUES(VALUE), MODIFIED=VALUES(MODIFIED), PROFILE_ITEM_ID=VALUES(PROFILE_ITEM_ID), ITEM_VALUE_DEFINITION_ID=VALUES(ITEM_VALUE_DEFINITION_ID)"
} else {
    profileItemTextValueSql =
        "INSERT INTO PROFILE_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
}
def profileItemTextValueStatement = sqlInsert.connection.prepareStatement(profileItemTextValueSql)

def rs = st.executeQuery("SELECT ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID, VALUE_TYPE, UNIT, PER_UNIT FROM profile_item_values WHERE MODIFIED >= '${from}'")

while (rs.next()) {
    rowValType = rs.getInt("VALUE_TYPE")

    if (rowValType == valueTypes.indexOf("INTEGER") ||
        rowValType == valueTypes.indexOf("DECIMAL")) {

        // Handle numbers
        try {
            profileItemNumberValueStatement.with {
                setObject(1, rs.getLong("ID"))
                setObject(2, rs.getString("UID"))
                setObject(3, rs.getInt("STATUS"))
                def rowVal = rs.getString("VALUE")

                // Handle empty values
                if (rowVal == "" || rowVal == "-") {
                    setObject(4, null)
                } else if (Double.parseDouble(rowVal).infinite || Double.parseDouble(rowVal).naN) {

                    // Technically a Double but would throw an SQLException if we tried to insert these values.
                    throw new NumberFormatException()
                } else {

                    // By now the value must be good
                    setObject(4, Double.parseDouble(rowVal))
                }
                setObject(5, rs.getTimestamp("CREATED"))
                setObject(6, rs.getTimestamp("MODIFIED"))
                setObject(7, rs.getLong("PROFILE_ITEM_ID"))
                setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                def rowUnit = rs.getString("UNIT")
                setObject(9, (rowUnit ? rowUnit : ''))
                def perUnit = rs.getString("PER_UNIT")
                setObject(10, (perUnit ? perUnit : ''))

                addBatch()
                numberValueBatchCount++

                if (numberValueBatchCount >= numberValueBatch) {
                    // Execute this batch.
                    if (dryRun) {
                        clearBatch()
                    } else {
                        executeBatch()                        
                    }
                    log "Created ${numberValueBatch} PROFILE_ITEM_NUMBER_VALUEs in a batch."
                    numberValueBatchCount = 0
                }
            }
        } catch (NumberFormatException e) {
            logError "Error parsing PROFILE_ITEM value as double. ITEM_VALUE.ID: ${rs.getString('ID')}, ITEM_VALUE.UID: ${rs.getString('UID')}, " +
                "ITEM_VALUE_DEFINITION_ID: ${rs.getString('ITEM_VALUE_DEFINITION_ID')}, VALUE: '${rs.getString('VALUE')}'"
        }
    } else {
        
        // Handle text
        profileItemTextValueStatement.with {
            setObject(1, rs.getLong("ID"))
            setObject(2, rs.getString("UID"))
            setObject(3, rs.getInt("STATUS"))

            // Truncate any strings > 32767
            value = rs.getString("VALUE")
            if (value.size() > 32767) {
                logError "Truncating PROFILE_ITEM string value. ID: ${rs.getString('ID')}"
                setObject(4, value[0..32766])
            } else {
                setObject(4, value)
            }
            setObject(5, rs.getTimestamp("CREATED"))
            setObject(6, rs.getTimestamp("MODIFIED"))
            setObject(7, rs.getLong("PROFILE_ITEM_ID"))
            setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))

            addBatch()
            textValueBatchCount++

            if (textValueBatchCount >= textValueBatch) {
                // Execute this batch.
                if (dryRun) {
                    clearBatch()
                } else {
                    executeBatch()
                }
                log "Created ${textValueBatch} PROFILE_ITEM_TEXT_VALUEs in a batch."
                textValueBatchCount = 0
            }
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
    sql.commit()
    sqlInsert.commit()
}

// Get scrollable Statement.
st = sql.connection.createStatement(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY);
st.setFetchSize(Integer.MIN_VALUE);

// Create a view for the data item values
sql.execute "CREATE OR REPLACE VIEW data_item_values AS " +
    "SELECT iv.ID, iv.UID, iv.STATUS, iv.VALUE, iv.CREATED, iv.MODIFIED, i.ID as DATA_ITEM_ID, iv.ITEM_VALUE_DEFINITION_ID, vd.VALUE_TYPE, iv.UNIT, iv.PER_UNIT, iv.START_DATE " +
    "FROM ITEM_VALUE AS iv JOIN ITEM i ON iv.ITEM_ID = i.ID " +
    "JOIN ITEM_VALUE_DEFINITION ivd ON iv.ITEM_VALUE_DEFINITION_ID = ivd.ID " +
    "JOIN VALUE_DEFINITION vd on ivd.VALUE_DEFINITION_ID = vd.ID " +
    "WHERE i.TYPE = 'DI'"

def dataItemNumberValueSql
if (replace) {
    dataItemNumberValueSql =
        "INSERT INTO DATA_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE STATUS=VALUES(STATUS), VALUE=VALUES(VALUE), MODIFIED=VALUES(MODIFIED), DATA_ITEM_ID=VALUES(DATA_ITEM_ID), " +
        "ITEM_VALUE_DEFINITION_ID=VALUES(ITEM_VALUE_DEFINITION_ID), UNIT=VALUES(UNIT), PER_UNIT=VALUES(PER_UNIT)"
} else {
    dataItemNumberValueSql =
        "INSERT INTO DATA_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
}
def dataItemNumberValueStatement = sqlInsert.connection.prepareStatement(dataItemNumberValueSql)

def dataItemTextValueSql
if (replace) {
    dataItemTextValueSql =
        "INSERT INTO DATA_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE STATUS=VALUES(STATUS), VALUE=VALUES(VALUE), MODIFIED=VALUES(MODIFIED), DATA_ITEM_ID=VALUES(DATA_ITEM_ID), ITEM_VALUE_DEFINITION_ID=VALUES(ITEM_VALUE_DEFINITION_ID)"
} else {
    dataItemTextValueSql =
        "INSERT INTO DATA_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
}
def dataItemTextValueStatement = sqlInsert.connection.prepareStatement(dataItemTextValueSql)

def dataItemNumberValueHistorySql
if (replace) {
    dataItemNumberValueHistorySql =
        "INSERT INTO DATA_ITEM_NUMBER_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT, START_DATE) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE STATUS=VALUES(STATUS), VALUE=VALUES(VALUE), MODIFIED=VALUES(MODIFIED), DATA_ITEM_ID=VALUES(DATA_ITEM_ID), " +
        "ITEM_VALUE_DEFINITION_ID=VALUES(ITEM_VALUE_DEFINITION_ID), UNIT=VALUES(UNIT), PER_UNIT=VALUES(PER_UNIT), START_DATE=VALUES(START_DATE)"
} else {
    dataItemNumberValueHistorySql =
        "INSERT INTO DATA_ITEM_NUMBER_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT, START_DATE) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
}
def dataItemNumberValueHistoryStatement = sqlInsert.connection.prepareStatement(dataItemNumberValueHistorySql)

def dataItemTextValueHistorySql
if (replace) {
    dataItemTextValueHistorySql =
        "INSERT INTO DATA_ITEM_TEXT_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, START_DATE) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE STATUS=VALUES(STATUS), VALUE=VALUES(VALUE), MODIFIED=VALUES(MODIFIED), DATA_ITEM_ID=VALUES(DATA_ITEM_ID), " +
        "ITEM_VALUE_DEFINITION_ID=VALUES(ITEM_VALUE_DEFINITION_ID), START_DATE=VALUES(START_DATE)"
} else {
    dataItemTextValueHistorySql =
        "INSERT INTO DATA_ITEM_TEXT_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, START_DATE) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
}
def dataItemTextValueHistoryStatement = sqlInsert.connection.prepareStatement(dataItemTextValueHistorySql)

rs = st.executeQuery("SELECT ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, VALUE_TYPE, UNIT, PER_UNIT, START_DATE FROM data_item_values WHERE MODIFIED >= '${from}'")

while (rs.next()) {
    rowValType = rs.getInt("VALUE_TYPE")

    if (rowValType == valueTypes.indexOf("INTEGER") ||
        rowValType == valueTypes.indexOf("DECIMAL")) {

        // Handle numbers
        try {

            if (rs.getTimestamp("START_DATE").toString().equals('1970-01-01 00:00:00.0')) {

                // Add to standard table
                dataItemNumberValueStatement.with {
                    setObject(1, rs.getLong("ID"))
                    setObject(2, rs.getString("UID"))
                    setObject(3, rs.getInt("STATUS"))
                    def rowVal = rs.getString("VALUE")

                    // Handle empty values
                    if (rowVal == "" || rowVal == "-") {
                        setObject(4, null)
                    } else if (Double.parseDouble(rowVal).infinite || Double.parseDouble(rowVal).naN) {

                        // Technically a Double but would throw an SQLException if we tried to insert these values.
                        throw new NumberFormatException()
                    } else {

                        // By now the value must be good
                        setObject(4, Double.parseDouble(rowVal))
                    }
                    setObject(5, rs.getTimestamp("CREATED"))
                    setObject(6, rs.getTimestamp("MODIFIED"))
                    setObject(7, rs.getLong("DATA_ITEM_ID"))
                    setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                    def rowUnit = rs.getString("UNIT")
                    setObject(9, (rowUnit ? rowUnit : ''))
                    def perUnit = rs.getString("PER_UNIT")
                    setObject(10, (perUnit ? perUnit : ''))

                    addBatch()
                    numberValueBatchCount++

                    if (numberValueBatchCount >= numberValueBatch) {
                        // Execute this batch.
                        if (dryRun) {
                            clearBatch()
                        } else {
                            executeBatch()
                        }
                        log "Created ${numberValueBatch} DATA_ITEM_NUMBER_VALUEs in a batch."
                        numberValueBatchCount = 0
                    }
                }
            } else {

                // Add to history table
                dataItemNumberValueHistoryStatement.with {
                    setObject(1, rs.getLong("ID"))
                    setObject(2, rs.getString("UID"))
                    setObject(3, rs.getInt("STATUS"))
                    def rowVal = rs.getString("VALUE")
                    setObject(4, rowVal == "" || rowVal == "-" ? null : Double.parseDouble(rowVal))
                    setObject(5, rs.getTimestamp("CREATED"))
                    setObject(6, rs.getTimestamp("MODIFIED"))
                    setObject(7, rs.getLong("DATA_ITEM_ID"))
                    setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                    def rowUnit = rs.getString("UNIT")
                    setObject(9, (rowUnit ? rowUnit : ''))
                    def perUnit = rs.getString("PER_UNIT")
                    setObject(10, (perUnit ? perUnit : ''))
                    setObject(11, rs.getTimestamp("START_DATE"))

                    addBatch()
                    numberValueHistoryBatchCount++

                    if (numberValueHistoryBatchCount >= numberValueHistoryBatch) {
                        // Execute this batch.
                        if (dryRun) {
                            clearBatch()
                        } else {
                            executeBatch()
                        }
                        log "Created ${numberValueHistoryBatch} DATA_ITEM_NUMBER_VALUE_HISTORYs in a batch."
                        numberValueHistoryBatchCount = 0
                    }
                }
            }
        } catch (NumberFormatException e) {
            logError "Error parsing DATA_ITEM value as double. ITEM_VALUE.ID: ${rs.getLong("ID")}, ITEM_VALUE.UID: ${rs.getString("UID")}, " +
                "ITEM_VALUE_DEFINITION_ID: ${rs.getLong("ITEM_VALUE_DEFINITION_ID")}, VALUE: '${rs.getString("VALUE")}'"
        }
    } else {

        // Handle text
        if (rs.getTimestamp("START_DATE").toString().equals('1970-01-01 00:00:00.0')) {
            dataItemTextValueStatement.with {
                setObject(1, rs.getLong("ID"))
                setObject(2, rs.getString("UID"))
                setObject(3, rs.getInt("STATUS"))

                // Truncate any strings > 32767
                value = rs.getString("VALUE")
                if (value.size() > 32767) {
                    logError "Truncating DATA_ITEM string value. ID: ${rs.getLong("ID")}"
                    setObject(4, value[0..32766])
                } else {
                    setObject(4, value)
                }
                setObject(5, rs.getTimestamp("CREATED"))
                setObject(6, rs.getTimestamp("MODIFIED"))
                setObject(7, rs.getLong("DATA_ITEM_ID"))
                setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))

                addBatch()
                textValueBatchCount++

                if (textValueBatchCount >= textValueBatch) {
                    // Execute this batch.
                    if (dryRun) {
                        clearBatch()
                    } else {
                        executeBatch()
                    }
                    log "Created ${textValueBatch} DATA_ITEM_TEXT_VALUEs in a batch."
                    textValueBatchCount = 0
                }
            }
        } else {
            dataItemTextValueHistoryStatement.with {
                setObject(1, rs.getLong("ID"))
                setObject(2, rs.getString("UID"))
                setObject(3, rs.getInt("STATUS"))

                // Truncate any strings > 32767
                value = rs.getString("VALUE")
                if (value.size() > 32767) {
                    logError "Truncating DATA_ITEM string value. ID: ${rs.getLong("ID")}"
                    setObject(4, value[0..32766])
                } else {
                    setObject(4, value)
                }
                setObject(5, rs.getTimestamp("CREATED"))
                setObject(6, rs.getTimestamp("MODIFIED"))
                setObject(7, rs.getLong("DATA_ITEM_ID"))
                setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                setObject(9, rs.getTimestamp("START_DATE"))

                addBatch()
                textValueHistoryBatchCount++

                if (textValueHistoryBatchCount >= textValueHistoryBatch) {
                    // Execute this batch.
                    if (dryRun) {
                        clearBatch()
                    } else {
                        executeBatch()
                    }
                    log "Created ${textValueHistoryBatch} DATA_ITEM_TEXT_VALUE_HISTORYs in a batch."
                    textValueHistoryBatchCount = 0
                }
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

def configureCliBuilder() {
    def cli = new CliBuilder(usage: 'groovy item_migrate.groovy [-h] [-s server] [-t target] [-d database] [-ur user] [-pr password] [-uw user] [-pw password] [-r] [-f date]')
    cli.h(longOpt: 'help', 'usage information')
    cli.s(argName: 'servername', longOpt: 'server', args: 1, required: false, type: GString, "server name (default 'localhost')")
    cli.t(argName: 'target', longOpt: 'target', args: 1, required: false, type: GString, "target server name (default 'localhost')")
    cli.d(argName: 'database', longOpt: 'database', args: 1, required: false, type: GString, "database name (default 'amee')")
    cli.ur(argName: 'user-read', longOpt: 'user-read', args: 1, required: false, type: GString, "reader username (default 'amee')")
    cli.pr(argName: 'password-read', longOpt: 'password-read', args: 1, required: false, type: GString, "reader password (default 'amee')")
    cli.uw(argName: 'user-write', longOpt: 'user-write', args: 1, required: false, type: GString, "writer username (default 'amee')")
    cli.pw(argName: 'password-write', longOpt: 'password-write', args: 1, required: false, type: GString, "writer password (default 'amee')")
    cli.r(argName: 'dryrun', longOpt: 'dryrun', args: 0, required: false, type: GString, "dry-run (does not commit data)")
    cli.f(argName: 'from', longOpt: 'from', args: 1, required: false, type: GString, "select data from this date (default 1970-01-01 00:00:00")
    return cli
}

def log(message) {
    println new Date().toString() + ' ' + message
}

def logError(message) {
    System.err.println new Date().toString() + ' ' + message
}
