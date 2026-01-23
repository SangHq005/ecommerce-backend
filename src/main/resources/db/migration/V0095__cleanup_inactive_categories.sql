-- 1. Migrate products from Inactive to Active categories

-- Điện thoại & Tablet (2, 5) -> Điện thoại & Máy tính bảng (28)
UPDATE product SET category_id = 28 WHERE category_id IN (2, 5);

-- Máy tính & Laptop (3) -> Máy tính & Laptop (30)
UPDATE product SET category_id = 30 WHERE category_id = 3;

-- Thời trang chung (4) -> Thời trang Nam (32)
UPDATE product SET category_id = 32 WHERE category_id = 4;

-- Headphones (6) -> Phụ kiện điện thoại (29)
UPDATE product SET category_id = 29 WHERE category_id = 6;

-- Smartwatches (7) -> Phụ kiện điện thoại (29)
UPDATE product SET category_id = 29 WHERE category_id = 7;

-- Thời trang Nam (8) -> Thời trang Nam (32)
UPDATE product SET category_id = 32 WHERE category_id = 8;

-- Thời trang Nữ (9) -> Thời trang Nữ (33)
UPDATE product SET category_id = 33 WHERE category_id = 9;

-- Giày dép (10) -> Thời trang Nam (32) (Fallback)
UPDATE product SET category_id = 32 WHERE category_id = 10;

-- Accessories (15) -> Phụ kiện điện thoại (29)
UPDATE product SET category_id = 29 WHERE category_id = 15;

-- Nhà cửa & Đời sống (11) -> Đồ gia dụng (36)
UPDATE product SET category_id = 36 WHERE category_id = 11;

-- Sports & Outdoors (12) -> Dụng cụ thể thao (43)
UPDATE product SET category_id = 43 WHERE category_id = 12;

-- Books & Media (13) -> Sách giáo khoa (47)
UPDATE product SET category_id = 47 WHERE category_id = 13;


-- 2. Update Parent IDs for subcategories (if any) to avoid FK constraint fails
-- Move any children of inactive categories to 'Phụ kiện điện thoại' (29) as a safe fallback or just set NULL
UPDATE category SET parent_id = NULL WHERE parent_id IN (SELECT id FROM (SELECT id FROM category WHERE is_active = FALSE) AS c);

-- 3. Delete Inactive Categories
DELETE FROM category WHERE is_active = FALSE;
