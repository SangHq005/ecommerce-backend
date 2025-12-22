-- Add expires_at column to stock_reservation table
-- This allows automatic cleanup of expired reservations

ALTER TABLE stock_reservation
ADD COLUMN expires_at TIMESTAMP NULL AFTER status;

-- Add index for efficient cleanup queries
CREATE INDEX idx_stock_reservation_expires
ON stock_reservation(status, expires_at);

-- Update existing records to set expires_at to 15 minutes after creation
UPDATE stock_reservation
SET expires_at = DATE_ADD(created_at, INTERVAL 15 MINUTE)
WHERE expires_at IS NULL AND status = 'RESERVED';
