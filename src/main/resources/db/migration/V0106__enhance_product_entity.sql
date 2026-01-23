-- ============================================================================
-- V0106: Enhance Product Entity for Shopee-like Catalog
-- Module B: Product Catalog Enhancement
-- ============================================================================

-- Add new columns to product table
ALTER TABLE product
    ADD COLUMN quality_score INT DEFAULT NULL COMMENT 'Product listing quality score 0-100',
    ADD COLUMN published_at DATETIME DEFAULT NULL COMMENT 'When product was first made ACTIVE',
    ADD COLUMN hidden_at DATETIME DEFAULT NULL COMMENT 'When seller hid the product',
    ADD COLUMN rejected_at DATETIME DEFAULT NULL COMMENT 'When admin rejected',
    ADD COLUMN rejected_by BIGINT DEFAULT NULL COMMENT 'Admin who rejected',
    ADD COLUMN is_featured TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Featured by shop owner',
    ADD COLUMN weight_grams INT DEFAULT NULL COMMENT 'For shipping calculation',
    ADD COLUMN shipping_fee_type VARCHAR(20) DEFAULT 'STANDARD' COMMENT 'STANDARD, FREE, CONDITIONAL_FREE';

-- Index for featured products query
CREATE INDEX idx_product_shop_featured ON product(shop_id, is_featured, status);

-- Index for quality score sorting
CREATE INDEX idx_product_quality_score ON product(quality_score DESC);

-- Index for published products
CREATE INDEX idx_product_published ON product(status, published_at DESC);
