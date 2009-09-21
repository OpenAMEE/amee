BEGIN;

# Group amee to AMEE Environment
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_TYPE, PRINCIPAL_ID, PRINCIPAL_UID, PRINCIPAL_TYPE, ENTRIES, CREATED, MODIFIED)
  VALUES ('DA2C6DA71BB6', 2, 1, 2, '5F5887BCF726', 'ENV', 5, 'AC65FFA5F9D9', 'GRP', '{"e":[{"v":"v"},{"v":"c.pr"}]}', now(), now());

# Group Administrators to AMEE Environment
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_TYPE, PRINCIPAL_ID, PRINCIPAL_UID, PRINCIPAL_TYPE, ENTRIES, CREATED, MODIFIED)
  VALUES ('DA2C6DA71BB7', 2, 1, 2, '5F5887BCF726', 'ENV', 4, '2E4FE8076E5C', 'GRP', '{"e":[{"v":"v"},{"v":"v", "s":"2"},{"v":"c"},{"v":"m"},{"v":"d"}]}', now(), now());

# Group Administrators to Back Office Environment
INSERT INTO PERMISSION (UID, ENVIRONMENT_ID, STATUS, ENTITY_ID, ENTITY_UID, ENTITY_TYPE, PRINCIPAL_ID, PRINCIPAL_UID, PRINCIPAL_TYPE, ENTRIES, CREATED, MODIFIED)
  VALUES ('DA2C6DA71BB8', 3, 1, 3, '2E54C132ECF2', 'ENV', 2, '2E54C132ECF2', 'GRP', '{"e":[{"v":"v"},{"v":"v", "s":"2"},{"v":"c"},{"v":"m"},{"v":"d"}]}', now(), now());

COMMIT;