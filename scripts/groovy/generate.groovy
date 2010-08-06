import com.amee.base.utils.UidGen
import groovy.sql.Sql

sub1Count = 160
sub2Count = 100
valueDefCount = 15
itemCount = 500


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

String.metaClass.'static'.randomString = { length ->
    // The chars used for the random string
    def list = ('a'..'z')+('A'..'Z')+('0'..'9')
    // Make sure the list is long enough
    list = list * ( 1 + length / list.size())
    // Shuffle it up good
    Collections.shuffle(list)
    length > 0 ? list[0..length - 1].join() : ''
}

// 1) Create a single DC called 'lca'
println "Creating the root LCA DC"

uid = UidGen.INSTANCE_12.getUid()
name = 'lca'
path = name
environmentId = 2
dataCategoryId = 14
wikiName = name
rootKeys = sql.executeInsert("INSERT INTO DATA_CATEGORY (UID, NAME, PATH, CREATED, ENVIRONMENT_ID, DATA_CATEGORY_ID, WIKI_NAME)" +
    "VALUES (?, ?, ?, NOW(), ?, ?, ?)",
    [uid, name, path, environmentId, dataCategoryId, wikiName])

lcaRootId = rootKeys[0][0]
println "Created LCA root DC with ID: ${lcaRootId}"

// 2) Create 160 sub1 DCs with parent ID created in (1)
println "Creating 160 sub1 cats with parent ID: ${lcaRootId}"
sub1Keys = []
sub1Count.times {
    uid = UidGen.INSTANCE_12.getUid()
    name = String.randomString(12)
    path = name
    environmentId = 2
    dataCategoryId = lcaRootId
    wikiName = name
    sub1Keys << sql.executeInsert("INSERT INTO DATA_CATEGORY (UID, NAME, PATH, CREATED, ENVIRONMENT_ID, DATA_CATEGORY_ID, WIKI_NAME)" +
        "VALUES (?, ?, ?, NOW(), ?, ?, ?)",
        [uid, name, path, environmentId, dataCategoryId, wikiName])
}

//sub1Keys.each {println it[0]}

// 3) For each sub1 DC created in (2)
//    create 100 sub2 DCs with ItemDefs
//    store the list of ItemDef IDs and DC IDs created in a Map(DC_ID => ItemDef_ID)

map3 = [:]

sub1Keys.each { sub1Key ->

    sub2Keys = []
    sub2Count.times {

        // Create an Item Def
        name = String.randomString(12)

        //
        drillDownList = []
        valueDefCount.times {
            drillDownList << String.randomString(4)
        }
        environmentId = 2

        itemDefKeys = sql.executeInsert("INSERT INTO ITEM_DEFINITION (UID, NAME, DRILL_DOWN, CREATED, ENVIRONMENT_ID)" +
            "VALUES (?, ?, ?, NOW(), ?)",
            [uid, name, drillDownList.join(','), environmentId])


        itemDefKey = itemDefKeys[0][0]
        println "Created Item Definition with ID: ${itemDefKey}"

        // And a linked Data Category
        println "Creating a Data Cat with parent ID: ${sub1Key[0][0]} and Item Def ID: ${itemDefKey}"
        uid = UidGen.INSTANCE_12.getUid()
        name = String.randomString(12)
        path = name
        environmentId = 2
        dataCategoryId = sub1Key[0][0]
        wikiName = name
        sub2Keys << sql.executeInsert("INSERT INTO DATA_CATEGORY (UID, NAME, PATH, CREATED, ENVIRONMENT_ID, DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME)" +
            "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?)",
            [uid, name, path, environmentId, dataCategoryId, itemDefKey, wikiName])


        // Map of DC IDs to Item Def IDs
        map3[sub2Keys[0][0]] = itemDefKeys[0][0]


        //4) For each sub2 ItemDef created in (3)
        //   create 15 ItemValueDefs that match the drill downs set in the Item Def

        drillDownList.each { drillDown ->
            uid = UidGen.INSTANCE_12.getUid()
            name = drillDown
            path = drillDown
            itemDefinitionId = itemDefKeys[0][0]
            rand = new Random()
            valueDefinitionId = rand.nextInt(100)
            valueDefKeys = sql.executeInsert("INSERT INTO ITEM_VALUE_DEFINITION (UID, NAME, PATH, FROM_DATA, CREATED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, VALUE_DEFINITION_ID)" +
                "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?)",
                [uid, name, path, 1, environmentId, itemDefinitionId, valueDefinitionId])

            println "Created value def with drill: ${drillDown} and ID: ${valueDefKeys[0][0]}"
        }

    }
}

// 5) For each sub2 ItemDef created in (3)
//    create 500 Items
itemKeys = []
map3.each { catId, itemDefId ->
    itemCount.times {
        uid = UidGen.INSTANCE_12.getUid()
        itemDefinitionId = itemDefId
        dataCategory = dataCategoryId
        itemKeys << sql.executeInsert("INSERT INTO ITEM (UID, CREATED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, DATA_CATEGORY_ID)" +
            "VALUES (?, NOW(), ?, ?, ?)",
            [uid, environmentId, itemDefinitionId, dataCategoryId])

        println "Created item with itemDefId: ${itemDefinitionId} and dataCatId: ${dataCategoryId}"
    }
}

// 6) For each Item created in (5)
//    For each ItemValueDef for that Item
//      create an ItemValue
itemKeys.each { itemKey ->
    // Get a list of Item Value Definitions for this item's item definition
    // select ITEM_VALUE_DEFINITION.ID from ITEM_VALUE_DEFINITION
    // join ITEM_DEFINITION on (ITEM_VALUE_DEFINITION.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)
    // join ITEM on (ITEM.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)
    // where ITEM.ID = itemKey[0][0];

    sql.eachRow("select ITEM_VALUE_DEFINITION.ID from ITEM_VALUE_DEFINITION" +
        " join ITEM_DEFINITION on (ITEM_VALUE_DEFINITION.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)" +
        " join ITEM on (ITEM.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)" +
        " where ITEM.ID = ${itemKey[0][0]}") { row ->
        uid = UidGen.INSTANCE_12.getUid()
        itemValueDefinitionId = row.ID
        itemId = itemKey[0][0]
        value = String.randomString(5)
        itemKeys << sql.executeInsert("INSERT INTO ITEM_VALUE (UID, CREATED, ENVIRONMENT_ID, ITEM_VALUE_DEFINITION_ID, ITEM_ID, VALUE)" +
            "VALUES (?, NOW(), ?, ?, ?, ?)",
            [uid, environmentId, itemValueDefinitionId, itemId, value])

        println "Created item value for item ID: ${itemId} and itemValueDef ID: ${itemValueDefinitionId}"
    }


}





