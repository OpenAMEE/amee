import groovy.sql.Sql

def profileItemBatch = 1000
def dataItemBatch = 1000

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

def itemDataSet = sql.dataSet('ITEM')
def batchCount = 0

// Migrate PROFILE_ITEMs
def profileItems = itemDataSet.findAll { it.type == 'PI' }
def profileItemSql = "INSERT INTO PROFILE_ITEM (ID, UID, NAME, CREATED, MODIFIED, START_DATE, END_DATE, ITEM_DEFINITION_ID, DATA_ITEM_ID, PROFILE_ID, STATUS) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def profileItemStatement = sql.connection.prepareStatement(profileItemSql)
profileItems.each { row ->

    profileItemStatement.with {
        setObject(1, row.ID)
        setObject(2, row.UID)
        setObject(3, row.NAME)
        setObject(4, row.CREATED)
        setObject(5, row.MODIFIED)
        setObject(6, row.START_DATE)
        setObject(7, row.END_DATE)
        setObject(8, row.ITEM_DEFINITION_ID)
        setObject(9, row.DATA_ITEM_ID)
        setObject(10, row.PROFILE_ID)
        setObject(11, row.STATUS)

        addBatch()
        batchCount++

        if (batchCount >= profileItemBatch) {
            // Execute this batch.
            executeBatch();
            println "Created ${batchCount} PROFILE_ITEMs in a batch."
            batchCount = 0
        }
    }
}

// Handle remaining Item Values in current batch.
if (batchCount > 0) {
    profileItemStatement.executeBatch()
    println "Created ${batchCount} PROFILE_ITEMs in a batch."
    batchCount = 0
}
sql.commit()

// Migrate DATA_ITEMs
def dataItems = itemDataSet.findAll { it.type == 'DI' }
def dataItemSql = "INSERT INTO DATA_ITEM (ID, UID, NAME, PATH, CREATED, MODIFIED, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, STATUS) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemStatement = sql.connection.prepareStatement(dataItemSql)
dataItems.each { row ->

    dataItemStatement.with {
        setObject(1, row.ID)
        setObject(2, row.UID)
        setObject(3, row.NAME)
        setObject(4, row.PATH)
        setObject(5, row.CREATED)
        setObject(6, row.MODIFIED)
        setObject(7, row.ITEM_DEFINITION_ID)
        setObject(8, row.DATA_CATEGORY_ID)
        setObject(9, row.STATUS)

        addBatch()
        batchCount++

        if (batchCount >= dataItemBatch) {
            // Execute this batch.
            executeBatch();
            println "Created ${batchCount} DATA_ITEMs in a batch."
            batchCount = 0;
        }
    }
}

// Handle remaining Item Values in current batch.
if (batchCount > 0) {
    dataItemStatement.executeBatch()
    println "Created ${batchCount} DATA_ITEMs in a batch."
    batchCount = 0
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
