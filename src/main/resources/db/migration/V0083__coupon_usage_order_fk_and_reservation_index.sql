-- Add foreign key from coupon_usage.order_id to orders.id
ALTER TABLE coupon_usage
    ADD CONSTRAINT fk_coupon_usage_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

-- Add index on stock_reservation.sku_id for faster lookups during cleanup
-- Note: MySQL may already have an implicit index from the FK, but explicit index ensures optimal query performance
CREATE INDEX idx_stock_reservation_sku ON stock_reservation(sku_id);
