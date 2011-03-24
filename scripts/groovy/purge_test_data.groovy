/**
 * Purges test data from the database.
 *
 * This script requires the MySQL connector. Copy the jar to ~/.groovy/lib/
 */


import groovy.sql.Sql

// Handle command line parameters
def cli = configureCliBuilder()
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    return
}

// Database options.
server = opt.s ?: 'localhost'
database = opt.d ?: "amee"
user = opt.u ?: "amee"
password = opt.p ?: "amee"

// Configure DataSource.
sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")

// The following users' profile data will be removed.
def testUsers = ['ameetest', 'ameetest2', 'adminv2', 'floppyv2']
def userIds = testUsers.collect { username ->
    sql.eachRow("select ID from USER where USERNAME = ${username}") {
        it.ID
    }
}

// Profile
userIds.each {

    // Delete the profile item number values
    sql.execute """delete PROFILE_ITEM_NUMBER_VALUE from PROFILE
        join PROFILE_ITEM on PROFILE_ITEM.PROFILE_ID = PROFILE.ID
        join PROFILE_ITEM_NUMBER_VALUE on PROFILE_ITEM_NUMBER_VALUE.PROFILE_ITEM_ID = PROFILE_ITEM.ID
        where PROFILE.USER_ID = ${it}"""

    // Delete the profile item text values
    sql.execute """delete PROFILE_ITEM_TEXT_VALUE from PROFILE
        join PROFILE_ITEM on PROFILE_ITEM.PROFILE_ID = PROFILE.ID
        join PROFILE_ITEM_TEXT_VALUE on PROFILE_ITEM_TEXT_VALUE.PROFILE_ITEM_ID = PROFILE_ITEM.ID
        where PROFILE.USER_ID = ${it}"""

    // Delete the profile items
    sql.execute """delete PROFILE_ITEM from PROFILE
        join PROFILE_ITEM on PROFILE_ITEM.PROFILE_ID = PROFILE.ID
        where PROFILE.USER_ID = ${it}"""

    // Delete the profiles
    sql.execute "delete from PROFILE where PROFILE.USER_ID = ${it}"
}

// Delete Item Definition
sql.execute "delete from ITEM_DEFINITION where NAME = 'New Item Definition'"

// The test Category
def testCategoryPath = 'apitests'
def testCategoryId = sql.firstRow("select ID from DATA_CATEGORY where PATH = ${testCategoryPath}").ID

// Delete Data Item Number Value
sql.execute """delete DATA_ITEM_NUMBER_VALUE from DATA_ITEM_NUMBER_VALUE
    join DATA_ITEM on DATA_ITEM_NUMBER_VALUE.DATA_ITEM_ID = DATA_ITEM.ID
    where DATA_ITEM.DATA_CATEGORY_ID = ${testCategoryId}"""

// Delete Data Item Text Value
sql.execute """delete DATA_ITEM_TEXT_VALUE from DATA_ITEM_TEXT_VALUE
    join DATA_ITEM on DATA_ITEM_TEXT_VALUE.DATA_ITEM_ID = DATA_ITEM.ID
    where DATA_ITEM.DATA_CATEGORY_ID = ${testCategoryId}"""

// Delete Data Item
sql.execute "delete from DATA_ITEM where DATA_CATEGORY_ID = ${testCategoryId}"

// Delete Data Category
sql.execute "delete from DATA_CATEGORY where DATA_CATEGORY_ID = ${testCategoryId}"

def configureCliBuilder() {
    def cli = new CliBuilder(usage: 'groovy item_migrate.groovy [-h] [-s server] [-d database] [-u user] [-p password]')
    cli.h(longOpt: 'help', 'usage information')
    cli.s(argName: 'servername', longOpt: 'server', args: 1, required: false, type: GString, "server name (default 'localhost')")
    cli.d(argName: 'database', longOpt: 'database', args: 1, required: false, type: GString, "database name (default 'amee')")
    cli.u(argName: 'user', longOpt: 'user', args: 1, required: false, type: GString, "username (default 'amee')")
    cli.p(argName: 'password', longOpt: 'password', args: 1, required: false, type: GString, "password (default 'amee')")
    return cli
}