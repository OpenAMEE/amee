-- Script to increment primary keys in the profile tables. New tables are created, NO EXISTING DATA IS UPDATED.
-- This should be run in part 2 of the live migration process on the master database (db2) in preparation for dumping data from
-- the PROFILE_ITEM tables to then be inserted into the slave (db3).

-- Create the new tables
create table if not exists PROFILE_ITEM_NEW like PROFILE_ITEM;
create table if not exists PROFILE_ITEM_NUMBER_VALUE_NEW like PROFILE_ITEM_NUMBER_VALUE;
create table if not exists PROFILE_ITEM_TEXT_VALUE_NEW like PROFILE_ITEM_TEXT_VALUE;

-- Drop the indexes to make copying the table faster
-- Indexes will be recreated manually after the data has been migrated
alter table PROFILE_ITEM_NEW drop index UID, ITEM_DEFINITION_ID_KEY, DATA_ITEM_ID_KEY, PROFILE_ID_KEY;
alter table PROFILE_ITEM_NUMBER_VALUE_NEW drop index UID, ITEM_VALUE_DEFINITION_ID_KEY, PROFILE_ITEM_ID_KEY;
alter table PROFILE_ITEM_TEXT_VALUE_NEW drop index UID, ITEM_VALUE_DEFINITION_ID_KEY, PROFILE_ITEM_ID_KEY;

-- Copy data
start transaction;
insert into PROFILE_ITEM_NEW (ID, UID, NAME, CREATED, MODIFIED, START_DATE, END_DATE, ITEM_DEFINITION_ID, DATA_ITEM_ID, PROFILE_ID, STATUS, DATA_CATEGORY_ID)
    select ID + 8000000, UID, NAME, CREATED, MODIFIED, START_DATE, END_DATE, ITEM_DEFINITION_ID, DATA_ITEM_ID, PROFILE_ID, STATUS, DATA_CATEGORY_ID
    from PROFILE_ITEM;
insert into PROFILE_ITEM_NUMBER_VALUE_NEW (ID + 50000000, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT)
    select ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID + 8000000, ITEM_VALUE_DEFINITION_ID, UNIT, PER_UNIT
    from PROFILE_ITEM_NUMBER_VALUE;
insert into PROFILE_ITEM_TEXT_VALUE_NEW (ID + 50000000, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID, ITEM_VALUE_DEFINITION_ID)
    select ID, UID, STATUS, VALUE, CREATED, MODIFIED, PROFILE_ITEM_ID + 8000000, ITEM_VALUE_DEFINITION_ID);
commit;

