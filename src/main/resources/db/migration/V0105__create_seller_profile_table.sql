-- ============================================================================
-- V0105: Create Seller Profile Table
-- Module A: Seller Account & Onboarding - Seller Verification Flow
-- ============================================================================

CREATE TABLE seller_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- Status
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_VERIFICATION' 
        COMMENT 'PENDING_VERIFICATION, ACTIVE, SUSPENDED, REJECTED',
    seller_type VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL' 
        COMMENT 'INDIVIDUAL or BUSINESS',
    
    -- Identity Information
    full_name VARCHAR(100),
    id_type VARCHAR(30) COMMENT 'CCCD, PASSPORT, BUSINESS_LICENSE',
    id_number VARCHAR(50),
    tax_code VARCHAR(50),
    
    -- Contact Information
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    
    -- Address
    city VARCHAR(100),
    address VARCHAR(255),
    
    -- Timestamps
    submitted_at DATETIME COMMENT 'When profile was submitted for verification',
    verified_at DATETIME COMMENT 'When profile was verified',
    rejected_at DATETIME COMMENT 'When profile was rejected',
    rejected_reason VARCHAR(500),
    verified_by BIGINT COMMENT 'Admin user ID who verified',
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Foreign Key
    CONSTRAINT fk_seller_profile_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_seller_profile_status (status),
    INDEX idx_seller_profile_submitted (submitted_at DESC),
    INDEX idx_seller_profile_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Seller profile for verification - separate from shop';
