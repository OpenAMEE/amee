BEGIN;

# ITEM_VALUE
UPDATE ITEM_VALUE SET START_DATE = CREATED where START_DATE is NULL;

# PERMISSION
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_TYPE, PRINCIPLE_ID, PRINCIPLE_UID, PRINCIPLE_TYPE, ENTRIES, CREATED, MODIFIED)
  VALUES ('DA2C6DA71AA7', 2, 1, 14, 'CD310BEBAC52', 'DC', 45, 'DB2C6DA7EAA7', 'USR', '{"entries":[{"value":"view"}]}', now(), now());
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_TYPE, PRINCIPLE_ID, PRINCIPLE_UID, PRINCIPLE_TYPE, ENTRIES, CREATED, MODIFIED)
  VALUES ('DA2C6DA71AA6', 2, 1, 2, '5F5887BCF726', 'ENV', 45, 'DB2C6DA7EAA7', 'USR', '{"entries":[{"value":"view"}]}', now(), now());

COMMIT;
