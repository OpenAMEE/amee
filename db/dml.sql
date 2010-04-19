BEGIN;

-- All current users should be Europe/London
UPDATE USER SET TIME_ZONE = 'Europe/London';

-- Fix the Epoch date
UPDATE ITEM_VALUE SET START_DATE = '1970-01-01 00:00:00' WHERE START_DATE = '1970-01-01 01:00:00';

COMMIT;
