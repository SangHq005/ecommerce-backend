-- =====================================================
-- Add sold_count to product table
-- Track actual number of items sold
-- =====================================================

-- Add sold_count column
ALTER TABLE product 
ADD COLUMN sold_count INT NOT NULL DEFAULT 0 COMMENT 'Number of items sold' AFTER review_count;

-- Add index for sorting by sold_count
CREATE INDEX idx_product_sold_count ON product(sold_count DESC);

-- Update sold_count from existing completed orders
UPDATE product p
SET p.sold_count = (
    SELECT COALESCE(SUM(oi.quantity), 0)
    FROM order_item oi
    INNER JOIN orders o ON oi.order_id = o.id
    WHERE oi.product_id = p.id
    AND o.status IN ('PAID', 'PROCESSING', 'READY_TO_SHIP', 'SHIPPED', 'DELIVERED', 'COMPLETED')
);
