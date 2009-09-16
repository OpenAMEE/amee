BEGIN;

# Group (amee) to Environment
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_TYPE, PRINCIPLE_ID, PRINCIPLE_UID, PRINCIPLE_TYPE, ENTRIES, CREATED, MODIFIED)
  VALUES ('DA2C6DA71BB6', 2, 1, 2, '5F5887BCF726', 'ENV', 5, 'AC65FFA5F9D9', 'GRP', '{"entries":[{"value":"view"},{"value":"create.pr"}]}', now(), now());

# Group (Administrators) to Environment
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_TYPE, PRINCIPLE_ID, PRINCIPLE_UID, PRINCIPLE_TYPE, ENTRIES, CREATED, MODIFIED)
  VALUES ('DA2C6DA71BB7', 2, 1, 2, '5F5887BCF726', 'ENV', 4, '2E4FE8076E5C', 'GRP', '{"entries":[{"value":"view"},{"value":"view", "status":"deprecated"},{"value":"create"},{"value":"modify"},{"value":"delete"}]}', now(), now());

COMMIT;