-- Create payments table for payment gateway integration
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    amount BIGINT NOT NULL COMMENT 'Amount in smallest currency unit (e.g., cents for USD, dong for VND)',
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    method VARCHAR(50) NOT NULL COMMENT 'VNPAY, MOMO, STRIPE, PAYPAL, COD',
    status VARCHAR(20) NOT NULL COMMENT 'PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED',
    transaction_id VARCHAR(255) NULL COMMENT 'Unique transaction ID from payment gateway',
    gateway VARCHAR(50) NOT NULL COMMENT 'Payment gateway name',
    gateway_response JSON NULL COMMENT 'Full response from payment gateway',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    INDEX idx_order_id (order_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Payment transactions for orders';
