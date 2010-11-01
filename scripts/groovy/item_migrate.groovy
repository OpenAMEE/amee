/*
 * To redirect stdout and stderr:
 * groovy item_value_migrate.groovy 1> out.log 2> err.log
 *
 * This script requires the MySQL connector. Copy the jar to ~/.groovy/lib/
 *
 */

import groovy.sql.Sql
import java.sql.ResultSet
import java.sql.Statement
import java.sql.DatabaseMetaData

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
def dryRun = false
if (opt.r) dryRun = true

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

def batchCount = 0

// Migrate PROFILE_ITEMs
def profileItemSql = "INSERT INTO PROFILE_ITEM (ID, UID, NAME, CREATED, MODIFIED, START_DATE, END_DATE, ITEM_DEFINITION_ID, DATA_ITEM_ID, PROFILE_ID, STATUS) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
def profileItemStatement = sqlInsert.connection.prepareStatement(profileItemSql)

def rs = st.executeQuery("SELECT ID, UID, NAME, CREATED, MODIFIED, START_DATE, END_DATE, ITEM_DEFINITION_ID, DATA_ITEM_ID, PROFILE_ID, STATUS FROM ITEM WHERE TYPE = 'PI'")
while (rs.next()) {
    profileItemStatement.with {
        setObject(1, rs.getLong("ID"))
        setObject(2, rs.getString("UID"))
        setObject(3, rs.getString("NAME"))
        setObject(4, rs.getTimestamp("CREATED"))
        setObject(5, rs.getTimestamp("MODIFIED"))
        setObject(6, rs.getTimestamp("START_DATE"))
        setObject(7, rs.getTimestamp("END_DATE"))
        setObject(8, rs.getLong("ITEM_DEFINITION_ID"))
        setObject(9, rs.getLong("DATA_ITEM_ID"))
        setObject(10, rs.getLong("PROFILE_ID"))
        setObject(11, rs.getInt("STATUS"))

        addBatch()
        batchCount++

        if (batchCount >= profileItemBatch) {
            // Execute this batch.
            if (dryRun) {
                clearBatch()
            } else {
                executeBatch()  
            }
            println "Created ${batchCount} PROFILE_ITEMs in a batch."
            batchCount = 0
        }
    }
}

// Handle remaining Item Values in current batch.
if (batchCount > 0) {
    if (!dryRun) profileItemStatement.executeBatch()
    println "Created ${batchCount} PROFILE_ITEMs in a batch."
    batchCount = 0
}
if (dryRun) {
    sql.rollback()
    sqlInsert.rollback()
} else {
    sql.commit()
    sqlInsert.commit()
}

// Migrate DATA_ITEMs
st = sql.connection.createStatement(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY);
st.setFetchSize(Integer.MIN_VALUE);

def dataItemSql = "INSERT INTO DATA_ITEM (ID, UID, NAME, PATH, CREATED, MODIFIED, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, STATUS) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
def dataItemStatement = sqlInsert.connection.prepareStatement(dataItemSql)

rs = st.executeQuery("SELECT ID, UID, NAME, PATH, CREATED, MODIFIED, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, STATUS FROM ITEM WHERE TYPE = 'DI'")
while (rs.next()) {
    dataItemStatement.with {
        setObject(1, rs.getLong("ID"))
        setObject(2, rs.getString("UID"))
        setObject(3, rs.getString("NAME"))
        setObject(4, rs.getString("PATH"))
        setObject(5, rs.getTimestamp("CREATED"))
        setObject(6, rs.getTimestamp("MODIFIED"))
        setObject(7, rs.getLong("ITEM_DEFINITION_ID"))
        setObject(8, rs.getLong("DATA_CATEGORY_ID"))
        setObject(9, rs.getInt("STATUS"))

        addBatch()
        batchCount++

        if (batchCount >= dataItemBatch) {
            // Execute this batch.
            if (dryRun) {
                clearBatch()
            } else {
                executeBatch()
            }
            println "Created ${batchCount} DATA_ITEMs in a batch."
            batchCount = 0;
        }
    }
}

// Handle remaining Item Values in current batch.
if (batchCount > 0) {
    if (dryRun) {
        dataItemStatement.clearBatch()
    } else {
        dataItemStatement.executeBatch()
    }
    println "Created ${batchCount} DATA_ITEMs in a batch."
    batchCount = 0
}
if (dryRun) {
    sql.rollback()
    sqlInsert.rollback()
} else {
    sql.commit()
    sqlInsert.commit()
}


def configureCliBuilder() {
    def cli = new CliBuilder(usage: 'groovy item_migrate.groovy [-h] [-s server] [-d database] [-u user] [-p password] [-r]')
    cli.h(longOpt: 'help', 'usage information')
    cli.s(argName: 'servername', longOpt: 'server', args: 1, required: false, type: GString, "server name (default 'localhost')")
    cli.d(argName: 'database', longOpt: 'database', args: 1, required: false, type: GString, "database name (default 'amee')")
    cli.u(argName: 'user', longOpt: 'user', args: 1, required: false, type: GString, "username (default 'amee')")
    cli.p(argName: 'password', longOpt: 'password', args: 1, required: false, type: GString, "password (default 'amee')")
    cli.r(argName: 'dryrun', longOpt: 'dryrun', args: 0, required: false, type: GString, "dry-run (does not commit data)")
    return cli
}
