import groovy.sql.Sql
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

// The environmentId is always 2.
environmentId = 2;

// Counters.
numbers = 0;
strings = 0;
minNumber = new BigDecimal("0");
maxNumber = new BigDecimal("0");

// Handle command line parameters
def CliBuilder cli = configureCliBuilder();
def opt = cli.parse(args);
if (opt.h) {
  cli.usage();
  return;
}

// Database options.
def server = "localhost";
if (opt.s)
  server = opt.s;
def database = "amee";
if (opt.d)
  database = opt.d;
def user = "amee";
if (opt.u)
  user = opt.u;
def password = "amee";
if (opt.p)
  password = opt.p;

// Configure DataSource.
sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver");
sql.connection.autoCommit = false;

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

// Execute query and get scrollable ResultSet.
rs = st.executeQuery("SELECT VALUE FROM ITEM_VALUE WHERE ITEM_ID IN (SELECT ID FROM ITEM WHERE TYPE = 'PI')");

// Iterate over all Item Values.
while (rs.next()) {
  try {
    number = new BigDecimal(rs.getString("VALUE"));
    numbers++;
    if (number.compareTo(minNumber) < 0) {
      minNumber = number;
    }
    if (number.compareTo(maxNumber) > 0) {
      maxNumber = number;
    }
  } catch (NumberFormatException e) {
    strings++;
//    println(rs.getString("VALUE").length());
  }
}

// We're done!
rs.close();
st.close();

// Print results.
println "Numbers: " + numbers;
println "  Max: " + maxNumber;
println "  Min: " + minNumber;
println "Strings: " + strings;

// ***** Private methods below. *****

private def configureCliBuilder() {
  def cli = new CliBuilder(usage: 'groovy generate.groovy [-h] [-s server] [-d database] [-u user] [-p password]');
  cli.h(longOpt: 'help', 'usage information');
  cli.s(argName: 'servername', longOpt: 'server', args: 1, required: false, type: GString, "server name (default 'localhost')");
  cli.d(argName: 'database', longOpt: 'database', args: 1, required: false, type: GString, "database name (default 'amee')");
  cli.u(argName: 'user', longOpt: 'user', args: 1, required: false, type: GString, "username (default 'amee')");
  cli.p(argName: 'password', longOpt: 'password', args: 1, required: false, type: GString, "password (default 'amee')");
  return cli;
}
