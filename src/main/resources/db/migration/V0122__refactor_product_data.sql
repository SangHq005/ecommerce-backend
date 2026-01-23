-- ============================================================================
-- V0122: Refactor Product Data - Standardize and Fix Product Data
-- 
-- This migration:
-- 1. Handles duplicate products (merge/delete)
-- 2. Ensures all ACTIVE products have at least 1 SKU
-- 3. Seeds options and SKUs for variant products
-- 4. Syncs stock_quantity with SKU totals
-- 5. Cleans up inconsistencies
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- PART 1: HANDLE DUPLICATE PRODUCTS
-- Keep products with variants, merge attributes, deactivate old ones
-- ============================================================================

-- Step 1.1: Merge attributes from old iPhone to new iPhone (if new doesn't have them)
-- Old: iphone-15-pro-max-256gb-chinh-hang
-- New: iphone-15-pro-max-titan
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, value_boolean, created_at, updated_at)
SELECT 
  p_new.id AS product_id,
  pav_old.attribute_id,
  pav_old.display_value,
  pav_old.value_text,
  pav_old.value_number,
  pav_old.value_boolean,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p_old
JOIN product p_new ON p_new.slug = 'iphone-15-pro-max-titan'
JOIN product_attribute_value pav_old ON pav_old.product_id = p_old.id
WHERE p_old.slug = 'iphone-15-pro-max-256gb-chinh-hang'
  AND NOT EXISTS (
    SELECT 1 FROM product_attribute_value pav_new 
    WHERE pav_new.product_id = p_new.id AND pav_new.attribute_id = pav_old.attribute_id
  );

-- Step 1.2: Merge attributes from old Samsung to new Samsung
-- Old: samsung-galaxy-s24-ultra-256gb
-- New: samsung-galaxy-s24-ultra
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, value_boolean, created_at, updated_at)
SELECT 
  p_new.id AS product_id,
  pav_old.attribute_id,
  pav_old.display_value,
  pav_old.value_text,
  pav_old.value_number,
  pav_old.value_boolean,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p_old
JOIN product p_new ON p_new.slug = 'samsung-galaxy-s24-ultra'
JOIN product_attribute_value pav_old ON pav_old.product_id = p_old.id
WHERE p_old.slug = 'samsung-galaxy-s24-ultra-256gb'
  AND NOT EXISTS (
    SELECT 1 FROM product_attribute_value pav_new 
    WHERE pav_new.product_id = p_new.id AND pav_new.attribute_id = pav_old.attribute_id
  );

-- Step 1.3: Deactivate old duplicate products
UPDATE product 
SET status = 'INACTIVE',
    action_reason = 'Merged into variant product',
    updated_at = NOW()
WHERE slug IN ('iphone-15-pro-max-256gb-chinh-hang', 'samsung-galaxy-s24-ultra-256gb')
  AND status = 'ACTIVE';

-- ============================================================================
-- PART 2: CREATE DEFAULT SKUs FOR PRODUCTS WITHOUT SKUs
-- Every ACTIVE product should have at least 1 SKU
-- ============================================================================

-- Create default SKU for products without any SKU
INSERT INTO product_sku (
  product_id, 
  sku_code, 
  price, 
  stock_on_hand, 
  reserved_stock, 
  option_signature, 
  option_signature_hash, 
  compare_at_price, 
  is_active, 
  created_at, 
  updated_at
)
SELECT 
  p.id AS product_id,
  CONCAT('DEFAULT-', p.id, '-', UNIX_TIMESTAMP()) AS sku_code,
  p.price,
  GREATEST(p.stock_quantity, 0) AS stock_on_hand,
  0 AS reserved_stock,
  'default' AS option_signature,
  SHA2('default', 256) AS option_signature_hash,
  p.original_price AS compare_at_price,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
WHERE p.status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps WHERE ps.product_id = p.id
  );

-- ============================================================================
-- PART 3: ENSURE VARIANT PRODUCTS HAVE COMPLETE OPTIONS
-- ============================================================================

-- 3.1: iPhone 15 Pro Max - Ensure Màu sắc group exists
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'iphone-15-pro-max-titan'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group pog 
    WHERE pog.product_id = p.id AND pog.name = 'Màu sắc'
  );

-- 3.2: iPhone - Ensure color values exist
INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Tự Nhiên', 0, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Tự Nhiên');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Xanh', 1, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Xanh');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Trắng', 2, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Trắng');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Đen', 3, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Đen');

-- 3.3: iPhone - Ensure Dung lượng group exists
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Dung lượng', 1, NOW()
FROM product p
WHERE p.slug = 'iphone-15-pro-max-titan'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group pog 
    WHERE pog.product_id = p.id AND pog.name = 'Dung lượng'
  );

-- 3.4: iPhone - Ensure storage values exist
INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '256GB', 0, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND pog.name = 'Dung lượng'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '256GB');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '512GB', 1, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND pog.name = 'Dung lượng'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '512GB');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '1TB', 2, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND pog.name = 'Dung lượng'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '1TB');

-- 3.5: Samsung Galaxy S24 Ultra - Ensure Màu sắc group exists
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'samsung-galaxy-s24-ultra'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group pog 
    WHERE pog.product_id = p.id AND pog.name = 'Màu sắc'
  );

-- 3.6: Samsung - Ensure color values exist
INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Đen', 0, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Đen');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Xám', 1, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Xám');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Tím', 2, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Tím');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Titan Vàng', 3, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Titan Vàng');

-- 3.7: Nike Air Jordan - Ensure Màu sắc group exists
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'nike-air-jordan-1-retro-high'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group pog 
    WHERE pog.product_id = p.id AND pog.name = 'Màu sắc'
  );

-- 3.8: Nike - Ensure color values exist
INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Đỏ Trắng (Chicago)', 0, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Đỏ Trắng (Chicago)');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Đen Trắng', 1, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Đen Trắng');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, 'Xanh Trắng', 2, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = 'Xanh Trắng');

-- 3.9: Nike - Ensure Kích cỡ group exists
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Kích cỡ', 1, NOW()
FROM product p
WHERE p.slug = 'nike-air-jordan-1-retro-high'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group pog 
    WHERE pog.product_id = p.id AND pog.name = 'Kích cỡ'
  );

-- 3.10: Nike - Ensure size values exist
INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '39', 0, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '39');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '40', 1, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '40');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '41', 2, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '41');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '42', 3, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '42');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT pog.id, '43', 4, NOW()
FROM product_option_group pog
JOIN product p ON p.id = pog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND pog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value pov WHERE pov.option_group_id = pog.id AND pov.value = '43');

-- ============================================================================
-- PART 4: CREATE SKUs FOR ALL VARIANT COMBINATIONS
-- ============================================================================

-- 4.1: iPhone 15 Pro Max - Create SKUs for all color × storage combinations (4 × 3 = 12 SKUs)
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, is_active, created_at, updated_at)
SELECT 
  p.id AS product_id,
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
WHERE p.slug = 'iphone-15-pro-max-titan'
  AND g_color.product_id = p.id AND g_color.name = 'Màu sắc'
  AND v_color.option_group_id = g_color.id
  AND g_storage.product_id = p.id AND g_storage.name = 'Dung lượng'
  AND v_storage.option_group_id = g_storage.id
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = CONCAT(v_color.value, '-', v_storage.value)
  );

-- 4.2: Samsung Galaxy S24 Ultra - Create SKUs for all colors (4 SKUs)
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, is_active, created_at, updated_at)
SELECT 
  p.id AS product_id,
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
WHERE p.slug = 'samsung-galaxy-s24-ultra'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = v.value
  );

-- 4.3: Nike Air Jordan - Create SKUs for all color × size combinations (3 × 5 = 15 SKUs)
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, is_active, created_at, updated_at)
SELECT 
  p.id AS product_id,
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

-- ============================================================================
-- PART 5: SYNC stock_quantity WITH SKU TOTALS
-- ============================================================================

-- Update product.stock_quantity = sum of all active SKU stock_on_hand
UPDATE product p
SET p.stock_quantity = (
  SELECT COALESCE(SUM(ps.stock_on_hand), 0)
  FROM product_sku ps
  WHERE ps.product_id = p.id AND ps.is_active = TRUE
),
p.updated_at = NOW()
WHERE p.status = 'ACTIVE'
  AND EXISTS (SELECT 1 FROM product_sku ps WHERE ps.product_id = p.id);

-- ============================================================================
-- PART 6: CLEANUP - Remove duplicate/inactive SKUs
-- ============================================================================

-- Remove duplicate SKUs with same option_signature (keep the first one)
DELETE ps1 FROM product_sku ps1
INNER JOIN product_sku ps2 
WHERE ps1.id > ps2.id
  AND ps1.product_id = ps2.product_id
  AND ps1.option_signature = ps2.option_signature
  AND ps1.option_signature != 'default';

-- Remove default SKUs if product has variant SKUs
DELETE ps FROM product_sku ps
INNER JOIN product_sku ps2 ON ps.product_id = ps2.product_id
WHERE ps.option_signature = 'default'
  AND ps2.option_signature != 'default';

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF REFACTOR MIGRATION
-- ============================================================================
