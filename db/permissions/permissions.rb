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
require '../../server/amee-domain/target/amee-domain-2.1.jar'
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

# Migrating ProfileItems where end is true
def profile_migration
  
  puts "Profile Migration - Start"

  begin
    
    migrated_count = 0
    conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
    stmt1 = conn.create_statement()
    stmt2 = conn.create_statement()
    stmt3 = conn.create_statement()

    # Get a count of rows to be updated
    rs = stmt1.execute_query("select count(ID) as c from PROFILE where USER_ID = 0")
    rs.first()
    before_count = rs.getInt("c")
    puts "Profile Migration - Need to update #{before_count} rows"

    # Update all Profiles with correct User ID
    rs = stmt2.execute_query("select pro.ID as PROFILE_ID, per.USER_ID from PROFILE pro, PERMISSION per where per.ID = pro.PERMISSION_ID and pro.USER_ID = 0")
    while (rs.next) 
      profile_id = rs.getInt("PROFILE_ID")
      user_id = rs.getInt("USER_ID")
      migrated_count = migrated_count + stmt3.executeUpdate("update PROFILE set USER_ID = #{user_id} where ID = #{profile_id}")
    end
    
    # Done!
    puts "Profile Migration - Updated #{migrated_count} rows"
    
  rescue => err
    puts err
    break
  
  ensure
    puts "Profile Migration - Finished"
    rs.close
    stmt1.close
    stmt2.close
    stmt3.close
    conn.close
  end
    
end

# Migrating GroupUser to GroupPrinciple
def group_user_migration

  puts "GroupUser Migration - Start"

  begin

    count = 0
    conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
    stmt1 = conn.create_statement()
    stmt2 = conn.create_statement()

    rs = stmt1.execute_query("select gr.ID GROUP_ID, u.ID USER_ID, u.UID USER_UID, gu.ENVIRONMENT_ID from GROUPS gr, GROUP_USER gu, USER u where gr.ID = gu.GROUP_ID and u.ID = gu.USER_ID")
    while (rs.next)
      group_id = rs.getInt("GROUP_ID")
      user_id = rs.getInt("USER_ID")
      user_uid = rs.getString("USER_UID")
      environment_id = rs.getInt("ENVIRONMENT_ID)
      uid = JM::UidGen.getUid()
      count = count + stmt2.executeUpdate("INSERT INTO GROUP_PRINCIPLE (UID, ENVIRONMENT_ID, STATUS, GROUP_ID, PRINCIPLE_ID, PRINCIPLE_UID, PRINCIPLE_TYPE, CREATED, MODIFIED) VALUES ('#{uid}', #{environment_id}, 1, #{group_id}, #{user_id}, '#{user_uid}', 'USR', now(), now())")
    end

    puts "GroupUser Migration - Inserted #{count} rows"

  rescue => err
    puts err
    break

  ensure
    puts "GroupUser Migration - Finished"
    rs.close
    stmt1.close
    stmt2.close
    conn.close
  end

end

# Load and run SQL commands
def run_sql(file) 
  puts "Starting #{file} migrations"
  system("mysql -u#{@user} -p#{@pswd} -D#{@database} < #{file}")
  puts "Finished #{file} migrations"
end

# What's this for?
class String
  def startsWith str
    return self[0...str.length] == str
  end
end

# Run the migrations
#run_sql("before_profile_migration.sql")
#profile_migration
#run_sql("after_profile_migration.sql")
#run_sql("alter_permission_table.sql")
#run_sql("create_group_principle_table.sql")
#group_user_migration
#run_sql("create_permissions.sql")
#run_sql("remove_redundant_tables.sql")
