require 'java'
require '../lib/amee-maven/utils-1.0.0-SNAPSHOT.jar'
require '../lib/mysql/mysql-connector-java.jar'

module JavaLang
  include_package "java.lang"
end

module JavaSql
  include_package 'java.sql'
end

module JM
  include_package 'com.jellymold.utils.domain' 
end

# Connection Parameters - move to config
@url = "jdbc:mysql://localhost/amee"
@user = "amee"
@pswd = "amee"
Java::com.mysql.jdbc.Driver

# Migration ProfileItems where end is true
def migrate_pi_end

  begin
    conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
    stmt = conn.create_statement()

    # Get a count of rows to be updated
    rs = stmt.execute_query("select count(ID) as c from ITEM where TYPE = 'PI' and END is FALSE")
    rs.first()
    before_count = rs.getInt("c")
    puts "migrate_pi_end - updating #{before_count} rows"
    
    # Select all ProfileItems where end is true
    query = "update ITEM set START_DATE = END_DATE where TYPE = 'PI' and END is FALSE"
    after_count = stmt.executeUpdate(query)
    puts "migrate_pi_end - updated #{after_count} rows"

  rescue => err
    puts "migrate_pi_end - exception: #{err} : #{err.backtrace}"
  ensure
    rs.close
    stmt.close
    conn.close
  end
    
end

# Insert new ItemValueDefinitions
def insert_new_ivd
  conn = JavaSql::DriverManager.get_connection(@url, @user, @pswd)
  stmt = conn.create_statement()
  
  file = File.new("insert_ivd.sql","r")
  while(line = file.gets)
    puts line.sub(/\{UID\}/, JM::UidGen.getUid())
    #count = stmt.executeUpdate(line)
    #puts "#{count} row updated 
end

# Run the migrations
migrate_pi_end
insert_new_ivd
