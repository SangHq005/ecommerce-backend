-- =====================================================
-- V0114: Seed Product Specs for Compare Feature Testing
-- =====================================================
-- Seed specs for iPhone 15 Pro and Galaxy S24 (same category: Phones)
-- Includes specs that are SAME (for no highlight) and DIFFERENT (for highlight)
-- =====================================================

-- STEP 1: Map attributes to category "Điện thoại & Máy tính bảng"
-- First get the category ID
SET @phone_category_id = (SELECT id FROM category WHERE slug = 'dien-thoai-may-tinh-bang' LIMIT 1);

-- Fallback to old category name if new one doesn't exist
SET @phone_category_id = IFNULL(@phone_category_id, 
    (SELECT id FROM category WHERE name LIKE '%Phone%' AND is_active = TRUE LIMIT 1));

-- If still null, use category_id from existing products
SET @phone_category_id = IFNULL(@phone_category_id,
    (SELECT category_id FROM product WHERE name = 'iPhone 15 Pro' LIMIT 1));

-- Map all attributes to phone category
INSERT IGNORE INTO category_attribute (category_id, attribute_id, is_required, sort_order)
SELECT @phone_category_id, id, FALSE, sort_order FROM attribute WHERE is_active = TRUE;

-- STEP 2: Get product IDs
SET @iphone_id = (SELECT id FROM product WHERE name = 'iPhone 15 Pro' LIMIT 1);
SET @galaxy_id = (SELECT id FROM product WHERE name = 'Galaxy S24' LIMIT 1);

-- If products don't exist, create them
-- iPhone 15 Pro (if not exists)
INSERT INTO product (shop_id, seller_user_id, category_id, name, slug, description, status, main_image_url, price, original_price, stock_quantity, currency, created_at, updated_at)
SELECT 1, 2, @phone_category_id, 'iPhone 15 Pro', 'iphone-15-pro-compare', 
    'Flagship smartphone từ Apple với chip A17 Pro, camera 48MP, Dynamic Island',
    'ACTIVE', 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=400', 
    28990000, 29990000, 50, 'VND', NOW(), NOW()
WHERE @iphone_id IS NULL;

SET @iphone_id = IFNULL(@iphone_id, LAST_INSERT_ID());

-- Galaxy S24 (if not exists)
INSERT INTO product (shop_id, seller_user_id, category_id, name, slug, description, status, main_image_url, price, original_price, stock_quantity, currency, created_at, updated_at)
SELECT 1, 2, @phone_category_id, 'Galaxy S24', 'galaxy-s24-compare', 
    'Flagship Android từ Samsung với AI Galaxy, camera 50MP, One UI 6',
    'ACTIVE', 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=400', 
    22990000, 24990000, 50, 'VND', NOW(), NOW()
WHERE @galaxy_id IS NULL;

SET @galaxy_id = IFNULL(@galaxy_id, LAST_INSERT_ID());

-- STEP 3: Insert Product Attribute Values for iPhone 15 Pro
-- Delete existing values first to avoid duplicates
DELETE FROM product_attribute_value WHERE product_id = @iphone_id;

-- Màn hình (Group 1)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @iphone_id, id, NULL, 6.1, '6.1 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, '2556 x 1179 pixels', '2556 x 1179 pixels' FROM attribute WHERE slug = 'do-phan-giai';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Super Retina XDR OLED', 'Super Retina XDR OLED' FROM attribute WHERE slug = 'cong-nghe-man-hinh';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 2000, '2000 nits' FROM attribute WHERE slug = 'do-sang-toi-da';

-- Hiệu năng (Group 2)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Apple A17 Pro', 'Apple A17 Pro (3nm)' FROM attribute WHERE slug = 'chip-xu-ly';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 8, '8 GB' FROM attribute WHERE slug = 'ram';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 256, '256 GB' FROM attribute WHERE slug = 'bo-nho-trong';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Apple GPU 6-core', 'Apple GPU 6-core' FROM attribute WHERE slug = 'gpu';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 1650000, '1,650,000 điểm' FROM attribute WHERE slug = 'antutu-score';

-- Camera (Group 3)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, '48MP + 12MP + 12MP', '48MP chính + 12MP ultra-wide + 12MP tele 3x' FROM attribute WHERE slug = 'camera-chinh';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, '12MP TrueDepth', '12MP TrueDepth, Face ID' FROM attribute WHERE slug = 'camera-truoc';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, '4K 60fps, ProRes', '4K@60fps, ProRes, Cinematic mode' FROM attribute WHERE slug = 'quay-video';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Action mode, Smart HDR 5', 'Action mode, Smart HDR 5, Night mode' FROM attribute WHERE slug = 'tinh-nang-camera';

-- Pin & Sạc (Group 4)
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 3274, '3274 mAh' FROM attribute WHERE slug = 'dung-luong-pin';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 27, '27W' FROM attribute WHERE slug = 'cong-suat-sac';

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @iphone_id, id, TRUE, 'Có (MagSafe 15W)' FROM attribute WHERE slug = 'sac-khong-day';

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @iphone_id, id, FALSE, 'Không' FROM attribute WHERE slug = 'sac-nguoc-khong-day';

-- Kết nối (Group 5)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, '5G', '5G Sub-6GHz + mmWave' FROM attribute WHERE slug = 'mang-di-dong';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'eSIM + Nano SIM', '1 eSIM + 1 Nano SIM' FROM attribute WHERE slug = 'sim';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'WiFi 6E', 'WiFi 6E (802.11ax)' FROM attribute WHERE slug = 'wifi';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Bluetooth 5.3', 'Bluetooth 5.3' FROM attribute WHERE slug = 'bluetooth';

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @iphone_id, id, TRUE, 'Có' FROM attribute WHERE slug = 'nfc';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'USB-C', 'USB-C (USB 3)' FROM attribute WHERE slug = 'cong-ket-noi';

-- Thiết kế (Group 6)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, '146.6 x 70.6 x 8.25 mm', '146.6 x 70.6 x 8.25 mm' FROM attribute WHERE slug = 'kich-thuoc';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @iphone_id, id, 187, '187 gram' FROM attribute WHERE slug = 'trong-luong';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Titanium + Ceramic Shield', 'Khung Titanium, mặt kính Ceramic Shield' FROM attribute WHERE slug = 'chat-lieu';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'IP68', 'IP68 (6m trong 30 phút)' FROM attribute WHERE slug = 'chuan-khang-nuoc';

-- Tính năng khác (Group 7)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'iOS 17', 'iOS 17' FROM attribute WHERE slug = 'he-dieu-hanh';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Face ID', 'Face ID' FROM attribute WHERE slug = 'bao-mat';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @iphone_id, id, 'Dynamic Island, Action Button', 'Dynamic Island, Action Button, Emergency SOS' FROM attribute WHERE slug = 'tinh-nang-dac-biet';


-- =====================================================
-- STEP 4: Insert Product Attribute Values for Galaxy S24
-- =====================================================
DELETE FROM product_attribute_value WHERE product_id = @galaxy_id;

-- Màn hình (Group 1) - DIFFERENT values for comparison testing
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, value_number, display_value)
SELECT @galaxy_id, id, NULL, 6.2, '6.2 inch' FROM attribute WHERE slug = 'kich-thuoc-man-hinh';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, '2340 x 1080 pixels', '2340 x 1080 pixels (FHD+)' FROM attribute WHERE slug = 'do-phan-giai';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Dynamic AMOLED 2X', 'Dynamic AMOLED 2X' FROM attribute WHERE slug = 'cong-nghe-man-hinh';

-- SAME value as iPhone for testing
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 120, '120 Hz' FROM attribute WHERE slug = 'tan-so-quet';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 2600, '2600 nits' FROM attribute WHERE slug = 'do-sang-toi-da';

-- Hiệu năng (Group 2)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Snapdragon 8 Gen 3', 'Snapdragon 8 Gen 3 for Galaxy' FROM attribute WHERE slug = 'chip-xu-ly';

-- SAME RAM
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 8, '8 GB' FROM attribute WHERE slug = 'ram';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 128, '128 GB' FROM attribute WHERE slug = 'bo-nho-trong';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Adreno 750', 'Adreno 750' FROM attribute WHERE slug = 'gpu';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 2100000, '2,100,000 điểm' FROM attribute WHERE slug = 'antutu-score';

-- Camera (Group 3)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, '50MP + 12MP + 10MP', '50MP chính + 12MP ultra-wide + 10MP tele 3x' FROM attribute WHERE slug = 'camera-chinh';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, '12MP', '12MP f/2.2' FROM attribute WHERE slug = 'camera-truoc';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, '8K 30fps, 4K 60fps', '8K@30fps, 4K@60fps, Super HDR' FROM attribute WHERE slug = 'quay-video';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'AI Photo Editor, Nightography', 'AI Photo Editor, Nightography, Pro mode' FROM attribute WHERE slug = 'tinh-nang-camera';

-- Pin & Sạc (Group 4) - DIFFERENT values
INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 4000, '4000 mAh' FROM attribute WHERE slug = 'dung-luong-pin';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 25, '25W' FROM attribute WHERE slug = 'cong-suat-sac';

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @galaxy_id, id, TRUE, 'Có (Qi 15W)' FROM attribute WHERE slug = 'sac-khong-day';

-- DIFFERENT: Galaxy has reverse wireless charging
INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @galaxy_id, id, TRUE, 'Có (Wireless PowerShare 4.5W)' FROM attribute WHERE slug = 'sac-nguoc-khong-day';

-- Kết nối (Group 5) - SAME 5G
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, '5G', '5G Sub-6GHz + mmWave' FROM attribute WHERE slug = 'mang-di-dong';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Dual Nano SIM + eSIM', '2 Nano SIM + eSIM' FROM attribute WHERE slug = 'sim';

-- SAME WiFi 6E
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'WiFi 6E', 'WiFi 6E (802.11ax)' FROM attribute WHERE slug = 'wifi';

-- SAME Bluetooth
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Bluetooth 5.3', 'Bluetooth 5.3' FROM attribute WHERE slug = 'bluetooth';

INSERT INTO product_attribute_value (product_id, attribute_id, value_boolean, display_value)
SELECT @galaxy_id, id, TRUE, 'Có' FROM attribute WHERE slug = 'nfc';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'USB-C', 'USB-C (USB 3.2)' FROM attribute WHERE slug = 'cong-ket-noi';

-- Thiết kế (Group 6)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, '147 x 70.6 x 7.6 mm', '147 x 70.6 x 7.6 mm' FROM attribute WHERE slug = 'kich-thuoc';

INSERT INTO product_attribute_value (product_id, attribute_id, value_number, display_value)
SELECT @galaxy_id, id, 167, '167 gram' FROM attribute WHERE slug = 'trong-luong';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Armor Aluminum + Gorilla Glass', 'Khung Armor Aluminum, Gorilla Glass Victus 2' FROM attribute WHERE slug = 'chat-lieu';

-- SAME IP68
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'IP68', 'IP68 (1.5m trong 30 phút)' FROM attribute WHERE slug = 'chuan-khang-nuoc';

-- Tính năng khác (Group 7)
INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Android 14, One UI 6.1', 'Android 14, One UI 6.1' FROM attribute WHERE slug = 'he-dieu-hanh';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Ultrasonic Fingerprint', 'Vân tay siêu âm dưới màn hình' FROM attribute WHERE slug = 'bao-mat';

INSERT INTO product_attribute_value (product_id, attribute_id, value_text, display_value)
SELECT @galaxy_id, id, 'Galaxy AI, Circle to Search', 'Galaxy AI, Circle to Search, Live Translate' FROM attribute WHERE slug = 'tinh-nang-dac-biet';

-- =====================================================
-- SUMMARY: Specs that are SAME (for no highlight):
-- - Tần số quét: 120 Hz
-- - RAM: 8 GB  
-- - WiFi: WiFi 6E
-- - Bluetooth: 5.3
-- - NFC: Có
-- - Chuẩn kháng nước: IP68
--
-- Specs that are DIFFERENT (for highlight):
-- - Màn hình, Độ phân giải, Độ sáng
-- - Chip, Bộ nhớ, GPU, AnTuTu
-- - Camera specs
-- - Pin, Sạc
-- - OS, Bảo mật
-- =====================================================
