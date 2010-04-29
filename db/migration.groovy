// AMEE DB migration script
//
// This script requires the mysql-connector-java.jar to be on your classpath.
// It also requires the mysql time zone tables to be loaded: mysql_tzinfo_to_sql /usr/share/zoneinfo | mysql mysql

import groovy.sql.Sql

def addDataCategoryWikiName() {
    println("Adding WIKI_NAME field to the DATA_CATEGORY table");
    sql.execute("ALTER TABLE DATA_CATEGORY ADD WIKI_NAME VARCHAR(255) NOT NULL DEFAULT ''");
}

def addTimeZoneField() {
    println("Adding TIME_ZONE field to the USER table")
    sql.execute("ALTER TABLE USER ADD TIME_ZONE VARCHAR( 255 ) NULL")
}

def dropTimeZoneField() {
    println("Dropping TIME_ZONE field from the USER table")
    sql.execute("ALTER TABLE USER DROP TIME_ZONE")
}

def convertTimeZone(fromTz, toTz) {
    println("Converting times from ${fromTz} to ${toTz}")
    sql.executeUpdate("UPDATE ALGORITHM SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE API_VERSION SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE DATA_CATEGORY SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE ENVIRONMENT SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE GROUPS SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE ITEM SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz}), START_DATE = CONVERT_TZ(START_DATE,${fromTz},${toTz}), END_DATE = CONVERT_TZ(END_DATE,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE ITEM_DEFINITION SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE ITEM_VALUE SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz}), START_DATE = CONVERT_TZ(START_DATE, ${fromTz},${toTz}), END_DATE = CONVERT_TZ(END_DATE,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE ITEM_VALUE_DEFINITION SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE LOCALE_NAME SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE PERMISSION SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE PROFILE SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE USER SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE USER_PROFILE_COUNT SET DATE = CONVERT_TZ(DATE,${fromTz},${toTz})")
    sql.executeUpdate("UPDATE VALUE_DEFINITION SET CREATED = CONVERT_TZ(CREATED,${fromTz},${toTz}), MODIFIED = CONVERT_TZ(MODIFIED,${fromTz},${toTz})")
}

def updateTimeZones(timeZone) {
    println("Setting all user time zones to ${timeZone}")
    sql.executeUpdate("UPDATE USER SET TIME_ZONE = ?", [timeZone])

    // Except for testing users
    println("Setting test user time zones to America/New_York")
    sql.executeUpdate("UPDATE USER SET TIME_ZONE = ? WHERE USERNAME IN ('floppy', 'floppyv2')", ["America/New_York"])
}

def fixEpoch() {
    println("Fixing the epoch date")
    sql.executeUpdate("UPDATE ITEM_VALUE SET START_DATE = '1970-01-01 00:00:00' WHERE START_DATE IS NOT NULL")
    sql.executeUpdate("UPDATE ITEM SET START_DATE = NULL WHERE TYPE = 'DI'")
}

def addIndex() {
    println("Adding index to ITEM table (OPS-38)")
    sql.execute("ALTER TABLE ITEM ADD INDEX (TYPE)")
}

def cli = new CliBuilder(usage: 'groovy migration.groovy [-cp path/to/mysql-connector-java.jar] [-h] [-s server] [-d database] [-u user] [-p password] [-r]')
cli.h(longOpt:'help', 'usage information')
cli.s(argName:'servername', longOpt:'server', args:1, required:false, type:GString, "server name (default 'localhost')")
cli.d(argName:'database', longOpt:'database', args:1, required:false, type:GString, "database name (default 'amee')")
cli.u(argName:'user', longOpt:'user', args:1, required:false, type:GString, "username (default 'amee')")
cli.p(argName:'password', longOpt:'password', args:1, required:false, type:GString, "password (default 'amee')")
cli.r(longOpt:'rollback', args:0, required:false, 'rollback the upgrade')
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    return
}

def server = "localhost"
if (opt.s) server = opt.s

def database = "amee"
if (opt.d) database = opt.d

def user = "amee"
if (opt.u) user = opt.u

def password = "amee"
if (opt.p) password = opt.p

sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")

if (opt.r) {
    println "Rolling back database migration."
    fromTz = "UTC"
    toTz = "Europe/London"

    dropTimeZoneField()
    convertTimeZone(fromTz, toTz)
} else {
    fromTz = "Europe/London"
    toTz = "UTC"

    addIndex()
    addTimeZoneField()
    updateTimeZones("Europe/London")
    convertTimeZone(fromTz, toTz)
    fixEpoch()
}



