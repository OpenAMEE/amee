set foreign_key_checks=0; 
set sql_log_bin=0;
set unique_checks=0;

# Item Definitions
load data infile '/Development/AMEE/amee.platform/project/amee/item_definition.csv' into table ITEM_DEFINITION FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, NAME, DRILL_DOWN, CREATED, MODIFIED, ENVIRONMENT_ID);

# Item Value Definitions
load data infile '/Development/AMEE/amee.platform/project/amee/item_value_definition.csv' into table ITEM_VALUE_DEFINITION FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, NAME, PATH, FROM_PROFILE, FROM_DATA, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, VALUE_DEFINITION_ID, ALLOWED_ROLES);

# Data Categories
load data infile '/Development/AMEE/amee.platform/project/amee/data_category.csv' into table DATA_CATEGORY FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, NAME, PATH, CREATED, MODIFIED, ENVIRONMENT_ID, DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME);

# Items
load data infile '/Development/AMEE/amee.platform/project/amee/item.csv' into table ITEM FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, CREATED, MODIFIED, ENVIRONMENT_ID, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, TYPE, NAME, PATH);

# Item Values
load data infile '/Development/AMEE/amee.platform/project/amee/item_value.csv' into table ITEM_VALUE FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, ITEM_ID, VALUE, START_DATE);
