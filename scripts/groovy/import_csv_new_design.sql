set foreign_key_checks=0; 
set sql_log_bin=0;
set unique_checks=0;

# Item Definitions
load data infile '/Development/AMEE/amee.platform/project/amee/scripts/groovy/item_definition.csv' IGNORE into table ITEM_DEFINITION FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, NAME, DRILL_DOWN, CREATED, MODIFIED);

# Item Value Definitions
load data infile '/Development/AMEE/amee.platform/project/amee/scripts/groovy/item_value_definition.csv' IGNORE into table ITEM_VALUE_DEFINITION FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, NAME, PATH, FROM_PROFILE, FROM_DATA, CREATED, MODIFIED, ITEM_DEFINITION_ID, VALUE_DEFINITION_ID, ALLOWED_ROLES);

# Data Categories
load data infile '/Development/AMEE/amee.platform/project/amee/scripts/groovy/data_category.csv' IGNORE into table DATA_CATEGORY FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, NAME, PATH, CREATED, MODIFIED,  DATA_CATEGORY_ID, ITEM_DEFINITION_ID, WIKI_NAME);

# Items
load data infile '/Development/AMEE/amee.platform/project/amee/scripts/groovy/data_item.csv' IGNORE into table DATA_ITEM FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, CREATED, MODIFIED, ITEM_DEFINITION_ID, DATA_CATEGORY_ID, NAME, PATH);

# Item Values
load data infile '/Development/AMEE/amee.platform/project/amee/scripts/groovy/data_item_number_value.csv' IGNORE into table DATA_ITEM_NUMBER_VALUE FIELDS TERMINATED BY ',' ENCLOSED BY '"' (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, UNIT, PER_UNIT);
