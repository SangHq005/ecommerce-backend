-- =====================================================
-- V0116: Seed Detailed Product Specs for Premium Phones
-- Following the pattern in V0114
-- =====================================================

-- STEP 1: Get Categories
SET @smartphone_cat_id = (SELECT id FROM category WHERE slug = 'dien-thoai-smartphone' LIMIT 1);
SET @phone_tablet_cat_id = (SELECT id FROM category WHERE slug = 'dien-thoai-may-tinh-bang' LIMIT 1);

-- Map all attributes to these categories if not already mapped
INSERT IGNORE INTO category_attribute (category_id, attribute_id, is_required, sort_order)
SELECT @smartphone_cat_id, id, FALSE, sort_order FROM attribute WHERE is_active = TRUE AND @smartphone_cat_id IS NOT NULL;

INSERT IGNORE INTO category_attribute (category_id, attribute_id, is_required, sort_order)
SELECT @phone_tablet_cat_id, id, FALSE, sort_order FROM attribute WHERE is_active = TRUE AND @phone_tablet_cat_id IS NOT NULL;

-- STEP 2: Seed for iPhone 15 Pro Max 256GB
SET @iphone_pm_id = (SELECT id FROM product WHERE slug = 'iphone-15-pro-max-256gb-chinh-hang' LIMIT 1);

-- Delete existing to refresh
DELETE FROM product_attribute_value WHERE product_id = @iphone_pm_id AND @iphone_pm_id IS NOT NULL;

-- Màn hình
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @iphone_pm_id, id, NULL, 6.7, '6.7 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, '2796 x 1290 pixels', '2796 x 1290 pixels' FROM attribute WHERE slug = 'do-phan-giai' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Super Retina XDR OLED', 'Super Retina XDR OLED, LTPO' FROM attribute WHERE slug = 'cong-nghe-man-hinh' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 2000, '2000 nits' FROM attribute WHERE slug = 'do-sang-toi-da' AND @iphone_pm_id IS NOT NULL;

-- Hiệu năng
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Apple A17 Pro', 'Apple A17 Pro (3nm)' FROM attribute WHERE slug = 'chip-xu-ly' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 8, '8 GB' FROM attribute WHERE slug = 'ram' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 256, '256 GB' FROM attribute WHERE slug = 'bo-nho-trong' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Apple GPU 6-core', 'Apple GPU 6-core' FROM attribute WHERE slug = 'gpu' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 1680000, '1,680,000 điểm' FROM attribute WHERE slug = 'antutu-score' AND @iphone_pm_id IS NOT NULL;

-- Camera
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, '48MP + 12MP + 12MP', '48MP chính + 12MP ultra-wide + 12MP tele 5x' FROM attribute WHERE slug = 'camera-chinh' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, '12MP TrueDepth', '12MP TrueDepth, Face ID' FROM attribute WHERE slug = 'camera-truoc' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, '4K 60fps, ProRes, Log', '4K@60fps, ProRes, Log, Cinematic mode' FROM attribute WHERE slug = 'quay-video' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Action mode, Smart HDR 5, Night mode', 'Action mode, Smart HDR 5, Night mode' FROM attribute WHERE slug = 'tinh-nang-camera' AND @iphone_pm_id IS NOT NULL;

-- Pin & Sạc
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 4441, '4441 mAh' FROM attribute WHERE slug = 'dung-luong-pin' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 27, '27W' FROM attribute WHERE slug = 'cong-suat-sac' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @iphone_pm_id, id, TRUE, 'Có (MagSafe 15W)' FROM attribute WHERE slug = 'sac-khong-day' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @iphone_pm_id, id, FALSE, 'Không' FROM attribute WHERE slug = 'sac-nguoc-khong-day' AND @iphone_pm_id IS NOT NULL;

-- Kết nối
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, '5G', '5G Sub-6GHz + mmWave' FROM attribute WHERE slug = 'mang-di-dong' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'eSIM + Nano SIM', '1 eSIM + 1 Nano SIM' FROM attribute WHERE slug = 'sim' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'WiFi 6E', 'WiFi 6E' FROM attribute WHERE slug = 'wifi' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Bluetooth 5.3', 'Bluetooth 5.3' FROM attribute WHERE slug = 'bluetooth' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @iphone_pm_id, id, TRUE, 'Có' FROM attribute WHERE slug = 'nfc' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'USB-C', 'USB-C (USB 3.2 Gen 2)' FROM attribute WHERE slug = 'cong-ket-noi' AND @iphone_pm_id IS NOT NULL;

-- Thiết kế
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, '159.9 x 76.7 x 8.25 mm', '159.9 x 76.7 x 8.25 mm' FROM attribute WHERE slug = 'kich-thuoc' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_pm_id, id, 221, '221 gram' FROM attribute WHERE slug = 'trong-luong' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Titanium + Ceramic Shield', 'Khung Titanium Grade 5, Ceramic Shield' FROM attribute WHERE slug = 'chat-lieu' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'IP68', 'IP68 (6m trong 30 phút)' FROM attribute WHERE slug = 'chuan-khang-nuoc' AND @iphone_pm_id IS NOT NULL;

-- Tính năng khác
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'iOS 17', 'iOS 17 (Cập nhật lên iOS 18)' FROM attribute WHERE slug = 'he-dieu-hanh' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Face ID', 'Face ID' FROM attribute WHERE slug = 'bao-mat' AND @iphone_pm_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_pm_id, id, 'Dynamic Island, Action Button, Ray Tracing', 'Dynamic Island, Action Button, Ray Tracing phần cứng' FROM attribute WHERE slug = 'tinh-nang-dac-biet' AND @iphone_pm_id IS NOT NULL;


-- STEP 3: Seed for Samsung Galaxy S24 Ultra
SET @galaxy_ultra_id = (SELECT id FROM product WHERE slug = 'samsung-galaxy-s24-ultra-256gb' LIMIT 1);

DELETE FROM product_attribute_value WHERE product_id = @galaxy_ultra_id AND @galaxy_ultra_id IS NOT NULL;

-- Màn hình
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @galaxy_ultra_id, id, NULL, 6.8, '6.8 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, '3120 x 1440 pixels', '3120 x 1440 pixels (QHD+)' FROM attribute WHERE slug = 'do-phan-giai' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Dynamic AMOLED 2X', 'Dynamic AMOLED 2X, LTPO' FROM attribute WHERE slug = 'cong-nghe-man-hinh' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 2600, '2600 nits' FROM attribute WHERE slug = 'do-sang-toi-da' AND @galaxy_ultra_id IS NOT NULL;

-- Hiệu năng
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Snapdragon 8 Gen 3 for Galaxy', 'Snapdragon 8 Gen 3 for Galaxy (4nm)' FROM attribute WHERE slug = 'chip-xu-ly' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 12, '12 GB' FROM attribute WHERE slug = 'ram' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 256, '256 GB' FROM attribute WHERE slug = 'bo-nho-trong' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Adreno 750 (1GHz)', 'Adreno 750 (1GHz)' FROM attribute WHERE slug = 'gpu' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 2150000, '2,150,000 điểm' FROM attribute WHERE slug = 'antutu-score' AND @galaxy_ultra_id IS NOT NULL;

-- Camera
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, '200MP + 50MP + 12MP + 10MP', '200MP chính + 50MP tele 5x + 12MP ultra-wide + 10MP tele 3x' FROM attribute WHERE slug = 'camera-chinh' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, '12MP', '12MP, f/2.2' FROM attribute WHERE slug = 'camera-truoc' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, '8K 30fps, 4K 120fps', '8K@30fps, 4K@120fps, HDR10+' FROM attribute WHERE slug = 'quay-video' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'AI Photo Editor, Nightography, Space Zoom 100x', 'Galaxy AI, Nightography, Space Zoom 100x' FROM attribute WHERE slug = 'tinh-nang-camera' AND @galaxy_ultra_id IS NOT NULL;

-- Pin & Sạc
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 5000, '5000 mAh' FROM attribute WHERE slug = 'dung-luong-pin' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 45, '45W' FROM attribute WHERE slug = 'cong-suat-sac' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @galaxy_ultra_id, id, TRUE, 'Có (Qi 15W)' FROM attribute WHERE slug = 'sac-khong-day' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @galaxy_ultra_id, id, TRUE, 'Có (Wireless PowerShare 4.5W)' FROM attribute WHERE slug = 'sac-nguoc-khong-day' AND @galaxy_ultra_id IS NOT NULL;

-- Kết nối
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, '5G', '5G Sub-6GHz + mmWave' FROM attribute WHERE slug = 'mang-di-dong' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Dual Nano SIM + eSIM', '2 Nano SIM + eSIM' FROM attribute WHERE slug = 'sim' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'WiFi 7', 'WiFi 7' FROM attribute WHERE slug = 'wifi' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Bluetooth 5.3', 'Bluetooth 5.3' FROM attribute WHERE slug = 'bluetooth' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @galaxy_ultra_id, id, TRUE, 'Có' FROM attribute WHERE slug = 'nfc' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'USB-C', 'USB-C (USB 3.2 Gen 1)' FROM attribute WHERE slug = 'cong-ket-noi' AND @galaxy_ultra_id IS NOT NULL;

-- Thiết kế
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, '162.3 x 79 x 8.6 mm', '162.3 x 79 x 8.6 mm' FROM attribute WHERE slug = 'kich-thuoc' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_ultra_id, id, 232, '232 gram' FROM attribute WHERE slug = 'trong-luong' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Titanium + Gorilla Armor', 'Khung Titanium, mặt kính Gorilla Armor' FROM attribute WHERE slug = 'chat-lieu' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'IP68', 'IP68 (1.5m trong 30 phút)' FROM attribute WHERE slug = 'chuan-khang-nuoc' AND @galaxy_ultra_id IS NOT NULL;

-- Tính năng khác
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Android 14, One UI 6.1', 'Android 14, One UI 6.1' FROM attribute WHERE slug = 'he-dieu-hanh' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'In-display Ultrasonic Fingerprint', 'Vân tay siêu âm dưới màn hình' FROM attribute WHERE slug = 'bao-mat' AND @galaxy_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_ultra_id, id, 'Galaxy AI, S-Pen, Circle to Search', 'Galaxy AI, S-Pen tích hợp, Circle to Search' FROM attribute WHERE slug = 'tinh-nang-dac-biet' AND @galaxy_ultra_id IS NOT NULL;


-- STEP 4: Seed for Xiaomi 14 Ultra
SET @xiaomi_ultra_id = (SELECT id FROM product WHERE slug = 'xiaomi-14-ultra-512gb-leica' LIMIT 1);

DELETE FROM product_attribute_value WHERE product_id = @xiaomi_ultra_id AND @xiaomi_ultra_id IS NOT NULL;

-- Màn hình
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @xiaomi_ultra_id, id, NULL, 6.73, '6.73 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, '3200 x 1440 pixels', '3200 x 1440 pixels (2K+)' FROM attribute WHERE slug = 'do-phan-giai' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'LTPO AMOLED', 'LTPO AMOLED, 68 tỷ màu, Dolby Vision' FROM attribute WHERE slug = 'cong-nghe-man-hinh' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 3000, '3000 nits' FROM attribute WHERE slug = 'do-sang-toi-da' AND @xiaomi_ultra_id IS NOT NULL;

-- Hiệu năng
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'Snapdragon 8 Gen 3', 'Snapdragon 8 Gen 3 (4nm)' FROM attribute WHERE slug = 'chip-xu-ly' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 16, '16 GB' FROM attribute WHERE slug = 'ram' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 512, '512 GB' FROM attribute WHERE slug = 'bo-nho-trong' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'Adreno 750', 'Adreno 750' FROM attribute WHERE slug = 'gpu' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 2100000, '2,100,000 điểm' FROM attribute WHERE slug = 'antutu-score' AND @xiaomi_ultra_id IS NOT NULL;

-- Camera
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, '50MP + 50MP + 50MP + 50MP', '50MP chính (1-inch) + 50MP tele + 50MP periscope + 50MP ultra-wide' FROM attribute WHERE slug = 'camera-chinh' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, '32MP', '32MP, f/2.0' FROM attribute WHERE slug = 'camera-truoc' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, '8K 30fps, 4K 120fps', '8K@30fps, 4K@120fps, Leica filters' FROM attribute WHERE slug = 'quay-video' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'Leica Optics, Summilux lens, Variable aperture', 'Leica Summilux lens, Variable aperture f/1.63-f/4.0' FROM attribute WHERE slug = 'tinh-nang-camera' AND @xiaomi_ultra_id IS NOT NULL;

-- Pin & Sạc
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 5000, '5000 mAh' FROM attribute WHERE slug = 'dung-luong-pin' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 90, '90W' FROM attribute WHERE slug = 'cong-suat-sac' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @xiaomi_ultra_id, id, TRUE, 'Có (80W)' FROM attribute WHERE slug = 'sac-khong-day' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @xiaomi_ultra_id, id, TRUE, 'Có (10W)' FROM attribute WHERE slug = 'sac-nguoc-khong-day' AND @xiaomi_ultra_id IS NOT NULL;

-- Kết nối
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, '5G', '5G' FROM attribute WHERE slug = 'mang-di-dong' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'Dual Nano SIM', '2 Nano SIM' FROM attribute WHERE slug = 'sim' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'WiFi 7', 'WiFi 7' FROM attribute WHERE slug = 'wifi' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'Bluetooth 5.4', 'Bluetooth 5.4' FROM attribute WHERE slug = 'bluetooth' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @xiaomi_ultra_id, id, TRUE, 'Có' FROM attribute WHERE slug = 'nfc' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'USB-C', 'USB-C (USB 3.2 Gen 2)' FROM attribute WHERE slug = 'cong-ket-noi' AND @xiaomi_ultra_id IS NOT NULL;

-- Thiết kế
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, '161.4 x 75.3 x 9.2 mm', '161.4 x 75.3 x 9.2 mm' FROM attribute WHERE slug = 'kich-thuoc' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @xiaomi_ultra_id, id, 220, '220 gram' FROM attribute WHERE slug = 'trong-luong' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'Aluminum / Titanium + Vegan Leather', 'Khung Aluminum, mặt lưng da thuần chay' FROM attribute WHERE slug = 'chat-lieu' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'IP68', 'IP68' FROM attribute WHERE slug = 'chuan-khang-nuoc' AND @xiaomi_ultra_id IS NOT NULL;

-- Tính năng khác
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'HyperOS based on Android 14', 'HyperOS (Android 14)' FROM attribute WHERE slug = 'he-dieu-hanh' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'In-display Optical Fingerprint', 'Vân tay quang học dưới màn hình' FROM attribute WHERE slug = 'bao-mat' AND @xiaomi_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @xiaomi_ultra_id, id, 'Leica Imagery, Two-step Shutter', 'Leica Photography Kit support, Two-step Shutter' FROM attribute WHERE slug = 'tinh-nang-dac-biet' AND @xiaomi_ultra_id IS NOT NULL;


-- STEP 5: Seed for OPPO Find X7 Ultra
SET @oppo_ultra_id = (SELECT id FROM product WHERE slug = 'oppo-find-x7-ultra-256gb' LIMIT 1);

DELETE FROM product_attribute_value WHERE product_id = @oppo_ultra_id AND @oppo_ultra_id IS NOT NULL;

-- Màn hình
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @oppo_ultra_id, id, NULL, 6.82, '6.82 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, '3168 x 1440 pixels', '3168 x 1440 pixels (QHD+)' FROM attribute WHERE slug = 'do-phan-giai' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'LTPO AMOLED', 'LTPO AMOLED, 1 tỷ màu, ProXDR' FROM attribute WHERE slug = 'cong-nghe-man-hinh' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 4500, '4500 nits' FROM attribute WHERE slug = 'do-sang-toi-da' AND @oppo_ultra_id IS NOT NULL;

-- Hiệu năng
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'Snapdragon 8 Gen 3', 'Snapdragon 8 Gen 3 (4nm)' FROM attribute WHERE slug = 'chip-xu-ly' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 12, '12 GB' FROM attribute WHERE slug = 'ram' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 256, '256 GB' FROM attribute WHERE slug = 'bo-nho-trong' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'Adreno 750', 'Adreno 750' FROM attribute WHERE slug = 'gpu' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 2120000, '2,120,000 điểm' FROM attribute WHERE slug = 'antutu-score' AND @oppo_ultra_id IS NOT NULL;

-- Camera
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, '50MP + 50MP + 50MP + 50MP', '50MP chính (1-inch) + 50MP periscope 3x + 50MP periscope 6x + 50MP ultra-wide' FROM attribute WHERE slug = 'camera-chinh' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, '32MP', '32MP, f/2.4' FROM attribute WHERE slug = 'camera-truoc' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, '4K 60fps, Dolby Vision', '4K@60fps, 10-bit Dolby Vision HDR' FROM attribute WHERE slug = 'quay-video' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'Hasselblad Color Calibration, Dual Periscope', 'Hasselblad Color Master, Dual Periscope zoom' FROM attribute WHERE slug = 'tinh-nang-camera' AND @oppo_ultra_id IS NOT NULL;

-- Pin & Sạc
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 5000, '5000 mAh' FROM attribute WHERE slug = 'dung-luong-pin' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 100, '100W' FROM attribute WHERE slug = 'cong-suat-sac' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @oppo_ultra_id, id, TRUE, 'Có (50W)' FROM attribute WHERE slug = 'sac-khong-day' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @oppo_ultra_id, id, TRUE, 'Có (10W)' FROM attribute WHERE slug = 'sac-nguoc-khong-day' AND @oppo_ultra_id IS NOT NULL;

-- Kết nối
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, '5G', '5G' FROM attribute WHERE slug = 'mang-di-dong' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'Dual Nano SIM', '2 Nano SIM' FROM attribute WHERE slug = 'sim' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'WiFi 7', 'WiFi 7' FROM attribute WHERE slug = 'wifi' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'Bluetooth 5.4', 'Bluetooth 5.4' FROM attribute WHERE slug = 'bluetooth' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @oppo_ultra_id, id, TRUE, 'Có' FROM attribute WHERE slug = 'nfc' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'USB-C', 'USB-C (USB 3.2)' FROM attribute WHERE slug = 'cong-ket-noi' AND @oppo_ultra_id IS NOT NULL;

-- Thiết kế
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, '164.3 x 76.2 x 9.5 mm', '164.3 x 76.2 x 9.5 mm' FROM attribute WHERE slug = 'kich-thuoc' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @oppo_ultra_id, id, 221, '221 gram' FROM attribute WHERE slug = 'trong-luong' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'Glass / Leather + Aluminum', 'Mặt lưng da + kính, khung nhôm' FROM attribute WHERE slug = 'chat-lieu' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'IP68', 'IP68' FROM attribute WHERE slug = 'chuan-khang-nuoc' AND @oppo_ultra_id IS NOT NULL;

-- Tính năng khác
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'ColorOS 14 (Android 14)', 'ColorOS 14 (Android 14)' FROM attribute WHERE slug = 'he-dieu-hanh' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'In-display Fingerprint', 'Vân tay dưới màn hình' FROM attribute WHERE slug = 'bao-mat' AND @oppo_ultra_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @oppo_ultra_id, id, 'Hasselblad Master Mode, Satellite Connectivity', 'Hasselblad Master Mode, Kết nối vệ tinh (tùy phiên bản)' FROM attribute WHERE slug = 'tinh-nang-dac-biet' AND @oppo_ultra_id IS NOT NULL;


-- STEP 6: Seed for Xiaomi Redmi Note 13 Pro+ 5G
SET @redmi_note_id = (SELECT id FROM product WHERE slug = 'xiaomi-redmi-note-13-pro-plus-5g' LIMIT 1);

DELETE FROM product_attribute_value WHERE product_id = @redmi_note_id AND @redmi_note_id IS NOT NULL;

-- Màn hình
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @redmi_note_id, id, NULL, 6.67, '6.67 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh' AND @redmi_note_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @redmi_note_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet' AND @redmi_note_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @redmi_note_id, id, 'AMOLED', 'CrystalRes AMOLED, 68 tỷ màu' FROM attribute WHERE slug = 'cong-nghe-man-hinh' AND @redmi_note_id IS NOT NULL;

-- Hiệu năng
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @redmi_note_id, id, 'Dimensity 7200 Ultra', 'MediaTek Dimensity 7200 Ultra (4nm)' FROM attribute WHERE slug = 'chip-xu-ly' AND @redmi_note_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @redmi_note_id, id, 8, '8 GB' FROM attribute WHERE slug = 'ram' AND @redmi_note_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @redmi_note_id, id, 256, '256 GB' FROM attribute WHERE slug = 'bo-nho-trong' AND @redmi_note_id IS NOT NULL;

-- Camera
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @redmi_note_id, id, '200MP + 8MP + 2MP', '200MP chính (OIS) + 8MP ultra-wide + 2MP macro' FROM attribute WHERE slug = 'camera-chinh' AND @redmi_note_id IS NOT NULL;

-- Pin & Sạc
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @redmi_note_id, id, 5000, '5000 mAh' FROM attribute WHERE slug = 'dung-luong-pin' AND @redmi_note_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @redmi_note_id, id, 120, '120W HyperCharge' FROM attribute WHERE slug = 'cong-suat-sac' AND @redmi_note_id IS NOT NULL;

-- Thiết kế & Khác
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @redmi_note_id, id, 'IP68', 'IP68' FROM attribute WHERE slug = 'chuan-khang-nuoc' AND @redmi_note_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @redmi_note_id, id, 'Android 13', 'MIUI 14 (Android 13)' FROM attribute WHERE slug = 'he-dieu-hanh' AND @redmi_note_id IS NOT NULL;


-- STEP 7: Seed for Samsung Galaxy A55 5G
SET @galaxy_a55_id = (SELECT id FROM product WHERE slug = 'samsung-galaxy-a55-5g-128gb' LIMIT 1);

DELETE FROM product_attribute_value WHERE product_id = @galaxy_a55_id AND @galaxy_a55_id IS NOT NULL;

-- Màn hình
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @galaxy_a55_id, id, NULL, 6.6, '6.6 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh' AND @galaxy_a55_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_a55_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet' AND @galaxy_a55_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_a55_id, id, 'Super AMOLED', 'Super AMOLED, HDR10+' FROM attribute WHERE slug = 'cong-nghe-man-hinh' AND @galaxy_a55_id IS NOT NULL;

-- Hiệu năng
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_a55_id, id, 'Exynos 1480', 'Samsung Exynos 1480 (4nm)' FROM attribute WHERE slug = 'chip-xu-ly' AND @galaxy_a55_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_a55_id, id, 8, '8 GB' FROM attribute WHERE slug = 'ram' AND @galaxy_a55_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_a55_id, id, 128, '128 GB' FROM attribute WHERE slug = 'bo-nho-trong' AND @galaxy_a55_id IS NOT NULL;

-- Camera
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_a55_id, id, '50MP + 12MP + 5MP', '50MP chính (OIS) + 12MP ultra-wide + 5MP macro' FROM attribute WHERE slug = 'camera-chinh' AND @galaxy_a55_id IS NOT NULL;

-- Pin & Sạc
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_a55_id, id, 5000, '5000 mAh' FROM attribute WHERE slug = 'dung-luong-pin' AND @galaxy_a55_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_a55_id, id, 25, '25W' FROM attribute WHERE slug = 'cong-suat-sac' AND @galaxy_a55_id IS NOT NULL;

-- Thiết kế & Khác
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_a55_id, id, 'IP67', 'IP67' FROM attribute WHERE slug = 'chuan-khang-nuoc' AND @galaxy_a55_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_a55_id, id, 'Android 14', 'One UI 6.1 (Android 14)' FROM attribute WHERE slug = 'he-dieu-hanh' AND @galaxy_a55_id IS NOT NULL;


-- STEP 8: Seed for iPhone 15 128GB
SET @iphone_15_id = (SELECT id FROM product WHERE slug = 'iphone-15-128gb-chinh-hang' LIMIT 1);

DELETE FROM product_attribute_value WHERE product_id = @iphone_15_id AND @iphone_15_id IS NOT NULL;

-- Màn hình
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @iphone_15_id, id, NULL, 6.1, '6.1 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh' AND @iphone_15_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_15_id, id, 60, '60 Hz' FROM attribute WHERE slug = 'tan-so-quet' AND @iphone_15_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_15_id, id, 'Super Retina XDR OLED', 'Super Retina XDR OLED' FROM attribute WHERE slug = 'cong-nghe-man-hinh' AND @iphone_15_id IS NOT NULL;

-- Hiệu năng
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_15_id, id, 'Apple A16 Bionic', 'Apple A16 Bionic (4nm)' FROM attribute WHERE slug = 'chip-xu-ly' AND @iphone_15_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_15_id, id, 6, '6 GB' FROM attribute WHERE slug = 'ram' AND @iphone_15_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_15_id, id, 128, '128 GB' FROM attribute WHERE slug = 'bo-nho-trong' AND @iphone_15_id IS NOT NULL;

-- Camera
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_15_id, id, '48MP + 12MP', '48MP chính + 12MP ultra-wide' FROM attribute WHERE slug = 'camera-chinh' AND @iphone_15_id IS NOT NULL;

-- Pin & Sạc
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_15_id, id, 3349, '3349 mAh' FROM attribute WHERE slug = 'dung-luong-pin' AND @iphone_15_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_15_id, id, 20, '20W' FROM attribute WHERE slug = 'cong-suat-sac' AND @iphone_15_id IS NOT NULL;

-- Thiết kế & Khác
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_15_id, id, 'IP68', 'IP68' FROM attribute WHERE slug = 'chuan-khang-nuoc' AND @iphone_15_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_15_id, id, 'iOS 17', 'iOS 17' FROM attribute WHERE slug = 'he-dieu-hanh' AND @iphone_15_id IS NOT NULL;

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_15_id, id, 'Dynamic Island', 'Dynamic Island' FROM attribute WHERE slug = 'tinh-nang-dac-biet' AND @iphone_15_id IS NOT NULL;
