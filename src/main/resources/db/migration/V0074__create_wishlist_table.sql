CREATE TABLE IF NOT EXISTS wishlist_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(500) NULL,

    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlist_user_product UNIQUE (user_id, product_id),

    INDEX idx_wishlist_user (user_id),
    INDEX idx_wishlist_product (product_id),
    INDEX idx_wishlist_added_at (added_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
