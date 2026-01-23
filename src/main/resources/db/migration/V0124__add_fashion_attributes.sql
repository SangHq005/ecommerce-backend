-- ============================================================================
-- V0124: Add Fashion/Clothing Attributes
-- 
-- Thêm attribute groups và attributes cho sản phẩm thời trang/quần áo
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- PART 1: CREATE ATTRIBUTE GROUPS FOR FASHION
-- ============================================================================

-- Thêm nhóm thuộc tính cho Thời trang
INSERT INTO attribute_group (name, slug, description, sort_order, is_active, created_at, updated_at)
SELECT 'Thông tin cơ bản', 'thong-tin-co-ban', 'Thông tin cơ bản sản phẩm', 1, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute_group WHERE slug = 'thong-tin-co-ban');

INSERT INTO attribute_group (name, slug, description, sort_order, is_active, created_at, updated_at)
SELECT 'Chất liệu & Bảo quản', 'chat-lieu-bao-quan', 'Thông tin chất liệu và cách bảo quản', 2, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute_group WHERE slug = 'chat-lieu-bao-quan');

-- ============================================================================
-- PART 2: CREATE ATTRIBUTES FOR FASHION
-- ============================================================================

-- Lấy ID của attribute groups vừa tạo
SET @thong_tin_group_id = (SELECT id FROM attribute_group WHERE slug = 'thong-tin-co-ban' LIMIT 1);
SET @chat_lieu_group_id = (SELECT id FROM attribute_group WHERE slug = 'chat-lieu-bao-quan' LIMIT 1);
SET @thiet_ke_group_id = (SELECT id FROM attribute_group WHERE slug = 'thiet-ke' LIMIT 1);

-- Thông tin cơ bản
INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @thong_tin_group_id, 'Màu sắc', 'mau', 'TEXT', NULL, 1, TRUE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'mau');

INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @thong_tin_group_id, 'Kiểu dáng', 'kieu-dang', 'TEXT', NULL, 2, TRUE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'kieu-dang');

INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @thong_tin_group_id, 'Mùa', 'mua', 'TEXT', NULL, 3, TRUE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'mua');

INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @thong_tin_group_id, 'Xuất xứ', 'xuat-xu', 'TEXT', NULL, 4, TRUE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'xuat-xu');

INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @thong_tin_group_id, 'Bảo hành', 'bao-hanh', 'TEXT', NULL, 5, FALSE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'bao-hanh');

INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @thong_tin_group_id, 'Thương hiệu', 'thuong-hieu', 'TEXT', NULL, 6, TRUE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'thuong-hieu');

-- Chất liệu & Bảo quản
INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @chat_lieu_group_id, 'Thành phần', 'thanh-phan', 'TEXT', NULL, 1, TRUE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'thanh-phan');

INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @chat_lieu_group_id, 'Hướng dẫn giặt', 'huong-dan-giat', 'TEXT', NULL, 2, FALSE, TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'huong-dan-giat');

INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable, is_active, created_at, updated_at)
SELECT @chat_lieu_group_id, 'Kiểu đóng gói', 'kieu-dong-goi', 'TEXT', NULL, 3, FALSE, FALSE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE slug = 'kieu-dong-goi');

-- Sử dụng attribute 'Chất liệu' đã có trong nhóm 'Thiết kế'
-- Không cần tạo lại

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF FASHION ATTRIBUTES MIGRATION
-- ============================================================================
