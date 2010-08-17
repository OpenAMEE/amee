import com.amee.base.utils.UidGen
import groovy.sql.Sql
import au.com.bytecode.opencsv.CSVWriter;
import org.joda.time.format.ISODateTimeFormat;

// LCA required testing scale:
// * sub1Count = 160
// * sub2Count = 100
// * valueDefCount = 15
// * itemCount = 500

// Testing scale (see above).
levelOneCategoryCount = 1
levelTwoCategoryCount = 160
valueDefCount = 15
itemCount = 500
itemValueBatch = 1000
itemValuesExpected = levelOneCategoryCount * levelTwoCategoryCount * itemCount * valueDefCount;

// CSV support.
writeToCSV = true;
batchObjects = new ArrayList<String>();
itemValueWriter = null;
dataCategoryWriter = null;
itemWriter = null;
itemDefinitionWriter = null;
itemValueDefinitionWriter = null;
if (writeToCSV) {
  itemValueWriter = new CSVWriter(new FileWriter("item_value.csv"), ",".charAt(0),  "\"".charAt(0));
  dataCategoryWriter = new CSVWriter(new FileWriter("data_category.csv"), ",".charAt(0),  "\"".charAt(0));
  itemWriter = new CSVWriter(new FileWriter("item.csv"), ",".charAt(0),  "\"".charAt(0));
  itemDefinitionWriter = new CSVWriter(new FileWriter("item_definition.csv"), ",".charAt(0),  "\"".charAt(0));
  itemValueDefinitionWriter = new CSVWriter(new FileWriter("item_value_definition.csv"), ",".charAt(0),  "\"".charAt(0));
}
DATE_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

// The environmentId is always 2.
environmentId = 2

// Add a random character generator to the String class.
addRandomStringMethodToString()

// Handle command line parameters
def CliBuilder cli = configureCliBuilder()
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    return
}

// Database options.
def server = "localhost"
if (opt.s) server = opt.s
def database = "amee"
if (opt.d) database = opt.d
def user = "amee"
if (opt.u) user = opt.u
def password = "amee"
if (opt.p) password = opt.p

// Configure DataSource.
sql = Sql.newInstance("jdbc:mysql://${server}:3306/${database}", user, password, "com.mysql.jdbc.Driver")
sql.connection.autoCommit = false

// Item Value batch vars.
itemValueSql = "INSERT INTO ITEM_VALUE (ID, UID, STATUS, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, ITEM_ID, VALUE, START_DATE) " +
  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
itemValueStatement = sql.connection.prepareStatement(itemValueSql)
batchCount = 0

// Get initial IDs.
nextDataCategoryId = getMaxId("DATA_CATEGORY") + 1;
nextItemDefinitionId = getMaxId("ITEM_DEFINITION") + 1;
nextItemValueDefinitionId = getMaxId("ITEM_VALUE_DEFINITION") + 1;
nextItemId = getMaxId("ITEM") + 1;
nextItemValueId = getMaxId("ITEM_VALUE") + 1;

// Find the Root & LCA data categories
def rootCategoryId = getRootCategoryId()
def rootLCACategoryId = getOrCreateRootLCACategory(rootCategoryId)
println "Top level LCA DATA_CATEGORY '${rootLCACategoryId}'."

// Commit all so far.
commit()

// Create sub1Count sub1 DCs with parent ID created in (1)
def levelOneCategoryIds = createLevelOneCategories(rootLCACategoryId);

// Commit all so far.
commit()

// Create Data Categories and Item Definitions.
createLevelTwoDataCategories(levelOneCategoryIds);

// Close CSV files.
if (writeToCSV) {
  itemValueWriter.close();
  dataCategoryWriter.close();
  itemWriter.close();
  itemDefinitionWriter.close();
  itemValueDefinitionWriter.close();
}

println "Created everything.";

// ***** Private methods below. *****

// 2) Create sub1Count sub1 DCs with parent ID created in (1)
private List createLevelOneCategories(rootLCACategoryId) {
  println "Creating ${levelOneCategoryCount} DATA_CATEGORYs with parent ID: ${rootLCACategoryId}."
  def levelOneCategoryIds = []
  levelOneCategoryCount.times {
    def name = 'LCA_' + String.randomString(12)
    def path = name
    def dataCategoryId = rootLCACategoryId
    def wikiName = name
    executeInsert(
      "INSERT INTO DATA_CATEGORY (ID, UID, STATUS, NAME, PATH, CREATED, MODIFIED, ENVIRONMENT_ID, DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
      [nextDataCategoryId, UidGen.INSTANCE_12.getUid(), 1, name, path, getNow(), getNow(), environmentId, dataCategoryId, null, wikiName])
    levelOneCategoryIds << nextDataCategoryId
    nextDataCategoryId++
  }
  return levelOneCategoryIds
}

// 3) For each sub1 DC created in (2)
//    create sub2Count DCs with ItemDefs
//    store the list of ItemDef IDs and DC IDs created in a Map(DC_ID => ItemDef_ID)
private def createLevelTwoDataCategories(levelOneCategoryIds) {

  levelOneCategoryIds.each { levelOneCategoryId ->

    levelTwoCategoryCount.times {

      // TODO: Typically drill downs only cover 2 or 3 IVDs rather than all of them.
      // Create drill down list.
      // Underscore used here is IVD paths need to be JS compatible.
      def drillDownList = []
      valueDefCount.times {
        drillDownList << 'LCA_' + String.randomString(4)
      }

      // Create Item Definition.
      def itemDefinitionId = createItemDefinition('LCA_' + String.randomString(12), drillDownList)

      // Create Level Two Data Category.
      def levelTwoCategoryId = createLevelTwoDataCategory(levelOneCategoryId, itemDefinitionId)

      // For each sub2 ItemDef created in (3) create itemCount Items
      def itemIds = createDataItems(levelTwoCategoryId, itemDefinitionId);

      // Create Item Value Definitions
      def itemValueDefinitionIds = createItemValueDefinitions(itemDefinitionId, drillDownList)

      // Commit all so far.
      commit()

      // For each Data Item created in:
      //   For each ItemValueDef for that Data Item:
      //      create an Item Value:
      itemIds.each { itemId ->
        createItemValues(itemValueDefinitionIds, itemId)
      }

      // Handle remaining Item Values in current batch.
      if (batchCount > 0) {
        itemValueStatement.executeBatch()
        println "Created ${batchCount} ITEM_VALUEs in a batch.";
        batchCount = 0
      }

      // Commit all so far.
      commit()
    }
  }
}

// Create Item Values in batches.
private def createItemValues(itemValueDefinitionIds, itemId) {

  // Iterate over all Item Value Definitions.
  itemValueDefinitionIds.each { itemValueDefinitionId ->

    // Create a single batch entry.
    setBatchObject(1, nextItemValueId)
    setBatchObject(2, UidGen.INSTANCE_12.getUid())
    setBatchObject(3, 1);
    setBatchObject(4, getNow());
    setBatchObject(5, getNow());
    setBatchObject(6, itemValueDefinitionId)
    setBatchObject(7, itemId)
    setBatchObject(8, String.randomString(5))
    setBatchObject(9, "1970-01-01 00:00:00")
    addBatch()

    // Next ID.
    nextItemValueId++

    // Handle batch.
    batchCount++
    if (batchCount >= itemValueBatch) {
      // Execute this batch.
      executeBatch();
      println "Created ${batchCount} ITEM_VALUEs in a batch.";
      batchCount = 0;
    }
  }
}

// Create Data Category
private def createLevelTwoDataCategory(levelOneCategoryId, itemDefinitionId) {
  def name = 'LCA_' + String.randomString(12)
  def path = name
  def wikiName = name
  executeInsert("INSERT INTO DATA_CATEGORY (ID, UID, STATUS, NAME, PATH, CREATED, MODIFIED, ENVIRONMENT_ID, DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
          [nextDataCategoryId, UidGen.INSTANCE_12.getUid(), 1, name, path, getNow(), getNow(), environmentId, levelOneCategoryId, itemDefinitionId, wikiName])
  def levelTwoCategoryId = nextDataCategoryId
  nextDataCategoryId++;
  println "Created DATA_CATEGORY '${levelTwoCategoryId}' with parent DATA_CATEGORY_ID '${levelOneCategoryId}' and ITEM_DEFINITION_ID '${itemDefinitionId}'."
  return levelTwoCategoryId;
}

// 4) For each sub2 ItemDef created in (3)
//   create valueDefCount ItemValueDefs that match the drill downs set in the Item Def
private def createItemValueDefinitions(itemDefinitionId, List drillDownList) {
  def itemValueDefinitionIds = []
  drillDownList.each { drillDown ->
    def name = drillDown
    def path = drillDown
    def rand = new Random()
    def valueDefinitionId = rand.nextInt(100)
    executeInsert("INSERT INTO ITEM_VALUE_DEFINITION (ID, UID, STATUS, NAME, PATH, FROM_PROFILE, FROM_DATA, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, VALUE_DEFINITION_ID, ALLOWED_ROLES) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '')",
            [nextItemValueDefinitionId, UidGen.INSTANCE_12.getUid(), 1, name, path, 0, 1, getNow(), getNow(), environmentId, itemDefinitionId, valueDefinitionId])
    def itemValueDefinitionId = nextItemValueDefinitionId
    nextItemValueDefinitionId++
    println "Created ITEM_VALUE_DEFINITION '${itemValueDefinitionId}' with NAME '${drillDown}'."
    itemValueDefinitionIds << itemValueDefinitionId
  }
  return itemValueDefinitionIds
}

// Create Item Definition.
private def createItemDefinition(String name, List drillDownList) {
  // Note: Using pipe delimiter instead of comma to make CSV SQL loading easier.
  executeInsert("INSERT INTO ITEM_DEFINITION (ID, UID, STATUS, NAME, DRILL_DOWN, CREATED, MODIFIED, ENVIRONMENT_ID) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
          [nextItemDefinitionId, UidGen.INSTANCE_12.getUid(), 1, name, drillDownList.join('|'), getNow(), getNow(), environmentId])
  def itemDefinitionId = nextItemDefinitionId
  nextItemDefinitionId++
  println "Created ITEM_DEFINITION '${itemDefinitionId}'."
  return itemDefinitionId;
}

// For each sub2 ItemDef created in (3) create itemCount Items
private List createDataItems(dataCategoryId, itemDefinitionId) {
  itemIds = []
  itemCount.times {
    executeInsert(
            "INSERT INTO ITEM (ID, UID, STATUS, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, TYPE, NAME, PATH) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '')",
            [nextItemId, UidGen.INSTANCE_12.getUid(), 1, getNow(), getNow(), environmentId, itemDefinitionId, dataCategoryId, 'DI', 'LCA data item'])
    itemIds << nextItemId
    nextItemId++
  }
  println "Created $itemCount ITEMs for DATA_CATEGORY_ID '${dataCategoryId}' and ITEM_DEFINITION_ID '${itemDefinitionId}'."
  return itemIds;
}

private def configureCliBuilder() {
  def cli = new CliBuilder(usage: 'groovy generate.groovy [-h] [-s server] [-d database] [-u user] [-p password]')
  cli.h(longOpt: 'help', 'usage information')
  cli.s(argName: 'servername', longOpt: 'server', args: 1, required: false, type: GString, "server name (default 'localhost')")
  cli.d(argName: 'database', longOpt: 'database', args: 1, required: false, type: GString, "database name (default 'amee')")
  cli.u(argName: 'user', longOpt: 'user', args: 1, required: false, type: GString, "username (default 'amee')")
  cli.p(argName: 'password', longOpt: 'password', args: 1, required: false, type: GString, "password (default 'amee')")
  return cli
}

// Add a random character generator to the String class.
private def addRandomStringMethodToString() {
  String.metaClass.'static'.randomString = { length ->
    // The chars used for the random string
    def list = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    // Make sure the list is long enough
    list = list * (1 + length / list.size())
    // Shuffle it up good
    Collections.shuffle(list)
    length > 0 ? list[0..length - 1].join() : ''
  }
}

// Find the Root data category.
private def getRootCategoryId() {
  return sql.firstRow("SELECT ID FROM DATA_CATEGORY WHERE NAME = 'Root' AND ENVIRONMENT_ID = 2").ID
}

// 1) Get or create the single top data category for LCA data called 'lca'.
private def getOrCreateRootLCACategory(rootCategoryId) {
  result = sql.firstRow("SELECT ID FROM DATA_CATEGORY WHERE PATH = 'lca' AND ENVIRONMENT_ID = 2 AND DATA_CATEGORY_ID = " + rootCategoryId);
  if (result != null) {
    return result.ID;
  } else {
    executeInsert(
      "INSERT INTO DATA_CATEGORY (ID, UID, STATUS, NAME, PATH, CREATED, MODIFIED, ENVIRONMENT_ID, DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
      [nextDataCategoryId, UidGen.INSTANCE_12.getUid(), 1, 'lca', 'lca', getNow(), getNow(), environmentId, rootCategoryId, null, 'lca'])
    nextDataCategoryId++;
    return nextDataCategoryId
  }
}

// Get max ID from column.
private def getMaxId(column) {
  return sql.firstRow("SELECT max(ID) ID FROM " + column).ID
}

// JDBC or CSV INSERT.
private def executeInsert(query, params) {
  if (!writeToCSV) {
    sql.executeInsert(query, params);
  } else {
    if (query.contains("INTO ITEM_DEFINITION")) {
      itemDefinitionWriter.writeNext(toStringArray(params));
    } else if (query.contains("INTO ITEM_VALUE_DEFINITION")) {
      itemValueDefinitionWriter.writeNext(toStringArray(params));
    } else if (query.contains("INTO DATA_CATEGORY")) {
      dataCategoryWriter.writeNext(toStringArray(params));
    } else if (query.contains("INTO ITEM")) {
      itemWriter.writeNext(toStringArray(params));
    }
  }
}

private def commit() {
  if (!writeToCSV) {
    sql.commit()
  } else {
    itemValueWriter.flush()
  }
}

private def executeBatch() {
  if (!writeToCSV) {
    itemValueStatement.executeBatch()
  }
}

private def addBatch() {
  if (!writeToCSV) {
    itemValueStatement.addBatch()
  } else {
    itemValueWriter.writeNext(toStringArray(batchObjects));
    batchObjects = new ArrayList<String>();
  }
}

private def setBatchObject(index, object) {
  if (!writeToCSV) {
    itemValueStatement.setObject(index, object)
  } else {
    batchObjects.add(object.toString());
  }
}

private String[] toStringArray(List params) {
  List<String> results = new ArrayList<String>();
  params.each { object ->
    if (object != null) {
      results.add(object.toString());
    } else {
      results.add("\\N");
    }
  }
  return results.toArray(new String[0]);
}

private def getNow() {
  if (!writeToCSV) {
    return new Date();
  } else {
    return DATE_FORMAT.print(new Date().time);
  }
}