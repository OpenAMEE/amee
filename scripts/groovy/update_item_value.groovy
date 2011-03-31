
/**
 * This script generates SQL statements to update invalid item values.
 *
 * It expects a tab separated value document as input. The following documents were used:
 *
 * STAGE - https://spreadsheets.google.com/a/amee.cc/ccc?key=0AkIZCHjBXlaPdDJDQ1EzLVh1M3dLRVh3aGQyYzl2WVE#gid=0
 *
 * LIVE - https://spreadsheets0.google.com/a/amee.cc/ccc?key=t_LNO1nD8fpAUMnWDadoiug#gid=0
 *      - https://spreadsheets1.google.com/a/amee.cc/ccc?key=tXHfcU-IBiDxHenKnkfmatQ#gid=0
 *
 * See: https://jira.amee.com/browse/PL-10403
 */

if (args.size() == 0) {
    println("Pass the csv file as an argument.")
    System.exit(1)
}

def file = new File(args[0]);

// Start a transaction
println "start transaction;"

// Iterate over the file
file.splitEachLine("\t") { row ->
    def id = row[2]
    def value = row[10]
    if (value == 'NULL') {
        value = "''";
    } else {
        value = "'${value}'"
    }
    println "update ITEM_VALUE set VALUE = ${value} where ID = ${id};"
}

// Commit the transaction
println "commit;"
