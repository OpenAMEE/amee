-- Adding missing databasechangelog.orderexecuted column
ALTER TABLE `DATABASECHANGELOG` ADD `ORDEREXECUTED` INT;
UPDATE `DATABASECHANGELOG` SET `ORDEREXECUTED` = -1;
ALTER TABLE `DATABASECHANGELOG` MODIFY `ORDEREXECUTED` INT NOT NULL;

-- Modifying size of databasechangelog.md5sum column
ALTER TABLE `DATABASECHANGELOG` MODIFY `MD5SUM` VARCHAR(35);

-- Modifying size of databasechangelog.liquibase column
ALTER TABLE `DATABASECHANGELOG` MODIFY `LIQUIBASE` VARCHAR(20);

-- Adding missing databasechangelog.exectype column
ALTER TABLE `DATABASECHANGELOG` ADD `EXECTYPE` VARCHAR(10);
UPDATE `DATABASECHANGELOG` SET `EXECTYPE` = 'EXECUTED';
ALTER TABLE `DATABASECHANGELOG` MODIFY `EXECTYPE` VARCHAR(10) NOT NULL;

-- DatabaseChangeLog checksums are an incompatible version.  Setting them to null so they will be updated on next database update
UPDATE `DATABASECHANGELOG` SET MD5SUM=null;
