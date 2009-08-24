BEGIN;

# ITEM_VALUE
UPDATE ITEM_VALUE SET START_DATE = CREATED where START_DATE is NULL;

# PERMISSION
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_CLASS, PRINCIPLE_ID, PRINCIPLE_UID, PRINCIPLE_CLASS, ENTRIES, CREATED, MODIFIED, GROUP_ALLOW_VIEW, GROUP_ALLOW_MODIFY, OTHER_ALLOW_VIEW, OTHER_ALLOW_MODIFY)
  VALUES ('DA2C6DA71AA7', 2, 1, 919, 'B10138EEFD0F', 'DataItem', 45, 'DB2C6DA7EAA7', 'User', '{"entries":[{"value":"view"}]}', now(), now(), 0, 0, 0, 0);

COMMIT;
