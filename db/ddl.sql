BEGIN;

ALTER TABLE ITEM_VALUE_DEFINITION ADD COLUMN FORCE_TIMESERIES BIT(1) NOT NULL DEFAULT 0;

COMMIT;
