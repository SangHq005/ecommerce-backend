-- ============================================================================
-- V0125: Seed Attributes for All Remaining Products
-- 
-- This migration adds product attributes (specs) for all ACTIVE products
-- that don't have attributes yet, organized by category
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- PART 1: SMARTPHONES - Seed attributes for phones without attributes
-- ============================================================================

-- Xiaomi 14 Ultra 512GB
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.73 inch', NULL, 6.73, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '3200 x 1440 pixels', '3200 x 1440 pixels', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'do-phan-giai'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'LTPO AMOLED 2K', 'LTPO AMOLED 2K, 120Hz', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Snapdragon 8 Gen 3', 'Snapdragon 8 Gen 3 (4nm)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '16 GB', 16, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '512 GB', 512, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '50MP Leica Summilux x4', '50MP chính + 50MP ultra-wide + 50MP tele 3.2x + 50MP periscope 5x', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '5300 mAh', 5300, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Android 14, MIUI 15', 'Android 14, MIUI 15', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- OPPO Find X7 Ultra
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.82 inch', NULL, 6.82, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Snapdragon 8 Gen 3', 'Snapdragon 8 Gen 3', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '16 GB', 16, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '50MP Hasselblad x2 + 64MP periscope', '50MP chính LYT-900 + 50MP ultra-wide + 64MP tele 3x + 64MP periscope 6x', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '5400 mAh', 5400, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Xiaomi Redmi Note 13 Pro+
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.67 inch', NULL, 6.67, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'MediaTek Dimensity 7200 Ultra', 'MediaTek Dimensity 7200 Ultra', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '12 GB', 12, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '200MP Samsung HP3', '200MP chính + 8MP ultra-wide + 2MP macro', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '5000 mAh', 5000, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Samsung Galaxy A55 5G
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.6 inch', NULL, 6.6, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Exynos 1480', 'Exynos 1480 (4nm)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '8 GB', 8, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '128 GB', 128, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '50MP OIS + 12MP + 5MP', '50MP chính OIS + 12MP ultra-wide + 5MP macro', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '5000 mAh', 5000, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- iPhone 15 128GB
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.1 inch', NULL, 6.1, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Apple A16 Bionic', 'Apple A16 Bionic (4nm)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '6 GB', 6, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '128 GB', 128, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '48MP + 12MP', '48MP chính + 12MP ultra-wide', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '3349 mAh', 3349, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'iOS 17', 'iOS 17', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- ============================================================================
-- PART 2: LAPTOPS - Seed attributes for laptops without attributes
-- ============================================================================

-- MacBook Pro 14 M3 Pro
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '14.2 inch', NULL, 14.2, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Apple M3 Pro', 'Apple M3 Pro (11 CPU + 14 GPU)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '18 GB', 18, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '512 GB', 512, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'macOS Sonoma', 'macOS Sonoma', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Dell XPS 15 9530
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '15.6 inch', NULL, 15.6, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Intel Core i7-13700H', 'Intel Core i7-13700H (14 nhân, 20 luồng)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '32 GB', 32, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '1 TB', 1024, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'NVIDIA RTX 4060 8GB', 'NVIDIA RTX 4060 8GB GDDR6', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'gpu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Windows 11 Pro', 'Windows 11 Pro', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- ASUS ROG Strix G16
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '16 inch', NULL, 16, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Intel Core i9-13980HX', 'Intel Core i9-13980HX (24 nhân, 32 luồng)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '32 GB', 32, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'NVIDIA RTX 4070 8GB', 'NVIDIA RTX 4070 8GB GDDR6', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'gpu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Windows 11 Home', 'Windows 11 Home', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Lenovo ThinkPad X1 Carbon
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '14 inch', NULL, 14, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Intel Core i7-1365U vPro', 'Intel Core i7-1365U vPro (10 nhân, 12 luồng)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '32 GB', 32, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Windows 11 Pro', 'Windows 11 Pro', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- MacBook Air 15 M3
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '15.3 inch', NULL, 15.3, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Apple M3', 'Apple M3 (8 CPU + 10 GPU)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '8 GB', 8, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'macOS Sonoma', 'macOS Sonoma', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- ============================================================================
-- PART 3: GIÀY THỂ THAO - Seed attributes for shoes
-- ============================================================================

-- Nike Air Force 1 07
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Da thật', 'Da thật', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'nike-air-force-1-07-triple-white' AND a.slug = 'chat-lieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Trắng', 'Trắng', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'nike-air-force-1-07-triple-white' AND a.slug = 'mau'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'nike-air-force-1-07-triple-white' AND a.slug = 'xuat-xu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Adidas Ultraboost 23
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Primeknit+', 'Primeknit+', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'adidas-ultraboost-23-core-black' AND a.slug = 'chat-lieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Đen', 'Đen', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'adidas-ultraboost-23-core-black' AND a.slug = 'mau'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'adidas-ultraboost-23-core-black' AND a.slug = 'xuat-xu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- ============================================================================
-- PART 4: THỜI TRANG - Seed attributes for clothing
-- ============================================================================

-- Áo Polo Uniqlo - Thêm đầy đủ attributes để hiển thị trong CHI TIẾT SẢN PHẨM
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'DRY-EX', 'DRY-EX', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'chat-lieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Navy (Xanh Navy)', 'Navy', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'mau'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'xuat-xu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Thêm các attributes khác cho Áo Polo để hiển thị đầy đủ trong CHI TIẾT SẢN PHẨM
-- Sử dụng attribute 'trong-luong' đã có sẵn
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '200 gram', '200', 200, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'trong-luong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Kiểu dáng (sẽ được tạo trong V0124)
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Polo', 'Polo', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'kieu-dang'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Mùa (sẽ được tạo trong V0124)
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Mùa hè', 'Mùa hè', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'mua'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Thành phần (sẽ được tạo trong V0124)
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '100% Polyester', '100% Polyester', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'thanh-phan'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Hướng dẫn giặt (sẽ được tạo trong V0124)
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Giặt máy 30°C, không tẩy', 'Giặt máy 30°C, không tẩy', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'huong-dan-giat'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Bảo hành (sẽ được tạo trong V0124)
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '12 tháng', '12 tháng', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'bao-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Thương hiệu (sẽ được tạo trong V0124)
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Uniqlo', 'Uniqlo', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND a.slug = 'thuong-hieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- ============================================================================
-- PART 5: PHỤ KIỆN - Seed attributes for accessories
-- ============================================================================

-- Anker PowerCore 10000mAh
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '10000 mAh', '10000', 10000, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'anker-powercore-10000mah-225w' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '22.5W', '22.5W', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'anker-powercore-10000mah-225w' AND a.slug = 'cong-suat-sac'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'USB-C, USB-A', 'USB-C, USB-A', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'anker-powercore-10000mah-225w' AND a.slug = 'cong-ket-noi'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'anker-powercore-10000mah-225w' AND a.slug = 'xuat-xu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Logitech MX Master 3S
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '8000 DPI', '8000', 8000, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'logitech-mx-master-3s-wireless' AND a.slug = 'do-phan-giai'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Không dây (Bluetooth, USB Receiver)', 'Không dây (Bluetooth, USB Receiver)', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'logitech-mx-master-3s-wireless' AND a.slug = 'cong-ket-noi'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '70 ngày', '70', 70, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'logitech-mx-master-3s-wireless' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'logitech-mx-master-3s-wireless' AND a.slug = 'xuat-xu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- ============================================================================
-- PART 6: GENERIC ATTRIBUTES FOR ALL REMAINING PRODUCTS
-- Add basic attributes (màu, chất liệu, xuất xứ) for products without any attributes
-- ============================================================================

-- Add generic attributes for all ACTIVE products that have no attributes at all
-- Sử dụng attribute 'xuat-xu' nếu có (sẽ được tạo trong V0124)
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT DISTINCT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p
CROSS JOIN attribute a
WHERE p.status = 'ACTIVE'
  AND a.slug = 'xuat-xu'
  AND a.is_active = TRUE
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id)
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Thêm bảo hành cho các sản phẩm đã có attributes
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT DISTINCT p.id, a.id, 'Chính hãng', 'Chính hãng', NOW(), NOW()
FROM product p
CROSS JOIN attribute a
WHERE p.status = 'ACTIVE'
  AND a.slug = 'bao-hanh'
  AND a.is_active = TRUE
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id)
  AND EXISTS (SELECT 1 FROM product_attribute_value pav2 WHERE pav2.product_id = p.id);

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF ATTRIBUTES SEEDING MIGRATION
-- ============================================================================
