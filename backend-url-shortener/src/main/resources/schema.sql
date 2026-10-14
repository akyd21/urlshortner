-- Remove legacy tables that are not part of the current ER design.
SET FOREIGN_KEY_CHECKS = 0;

-- Detach current tables from the removed legacy short_urls table.
SELECT IF(
    COUNT(*) > 0,
    CONCAT('ALTER TABLE `', MAX(table_name), '` DROP FOREIGN KEY `', MAX(constraint_name), '`'),
    'SELECT 1'
)
INTO @drop_old_url_mapping_fk
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND LOWER(table_name) = 'url_mapping'
  AND LOWER(referenced_table_name) = 'short_urls';
PREPARE drop_old_url_mapping_fk FROM @drop_old_url_mapping_fk;
EXECUTE drop_old_url_mapping_fk;
DEALLOCATE PREPARE drop_old_url_mapping_fk;

SELECT IF(
    COUNT(*) > 0,
    CONCAT('ALTER TABLE `', MAX(table_name), '` DROP FOREIGN KEY `', MAX(constraint_name), '`'),
    'SELECT 1'
)
INTO @drop_old_click_event_fk
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND LOWER(table_name) = 'click_event'
  AND LOWER(referenced_table_name) = 'short_urls';
PREPARE drop_old_click_event_fk FROM @drop_old_click_event_fk;
EXECUTE drop_old_click_event_fk;
DEALLOCATE PREPARE drop_old_click_event_fk;

DROP TABLE IF EXISTS short_urls;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- Remove columns left by the previous Java field-name mappings.
SELECT IF(
    COUNT(*) > 0,
    CONCAT('ALTER TABLE `', MAX(table_name), '` DROP COLUMN active'),
    'SELECT 1'
)
INTO @drop_url_mapping_active
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND LOWER(table_name) = 'url_mapping'
  AND LOWER(column_name) = 'active';
PREPARE drop_url_mapping_active FROM @drop_url_mapping_active;
EXECUTE drop_url_mapping_active;
DEALLOCATE PREPARE drop_url_mapping_active;

SELECT IF(
    COUNT(*) > 0,
    CONCAT('ALTER TABLE `', MAX(table_name), '` DROP COLUMN active'),
    'SELECT 1'
)
INTO @drop_custom_alias_active
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND LOWER(table_name) = 'custom_alias'
  AND LOWER(column_name) = 'active';
PREPARE drop_custom_alias_active FROM @drop_custom_alias_active;
EXECUTE drop_custom_alias_active;
DEALLOCATE PREPARE drop_custom_alias_active;

-- Browser User-Agent values can exceed the original VARCHAR(100) column.
SELECT IF(
    COUNT(*) > 0,
    CONCAT('ALTER TABLE `', MAX(table_name), '` MODIFY COLUMN device VARCHAR(512)'),
    'SELECT 1'
)
INTO @resize_click_event_device
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND LOWER(table_name) = 'click_event'
  AND LOWER(column_name) = 'device';
PREPARE resize_click_event_device FROM @resize_click_event_device;
EXECUTE resize_click_event_device;
DEALLOCATE PREPARE resize_click_event_device;
