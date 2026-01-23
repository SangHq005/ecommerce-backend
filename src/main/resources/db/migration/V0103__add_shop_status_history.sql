-- =====================================================
-- Shop Status History Table
-- Track all status changes for seller shops
-- =====================================================

CREATE TABLE IF NOT EXISTS shop_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,
    
    -- Status transition
    from_status VARCHAR(32) NULL COMMENT 'Previous status, NULL for initial creation',
    to_status VARCHAR(32) NOT NULL COMMENT 'New status: DRAFT, PENDING_REVIEW, ACTIVE, SUSPENDED, REJECTED',
    
    -- Actor information
    actor_type VARCHAR(20) NOT NULL COMMENT 'SYSTEM, SELLER, ADMIN',
    actor_id BIGINT NULL COMMENT 'User ID of actor, NULL for SYSTEM',
    
    -- Additional context
    reason VARCHAR(500) NULL COMMENT 'Reason for status change (e.g., rejection/suspension reason)',
    note TEXT NULL COMMENT 'Additional notes',
    metadata JSON NULL COMMENT 'Extra data like verification details',
    
    -- Timestamp
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_shop_history_shop FOREIGN KEY (shop_id) REFERENCES seller_shop(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_shop_history_shop (shop_id),
    INDEX idx_shop_history_status (to_status),
    INDEX idx_shop_history_actor (actor_type, actor_id),
    INDEX idx_shop_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Complete audit trail of shop status changes';
