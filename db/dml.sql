BEGIN;

# ENVIRONMENT 
UPDATE ENVIRONMENT SET ITEMS_PER_FEED = 10;
UPDATE ENVIRONMENT SET OWNER = '';

# APP
UPDATE APP SET TARGET_BUILDER = '';

# SITE
UPDATE SITE SET SERVER_ADDRESS = '', SERVER_PORT = '', SERVER_SCHEME = '', SECURE_AVAILABLE = 0;

# ITEM
UPDATE ITEM SET START_DATE = CREATED WHERE START_DATE IS NULL AND TYPE='DI';
UPDATE ITEM SET AMOUNT = AMOUNT_PER_MONTH WHERE TYPE='PI';
UPDATE ITEM SET AMOUNT = null WHERE TYPE='DI';

# ALGORITHM
UPDATE ALGORITHM set TYPE='AL';

# TARGET
DELETE FROM TARGET WHERE TARGET='skinRenderResource';
INSERT INTO TARGET
(ID, UID, NAME, DESCRIPTION, URI_PATTERN, TARGET, DEFAULT_TARGET, DIRECTORY_TARGET, ENABLED, CREATED, MODIFIED, APP_ID, TYPE)
VALUES ('67', 'F613C476EADD', 'Algorithm Contexts Resource', '', '/{environmentUid}/algorithmContexts',
        'algorithmContextsResource', b'00000000', b'00000000', b'00000001', SYSDATE(), SYSDATE(), '4', '0'),
       ('68', '21F188A4937F', 'Algorithm Context Resource', '', '/{environmentUid}/algorithmContexts/{algorithmContentUid}',
        'algorithmContextResource', b'00000000', b'00000000', b'00000001', SYSDATE(), SYSDATE(), '4', '0');

# API_VERSION
INSERT INTO API_VERSION (ID, CREATED, MODIFIED, UID, VERSION)
VALUES	('1', SYSDATE(), SYSDATE(), '655B1AD17733', '1.0'),
 		('2', SYSDATE(), SYSDATE(), '4D2BAA6BB1BE', '2.0');

# USER
UPDATE USER set API_VERSION = 1;
UPDATE USER SET LOCATION = '';
UPDATE USER SET NICK_NAME = '';
UPDATE USER SET STATUS = 0;
UPDATE USER SET USER_TYPE = 0;
UPDATE USER SET USER_TYPE = 3 WHERE USERNAME = 'root';

COMMIT;