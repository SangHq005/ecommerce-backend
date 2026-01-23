-- =============================================================================
-- Migration: Optimize Search Query Indexes
-- Description: Thêm indexes để tối ưu tốc độ search query và category filtering
-- =============================================================================

-- PRODUCT_SKU TABLE - Indexes for price aggregation and stock filtering
-- Index for active SKUs by product (used in LEFT JOIN aggregation)
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product_sku' AND index_name = 'idx_sku_product_active' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_sku_product_active exists"', 'CREATE INDEX idx_sku_product_active ON product_sku(product_id, is_active, stock_on_hand)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index for price range queries on SKUs
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product_sku' AND index_name = 'idx_sku_price_active' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_sku_price_active exists"', 'CREATE INDEX idx_sku_price_active ON product_sku(price, is_active)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- PRODUCT_IMAGE TABLE - Index for thumbnail selection
-- Index for getting first image per product
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product_image' AND index_name = 'idx_image_product_sort' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_image_product_sort exists"', 'CREATE INDEX idx_image_product_sort ON product_image(product_id, sort_order)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- PRODUCT TABLE - Additional composite indexes for common search patterns
-- Index for category + status + price (common filter combination)
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product' AND index_name = 'idx_product_category_status_price' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_product_category_status_price exists"', 'CREATE INDEX idx_product_category_status_price ON product(category_id, status, price)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index for brand + status + price
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product' AND index_name = 'idx_product_brand_status_price' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_product_brand_status_price exists"', 'CREATE INDEX idx_product_brand_status_price ON product(brand_id, status, price)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index for shop + status (for shop product listings)
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product' AND index_name = 'idx_product_shop_status_created' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_product_shop_status_created exists"', 'CREATE INDEX idx_product_shop_status_created ON product(shop_id, status, created_at DESC)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index for category + status + rating (for sorting by rating)
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'product' AND index_name = 'idx_product_category_status_rating' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_product_category_status_rating exists"', 'CREATE INDEX idx_product_category_status_rating ON product(category_id, status, average_rating DESC)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- SELLER_SHOP TABLE - Index for location filtering
-- Index for city filtering (used in search query)
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'seller_shop' AND index_name = 'idx_shop_city_status' AND table_schema = DATABASE());
SET @sql := IF (@exist > 0, 'SELECT "Index idx_shop_city_status exists"', 'CREATE INDEX idx_shop_city_status ON seller_shop(city, status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- ANALYZE TABLES FOR QUERY PLANNER
-- =============================================================================
ANALYZE TABLE product;
ANALYZE TABLE product_sku;
ANALYZE TABLE product_image;
ANALYZE TABLE seller_shop;
