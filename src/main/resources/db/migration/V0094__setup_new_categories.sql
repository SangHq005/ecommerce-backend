-- 1. Deactivate all existing categories first
UPDATE category SET is_active = FALSE;

-- 2. Insert or Update new categories
-- Helper procedure to insert or update
DROP PROCEDURE IF EXISTS EnsureCategory;

DELIMITER //
CREATE PROCEDURE EnsureCategory(IN cat_name VARCHAR(191), IN cat_slug VARCHAR(191), IN sort_order INT)
BEGIN
    DECLARE existing_id BIGINT;
    
    -- Fix: Use COLLATE to ensure same collation for comparison
    SELECT id INTO existing_id FROM category WHERE slug COLLATE utf8mb4_unicode_ci = cat_slug COLLATE utf8mb4_unicode_ci LIMIT 1;
    
    IF existing_id IS NOT NULL THEN
        UPDATE category SET name = cat_name, is_active = TRUE, sort_order = sort_order WHERE id = existing_id;
    ELSE
        INSERT INTO category (name, slug, parent_id, path, is_active, sort_order) 
        VALUES (cat_name, cat_slug, NULL, CONCAT('/', cat_slug), TRUE, sort_order);
    END IF;
END //
DELIMITER ;

-- 3. Execute for each new category
CALL EnsureCategory('Điện thoại & Máy tính bảng', 'dien-thoai-may-tinh-bang', 1);
CALL EnsureCategory('Phụ kiện điện thoại', 'phu-kien-dien-thoai', 2);
CALL EnsureCategory('Máy tính & Laptop', 'may-tinh-laptop', 3);
CALL EnsureCategory('Linh kiện máy tính', 'linh-kien-may-tinh', 4);
CALL EnsureCategory('Thời trang Nam', 'thoi-trang-nam', 5);
CALL EnsureCategory('Thời trang Nữ', 'thoi-trang-nu', 6);
CALL EnsureCategory('Thời trang Trẻ em', 'thoi-trang-tre-em', 7);
CALL EnsureCategory('Mẹ & Bé', 'me-va-be', 8);
CALL EnsureCategory('Đồ gia dụng', 'do-gia-dung', 9);
CALL EnsureCategory('Nội thất', 'noi-that', 10);
CALL EnsureCategory('Trang trí nhà cửa', 'trang-tri-nha-cua', 11);
CALL EnsureCategory('Dụng cụ nhà bếp', 'dung-cu-nha-bep', 12);
CALL EnsureCategory('Chăn ga gối nệm', 'chan-ga-goi-nem', 13);
CALL EnsureCategory('Nước hoa', 'nuoc-hoa', 14);
CALL EnsureCategory('Thực phẩm chức năng', 'thuc-pham-chuc-nang', 15);
CALL EnsureCategory('Dụng cụ thể thao', 'dung-cu-the-thao', 16);
CALL EnsureCategory('Trang phục thể thao', 'trang-phuc-the-thao', 17);
CALL EnsureCategory('Phụ kiện thể thao', 'phu-kien-the-thao', 18);
CALL EnsureCategory('Văn phòng phẩm', 'van-phong-pham', 19);
CALL EnsureCategory('Sách giáo khoa', 'sach-giao-khoa', 20);

-- Cleanup
DROP PROCEDURE EnsureCategory;
