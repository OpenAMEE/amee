/* You will probably need to increase the java heap space before running this script:
 * export JAVA_OPTS='-Xmx1G'
 *
 * To redirect stdout and stderr:
 * groovy item_value_migrate.groovy 1> out.log 2> err.log
 *
 * This script requires the MySQL connector. Copy the jar to ~/.groovy/lib/
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
def server = "localhost"
if (opt.s) server = opt.s
def database = "amee"
if (opt.d) database = opt.d
def user = "amee"
if (opt.u) user = opt.u
def password = "amee"
if (opt.p) password = opt.p

// Configure DataSource.
def sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")
sql.connection.autoCommit = false

def sqlInsert = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")
sqlInsert.connection.autoCommit = false

// Check for scolling.
DatabaseMetaData dbmd = sql.connection.getMetaData();
int JDBCVersion = dbmd.getJDBCMajorVersion();
boolean srs = dbmd.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY);
if (JDBCVersion > 2 || srs == true) {
  // println "ResultSet scrolling is supported.";
} else {
  println "ResultSet scrolling is NOT supported.";
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
            profileItemNumberValueStatement.with {
                setObject(1, rs.getLong("ID"))
                setObject(2, rs.getString("UID"))
                setObject(3, rs.getInt("STATUS"))
                def rowVal = rs.getString("VALUE")
                setObject(4, rowVal == "" || rowVal == "-" ? null : Double.parseDouble(rowVal))
                setObject(5, rs.getDate("CREATED"))
                setObject(6, rs.getDate("MODIFIED"))
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
                    executeBatch()
                    println "Created ${numberValueBatch} PROFILE_ITEM_NUMBER_VALUEs in a batch."
                    numberValueBatchCount = 0
                }
            }
        } catch (NumberFormatException e) {
            System.err.println "Error parsing PROFILE_ITEM value as double. ITEM_VALUE.ID: ${rs.getString('ID')}, ITEM_VALUE.UID: ${rs.getString('UID')}, " +
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
                println "Truncating PROFILE_ITEM string value. ID: ${rs.getString('ID')}"
                setObject(4, value[0..32766])
            } else {
                setObject(4, value)
            }
            setObject(5, rs.getDate("CREATED"))
            setObject(6, rs.getDate("MODIFIED"))
            setObject(7, rs.getLong("PROFILE_ITEM_ID"))
            setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))

            addBatch()
            textValueBatchCount++

            if (textValueBatchCount >= textValueBatch) {
                // Execute this batch.
                executeBatch()
                println "Created ${textValueBatch} PROFILE_ITEM_TEXT_VALUEs in a batch."
                textValueBatchCount = 0
            }
        }
    }
}

// Handle remaining Item Values in current batch.
if (numberValueBatchCount > 0) {
    profileItemNumberValueStatement.executeBatch()
    println "Created ${numberValueBatchCount} PROFILE_ITEM_NUMBER_VALUEs in a batch."
    numberValueBatchCount = 0
}
if (textValueBatchCount > 0) {
    profileItemTextValueStatement.executeBatch()
    println "Created ${textValueBatchCount} PROFILE_ITEM_TEXT_VALUEs in a batch."
    textValueBatchCount = 0
}

// Commit the profile item data
sql.commit()
sqlInsert.commit()


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

//def dataItemValues = sql.dataSet('data_item_values')

def dataItemNumberValueSql =
    "INSERT INTO DATA_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemNumberValueStatement = sqlInsert.connection.prepareStatement(dataItemNumberValueSql)

def dataItemTextValueSql =
    "INSERT INTO DATA_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemTextValueStatement = sqlInsert.connection.prepareStatement(dataItemTextValueSql)

def dataItemNumberValueHistorySql =
    "INSERT INTO DATA_ITEM_NUMBER_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, UNIT, PER_UNIT, START_DATE) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemNumberValueHistoryStatement = sqlInsert.connection.prepareStatement(dataItemNumberValueHistorySql)

def dataItemTextValueHistorySql =
    "INSERT INTO DATA_ITEM_TEXT_VALUE_HISTORY (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, START_DATE) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemTextValueHistoryStatement = sqlInsert.connection.prepareStatement(dataItemTextValueHistorySql)

rs = st.executeQuery("SELECT ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, VALUE_TYPE, UNIT, PER_UNIT, START_DATE FROM data_item_values")

while (rs.next()) {
    rowValType = rs.getInt("VALUE_TYPE")

    if (rowValType == valueTypes.indexOf("INTEGER") ||
        rowValType == valueTypes.indexOf("DECIMAL")) {

        // Handle numbers
        try {

            if (rs.getDate("START_DATE").toString().equals('1970-01-01 00:00:00.0')) {

                // Add to standard table
                dataItemNumberValueStatement.with {
                    setObject(1, rs.getLong("ID"))
                    setObject(2, rs.getString("UID"))
                    setObject(3, rs.getInt("STATUS"))
                    def rowVal = rs.getString("VALUE")
                    setObject(4, rowVal == "" || rowVal == "-" ? null : Double.parseDouble(rowVal))
                    setObject(5, rs.getDate("CREATED"))
                    setObject(6, rs.getDate("MODIFIED"))
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
                        executeBatch()
                        println "Created ${numberValueBatch} DATA_ITEM_NUMBER_VALUEs in a batch."
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
                    setObject(5, rs.getDate("CREATED"))
                    setObject(6, rs.getDate("MODIFIED"))
                    setObject(7, rs.getLong("DATA_ITEM_ID"))
                    setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                    def rowUnit = rs.getString("UNIT")
                    setObject(9, (rowUnit ? rowUnit : ''))
                    def perUnit = rs.getString("PER_UNIT")
                    setObject(10, (perUnit ? perUnit : ''))
                    setObject(11, rs.getDate("START_DATE"))

                    addBatch()
                    numberValueHistoryBatchCount++

                    if (numberValueHistoryBatchCount >= numberValueHistoryBatch) {
                        // Execute this batch.
                        executeBatch()
                        println "Created ${numberValueHistoryBatch} DATA_ITEM_NUMBER_VALUE_HISTORYs in a batch."
                        numberValueHistoryBatchCount = 0
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println "Error parsing DATA_ITEM value as double. ITEM_VALUE.ID: ${rs.getLong("ID")}, ITEM_VALUE.UID: ${rs.getString("UID")}, " +
                "ITEM_VALUE_DEFINITION_ID: ${rs.getLong("ITEM_VALUE_DEFINITION_ID")}, VALUE: '${rs.getString("VALUE")}'"
        }
    } else {

        // Handle text
        if (rs.getDate("START_DATE").toString().equals('1970-01-01 00:00:00.0')) {
            dataItemTextValueStatement.with {
                setObject(1, rs.getLong("ID"))
                setObject(2, rs.getString("UID"))
                setObject(3, rs.getInt("STATUS"))

                // Truncate any strings > 32767
                value = rs.getString("VALUE")
                if (value.size() > 32767) {
                    System.err.println "Truncating DATA_ITEM string value. ID: ${rs.getLong("ID")}"
                    setObject(4, value[0..32766])
                } else {
                    setObject(4, value)
                }
                setObject(5, rs.getDate("CREATED"))
                setObject(6, rs.getDate("MODIFIED"))
                setObject(7, rs.getLong("DATA_ITEM_ID"))
                setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))

                addBatch()
                textValueBatchCount++

                if (textValueBatchCount >= textValueBatch) {
                    // Execute this batch.
                    executeBatch()
                    println "Created ${textValueBatch} DATA_ITEM_TEXT_VALUEs in a batch."
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
                    System.err.println "Truncating DATA_ITEM string value. ID: ${rs.getLong("ID")}"
                    setObject(4, value[0..32766])
                } else {
                    setObject(4, value)
                }
                setObject(5, rs.getDate("CREATED"))
                setObject(6, rs.getDate("MODIFIED"))
                setObject(7, rs.getLong("DATA_ITEM_ID"))
                setObject(8, rs.getLong("ITEM_VALUE_DEFINITION_ID"))
                setObject(9, rs.getDate("START_DATE"))

                addBatch()
                textValueHistoryBatchCount++

                if (textValueHistoryBatchCount >= textValueHistoryBatch) {
                    // Execute this batch.
                    executeBatch()
                    println "Created ${textValueHistoryBatch} DATA_ITEM_TEXT_VALUE_HISTORYs in a batch."
                    textValueHistoryBatchCount = 0
                }
            }
        }
    }
}

// Handle remaining Item Values in current batch.
if (numberValueBatchCount > 0) {
    dataItemNumberValueStatement.executeBatch()
    println "Created ${numberValueBatchCount} DATA_ITEM_NUMBER_VALUEs in a batch."
    numberValueBatchCount = 0
}
if (textValueBatchCount > 0) {
    dataItemTextValueStatement.executeBatch()
    println "Created ${textValueBatchCount} DATA_ITEM_TEXT_VALUEs in a batch."
    textValueBatchCount = 0
}
if (numberValueHistoryBatchCount > 0) {
    dataItemNumberValueHistoryStatement.executeBatch()
    println "Created ${numberValueHistoryBatchCount} DATA_ITEM_NUMBER_VALUE_HISTORYs in a batch."
    numberValueHistoryBatchCount = 0
}
if (textValueHistoryBatchCount > 0) {
    dataItemTextValueHistoryStatement.executeBatch()
    println "Created ${textValueHistoryBatchCount} DATA_ITEM_TEXT_VALUE_HISTORYs in a batch."
    textValueHistoryBatchCount = 0
}

// Commit the data item data
sql.commit()



def configureCliBuilder() {
  def cli = new CliBuilder(usage: 'groovy item_migrate.groovy [-h] [-s server] [-d database] [-u user] [-p password]')
  cli.h(longOpt: 'help', 'usage information')
  cli.s(argName: 'servername', longOpt: 'server', args: 1, required: false, type: GString, "server name (default 'localhost')")
  cli.d(argName: 'database', longOpt: 'database', args: 1, required: false, type: GString, "database name (default 'amee')")
  cli.u(argName: 'user', longOpt: 'user', args: 1, required: false, type: GString, "username (default 'amee')")
  cli.p(argName: 'password', longOpt: 'password', args: 1, required: false, type: GString, "password (default 'amee')")
  return cli
}
