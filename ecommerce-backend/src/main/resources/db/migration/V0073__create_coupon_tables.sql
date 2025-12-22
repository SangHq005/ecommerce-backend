CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    discount_value BIGINT NOT NULL,
    max_discount_amount BIGINT NULL,
    min_order_amount BIGINT NULL,

    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,

    usage_limit INT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    usage_limit_per_user INT NULL,

    auto_apply BOOLEAN NOT NULL DEFAULT FALSE,

    applicable_product_ids JSON NULL,
    applicable_category_ids JSON NULL,
    applicable_user_ids JSON NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_coupon_code UNIQUE (code),
    INDEX idx_coupon_code (code),
    INDEX idx_coupon_status (status),
    INDEX idx_coupon_dates (start_date, end_date),
    INDEX idx_coupon_auto_apply (auto_apply)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS coupon_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount BIGINT NOT NULL,
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_coupon_usage_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(id) ON DELETE CASCADE,
    CONSTRAINT fk_coupon_usage_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,

    INDEX idx_coupon_usage_coupon (coupon_id),
    INDEX idx_coupon_usage_user (user_id),
    INDEX idx_coupon_usage_order (order_id),
    INDEX idx_coupon_usage_used_at (used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
