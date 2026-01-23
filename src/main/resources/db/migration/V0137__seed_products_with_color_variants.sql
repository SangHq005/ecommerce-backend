-- ============================================================================
-- V0137: Seed Products with Color Variants and SKU Images
-- Add color options and SKUs with image_url for multiple products
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- PART 1: XIAOMI 14 ULTRA - Add Color Variants with Images
-- ============================================================================

-- Add Color option group
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group vog 
    WHERE vog.product_id = p.id AND vog.name = 'Màu sắc'
  );

-- Add color values
INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Đen', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Đen');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Trắng', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Trắng');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Xanh Dương', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Xanh Dương');

-- Create SKUs with image_url for each color
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, image_url, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('XIAOMI-14U-', 
    CASE 
      WHEN v.value = 'Đen' THEN 'BLACK'
      WHEN v.value = 'Trắng' THEN 'WHITE'
      WHEN v.value = 'Xanh Dương' THEN 'BLUE'
      ELSE 'DEFAULT'
    END
  ) AS sku_code,
  29990000 AS price,
  50 AS stock_on_hand,
  0 AS reserved_stock,
  v.value AS option_signature,
  SHA2(v.value, 256) AS option_signature_hash,
  32990000 AS compare_at_price,
  CASE 
    WHEN v.value = 'Đen' THEN 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070925/86269ac3bf4f93b11581c15d653637a9_bi0zjp.jpg'
    WHEN v.value = 'Trắng' THEN 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800&h=800&fit=crop'
    WHEN v.value = 'Xanh Dương' THEN 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&h=800&fit=crop'
    ELSE 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070925/86269ac3bf4f93b11581c15d653637a9_bi0zjp.jpg'
  END AS image_url,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
JOIN product_option_group g ON g.product_id = p.id AND g.name = 'Màu sắc'
JOIN product_option_value v ON v.option_group_id = g.id
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = v.value
  );

-- ============================================================================
-- PART 2: OPPO FIND X7 ULTRA - Add Color Variants with Images
-- ============================================================================

INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'oppo-find-x7-ultra-256gb'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group vog 
    WHERE vog.product_id = p.id AND vog.name = 'Màu sắc'
  );

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Đen', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Đen');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Tím', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Tím');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Vàng', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Vàng');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, image_url, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('OPPO-X7U-', 
    CASE 
      WHEN v.value = 'Đen' THEN 'BLACK'
      WHEN v.value = 'Tím' THEN 'PURPLE'
      WHEN v.value = 'Vàng' THEN 'GOLD'
      ELSE 'DEFAULT'
    END
  ) AS sku_code,
  27990000 AS price,
  50 AS stock_on_hand,
  0 AS reserved_stock,
  v.value AS option_signature,
  SHA2(v.value, 256) AS option_signature_hash,
  29990000 AS compare_at_price,
  CASE 
    WHEN v.value = 'Đen' THEN 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg'
    WHEN v.value = 'Tím' THEN 'https://images.unsplash.com/photo-1601972602237-8b6020c2c7d7?w=800&h=800&fit=crop'
    WHEN v.value = 'Vàng' THEN 'https://images.unsplash.com/photo-1601972602237-8b6020c2c7d7?w=800&h=800&fit=crop&q=80'
    ELSE 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg'
  END AS image_url,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
JOIN product_option_group g ON g.product_id = p.id AND g.name = 'Màu sắc'
JOIN product_option_value v ON v.option_group_id = g.id
WHERE p.slug = 'oppo-find-x7-ultra-256gb'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = v.value
  );

-- ============================================================================
-- PART 3: XIAOMI REDMI NOTE 13 PRO+ - Add Color Variants with Images
-- ============================================================================

INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group vog 
    WHERE vog.product_id = p.id AND vog.name = 'Màu sắc'
  );

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Đen', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Đen');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Xanh Lá', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Xanh Lá');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Tím', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Tím');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, image_url, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('REDMI-N13P+-', 
    CASE 
      WHEN v.value = 'Đen' THEN 'BLACK'
      WHEN v.value = 'Xanh Lá' THEN 'GREEN'
      WHEN v.value = 'Tím' THEN 'PURPLE'
      ELSE 'DEFAULT'
    END
  ) AS sku_code,
  9990000 AS price,
  50 AS stock_on_hand,
  0 AS reserved_stock,
  v.value AS option_signature,
  SHA2(v.value, 256) AS option_signature_hash,
  10990000 AS compare_at_price,
  CASE 
    WHEN v.value = 'Đen' THEN 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg'
    WHEN v.value = 'Xanh Lá' THEN 'https://images.unsplash.com/photo-1601972602237-8b6020c2c7d7?w=800&h=800&fit=crop'
    WHEN v.value = 'Tím' THEN 'https://images.unsplash.com/photo-1601972602237-8b6020c2c7d7?w=800&h=800&fit=crop&q=80'
    ELSE 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg'
  END AS image_url,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
JOIN product_option_group g ON g.product_id = p.id AND g.name = 'Màu sắc'
JOIN product_option_value v ON v.option_group_id = g.id
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = v.value
  );

-- ============================================================================
-- PART 4: SAMSUNG GALAXY A55 5G - Add Color Variants with Images
-- ============================================================================

INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group vog 
    WHERE vog.product_id = p.id AND vog.name = 'Màu sắc'
  );

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Đen', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Đen');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Xanh Dương', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Xanh Dương');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Tím', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Tím');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, image_url, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('SAMSUNG-A55-', 
    CASE 
      WHEN v.value = 'Đen' THEN 'BLACK'
      WHEN v.value = 'Xanh Dương' THEN 'BLUE'
      WHEN v.value = 'Tím' THEN 'PURPLE'
      ELSE 'DEFAULT'
    END
  ) AS sku_code,
  9490000 AS price,
  50 AS stock_on_hand,
  0 AS reserved_stock,
  v.value AS option_signature,
  SHA2(v.value, 256) AS option_signature_hash,
  10490000 AS compare_at_price,
  CASE 
    WHEN v.value = 'Đen' THEN 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg'
    WHEN v.value = 'Xanh Dương' THEN 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&h=800&fit=crop'
    WHEN v.value = 'Tím' THEN 'https://images.unsplash.com/photo-1601972602237-8b6020c2c7d7?w=800&h=800&fit=crop'
    ELSE 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg'
  END AS image_url,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
JOIN product_option_group g ON g.product_id = p.id AND g.name = 'Màu sắc'
JOIN product_option_value v ON v.option_group_id = g.id
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = v.value
  );

-- ============================================================================
-- PART 5: IPHONE 15 - Add Color Variants with Images
-- ============================================================================

INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'iphone-15-128gb-chinh-hang'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group vog 
    WHERE vog.product_id = p.id AND vog.name = 'Màu sắc'
  );

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Hồng', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Hồng');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Xanh Dương', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Xanh Dương');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Vàng', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Vàng');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Xanh Lá', 3, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Xanh Lá');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, compare_at_price, image_url, is_active, created_at, updated_at)
SELECT 
  p.id,
  CONCAT('IPHONE-15-', 
    CASE 
      WHEN v.value = 'Hồng' THEN 'PINK'
      WHEN v.value = 'Xanh Dương' THEN 'BLUE'
      WHEN v.value = 'Vàng' THEN 'YELLOW'
      WHEN v.value = 'Xanh Lá' THEN 'GREEN'
      ELSE 'DEFAULT'
    END
  ) AS sku_code,
  24990000 AS price,
  50 AS stock_on_hand,
  0 AS reserved_stock,
  v.value AS option_signature,
  SHA2(v.value, 256) AS option_signature_hash,
  26990000 AS compare_at_price,
  CASE 
    WHEN v.value = 'Hồng' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
    WHEN v.value = 'Xanh Dương' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
    WHEN v.value = 'Vàng' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
    WHEN v.value = 'Xanh Lá' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
    ELSE 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
  END AS image_url,
  TRUE AS is_active,
  NOW() AS created_at,
  NOW() AS updated_at
FROM product p
JOIN product_option_group g ON g.product_id = p.id AND g.name = 'Màu sắc'
JOIN product_option_value v ON v.option_group_id = g.id
WHERE p.slug = 'iphone-15-128gb-chinh-hang'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku ps 
    WHERE ps.product_id = p.id 
      AND ps.option_signature = v.value
  );

-- ============================================================================
-- PART 6: UPDATE EXISTING IPHONE 15 PRO MAX SKUs WITH IMAGES
-- ============================================================================

-- Update iPhone 15 Pro Max SKUs with color-specific images
UPDATE product_sku ps
JOIN product p ON p.id = ps.product_id
SET ps.image_url = CASE 
  WHEN ps.option_signature LIKE 'Titan Tự Nhiên%' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
  WHEN ps.option_signature LIKE 'Titan Xanh%' THEN 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070795/8ba558e80bb840e0d22ec086ece78410_tbvs83.jpg'
  WHEN ps.option_signature LIKE 'Titan Trắng%' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
  WHEN ps.option_signature LIKE 'Titan Đen%' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&h=800&fit=crop'
  ELSE ps.image_url
END
WHERE (p.slug = 'iphone-15-pro-max-titan' OR p.slug = 'iphone-15-pro-max-256gb-chinh-hang')
  AND ps.image_url IS NULL;

-- ============================================================================
-- PART 7: UPDATE EXISTING SAMSUNG GALAXY S24 ULTRA SKUs WITH IMAGES
-- ============================================================================

UPDATE product_sku ps
JOIN product p ON p.id = ps.product_id
SET ps.image_url = CASE 
  WHEN ps.option_signature = 'Titan Đen' THEN 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070835/2f4543e8b063f32c4346ace156929ceb_crht0l.jpg'
  WHEN ps.option_signature = 'Titan Xám' THEN 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&h=800&fit=crop'
  WHEN ps.option_signature = 'Titan Tím' THEN 'https://images.unsplash.com/photo-1601972602237-8b6020c2c7d7?w=800&h=800&fit=crop'
  WHEN ps.option_signature = 'Titan Vàng' THEN 'https://images.unsplash.com/photo-1601972602237-8b6020c2c7d7?w=800&h=800&fit=crop&q=80'
  ELSE ps.image_url
END
WHERE (p.slug = 'samsung-galaxy-s24-ultra' OR p.slug = 'samsung-galaxy-s24-ultra-256gb')
  AND ps.image_url IS NULL;

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF COLOR VARIANTS AND SKU IMAGES MIGRATION
-- ============================================================================
