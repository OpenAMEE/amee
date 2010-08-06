import com.amee.base.utils.UidGen
import groovy.sql.Sql

// LCA scaling vars
//sub1Count = 160
//sub2Count = 100
//valueDefCount = 15
//itemCount = 500

// testing
sub1Count = 2
sub2Count = 2
valueDefCount = 2
itemCount = 2

// The environmentId is always 2
environmentId = 2

// Deal with command line parameters
def cli = new CliBuilder(usage: 'groovy generate.groovy [-h] [-s server] [-d database] [-u user] [-p password]')
cli.h(longOpt:'help', 'usage information')
cli.s(argName:'servername', longOpt:'server', args:1, required:false, type:GString, "server name (default 'localhost')")
cli.d(argName:'database', longOpt:'database', args:1, required:false, type:GString, "database name (default 'amee')")
cli.u(argName:'user', longOpt:'user', args:1, required:false, type:GString, "username (default 'amee')")
cli.p(argName:'password', longOpt:'password', args:1, required:false, type:GString, "password (default 'amee')")
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    return
}

def server = "localhost"
if (opt.s) server = opt.s

def database = "benerator"
if (opt.d) database = opt.d

def user = "root"
if (opt.u) user = opt.u

def password = ""
if (opt.p) password = opt.p

sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")

// Add a random character generator to the String class.
String.metaClass.'static'.randomString = { length ->
    // The chars used for the random string
    def list = ('a'..'z')+('A'..'Z')+('0'..'9')
    // Make sure the list is long enough
    list = list * ( 1 + length / list.size())
    // Shuffle it up good
    Collections.shuffle(list)
    length > 0 ? list[0..length - 1].join() : ''
}

// Find the Root data category
rootCat = sql.firstRow("SELECT ID FROM DATA_CATEGORY WHERE NAME = 'Root' AND ENVIRONMENT_ID = 2").ID

// 1) Create a single top data category for LCA data called 'lca'
rootKeys = sql.executeInsert(
    "INSERT INTO DATA_CATEGORY (UID, NAME, PATH, CREATED, ENVIRONMENT_ID, DATA_CATEGORY_ID, WIKI_NAME) VALUES (?, ?, ?, NOW(), ?, ?, ?)",
    [UidGen.INSTANCE_12.getUid(), 'lca', 'lca', environmentId, rootCat, 'lca'])

lcaRootId = rootKeys[0][0]
println "Created top level LCA DATA_CATEGORY (${lcaRootId})"

// 2) Create 160 sub1 DCs with parent ID created in (1)
println "Creating ${sub1Count} DATA_CATEGORYs with parent ID: ${lcaRootId}"
sub1Keys = []
sub1Count.times {
    name = 'LCA-' + String.randomString(12)
    path = name
    dataCategoryId = lcaRootId
    wikiName = name
    sub1Keys << sql.executeInsert(
        "INSERT INTO DATA_CATEGORY (UID, NAME, PATH, CREATED, ENVIRONMENT_ID, DATA_CATEGORY_ID, WIKI_NAME) VALUES (?, ?, ?, NOW(), ?, ?, ?)",
        [UidGen.INSTANCE_12.getUid(), name, path, environmentId, dataCategoryId, wikiName])
}

// 3) For each sub1 DC created in (2)
//    create 100 sub2 DCs with ItemDefs
//    store the list of ItemDef IDs and DC IDs created in a Map(DC_ID => ItemDef_ID)

map3 = [:]

sub1Keys.each { sub1Key ->

    sub2Keys = []
    sub2Count.times {

        // Create an Item Def
        name = 'LCA-' + String.randomString(12)

        //
        drillDownList = []
        valueDefCount.times {
            drillDownList << 'LCA-' + String.randomString(4)
        }

        itemDefKeys = sql.executeInsert("INSERT INTO ITEM_DEFINITION (UID, NAME, DRILL_DOWN, CREATED, ENVIRONMENT_ID)" +
            "VALUES (?, ?, ?, NOW(), ?)",
            [UidGen.INSTANCE_12.getUid(), name, drillDownList.join(','), environmentId])


        itemDefKey = itemDefKeys[0][0]
        println "Created ITEM_DEFINITION (${itemDefKey})"

        // And a linked Data Category
        name = 'LCA-' + String.randomString(12)
        path = name
        dataCategoryId = sub1Key[0][0]
        wikiName = name
        sub2Keys = sql.executeInsert("INSERT INTO DATA_CATEGORY (UID, NAME, PATH, CREATED, ENVIRONMENT_ID, DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME)" +
            "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?)",
            [UidGen.INSTANCE_12.getUid(), name, path, environmentId, dataCategoryId, itemDefKey, wikiName])

        println "Created DATA_CATEGORY (${sub2Keys[0][0]}) with parent DATA_CATEGORY_ID: ${dataCategoryId} and ITEM_DEFINITION_ID: ${itemDefKey}"
        
        // Map of DC IDs to Item Def IDs

        map3[(sub2Keys[0][0])] = itemDefKeys[0][0]


        //4) For each sub2 ItemDef created in (3)
        //   create 15 ItemValueDefs that match the drill downs set in the Item Def

        drillDownList.each { drillDown ->
            name = drillDown
            path = drillDown
            itemDefinitionId = itemDefKeys[0][0]
            rand = new Random()
            valueDefinitionId = rand.nextInt(100)
            valueDefKeys = sql.executeInsert("INSERT INTO ITEM_VALUE_DEFINITION (UID, NAME, PATH, FROM_DATA, CREATED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, VALUE_DEFINITION_ID)" +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?)",
                [UidGen.INSTANCE_12.getUid(), name, path, 1, environmentId, itemDefinitionId, valueDefinitionId])

            println "Created ITEM_VALUE_DEFINITION (${valueDefKeys[0][0]}) with NAME: ${drillDown}"
        }

    }
}

// 5) For each sub2 ItemDef created in (3)
//    create 500 Items
itemKeys = []
map3.each { catId, itemDefId ->
    itemCount.times {
        itemKeys << sql.executeInsert(
            "INSERT INTO ITEM (UID, CREATED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, TYPE, NAME) VALUES (?, NOW(), ?, ?, ?, ?, ?)",
            [UidGen.INSTANCE_12.getUid(), environmentId, itemDefId, catId, 'DI', 'LCA data item'])

        println "Created ITEM (${itemKeys[0][0]}) with ITEM_DEFINITION_ID: ${itemDefId} and DATA_CATEGORY_ID: ${catId}"
    }
}

// 6) For each Item created in (5)
//    For each ItemValueDef for that Item
//      create an ItemValue
itemValueKeys = []
itemKeys.each { itemKey ->
    // Get a list of Item Value Definitions for this item's item definition
    // select ITEM_VALUE_DEFINITION.ID from ITEM_VALUE_DEFINITION
    // join ITEM_DEFINITION on (ITEM_VALUE_DEFINITION.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)
    // join ITEM on (ITEM.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)
    // where ITEM.ID = itemKey[0][0];

    sql.eachRow("SELECT ITEM_VALUE_DEFINITION.ID FROM ITEM_VALUE_DEFINITION" +
        " JOIN ITEM_DEFINITION ON (ITEM_VALUE_DEFINITION.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)" +
        " JOIN ITEM ON (ITEM.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)" +
        " WHERE ITEM.ID = ${itemKey[0][0]}") { row ->
        itemValueDefinitionId = row.ID
        itemId = itemKey[0][0]
        value = String.randomString(5)
        itemValueKeys = sql.executeInsert("INSERT INTO ITEM_VALUE (UID, CREATED, ITEM_VALUE_DEFINITION_ID, ITEM_ID, VALUE, START_DATE)" +
            "VALUES (?, NOW(), ?, ?, ?, ?)",
            [UidGen.INSTANCE_12.getUid(), itemValueDefinitionId, itemId, value, '1970-01-01 00:00:00'])

        println "Created ITEM_VALUE (${itemValueKeys[0][0]}) with ITEM_ID: ${itemId} and ITEM_VALUE_DEFINITION_ID: ${itemValueDefinitionId}"
    }
}





