-- ============================================================================
-- V0101: Add Order Fulfillment Enhancement Fields
-- Sprint 1: Shopee-like Order Fulfillment Flow
-- ============================================================================

-- Add shipping provider fields
ALTER TABLE orders ADD COLUMN shipping_provider VARCHAR(50) NULL COMMENT 'Shipping carrier: GHN, GHTK, VNPost, JT Express, etc.';
ALTER TABLE orders ADD COLUMN shipping_tracking_url VARCHAR(512) NULL COMMENT 'URL to track shipment on carrier website';

-- Add timestamp fields for order lifecycle
ALTER TABLE orders ADD COLUMN shipped_at DATETIME NULL COMMENT 'Timestamp when order was shipped';
ALTER TABLE orders ADD COLUMN delivered_at DATETIME NULL COMMENT 'Timestamp when order was delivered';
ALTER TABLE orders ADD COLUMN completed_at DATETIME NULL COMMENT 'Timestamp when order was completed';
ALTER TABLE orders ADD COLUMN estimated_delivery_date DATETIME NULL COMMENT 'Estimated delivery date from carrier';

-- Add delivery tracking fields
ALTER TABLE orders ADD COLUMN delivery_attempts INT NOT NULL DEFAULT 0 COMMENT 'Number of delivery attempts';
ALTER TABLE orders ADD COLUMN delivery_failed_reason VARCHAR(255) NULL COMMENT 'Reason for failed delivery';

-- Add buyer confirmation fields
ALTER TABLE orders ADD COLUMN buyer_confirmed BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether buyer confirmed receipt';
ALTER TABLE orders ADD COLUMN buyer_confirmed_at DATETIME NULL COMMENT 'Timestamp when buyer confirmed receipt';

-- Add auto-complete scheduling
ALTER TABLE orders ADD COLUMN auto_complete_at DATETIME NULL COMMENT 'Scheduled auto-complete time (delivered_at + 7 days)';

-- Create index for auto-complete scheduler job
CREATE INDEX idx_orders_auto_complete ON orders(auto_complete_at, status) 
    COMMENT 'Index for auto-complete scheduler to find orders to complete';

-- Create index for shipping provider queries
CREATE INDEX idx_orders_shipping_provider ON orders(shipping_provider, status)
    COMMENT 'Index for filtering orders by shipping provider';

-- Create index for delivered orders needing confirmation
CREATE INDEX idx_orders_pending_confirm ON orders(status, delivered_at, buyer_confirmed)
    COMMENT 'Index for finding delivered orders pending buyer confirmation';
