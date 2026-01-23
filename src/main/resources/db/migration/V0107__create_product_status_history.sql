-- ============================================================================
-- V0107: Create Product Status History Table
-- Module B: Product Catalog - Status Tracking
-- ============================================================================

CREATE TABLE product_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    changed_by BIGINT COMMENT 'User ID who made the change',
    changed_by_type VARCHAR(20) COMMENT 'SELLER, ADMIN, SYSTEM',
    reason VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_psh_product (product_id, created_at DESC),
    INDEX idx_psh_shop (shop_id, created_at DESC),
    INDEX idx_psh_status (new_status, created_at DESC),
    
    CONSTRAINT fk_psh_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Product status change history for auditing';
