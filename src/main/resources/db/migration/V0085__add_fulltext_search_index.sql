-- Add Fulltext index for Product Search (Idempotent)

SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product' AND index_name = 'idx_product_fulltext_search' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index exists"', 'CREATE FULLTEXT INDEX idx_product_fulltext_search ON product(name, description)');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Analyze table to update statistics
ANALYZE TABLE product;
