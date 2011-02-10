
/**
 * This script generates SQL statements to update invalid item values.
 *
 * It expects a tab separated value document as input. The following documents were used:
 *
 * STAGE - https://spreadsheets.google.com/a/amee.cc/ccc?key=0AkIZCHjBXlaPdDJDQ1EzLVh1M3dLRVh3aGQyYzl2WVE&hl=en#gid=0
 *
 * LIVE - https://spreadsheets0.google.com/a/amee.cc/ccc?hl=en&key=t_LNO1nD8fpAUMnWDadoiug&hl=en#gid=0
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
    println "update ITEM_VALE set VALUE = ${row[10]} where ID = ${row[2]};"
}

// Commit the transaction
println "commit;"
