-- ============================================================================
-- V0120: Seed SKUs for Product Variants
-- Create SKUs for all variant combinations (color + size/storage)
-- This migration creates SKUs for products that have options defined in V0116
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- Note: This migration works with products that have options created in V0116
-- Make sure V0116 has been executed first to create the option groups and values

-- ============================================================================
-- PART 1: CREATE SKUs FOR IPHONE 15 PRO MAX
-- Combinations: 4 colors × 3 storage = 12 SKUs
-- ============================================================================

-- Helper: Create SKU with option signature
-- Format: "Màu sắc-Dung lượng" (e.g., "Titan Đen-256GB")

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('IPHONE-15PM-', 
    CASE 
      WHEN v_color.value = 'Titan Tự Nhiên' THEN 'TITAN-NAT'
      WHEN v_color.value = 'Titan Xanh' THEN 'TITAN-BLUE'
      WHEN v_color.value = 'Titan Trắng' THEN 'TITAN-WHITE'
      WHEN v_color.value = 'Titan Đen' THEN 'TITAN-BLACK'
      ELSE 'TITAN'
    END, '-', 
    REPLACE(v_storage.value, 'GB', 'GB')
  ) AS sku_code,
  CASE 
    WHEN v_storage.value = '256GB' THEN 34990000
    WHEN v_storage.value = '512GB' THEN 38990000
    WHEN v_storage.value = '1TB' THEN 44990000
    ELSE 34990000
  END AS price,
  50 AS stock_on_hand,
  0 AS reserved_stock,
  CONCAT(v_color.value, '-', v_storage.value) AS option_signature,
  SHA2(CONCAT(v_color.value, '-', v_storage.value), 256) AS option_signature_hash,
  CASE 
    WHEN v_storage.value = '256GB' THEN 36990000
    WHEN v_storage.value = '512GB' THEN 40990000
    WHEN v_storage.value = '1TB' THEN 46990000
    ELSE 36990000
  END AS compare_at_price,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
CROSS JOIN product_option_group g_color
CROSS JOIN product_option_value v_color
CROSS JOIN product_option_group g_storage
CROSS JOIN product_option_value v_storage
WHERE (p.slug = 'iphone-15-pro-max-titan' OR p.slug = 'iphone-15-pro-max-256gb-chinh-hang')
  AND g_color.product_id = p.id AND g_color.name = 'Màu sắc'
  AND v_color.option_group_id = g_color.id
  AND g_storage.product_id = p.id AND g_storage.name = 'Dung lượng'
  AND v_storage.option_group_id = g_storage.id
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = CONCAT(v_color.value, '-', v_storage.value)
  );

-- ============================================================================
-- PART 2: CREATE SKUs FOR SAMSUNG GALAXY S24 ULTRA
-- Only color variants (4 SKUs)
-- ============================================================================

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('S24U-', 
    CASE 
      WHEN v.value = 'Titan Đen' THEN 'BLACK'
      WHEN v.value = 'Titan Xám' THEN 'GRAY'
      WHEN v.value = 'Titan Tím' THEN 'PURPLE'
      WHEN v.value = 'Titan Vàng' THEN 'GOLD'
      ELSE 'DEFAULT'
    END
  ) AS sku_code,
  31990000 AS price,
  50 AS stock_on_hand,
  0 AS reserved_stock,
  v.value AS option_signature,
  SHA2(v.value, 256) AS option_signature_hash,
  33990000 AS compare_at_price,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
JOIN product_option_group g ON g.product_id = p.id AND g.name = 'Màu sắc'
JOIN product_option_value v ON v.option_group_id = g.id
WHERE (p.slug = 'samsung-galaxy-s24-ultra' OR p.slug = 'samsung-galaxy-s24-ultra-256gb')
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = v.value
  );

-- ============================================================================
-- PART 3: CREATE SKUs FOR NIKE AIR JORDAN (SHOES)
-- Combinations: 3 colors × 5 sizes = 15 SKUs
-- ============================================================================

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('JORDAN-1-', 
    CASE 
      WHEN v_color.value LIKE '%Đỏ%' THEN 'CHICAGO'
      WHEN v_color.value LIKE '%Đen%' THEN 'BLACK-WHITE'
      WHEN v_color.value LIKE '%Xanh%' THEN 'BLUE-WHITE'
      ELSE 'DEFAULT'
    END, '-SIZE', v_size.value
  ) AS sku_code,
  3500000 AS price,
  CASE 
    WHEN v_size.value IN ('39', '40', '41') THEN 30
    WHEN v_size.value IN ('42', '43') THEN 20
    ELSE 10
  END AS stock_on_hand,
  0 AS reserved_stock,
  CONCAT(v_color.value, '-', v_size.value) AS option_signature,
  SHA2(CONCAT(v_color.value, '-', v_size.value), 256) AS option_signature_hash,
  3800000 AS compare_at_price,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
CROSS JOIN product_option_group g_color
CROSS JOIN product_option_value v_color
CROSS JOIN product_option_group g_size
CROSS JOIN product_option_value v_size
WHERE p.slug = 'nike-air-jordan-1-retro-high'
  AND g_color.product_id = p.id AND g_color.name = 'Màu sắc'
  AND v_color.option_group_id = g_color.id
  AND g_size.product_id = p.id AND g_size.name = 'Kích cỡ'
  AND v_size.option_group_id = g_size.id
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = CONCAT(v_color.value, '-', v_size.value)
  );

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF SKU SEEDING MIGRATION
-- ============================================================================
