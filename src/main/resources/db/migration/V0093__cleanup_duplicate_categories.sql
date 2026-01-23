-- 1. Migrate products from duplicate categories to main categories

-- Điện tử (Move from 18 -> 1)
UPDATE product SET category_id = 1 WHERE category_id = 18;
UPDATE category SET parent_id = 1 WHERE parent_id = 18;

-- Điện thoại (Move from 21 -> 2)
UPDATE product SET category_id = 2 WHERE category_id = 21;
UPDATE category SET parent_id = 2 WHERE parent_id = 21;

-- Laptop (Move from 22 -> 3)
UPDATE product SET category_id = 3 WHERE category_id = 22;
UPDATE category SET parent_id = 3 WHERE parent_id = 22;

-- Máy tính bảng (Move from 23 -> 5)
UPDATE product SET category_id = 5 WHERE category_id = 23;
UPDATE category SET parent_id = 5 WHERE parent_id = 23;

-- Phụ kiện (Move from 24 -> 15)
UPDATE product SET category_id = 15 WHERE category_id = 24;
UPDATE category SET parent_id = 15 WHERE parent_id = 24;

-- Thời trang chung (Move from 17, 19 -> 4)
UPDATE product SET category_id = 4 WHERE category_id IN (17, 19);
UPDATE category SET parent_id = 4 WHERE parent_id IN (17, 19);

-- Giày dép (Move from 25 -> 10)
UPDATE product SET category_id = 10 WHERE category_id = 25;
UPDATE category SET parent_id = 10 WHERE parent_id = 25;

-- Thời trang nam (Move from 26 -> 8)
UPDATE product SET category_id = 8 WHERE category_id = 26;
UPDATE category SET parent_id = 8 WHERE parent_id = 26;

-- Thời trang nữ (Move from 27 -> 9)
UPDATE product SET category_id = 9 WHERE category_id = 27;
UPDATE category SET parent_id = 9 WHERE parent_id = 27;

-- Nhà cửa (Move from 20 -> 11)
UPDATE product SET category_id = 11 WHERE category_id = 20;
UPDATE category SET parent_id = 11 WHERE parent_id = 20;


-- 2. Delete duplicate categories
DELETE FROM category WHERE id IN (17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27);
