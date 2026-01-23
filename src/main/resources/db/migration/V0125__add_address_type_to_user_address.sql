-- ============================================================================
-- V0125: Add address_type column to user_address table
-- 
-- Entity UserAddressEntity has address_type field but table is missing this column
-- ============================================================================

-- Add address_type column (check if exists first to avoid error on re-run)
SET @dbname = DATABASE();
SET @tablename = "user_address";
SET @columnname = "address_type";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " VARCHAR(32) NULL AFTER postal_code")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Update existing records to have a default address_type if NULL
UPDATE user_address 
SET address_type = 'HOME' 
WHERE address_type IS NULL;
