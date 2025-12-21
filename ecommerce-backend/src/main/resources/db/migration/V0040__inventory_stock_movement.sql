CREATE TABLE IF NOT EXISTS stock_movement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku_id BIGINT NOT NULL,
  delta INT NOT NULL,
  reason VARCHAR(64) NOT NULL,
  actor_id BIGINT NULL,
  idem_scope VARCHAR(64) NOT NULL,
  idem_key VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_stock_movement_idem (idem_scope, idem_key),
  KEY idx_stock_movement_sku (sku_id),
  CONSTRAINT fk_stock_movement_sku FOREIGN KEY (sku_id) REFERENCES product_sku(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
