BEGIN;

CREATE TABLE PERMISSION_BACKUP select * from PERMISSION;

TRUNCATE TABLE PERMISSION;
ALTER TABLE PERMISSION
  ADD COLUMN ENTITY_ID BIGINT(20) NOT NULL DEFAULT 0,
  ADD COLUMN ENTITY_UID VARCHAR(12) NOT NULL DEFAULT '',
  ADD COLUMN ENTITY_TYPE VARCHAR(5) NOT NULL DEFAULT '',
  ADD COLUMN PRINCIPAL_ID BIGINT(20) NOT NULL DEFAULT 0,
  ADD COLUMN PRINCIPAL_UID VARCHAR(12) NOT NULL DEFAULT '',
  ADD COLUMN PRINCIPAL_TYPE VARCHAR(5) NOT NULL DEFAULT '',
  ADD COLUMN ENTRIES VARCHAR(1000) NOT NULL DEFAULT '',
  DROP COLUMN OBJECT_CLASS,
  DROP COLUMN OBJECT_UID,
  DROP COLUMN GROUP_ALLOW_VIEW,
  DROP COLUMN GROUP_ALLOW_MODIFY,
  DROP COLUMN OTHER_ALLOW_VIEW,
  DROP COLUMN OTHER_ALLOW_MODIFY,
  DROP COLUMN GROUP_ID,
  DROP COLUMN USER_ID;

ALTER TABLE PERMISSION ADD INDEX ENTITY_UID_IND USING BTREE(ENTITY_UID);
ALTER TABLE PERMISSION ADD INDEX ENTITY_TYPE_IND USING BTREE(ENTITY_TYPE);
ALTER TABLE PERMISSION ADD INDEX PRINCIPAL_UID_IND USING BTREE(PRINCIPAL_UID);
ALTER TABLE PERMISSION ADD INDEX PRINCIPAL_TYPE_IND USING BTREE(PRINCIPAL_TYPE);

COMMIT;