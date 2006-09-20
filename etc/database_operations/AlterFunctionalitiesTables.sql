ALTER TABLE `FUNCTIONALITY` ADD COLUMN `UUID` VARCHAR(255) NOT NULL AFTER `ID_INTERNAL`;
ALTER TABLE FUNCTIONALITY ADD UNIQUE (UUID);

ALTER TABLE `FUNCTIONALITY` ADD COLUMN `VISIBLE` BOOLEAN NOT NULL DEFAULT 1;
ALTER TABLE `FUNCTIONALITY` ADD COLUMN `MAXIMIZED` BOOLEAN NOT NULL DEFAULT 0;
ALTER TABLE `FUNCTIONALITY` ADD COLUMN `PRINCIPAL` BOOLEAN NOT NULL DEFAULT 1;
