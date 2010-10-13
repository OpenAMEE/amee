import groovy.sql.Sql

def numberValueBatch = 1000
def textValueBatch = 1000

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

// Create a view for the profile item values
sql.execute "CREATE OR REPLACE VIEW profile_item_values AS " +
    "SELECT iv.ID, iv.UID, iv.STATUS, iv.VALUE, iv.CREATED, iv.MODIFIED, i.ID as PROFILE_ITEM_ID, " +
    "iv.ITEM_VALUE_DEFINITION_ID, iv.UNIT, iv.PER_UNIT FROM ITEM_VALUE AS iv JOIN ITEM i ON iv.ITEM_ID = i.ID WHERE i.TYPE = 'PI'"

def profileItemValues = sql.dataSet('profile_item_values')
def numberValueBatchCount = 0
def textValueBatchCount = 0

def profileItemNumberValueSql =
    "INSERT INTO PROFILE_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def profileItemNumberValueStatement = sql.connection.prepareStatement(profileItemNumberValueSql)

def profileItemTextValueSql =
    "INSERT INTO PROFILE_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
def profileItemTextValueStatement = sql.connection.prepareStatement(profileItemTextValueSql)

profileItemValues.each { row ->

    if (row.VALUE.toString().isDouble()) {

        // Handle numbers
        profileItemNumberValueStatement.with {
            setObject(1, row.ID)
            setObject(2, row.UID)
            setObject(3, row.STATUS)
            setObject(4, Double.parseDouble(row.VALUE))
            setObject(5, row.CREATED)
            setObject(6, row.MODIFIED)
            setObject(7, row.PROFILE_ITEM_ID)
            setObject(8, row.ITEM_VALUE_DEFINITION_ID)
            setObject(9, (row.UNIT ? row.UNIT : ''))
            setObject(10, (row.PER_UNIT ? row.PER_UNIT : ''))

            addBatch()
            numberValueBatchCount++

            if (numberValueBatchCount >= numberValueBatch) {
                // Execute this batch.
                executeBatch()
                println "Created ${numberValueBatch} PROFILE_ITEM_NUMBER_VALUEs in a batch."
                numberValueBatchCount = 0
            }
        }
    } else {
        
        // Handle text
        profileItemTextValueStatement.with {
            setObject(1, row.ID)
            setObject(2, row.UID)
            setObject(3, row.STATUS)

            // Truncate any strings > 255
            value = row.VALUE.toString()
            if (value.size() > 255) {
                setObject(4, value[0..254])
            } else {
                setObject(4, value)
            }
            setObject(5, row.CREATED)
            setObject(6, row.MODIFIED)
            setObject(7, row.PROFILE_ITEM_ID)
            setObject(8, row.ITEM_VALUE_DEFINITION_ID)

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

sql.commit()


// Create a view for the data item values
sql.execute "CREATE OR REPLACE VIEW data_item_values AS " +
    "SELECT iv.ID, iv.UID, iv.STATUS, iv.VALUE, iv.CREATED, iv.MODIFIED, i.ID as DATA_ITEM_ID, " +
    "iv.ITEM_VALUE_DEFINITION_ID, iv.UNIT, iv.PER_UNIT FROM ITEM_VALUE AS iv JOIN ITEM i ON iv.ITEM_ID = i.ID WHERE i.TYPE = 'DI'"

def dataItemValues = sql.dataSet('data_item_values')

def dataItemNumberValueSql =
    "INSERT INTO DATA_ITEM_NUMBER_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemNumberValueStatement = sql.connection.prepareStatement(dataItemNumberValueSql)

def dataItemTextValueSql =
    "INSERT INTO DATA_ITEM_TEXT_VALUE (ID, UID, STATUS, VALUE, CREATED, MODIFIED, DATA_ITEM_ID, ITEM_VALUE_DEFINITION_ID) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemTextValueStatement = sql.connection.prepareStatement(dataItemTextValueSql)

//def query = "SELECT * FROM data_item_values WHERE ID > ? LIMIT 1000"
//def maxId = 0
//def more = true
//def executeQuery = {
//    def oldMaxId = maxId
//    sql.eachRow(query, [maxId]) { row ->
//
//        println row.ID
//
//        if (row.VALUE.toString().isDouble()) {
//
//            // Handle numbers
//            dataItemNumberValueStatement.with {
//                setObject(1, row.ID)
//                setObject(2, row.UID)
//                setObject(3, row.STATUS)
//                setObject(4, row.VALUE)
//                setObject(5, row.CREATED)
//                setObject(6, row.MODIFIED)
//                setObject(7, row.DATA_ITEM_ID)
//                setObject(8, row.ITEM_VALUE_DEFINITION_ID)
//                setObject(9, (row.UNIT ? row.UNIT : ''))
//                setObject(10, (row.PER_UNIT ? row.PER_UNIT : ''))
//
//                addBatch()
//                numberValueBatchCount++
//
//                if (numberValueBatchCount >= numberValueBatch) {
//                    // Execute this batch.
//                    executeBatch()
//                    println "Created ${numberValueBatch} DATA_ITEM_NUMBER_VALUEs in a batch."
//                    numberValueBatchCount = 0
//                }
//            }
//        } else {
//
//            // Handle text
//            dataItemTextValueStatement.with {
//                setObject(1, row.ID)
//                setObject(2, row.UID)
//                setObject(3, row.STATUS)
//                setObject(4, row.VALUE)
//                setObject(5, row.CREATED)
//                setObject(6, row.MODIFIED)
//                setObject(7, row.DATA_ITEM_ID)
//                setObject(8, row.ITEM_VALUE_DEFINITION_ID)
//
//                addBatch()
//                textValueBatchCount++
//
//                if (textValueBatchCount >= textValueBatch) {
//                    // Execute this batch.
//                    executeBatch()
//                    println "Created ${textValueBatch} DATA_ITEM_TEXT_VALUEs in a batch."
//                    textValueBatchCount = 0
//                }
//            }
//        }
//
//        maxId = row.ID
//    }
//    more = maxId != oldMaxId
//}
//
//while (more) { executeQuery() }

dataItemValues.each { row ->

    if (row.VALUE.toString().isDouble()) {

        // Handle numbers
        dataItemNumberValueStatement.with {
            setObject(1, row.ID)
            setObject(2, row.UID)
            setObject(3, row.STATUS)
            setObject(4, Double.parseDouble(row.VALUE))
            setObject(5, row.CREATED)
            setObject(6, row.MODIFIED)
            setObject(7, row.DATA_ITEM_ID)
            setObject(8, row.ITEM_VALUE_DEFINITION_ID)
            setObject(9, (row.UNIT ? row.UNIT : ''))
            setObject(10, (row.PER_UNIT ? row.PER_UNIT : ''))

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

        // Handle text
        dataItemTextValueStatement.with {
            setObject(1, row.ID)
            setObject(2, row.UID)
            setObject(3, row.STATUS)

            // Truncate any strings > 255
            // TODO: log the original string
            value = row.VALUE.toString()
            if (value.size() > 255) {
                setObject(4, value[0..254])
            } else {
                setObject(4, value)
            }
            setObject(5, row.CREATED)
            setObject(6, row.MODIFIED)
            setObject(7, row.DATA_ITEM_ID)
            setObject(8, row.ITEM_VALUE_DEFINITION_ID)

            addBatch()
            textValueBatchCount++

            if (textValueBatchCount >= textValueBatch) {
                // Execute this batch.
                executeBatch()
                println "Created ${textValueBatch} DATA_ITEM_TEXT_VALUEs in a batch."
                textValueBatchCount = 0
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
