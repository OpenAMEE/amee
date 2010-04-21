# == Synopsis
#
# AMEE Database Migration Script. 
#
# Defaults:
#   servername=localhost
#   database=amee
#   username=amee
#   password=amee
#
# == Usage
#
# -h, --help:
#    show help
#
# -s, --server [servername]:
#    the database server on which to perform the migrations 
#
# -d, --database [database]:
#    the database to migrate 
#
# -u, --user [username]:
#    the login username  
#
# -p, --password [password]:
#    the login password  
#
require 'java'
require '../server/amee-domain/target/amee-domain-2.1.jar'
require '/Development/repository/mysql/mysql-connector-java/5.1.6/mysql-connector-java-5.1.6.jar'
require '/Development/repository/commons-logging/commons-logging/1.1/commons-logging-1.1.jar'
require '/Development/repository/org/jscience/jscience/4.3.1/jscience-4.3.1.jar'

require 'getoptlong'
require 'rdoc/usage'
    
# Java packages

module JavaLang
  include_package "java.lang"
end

module JavaSql
  include_package 'java.sql'
end

module JM
  include_package 'com.amee.domain' 
end

module Core
  include_package 'com.amee.domain.core' 
end

# JDBC Default Parameters
@host="localhost"
@database = "amee"
@user = "amee"
@pswd = "amee"

# Parse command line arguments
parser = GetoptLong.new

# Warning, dryrun is experimental and should only be used with knowledge of the migration script
parser.set_options(
  ["-h", "--help", GetoptLong::NO_ARGUMENT],
  ["-s","--server", GetoptLong::REQUIRED_ARGUMENT],
  ["-d","--database", GetoptLong::REQUIRED_ARGUMENT],
  ["-u","--user", GetoptLong::REQUIRED_ARGUMENT],
  ["-p","--password", GetoptLong::REQUIRED_ARGUMENT],
  ["--dryrun", GetoptLong::NO_ARGUMENT]
)

loop do
  begin
    opt, arg = parser.get
    break if not opt
    case opt
      when "-h"
        RDoc::usage
      when "-s"
        @host = arg
      when "-d"
        @database = arg
      when "-u"
        @user = arg
      when "-p"
        @pswd = arg
      when "--dryrun"
        @dryrun = true
    end   

  rescue => err
    puts err
    break
  end
end

# JDBC Parameters
@url = "jdbc:mysql://#{@host}/#{@database}"
Java::com.mysql.jdbc.Driver
puts "Running migration using url: #{@url}, user: #{@user}, password: #{@pswd}" 

# Load the time zone data and migrate data
def migrate_tz(from_tz, to_tz)
  puts "Loading the time zone tables"

  system("mysql_tzinfo_to_sql /usr/share/zoneinfo | mysql -u#{@user} -p#{@pswd} mysql")

  puts "Migrating dates from #{from_tz} to #{to_tz}"

  begin
    conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
    conn.setAutoCommit(false);

    stmt = conn.create_statement()
    stmt.addBatch("UPDATE ALGORITHM SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE API_VERSION SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE DATA_CATEGORY SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE ENVIRONMENT SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE GROUPS SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE ITEM SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}'), START_DATE = CONVERT_TZ(START_DATE,'#{from_tz}','#{to_tz}'), END_DATE = CONVERT_TZ(END_DATE,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE ITEM_DEFINITION SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE ITEM_VALUE SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}'), START_DATE = CONVERT_TZ(START_DATE, '#{from_tz}','#{to_tz}'), END_DATE = CONVERT_TZ(END_DATE,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE ITEM_VALUE_DEFINITION SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE LOCALE_NAME SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE PERMISSION SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE PROFILE SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE USER SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE USER_PROFILE_COUNT SET DATE = CONVERT_TZ(DATE,'#{from_tz}','#{to_tz}')")
    stmt.addBatch("UPDATE VALUE_DEFINITION SET CREATED = CONVERT_TZ(CREATED,'#{from_tz}','#{to_tz}'), MODIFIED = CONVERT_TZ(MODIFIED,'#{from_tz}','#{to_tz}')")

    stmt.executeBatch();
    conn.commit();

  rescue => err
    conn.rollback()
    puts err
    break
  
  ensure
    puts "Finished time zone migrations"
    stmt.close
    conn.close
  end
    
end

# Load and run SQL commands
def run_sql(file) 
  puts "Starting #{file} migrations"
  system("mysql -u#{@user} -p#{@pswd} -D#{@database} < #{file}")
  puts "Finished #{file} migrations"
end

# Run the migrations
run_sql("ddl.sql")
migrate_tz('Europe/London', 'UTC')
run_sql("dml.sql")