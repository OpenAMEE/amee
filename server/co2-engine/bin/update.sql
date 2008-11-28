# Fix Environments & Users
UPDATE amee.ENVIRONMENT SET ITEMS_PER_FEED = 10;
UPDATE amee.ENVIRONMENT SET OWNER = '';
UPDATE amee.USER SET LOCATION = '';
UPDATE amee.USER SET NICK_NAME = '';
UPDATE amee.USER SET USER_TYPE = 0;
UPDATE amee.USER SET USER_TYPE = 3 WHERE USERNAME = 'root';
ALTER TABLE amee.USER DROP COLUMN SUPER_USER;

# Fix Apps, Sites, etc.
UPDATE amee.APP SET TARGET_BUILDER = '';
UPDATE amee.SITE SET SERVER_ADDRESS = '', SERVER_PORT = '', SERVER_SCHEME = '', SECURE_AVAILABLE = 0;
DELETE FROM amee.TARGET WHERE TARGET='skinRenderResource';

# Fix Skins
DROP TABLE amee.SKIN_FILE;
DROP TABLE amee.SKIN_FILE_VERSION;
UPDATE amee.SKIN SET PATH = 'base-import' WHERE ID=1;
UPDATE amee.SKIN SET PATH = 'amee-base-import' WHERE ID=2;
UPDATE amee.SKIN SET PATH = 'admin-import' WHERE ID=3;
UPDATE amee.SKIN SET PATH = 'admin-default' WHERE ID=4;
update amee.SITE_APP set SKIN_PATH = 'admin-default' where SKIN_PATH = 'default.admin.skin';
UPDATE amee.SKIN SET PATH = 'app-admin' WHERE ID=5;
UPDATE amee.SITE_APP set SKIN_PATH = 'app-admin' WHERE SKIN_PATH = 'app.admin.skin';
UPDATE amee.SKIN SET PATH = 'cache-admin' WHERE ID=6;
UPDATE amee.SITE_APP set SKIN_PATH = 'cache-admin' WHERE SKIN_PATH = 'cache.skin';
UPDATE amee.SKIN SET PATH = 'site-admin' WHERE ID=7;
UPDATE amee.SITE_APP set SKIN_PATH = 'site-admin' WHERE SKIN_PATH = 'site.admin.skin';
UPDATE amee.SKIN SET PATH = 'skin-admin' WHERE ID=8;
UPDATE amee.SITE_APP set SKIN_PATH = 'skin-admin' WHERE SKIN_PATH = 'skin.admin.skin';
UPDATE amee.SKIN SET PATH = 'client-import' WHERE ID=10;
UPDATE amee.SITE_APP set SKIN_PATH = 'client-default' WHERE SKIN_PATH = 'default.client.skin';
UPDATE amee.SKIN SET PATH = 'client-default' WHERE ID=11;
UPDATE amee.SITE_APP set SKIN_PATH = 'client-default' WHERE SKIN_PATH = 'default.client.skin';
UPDATE amee.SKIN SET PATH = 'auth' WHERE ID=12;

# Fix AMEE
ALTER TABLE amee.ITEM DROP COLUMN START_DATE;
ALTER TABLE amee.ITEM CHANGE VALID_FROM START_DATE DATETIME;
UPDATE amee.ITEM SET START_DATE = CREATED WHERE START_DATE IS NULL AND TYPE='DI';
UPDATE amee.ITEM SET AMOUNT = AMOUNT_PER_MONTH WHERE TYPE='PI';
UPDATE amee.ITEM SET AMOUNT = null WHERE TYPE='DI';
ALTER TABLE amee.ITEM DROP COLUMN AMOUNT_PER_MONTH;

# AMEE Defs
INSERT INTO amee.UNIT_DEFINITION VALUES CREATED = CURDATE(), MODIFIED = CURDATE(), ENVIRONMENT_ID = 1, NAME='distance', DESCRIPTION='distance', UNITS='km,mi', INTERNAL_UNIT='km';
INSERT INTO amee.UNIT_DEFINITION VALUES CREATED = CURDATE(), MODIFIED = CURDATE(), ENVIRONMENT_ID = 1, NAME='time', DESCRIPTION='time', UNITS='none,month,year', INTERNAL_UNIT='year';