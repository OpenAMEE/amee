-- Script to increment primary keys in new ITEM and ITEM_VALUE tables on live to prevent conflicts during migration.

-- Need to use a temp table to prevent ID conflicts with itself
create table if not exists DATA_ITEM_NUMBER_VALUE_NEW like DATA_ITEM_NUMBER_VALUE;
create table if not exists DATA_ITEM_TEXT_VALUE_NEW like DATA_ITEM_TEXT_VALUE;

-- Drop the indexes to make copying the table faster
-- Indexes will be recreated manually after the data has been migrated
alter table DATA_ITEM_NUMBER_VALUE_NEW drop index UID, drop index ITEM_VALUE_DEFINITION_ID_KEY, drop index DATA_ITEM_ID_KEY;
alter table DATA_ITEM_TEXT_VALUE_NEW drop index UID, drop index ITEM_VALUE_DEFINITION_ID_KEY, drop index DATA_ITEM_ID_KEY;

-- Increment PROFILE_ITEM table
start transaction;
update PROFILE_ITEM set ID = ID + 8000000;
update PROFILE_ITEM_NUMBER_VALUE set PROFILE_ITEM_ID = PROFILE_ITEM_ID + 8000000;
update PROFILE_ITEM_TEXT_VALUE set PROFILE_ITEM_ID = PROFILE_ITEM_ID + 8000000;
commit;

-- Increment new item value tables
start transaction;
update PROFILE_ITEM_NUMBER_VALUE set ID = ID + 40000000;
update PROFILE_ITEM_TEXT_VALUE set ID = ID + 40000000;
commit;

-- Copy data
start transaction;
insert into DATA_ITEM_NUMBER_VALUE_NEW (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, UNIT, PER_UNIT) 
	select ID + 40000000, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID, UNIT, PER_UNIT
	from DATA_ITEM_NUMBER_VALUE;
insert into DATA_ITEM_TEXT_VALUE_NEW (ID, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID) 
	select ID + 40000000, UID, STATUS, VALUE, CREATED, MODIFIED, ITEM_VALUE_DEFINITION_ID, DATA_ITEM_ID
	from DATA_ITEM_TEXT_VALUE;
commit;

-- Rename tables
rename table DATA_ITEM_NUMBER_VALUE to DATA_ITEM_NUMBER_VALUE_OLD, DATA_ITEM_NUMBER_VALUE_NEW TO DATA_ITEM_NUMBER_VALUE; 
rename table DATA_ITEM_TEXT_VALUE to DATA_ITEM_TEXT_VALUE_OLD, DATA_ITEM_TEXT_VALUE_NEW TO DATA_ITEM_TEXT_VALUE; 

-- Recreate indexes
-- alter table DATA_ITEM_NUMBER_VALUE add unique index UID(UID), add index ITEM_VALUE_DEFINITION_ID_KEY(ITEM_VALUE_DEFINITION_ID), add index DATA_ITEM_ID_KEY(DATA_ITEM_ID);
-- alter table DATA_ITEM_TEXT_VALUE add unique index UID(UID), add index ITEM_VALUE_DEFINITION_ID_KEY(ITEM_VALUE_DEFINITION_ID), add index DATA_ITEM_ID_KEY(DATA_ITEM_ID);

-- Update dependent LOCALE_NAME table
-- This table has been deprecated
-- update LOCALE_NAME set ENTITY_ID = ENTITY_ID + 30000000 where ENTITY_ID >= 1548000000 and ENTITY_TYPE = 'IV'

