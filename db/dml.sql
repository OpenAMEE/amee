BEGIN;

# SITE_APP
UPDATE SITE_APP SET SKIN_PATH = 'client-default' WHERE SKIN_PATH = 'default.client.skin';
UPDATE SITE_APP SET SKIN_PATH = 'admin-default' WHERE SKIN_PATH = 'default.admin.skin';
UPDATE SITE_APP SET SKIN_PATH = 'app-admin' WHERE SKIN_PATH = 'app.admin.skin';
UPDATE SITE_APP SET SKIN_PATH = 'cache-admin' WHERE SKIN_PATH = 'cache.skin';
DELETE FROM SITE_APP WHERE APP_ID IN (SELECT ID FROM APP WHERE NAME = 'My Account');
DELETE FROM SITE_APP WHERE APP_ID IN (SELECT ID FROM APP WHERE NAME = 'Skin Administration');
DELETE FROM SITE_APP WHERE SKIN_PATH = 'site.admin.skin';

# ENVIRONMENT 
UPDATE ENVIRONMENT SET ITEMS_PER_FEED = 10;
UPDATE ENVIRONMENT SET OWNER = '';

# APP
DELETE FROM APP WHERE NAME = 'My Account';
DELETE FROM APP WHERE NAME = 'Skin Administration';

# SITE
UPDATE SITE SET SECURE_AVAILABLE = 0;
UPDATE SITE SET NAME = 'AMEE.ADMIN' WHERE NAME = 'admin.live.co2.dgen.net';
UPDATE SITE SET NAME = 'AMEE' WHERE NAME = 'live.co2.dgen.net';
UPDATE SITE SET NAME = 'GOOGLE' WHERE NAME = 'google.co2.dgen.net';
UPDATE SITE SET NAME = 'HEREFORDSHIRE' WHERE NAME = 'herefordshire.co2.dgen.net';
UPDATE SITE SET NAME = 'DEFRA' WHERE NAME = 'stage.defra.co2.dgen.net';

# ITEM
UPDATE ITEM SET START_DATE = CREATED WHERE START_DATE IS NULL AND TYPE='DI';
UPDATE ITEM SET AMOUNT = NULL WHERE TYPE='DI';

# ITEM_VALUE
UPDATE ITEM_VALUE SET START_DATE = (select START_DATE from ITEM where ID = ITEM_VALUE.ITEM_ID);

# ALGORITHM
UPDATE ALGORITHM set TYPE='AL';

# API_VERSION
INSERT INTO API_VERSION (ID, CREATED, MODIFIED, UID, VERSION)
VALUES	('1', SYSDATE(), SYSDATE(), '655B1AD17733', '1.0'),
 		('2', SYSDATE(), SYSDATE(), '4D2BAA6BB1BE', '2.0');

# USER
UPDATE USER set API_VERSION_ID = 1;
UPDATE USER SET STATUS = 0;
UPDATE USER SET USER_TYPE = 0;
UPDATE USER SET USER_TYPE = 3 WHERE USERNAME = 'root';

# New Administration APP
INSERT INTO APP (ID, CREATED, MODIFIED, UID, NAME, ALLOW_CLIENT_CACHE)
	VALUES	(10, SYSDATE(), SYSDATE(), '463CD8CAD2DB', 'AMEE Administration', 1);
INSERT INTO SITE_APP (ID, CREATED, MODIFIED, UID, SKIN_PATH, ENVIRONMENT_ID, APP_ID, SITE_ID)
	VALUES	(29, SYSDATE(), SYSDATE(), 'C1A1588F99A4', 'admin.skin', 2, 10, 2);

# Update STATUS
UPDATE ACTION SET STATUS = 0;
UPDATE ALGORITHM SET STATUS = 0;
UPDATE API_VERSION SET STATUS = 0;
UPDATE APP SET STATUS = 0;
UPDATE DATA_CATEGORY SET STATUS = 0;
UPDATE ENVIRONMENT SET STATUS = 0;
UPDATE GROUPs SET STATUS = 0;
UPDATE GROUP_USER SET STATUS = 0;
UPDATE ITEM SET STATUS = 0;
UPDATE ITEM_DEFINITION SET STATUS = 0;
UPDATE ITEM_VALUE SET STATUS = 0;
UPDATE ITEM_VALUE_DEFINITION SET STATUS = 0;
UPDATE PERMISSION SET STATUS = 0;
UPDATE PROFILE SET STATUS = 0;
UPDATE ROLE SET STATUS = 0;
UPDATE SITE SET STATUS = 0;
UPDATE SITE_APP SET STATUS = 0;
UPDATE USER SET STATUS = 0;
UPDATE VALUE_DEFINITION SET STATUS = 0;

COMMIT;
