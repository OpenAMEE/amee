import com.amee.base.utils.UidGen
import groovy.sql.Sql

// LCA scaling vars:
// * sub1Count = 160
// * sub2Count = 100
// * valueDefCount = 15
// * itemCount = 500

// testing
sub1Count = 1
sub2Count = 1
valueDefCount = 15
itemCount = 50
itemValueBatch = 300
itemValueBatchGroup = itemValueBatch * 10
itemValuesExpected = sub1Count * sub2Count * valueDefCount * itemCount;

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

def database = "amee"
if (opt.d) database = opt.d

def user = "amee"
if (opt.u) user = opt.u

def password = "amee"
if (opt.p) password = opt.p

sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")
sql.connection.autoCommit = false

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

// TODO: Should we use '/test' instead?
// Find the Root data category
rootCat = sql.firstRow("SELECT ID FROM DATA_CATEGORY WHERE NAME = 'Root' AND ENVIRONMENT_ID = 2").ID

// TODO: Detect if this already exists already.
// 1) Create a single top data category for LCA data called 'lca'
rootKeys = sql.executeInsert(
    "INSERT INTO DATA_CATEGORY (UID, STATUS, NAME, PATH, CREATED, MODIFIED, ENVIRONMENT_ID, DATA_CATEGORY_ID, WIKI_NAME) " +
    "VALUES (?, 1, ?, ?, NOW(), NOW(), ?, ?, ?)",
    [UidGen.INSTANCE_12.getUid(), 'lca', 'lca', environmentId, rootCat, 'lca'])

lcaRootId = rootKeys[0][0]
println "Created top level LCA DATA_CATEGORY '${lcaRootId}'."

// 2) Create sub1Count sub1 DCs with parent ID created in (1)
sub1Keys = getLevelOneSubCategoryIDs();

// 3) Create Data Categories and Item Definitions.
dataCategoryToItemDefinitionMap = getDataCategoryToItemDefinitionMap(sub1Keys);

// 5) For each sub2 ItemDef created in (3) create itemCount Items
itemKeys = getItemIDs(dataCategoryToItemDefinitionMap);

// Commit all so far.
sql.commit()


println "Creating ${itemValuesExpected} Item Values."

// SQL to create ItemValues.
ivSql = "INSERT INTO ITEM_VALUE (UID, STATUS, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, ITEM_ID, VALUE, START_DATE) " +
  "VALUES (?, 1, NOW(), NOW(), ?, ?, ?, ?)";
stmt = sql.connection.prepareStatement(ivSql)

// Counter for batching.
batchCount = 0
batchGroupCount = 0

// 6) For each Item created in (5)
//    For each ItemValueDef for that Item
//      create an ItemValue
itemKeys.each { itemKey ->

  // TODO: Use value types (numbers, strings) that match the IVD & VD.
  // Get a list of Item Value Definitions for this item's item definition

  sql.eachRow("SELECT ITEM_VALUE_DEFINITION.ID FROM ITEM_VALUE_DEFINITION" +
      " JOIN ITEM_DEFINITION ON (ITEM_VALUE_DEFINITION.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)" +
      " JOIN ITEM ON (ITEM.ITEM_DEFINITION_ID = ITEM_DEFINITION.ID)" +
      " WHERE ITEM.ID = ${itemKey[0][0]}") { row ->


    // Vars for this ItemValue.
    itemValueDefinitionId = row.ID
    itemId = itemKey[0][0]
    value = String.randomString(5)

    // Execute INSERTs in a batches.
    valueDefCount.times {

      // Create a single batch entry.
      stmt.setObject(1, UidGen.INSTANCE_12.getUid())
      stmt.setObject(2, itemValueDefinitionId)
      stmt.setObject(3, itemId)
      stmt.setObject(4, value)
      stmt.setObject(5, "1970-01-01 00:00:00")
      stmt.addBatch()

      // Handle batch.
      batchCount++
      batchGroupCount++
      if (batchCount >= itemValueBatch) {
        stmt.executeUpdate();
        println "Created ${batchCount} ITEM_VALUEs in a batch.";
        batchCount = 0;
        if (batchGroupCount > itemValueBatchGroup) {
          stmt.close()
          sql.commit()
          stmt = sql.connection.prepareStatement(ivSql)
          println "Starting new batch group after ${batchGroupCount} ITEM_VALUEs.";
          batchGroupCount = 0;
        }
      }
    }
  }
}

// Handle remaining Item Values in current batch.
if (batchCount > 0) {
  stmt.executeUpdate();
  println "Created ${batchCount} ITEM_VALUEs in a batch.";
}
stmt.close()
sql.commit()

println "Created all ITEM_VALUEs.";

// ***** Private methods below. *****

// 2) Create sub1Count sub1 DCs with parent ID created in (1)
private List getLevelOneSubCategoryIDs() {
  println "Creating ${sub1Count} DATA_CATEGORYs with parent ID: ${lcaRootId}."
  sub1Keys = []
  sub1Count.times {
    name = 'LCA_' + String.randomString(12)
    path = name
    dataCategoryId = lcaRootId
    wikiName = name
    sub1Keys << sql.executeInsert(
            "INSERT INTO DATA_CATEGORY (UID, STATUS, NAME, PATH, CREATED, MODIFIED, ENVIRONMENT_ID, DATA_CATEGORY_ID, WIKI_NAME) " +
                    "VALUES (?, 1, ?, ?, NOW(), NOW(), ?, ?, ?)",
            [UidGen.INSTANCE_12.getUid(), name, path, environmentId, dataCategoryId, wikiName])
  }
  return sub1Keys
}

// 3) For each sub1 DC created in (2)
//    create sub2Count DCs with ItemDefs
//    store the list of ItemDef IDs and DC IDs created in a Map(DC_ID => ItemDef_ID)
private def getDataCategoryToItemDefinitionMap(sub1Keys) {

  dataCategoryToItemDefinitionMap = [:]

  sub1Keys.each { sub1Key ->

    sub2Keys = []
    sub2Count.times {

      // Create an Item Def
      name = 'LCA_' + String.randomString(12)

      // TODO: Typically drill downs only cover 2 or 3 IVDs rather than all of them.
      // Create drill down list.
      // Underscore used here is IVD paths need to be JS compatible.
      drillDownList = []
      valueDefCount.times {
        drillDownList << 'LCA_' + String.randomString(4)
      }

      itemDefKeys = sql.executeInsert("INSERT INTO ITEM_DEFINITION (UID, STATUS, NAME, DRILL_DOWN, CREATED, MODIFIED, ENVIRONMENT_ID)" +
              "VALUES (?, 1, ?, ?, NOW(), NOW(), ?)",
              [UidGen.INSTANCE_12.getUid(), name, drillDownList.join(','), environmentId])


      itemDefKey = itemDefKeys[0][0]
      println "Created ITEM_DEFINITION '${itemDefKey}'."

      // And a linked Data Category
      name = 'LCA_' + String.randomString(12)
      path = name
      dataCategoryId = sub1Key[0][0]
      wikiName = name
      sub2Keys = sql.executeInsert("INSERT INTO DATA_CATEGORY (UID, STATUS, NAME, PATH, CREATED, MODIFIED, ENVIRONMENT_ID, DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME)" +
              "VALUES (?, 1, ?, ?, NOW(), NOW(), ?, ?, ?, ?)",
              [UidGen.INSTANCE_12.getUid(), name, path, environmentId, dataCategoryId, itemDefKey, wikiName])

      println "Created DATA_CATEGORY '${sub2Keys[0][0]}' with parent DATA_CATEGORY_ID '${dataCategoryId}' and ITEM_DEFINITION_ID '${itemDefKey}'."

      // Map of DC IDs to Item Def IDs
      dataCategoryToItemDefinitionMap[(sub2Keys[0][0])] = itemDefKeys[0][0]

      // TODO: Use workable Value Definition IDs.
      // 4) For each sub2 ItemDef created in (3)
      //   create valueDefCount ItemValueDefs that match the drill downs set in the Item Def

      drillDownList.each { drillDown ->
        name = drillDown
        path = drillDown
        itemDefinitionId = itemDefKeys[0][0]
        rand = new Random()
        valueDefinitionId = rand.nextInt(100)
        valueDefKeys = sql.executeInsert("INSERT INTO ITEM_VALUE_DEFINITION (UID, STATUS, NAME, PATH, FROM_DATA, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, VALUE_DEFINITION_ID)" +
                "VALUES (?, 1, ?, ?, ?, NOW(), NOW(), ?, ?, ?)",
                [UidGen.INSTANCE_12.getUid(), name, path, 1, environmentId, itemDefinitionId, valueDefinitionId])

        println "Created ITEM_VALUE_DEFINITION '${valueDefKeys[0][0]}' with NAME '${drillDown}'."
      }
    }
  }

  return dataCategoryToItemDefinitionMap
}

// 5) For each sub2 ItemDef created in (3) create itemCount Items
private List getItemIDs(dataCategoryToItemDefinitionMap) {
  itemKeys = []
  dataCategoryToItemDefinitionMap.each { catId, itemDefId ->
    itemCount.times {
      itemKey = sql.executeInsert(
              "INSERT INTO ITEM (UID, STATUS, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, TYPE, NAME) " +
                      "VALUES (?, 1, NOW(), NOW(), ?, ?, ?, ?, ?)",
              [UidGen.INSTANCE_12.getUid(), environmentId, itemDefId, catId, 'DI', 'LCA data item'])
      itemKeys << itemKey
    }
    println "Created $itemCount ITEMs for DATA_CATEGORY_ID '${catId}' and ITEM_DEFINITION_ID '${itemDefId}'."
  }
  return itemKeys;
}


