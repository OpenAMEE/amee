BEGIN;

# New Administration APP

INSERT INTO APP (ID, CREATED, MODIFIED, UID, NAME, ALLOW_CLIENT_CACHE, STATUS)
	VALUES	(10, SYSDATE(), SYSDATE(), '463CD8CAD2DB', 'AMEE Definitions', 1, 1);
INSERT INTO SITE_APP (ID, CREATED, MODIFIED, UID, SKIN_PATH, ENVIRONMENT_ID, APP_ID, SITE_ID, STATUS)
	VALUES	(29, SYSDATE(), SYSDATE(), 'C1A1588F99A4', 'definitions.skin', 2, 10, 2, 1);

# New Roles - Definition View
INSERT INTO ROLE (ID, CREATED, MODIFIED, UID, NAME, ENVIRONMENT_ID, STATUS)
	VALUES	(20, SYSDATE(), SYSDATE(), 'C1B1688F99A4', 'definitionView', 2, 1);
INSERT INTO ROLE_ACTION (ROLE_ID, ACTION_ID)
	VALUES	(20, 6),
            (20, 52),
            (20, 1),
            (20, 5),
            (20, 51),
            (20, 47),
            (20, 46),
            (20, 42);

# 6, List Item Definitions
# 52, View Item Definition
# 1, List Item Value Definitions
# 5, View Item Value Definition
# 51, List Value Definitions
# 47, View Value Definition
# 46, List Algorithms
# 42, View Algorithm

# New Roles - Definition Admin
INSERT INTO ROLE (ID, CREATED, MODIFIED, UID, NAME, ENVIRONMENT_ID, STATUS)
	VALUES	(21, SYSDATE(), SYSDATE(), 'C2B1688F99A6', 'definitionAdmin', 2, 1);
  INSERT INTO ROLE_ACTION (ROLE_ID, ACTION_ID)
    VALUES	(21, 53),
            (21, 54),
            (21, 55),
            (21, 4),
            (21, 3),
            (21, 2),
            (21, 48),
            (21, 49),
            (21, 50),
            (21, 43),
            (21, 44),
            (21, 45);

# 53, Create Item Definition
# 54, Modify Item Definition
# 55, Delete Item Definition
# 4, Create Item Value Definition
# 3, Modify Item Value Definition
# 2, Delete Item Value Definition
# 48, Create Value Definition
# 49, Modify Value Definition
# 50, Delete Value Definition
# 43, Create Algorithm
# 44, Modify Algorithm
# 45, Delete Algorithm

# Update STATUS for all tables
UPDATE ACTION SET STATUS = 1;
UPDATE ALGORITHM SET STATUS = 1;
UPDATE API_VERSION SET STATUS = 1;
UPDATE APP SET STATUS = 1;
UPDATE DATA_CATEGORY SET STATUS = 1;
UPDATE ENVIRONMENT SET STATUS = 1;
UPDATE GROUPS SET STATUS = 1;
UPDATE GROUP_USER SET STATUS = 1;
UPDATE ITEM SET STATUS = 1;
UPDATE ITEM_DEFINITION SET STATUS = 1;
UPDATE ITEM_VALUE SET STATUS = 1;
UPDATE ITEM_VALUE_DEFINITION SET STATUS = 1;
UPDATE PERMISSION SET STATUS = 1;
UPDATE PROFILE SET STATUS = 1;
UPDATE ROLE SET STATUS = 1;
UPDATE SITE SET STATUS = 1;
UPDATE SITE_APP SET STATUS = 1;
UPDATE USER SET STATUS = 1;
UPDATE VALUE_DEFINITION SET STATUS = 1;

UPDATE ITEM_VALUE SET START_DATE = (select START_DATE from ITEM where ID = ITEM_VALUE.ITEM_ID) WHERE START_DATE IS NULL;

COMMIT;
