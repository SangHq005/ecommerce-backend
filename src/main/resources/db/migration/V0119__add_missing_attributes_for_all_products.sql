-- =============================================================================
-- V0119: THÊM ATTRIBUTES CHO TẤT CẢ SẢN PHẨM CÒN THIẾU
-- =============================================================================
-- Mục tiêu: Đảm bảo tất cả sản phẩm đều có đầy đủ attributes để hiển thị
-- =============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- =============================================================================
-- 1. ATTRIBUTES CHO TAI NGHE
-- =============================================================================

-- Apple AirPods Pro 2 - Sử dụng attributes cho thiết bị điện tử
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'True Wireless', 'True Wireless', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-airpods-pro-2-usb-c' AND a.slug = 'chat-lieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Trắng', 'Trắng', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-airpods-pro-2-usb-c' AND a.slug = 'mau'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-airpods-pro-2-usb-c' AND a.slug = 'xuat-xu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Đơn', 'Đơn', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-airpods-pro-2-usb-c' AND a.slug = 'kieu-dong-goi'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Samsung Galaxy Buds3 Pro
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'True Wireless', 'True Wireless', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-buds3-pro' AND a.slug = 'chat-lieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Đen', 'Đen', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-buds3-pro' AND a.slug = 'mau'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Việt Nam', 'Việt Nam', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-buds3-pro' AND a.slug = 'xuat-xu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Đơn', 'Đơn', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-buds3-pro' AND a.slug = 'kieu-dong-goi'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- =============================================================================
-- 2. ATTRIBUTES CHO SMARTWATCH
-- =============================================================================

-- Apple Watch Series 9
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '1.9 inch', '1.9', 1.9, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-watch-series-9-gps-45mm' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Retina LTPO OLED', 'Retina LTPO OLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-watch-series-9-gps-45mm' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'watchOS 10', 'watchOS 10', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-watch-series-9-gps-45mm' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '18 giờ', '18', 18, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-watch-series-9-gps-45mm' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Nhôm', 'Nhôm', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'apple-watch-series-9-gps-45mm' AND a.slug = 'chat-lieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Samsung Galaxy Watch 6 Classic
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '1.5 inch', '1.5', 1.5, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-watch-6-classic-47mm' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Super AMOLED', 'Super AMOLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-watch-6-classic-47mm' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Wear OS 4', 'Wear OS 4', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-watch-6-classic-47mm' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '40 giờ', '40', 40, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-watch-6-classic-47mm' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Thép không gỉ', 'Thép không gỉ', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-watch-6-classic-47mm' AND a.slug = 'chat-lieu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

SET FOREIGN_KEY_CHECKS=1;
