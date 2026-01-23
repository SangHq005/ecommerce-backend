-- ============================================================================
-- V0116: Add Color Variants to Products
-- Add color options and update SKUs with color information
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- PART 1: ADD COLOR VARIANTS FOR IPHONE 15 PRO MAX
-- ============================================================================

-- Add Color option group for iPhone 15 Pro Max
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'iphone-15-pro-max-titan'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group vog 
    WHERE vog.product_id = p.id AND vog.name = 'Màu sắc'
  );

-- Add color values for iPhone
INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Tự Nhiên', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Tự Nhiên');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Xanh', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Xanh');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Trắng', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Trắng');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Đen', 3, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Đen');

-- Add Storage option group for iPhone
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Dung lượng', 1, NOW()
FROM product p
WHERE p.slug = 'iphone-15-pro-max-titan'
  AND NOT EXISTS (
    SELECT 1 FROM product_option_group vog 
    WHERE vog.product_id = p.id AND vog.name = 'Dung lượng'
  );

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '256GB', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND vog.name = 'Dung lượng'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '256GB');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '512GB', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND vog.name = 'Dung lượng'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '512GB');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '1TB', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'iphone-15-pro-max-titan' AND vog.name = 'Dung lượng'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '1TB');

-- ============================================================================
-- PART 2: ADD COLOR VARIANTS FOR SAMSUNG GALAXY S24 ULTRA
-- ============================================================================

INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'samsung-galaxy-s24-ultra'
  AND NOT EXISTS (SELECT 1 FROM product_option_group vog WHERE vog.product_id = p.id AND vog.name = 'Màu sắc');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Đen', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Đen');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Xám', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Xám');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Tím', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Tím');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Titan Vàng', 3, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'samsung-galaxy-s24-ultra' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Titan Vàng');

-- ============================================================================
-- PART 3: ADD COLOR VARIANTS FOR SHOES
-- ============================================================================

-- Nike Air Jordan colors
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Màu sắc', 0, NOW()
FROM product p
WHERE p.slug = 'nike-air-jordan-1-retro-high'
  AND NOT EXISTS (SELECT 1 FROM product_option_group vog WHERE vog.product_id = p.id AND vog.name = 'Màu sắc');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Đỏ Trắng (Chicago)', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Đỏ Trắng (Chicago)');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Đen Trắng', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Đen Trắng');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, 'Xanh Dương Trắng', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Màu sắc'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = 'Xanh Dương Trắng');

-- Size option for shoes
INSERT INTO product_option_group (product_id, name, sort_order, created_at)
SELECT p.id, 'Kích cỡ', 1, NOW()
FROM product p
WHERE p.slug = 'nike-air-jordan-1-retro-high'
  AND NOT EXISTS (SELECT 1 FROM product_option_group vog WHERE vog.product_id = p.id AND vog.name = 'Kích cỡ');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '39', 0, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '39');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '40', 1, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '40');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '41', 2, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '41');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '42', 3, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '42');

INSERT INTO product_option_value (option_group_id, value, sort_order, created_at)
SELECT vog.id, '43', 4, NOW()
FROM product_option_group vog
JOIN product p ON p.id = vog.product_id
WHERE p.slug = 'nike-air-jordan-1-retro-high' AND vog.name = 'Kích cỡ'
  AND NOT EXISTS (SELECT 1 FROM product_option_value WHERE option_group_id = vog.id AND value = '43');

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF COLOR VARIANTS MIGRATION
-- ============================================================================
