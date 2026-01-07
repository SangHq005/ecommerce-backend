ALTER TABLE orders ADD COLUMN shop_id BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_order_shop ON orders(shop_id);
