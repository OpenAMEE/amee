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
require '../lib/amee-maven/utils-1.0.0-SNAPSHOT.jar'
require '../lib/mysql/mysql-connector-java.jar'
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
  include_package 'com.jellymold.utils.domain' 
end

# JDBC Default Parameters
@host="localhost"
@database = "amee"
@user = "root"
@pswd = "root"

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

# Migration ProfileItems where end is true
def migrate_pi
  puts "Starting ITEM migrations"

  begin
    conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
    stmt = conn.create_statement()

    # Get a count of rows to be updated
    rs = stmt.execute_query("select count(ID) as c from ITEM where TYPE = 'PI' and END is TRUE")
    rs.first()
    before_count = rs.getInt("c")
    puts "migrate_pi - updating #{before_count} rows"
    
    # Select all ProfileItems where end is true
    query = "update ITEM set END_DATE = START_DATE where TYPE = 'PI' and END is TRUE"
    after_count = stmt.executeUpdate(query)
    puts "migrate_pi - updated #{after_count} rows"

    # Drop the END column following migration to END_DATE
    stmt.execute("ALTER TABLE ITEM DROP COLUMN END")
    
  rescue => err
    puts err
    break
  
  ensure
    puts "Finished ITEM migrations"
    rs.close
    stmt.close
    conn.close
  end
    
end

def migrate_ivd 
  puts "Starting ITEM_VALUE_DEFINITION migrations"

  begin
    conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
    stmt = conn.create_statement(JavaSql::ResultSet::TYPE_SCROLL_SENSITIVE, JavaSql::ResultSet::CONCUR_UPDATABLE)
    conn.setAutoCommit(false)

    # SQL Statements
    select_old = "SELECT ID, VALUE, CHOICES, ITEM_DEFINITION_ID FROM ITEM_VALUE_DEFINITION WHERE PATH={OLD_PATH}"
    insert_new = "INSERT INTO ITEM_VALUE_DEFINITION(UID, NAME, PATH, VALUE, CHOICES, FROM_PROFILE, FROM_DATA, ALLOWED_ROLES, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, VALUE_DEFINITION_ID, PER_UNIT,UNIT) " + 
      "VALUES('{UID}',{NEW_NAME},{NEW_PATH},'{VALUE}', '{CHOICES}', true, false, '', curdate(), curdate(), 2, {ITEM_DEFINITION_ID}, 16,{NEW_PER_UNIT},{NEW_UNIT})"
    update_old = "UPDATE ITEM_VALUE_DEFINITION SET ALIASED_TO_ID=LAST_INSERT_ID(), PER_UNIT={OLD_PER_UNIT},UNIT={OLD_UNIT} WHERE ID={ID}"
    insert_old_api_version = "INSERT INTO ITEM_VALUE_DEFINITION_API_VERSION(ITEM_VALUE_DEFINITION_ID, API_VERSION_ID) VALUES({ITEM_VALUE_DEFINITION_ID},1)"
    insert_new_api_version = "INSERT INTO ITEM_VALUE_DEFINITION_API_VERSION(ITEM_VALUE_DEFINITION_ID, API_VERSION_ID) VALUES(LAST_INSERT_ID(),2)"

    file = File.new("ivd.csv","r")
    while(line = file.gets)
      # Row order: OLD_PATH, OLD_UNIT, OLD_PER_UNIT, NEW_NAME, NEW_PATH, NEW_UNIT, NEW_PER_UNIT
      old_path, old_unit, old_per_unit, new_name, new_path, new_unit, new_per_unit = line.split(",")

      query = select_old.sub(/\{OLD_PATH\}/, old_path)
      puts query 
      rs = stmt.executeQuery(query)
      
      while(rs.next())
        item_value_definition_id = rs.getInt("ID").to_s
        item_definition_id = rs.getInt("ITEM_DEFINITION_ID").to_s
        value = rs.getInt("VALUE").to_s
        choices = rs.getInt("CHOICES").to_s
        query = insert_new.sub(/\{UID\}/, JM::UidGen.getUid())
        query = query.sub(/\{NEW_NAME\}/, new_name)
        query = query.sub(/\{NEW_PATH\}/, new_path)
        query = query.sub(/\{ITEM_DEFINITION_ID\}/, item_definition_id)
        query = query.sub(/\{NEW_UNIT\}/, new_unit)
        query = query.sub(/\{NEW_PER_UNIT\}/, new_per_unit)
        query = query.sub(/\{VALUE\}/, value)
        query = query.sub(/\{CHOICES\}/, choices)
        puts query
        stmt.addBatch(query)
        
        query = update_old.sub(/\{OLD_PATH\}/, old_path)
        query = query.sub(/\{OLD_UNIT\}/, old_unit)
        query = query.sub(/\{OLD_PER_UNIT\}/, old_per_unit)
        query = query.sub(/\{ID\}/, item_value_definition_id)
        puts query
        stmt.addBatch(query)

        puts insert_new_api_version
        stmt.addBatch(insert_new_api_version)

        query = insert_old_api_version.sub(/\{ITEM_VALUE_DEFINITION_ID\}/, item_value_definition_id)
        puts query
        stmt.addBatch(query)
        
      end
      
      unless @dryrun
        stmt.executeBatch()
        conn.commit()
        stmt.clearBatch()
      end

      puts "\n"
              
    end

    # Add all other ITEM_VALUE_DEFINITION_ID - API_VERSION_ID pairs into ITEM_VALUE_DEFINITION_API_VERSION
    insert_old_api_version = "INSERT INTO ITEM_VALUE_DEFINITION_API_VERSION(ITEM_VALUE_DEFINITION_ID, API_VERSION_ID) VALUES({ITEM_VALUE_DEFINITION_ID},1)"
    insert_new_api_version = "INSERT INTO ITEM_VALUE_DEFINITION_API_VERSION(ITEM_VALUE_DEFINITION_ID, API_VERSION_ID) VALUES({ITEM_VALUE_DEFINITION_ID},2)"
    query = "SELECT ID FROM ITEM_VALUE_DEFINITION WHERE ID NOT IN (SELECT ITEM_VALUE_DEFINITION_ID FROM ITEM_VALUE_DEFINITION_API_VERSION)"
    puts query
    rs = stmt.executeQuery(query)
    while(rs.next())
      item_value_definition_id = rs.getInt("ID").to_s
      query = insert_old_api_version.sub(/\{ITEM_VALUE_DEFINITION_ID\}/, item_value_definition_id)
      puts query
      stmt.addBatch(query)
      query = insert_new_api_version.sub(/\{ITEM_VALUE_DEFINITION_ID\}/, item_value_definition_id)
      puts query
      stmt.addBatch(query)
    end

    unless @dryrun
      stmt.executeBatch()
      conn.commit()
    end

  rescue => err
    puts err
    break
  
  ensure
    puts "Finished ITEM_VALUE_DEFINITION migrations"
    rs.close
    stmt.close
    conn.close
  end

end

# Migrate the algorithms
def migrate_algo
  puts "Starting ALGORITHM migrations"

  begin
    conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
    stmt = conn.create_statement()

    # SQL Statements
    select = "SELECT ID FROM ITEM_DEFINITION WHERE NAME='{NAME}' AND ENVIRONMENT_ID=2"
    insert = "INSERT INTO ALGORITHM(UID, NAME, CONTENT, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, TYPE) " + 
      "VALUES('{UID}','default','{CONTENT}', curdate(), curdate(), 2, {ID}, 'AL')"

    file = File.new("algo.csv","r")
    while(line = file.gets)
      path, name = line.split(",")
      query = select.sub(/\{NAME\}/, name.chomp!)
      puts query 
      rs = stmt.executeQuery(query)
      if rs.next()
        id = rs.getInt("ID") 
        query = insert.sub(/\{UID\}/, JM::UidGen.getUid())
        js = File.new(path + "/v2Algorithm.js","r")

        lines = js.readlines() 
        lines.each do |l| 
          l.chomp! 
          l.gsub!(/'/,"''")
        end
        query = query.sub(/\{CONTENT\}/,lines.join("\\n"))
        query = query.sub(/\{ID\}/,id.to_s)
        puts query
        count = stmt.executeUpdate(query)
        puts "migrate_algo - updated #{count} rows"
        puts "\n"
      else
        puts "Error: ID not found - #{id}"
      end
    end

  rescue => err
    puts err
    break
  
  ensure
    puts "Finished ALGORITHM migrations"
    rs.close
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
#run_sql("ddl.sql")
#migrate_ivd
#run_sql("dml.sql")
#migrate_pi
migrate_algo
#run_sql("innodb.sql")
