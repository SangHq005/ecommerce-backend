-- Create inventory_log table
CREATE TABLE IF NOT EXISTS inventory_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    reference_id VARCHAR(191),
    actor_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invlog_shop ON inventory_log(shop_id);
CREATE INDEX idx_invlog_sku ON inventory_log(sku_id);

-- Add address to seller_shop
ALTER TABLE seller_shop ADD COLUMN address VARCHAR(255);

-- Add tracking_number to orders
ALTER TABLE orders ADD COLUMN tracking_number VARCHAR(64);

-- Add action_reason to product (for rejection notes)
ALTER TABLE product ADD COLUMN action_reason VARCHAR(512);
