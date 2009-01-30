BEGIN;

# ITEM_VALUE_DEFINITION
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='currencyGBP', PER_UNIT='month' WHERE PATH='currencyGBPPerMonth')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='currencyUSD', PER_UNIT='month' WHERE PATH='currencyUSDPerMonth')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='distance', UNIT='km', PER_UNIT='month' WHERE PATH='distanceKmPerMonth')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='distance', UNIT='km', PER_UNIT='year' WHERE PATH='distanceKmPerYear')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='journeys', PER_UNIT='year' WHERE PATH='journeysPerYear')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='fuelConsumption', UNIT='km', PER_UNIT='L' WHERE PATH='kmPerLitre')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='fuelConsumptionOwn', UNIT='km', PER_UNIT='L' WHERE PATH='kmPerLitreOwn')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='energyPerTime', UNIT='kW*h', PER_UNIT='month' WHERE PATH='kWhPerMonth')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='energyPerTime', UNIT='kW*h', PER_UNIT='month' WHERE PATH='kWhGeneratedPerMonth')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='readingCurrent', UNIT='kW*h' WHERE PATH='kWhReadingCurrent')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='readingLast', UNIT='kW*h' WHERE PATH='kWhReadingLast')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='massPerTime', UNIT='kg', PER_UNIT='month' WHERE PATH='kgPerMonth')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='volumePerTime', UNIT='L', PER_UNIT='month' WHERE PATH='litresPerMonth')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='usesPerTime', PER_UNIT='day' WHERE PATH='usesPerDay')
UPDATE ITEM_VALUE_DEFINITION SET ALIAS_TO='massPerTime', UNIT='kg', PER_UNIT='month' WHERE PATH='kgPerMonth')

# ENVIRONMENT 
UPDATE amee.ENVIRONMENT SET ITEMS_PER_FEED = 10;
UPDATE amee.ENVIRONMENT SET OWNER = '';

# APP
UPDATE amee.APP SET TARGET_BUILDER = '';

# SITE
UPDATE amee.SITE SET SERVER_ADDRESS = '', SERVER_PORT = '', SERVER_SCHEME = '', SECURE_AVAILABLE = 0;

# ITEM
UPDATE amee.ITEM SET START_DATE = CREATED WHERE START_DATE IS NULL AND TYPE='DI';
UPDATE amee.ITEM SET AMOUNT = AMOUNT_PER_MONTH WHERE TYPE='PI';
UPDATE amee.ITEM SET AMOUNT = null WHERE TYPE='DI';

# ALGORITHM
UPDATE amee.ALGORITHM set TYPE='AL';

# TARGET
DELETE FROM amee.TARGET WHERE TARGET='skinRenderResource';
INSERT INTO amee.TARGET
(ID, UID, NAME, DESCRIPTION, URI_PATTERN, TARGET, DEFAULT_TARGET, DIRECTORY_TARGET, ENABLED, CREATED, MODIFIED, APP_ID, TYPE)
VALUES ('67', 'F613C476EADD', 'Algorithm Contexts Resource', '', '/{environmentUid}/algorithmContexts',
        'algorithmContextsResource', b'00000000', b'00000000', b'00000001', SYSDATE(), SYSDATE(), '4', '0'),
       ('68', '21F188A4937F', 'Algorithm Context Resource', '', '/{environmentUid}/algorithmContexts/{algorithmContentUid}',
        'algorithmContextResource', b'00000000', b'00000000', b'00000001', SYSDATE(), SYSDATE(), '4', '0');

# API_VERSION
INSERT INTO amee.API_VERSION (ID, CREATED, MODIFIED, UID, VERSION)
VALUES	('1', SYSDATE(), SYSDATE(), '655B1AD17733', '1.0'),
 		('2', SYSDATE(), SYSDATE(), '4D2BAA6BB1BE', '2.0');

# USER
UPDATE amee.USER set API_VERSION = 1;
UPDATE amee.USER SET LOCATION = '';
UPDATE amee.USER SET NICK_NAME = '';
UPDATE amee.USER SET STATUS = 0;
UPDATE amee.USER SET USER_TYPE = 0;
UPDATE amee.USER SET USER_TYPE = 3 WHERE USERNAME = 'root';

COMMIT;