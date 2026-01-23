-- ============================================================================
-- V0102: Seller Voucher System & Order Status History
-- P0 Features for Multi-vendor E-commerce
-- ============================================================================

-- ============================================================================
-- PART 1: SELLER VOUCHER SYSTEM
-- ============================================================================

-- Seller Voucher table - allows sellers to create shop-specific vouchers
CREATE TABLE IF NOT EXISTS seller_voucher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,
    
    -- Voucher identification
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description LONGTEXT NULL,
    
    -- Discount configuration
    discount_type VARCHAR(20) NOT NULL COMMENT 'PERCENTAGE or FIXED_AMOUNT',
    discount_value BIGINT NOT NULL COMMENT 'Percentage (1-100) or fixed amount in VND',
    max_discount_amount BIGINT NULL COMMENT 'Cap for percentage discount',
    min_order_amount BIGINT NULL COMMENT 'Minimum order value to use voucher',
    
    -- Validity period
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    
    -- Usage limits
    usage_limit INT NULL COMMENT 'Total vouchers available, NULL = unlimited',
    usage_count INT NOT NULL DEFAULT 0 COMMENT 'How many times used',
    usage_limit_per_user INT NULL COMMENT 'Per user limit, NULL = unlimited',
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT, ACTIVE, PAUSED, EXPIRED, DELETED',
    
    -- Applicability
    applicable_product_ids JSON NULL COMMENT 'Specific product IDs, NULL = all products in shop',
    applicable_category_ids JSON NULL COMMENT 'Specific category IDs',
    
    -- Metadata
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT fk_seller_voucher_shop FOREIGN KEY (shop_id) REFERENCES seller_shop(id) ON DELETE CASCADE,
    CONSTRAINT uk_seller_voucher_code UNIQUE (shop_id, code),
    
    -- Indexes
    INDEX idx_seller_voucher_shop (shop_id),
    INDEX idx_seller_voucher_status (status),
    INDEX idx_seller_voucher_dates (start_date, end_date),
    INDEX idx_seller_voucher_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Shop-specific vouchers created by sellers';

-- Seller Voucher Usage tracking
CREATE TABLE IF NOT EXISTS seller_voucher_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount BIGINT NOT NULL COMMENT 'Actual discount applied',
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_sv_usage_voucher FOREIGN KEY (voucher_id) REFERENCES seller_voucher(id) ON DELETE CASCADE,
    CONSTRAINT fk_sv_usage_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_sv_usage_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_sv_usage_voucher (voucher_id),
    INDEX idx_sv_usage_user (user_id),
    INDEX idx_sv_usage_order (order_id),
    INDEX idx_sv_usage_date (used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tracks seller voucher usage per user per order';

-- ============================================================================
-- PART 2: ORDER STATUS HISTORY
-- ============================================================================

-- Order Status History - tracks all status transitions
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    
    -- Status transition
    from_status VARCHAR(32) NULL COMMENT 'Previous status, NULL for initial creation',
    to_status VARCHAR(32) NOT NULL COMMENT 'New status',
    
    -- Actor information
    actor_type VARCHAR(20) NOT NULL COMMENT 'SYSTEM, BUYER, SELLER, ADMIN',
    actor_id BIGINT NULL COMMENT 'User ID of actor, NULL for SYSTEM',
    
    -- Additional context
    reason VARCHAR(500) NULL COMMENT 'Reason for status change',
    note TEXT NULL COMMENT 'Additional notes',
    metadata JSON NULL COMMENT 'Extra data like tracking info, payment details',
    
    -- Timestamp
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_order_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_order_history_order (order_id),
    INDEX idx_order_history_status (to_status),
    INDEX idx_order_history_actor (actor_type, actor_id),
    INDEX idx_order_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Complete audit trail of order status changes';

-- ============================================================================
-- PART 3: ADD SELLER VOUCHER FIELDS TO ORDERS
-- ============================================================================

-- Add seller voucher reference to orders table
ALTER TABLE orders 
    ADD COLUMN seller_voucher_id BIGINT NULL COMMENT 'Applied seller voucher',
    ADD COLUMN seller_voucher_discount BIGINT NOT NULL DEFAULT 0 COMMENT 'Discount from seller voucher',
    ADD INDEX idx_order_seller_voucher (seller_voucher_id);

-- Note: We don't add FK constraint here because voucher might be deleted later
-- Business logic will handle validation

-- ============================================================================
-- PART 4: SEED SAMPLE SELLER VOUCHERS (for testing)
-- ============================================================================

-- Will be added after shops exist - handled by application
