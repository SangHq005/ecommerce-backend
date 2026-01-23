-- ============================================================================
-- V0108: Enhance Refund Entity for Return Flow
-- Module D: Order Fulfillment - Return/Refund Enhancement
-- ============================================================================

-- Add new columns for return flow
ALTER TABLE refund
    ADD COLUMN refund_type VARCHAR(20) DEFAULT 'REFUND' COMMENT 'REFUND or RETURN',
    ADD COLUMN return_tracking_number VARCHAR(100) DEFAULT NULL COMMENT 'Return shipment tracking',
    ADD COLUMN return_shipping_provider VARCHAR(50) DEFAULT NULL COMMENT 'Return shipping provider';

-- Index for filtering by type
CREATE INDEX idx_refund_type ON refund(shop_id, refund_type, status);
