-- =====================================================
-- V0113: Create Product Attribute Tables for Comparison
-- =====================================================
-- Design: Attribute-based (EAV pattern) for flexible specs
-- 
-- Tables:
-- 1. attribute_group: Nhóm thuộc tính (Màn hình, Hiệu năng, Pin...)
-- 2. attribute: Định nghĩa thuộc tính (RAM, CPU, Màn hình...)
-- 3. product_attribute_value: Giá trị thuộc tính của sản phẩm
-- =====================================================

-- Table 1: Attribute Groups (Nhóm thuộc tính)
-- Dùng để gom các specs theo nhóm khi hiển thị so sánh
CREATE TABLE attribute_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Tên nhóm: Màn hình, Hiệu năng...',
    slug VARCHAR(100) NOT NULL UNIQUE COMMENT 'URL-friendly name',
    description VARCHAR(500) NULL COMMENT 'Mô tả nhóm',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_attribute_group_sort (sort_order),
    INDEX idx_attribute_group_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Nhóm thuộc tính sản phẩm cho so sánh';

-- Table 2: Attributes (Định nghĩa thuộc tính)
-- Thuộc tính có thể thuộc về 1 hoặc nhiều category
CREATE TABLE attribute (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attribute_group_id BIGINT NOT NULL COMMENT 'FK to attribute_group',
    name VARCHAR(100) NOT NULL COMMENT 'Tên: RAM, CPU, Màn hình...',
    slug VARCHAR(100) NOT NULL UNIQUE COMMENT 'URL-friendly name',
    data_type ENUM('TEXT', 'NUMBER', 'BOOLEAN', 'ENUM') NOT NULL DEFAULT 'TEXT' 
        COMMENT 'Loại dữ liệu để compare/normalize',
    unit VARCHAR(50) NULL COMMENT 'Đơn vị: GB, inch, mAh...',
    description VARCHAR(500) NULL,
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Thứ tự trong group',
    is_filterable BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Có thể filter không',
    is_comparable BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Hiển thị khi so sánh',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_attribute_group (attribute_group_id),
    INDEX idx_attribute_sort (sort_order),
    INDEX idx_attribute_filterable (is_filterable),
    INDEX idx_attribute_comparable (is_comparable),
    
    CONSTRAINT fk_attribute_group FOREIGN KEY (attribute_group_id) 
        REFERENCES attribute_group(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Định nghĩa thuộc tính sản phẩm';

-- Table 3: Category-Attribute mapping (thuộc tính nào cho category nào)
-- Cho phép define attributes applicable cho từng category
CREATE TABLE category_attribute (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    attribute_id BIGINT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Bắt buộc nhập không',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Thứ tự trong category',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_category_attribute (category_id, attribute_id),
    INDEX idx_category_attribute_cat (category_id),
    INDEX idx_category_attribute_attr (attribute_id),
    
    CONSTRAINT fk_cat_attr_category FOREIGN KEY (category_id) 
        REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT fk_cat_attr_attribute FOREIGN KEY (attribute_id) 
        REFERENCES attribute(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Mapping thuộc tính theo category';

-- Table 4: Product Attribute Values (Giá trị specs của sản phẩm)
CREATE TABLE product_attribute_value (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    attribute_id BIGINT NOT NULL,
    value_text VARCHAR(1000) NULL COMMENT 'Giá trị dạng text',
    value_number DECIMAL(15,4) NULL COMMENT 'Giá trị số (để so sánh)',
    value_boolean BOOLEAN NULL COMMENT 'Giá trị boolean',
    display_value VARCHAR(500) NOT NULL COMMENT 'Giá trị hiển thị (có unit)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_product_attribute (product_id, attribute_id),
    INDEX idx_pav_product (product_id),
    INDEX idx_pav_attribute (attribute_id),
    INDEX idx_pav_value_number (value_number),
    INDEX idx_pav_value_text (value_text(100)),
    
    CONSTRAINT fk_pav_product FOREIGN KEY (product_id) 
        REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT fk_pav_attribute FOREIGN KEY (attribute_id) 
        REFERENCES attribute(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Giá trị thuộc tính của sản phẩm';

-- =====================================================
-- Seed initial attribute groups and attributes for Electronics
-- =====================================================

-- Insert Attribute Groups
INSERT INTO attribute_group (name, slug, description, sort_order) VALUES
('Màn hình', 'man-hinh', 'Thông số màn hình', 1),
('Hiệu năng', 'hieu-nang', 'CPU, RAM, Bộ nhớ', 2),
('Camera', 'camera', 'Thông số camera', 3),
('Pin & Sạc', 'pin-sac', 'Dung lượng pin và sạc', 4),
('Kết nối', 'ket-noi', 'Kết nối mạng và không dây', 5),
('Thiết kế', 'thiet-ke', 'Kích thước và trọng lượng', 6),
('Tính năng khác', 'tinh-nang-khac', 'Các tính năng bổ sung', 7);

-- Insert Attributes for Phones/Laptops
INSERT INTO attribute (attribute_group_id, name, slug, data_type, unit, sort_order, is_filterable, is_comparable) VALUES
-- Màn hình (group 1)
(1, 'Kích thước màn hình', 'kich-thuoc-man-hinh', 'NUMBER', 'inch', 1, TRUE, TRUE),
(1, 'Độ phân giải', 'do-phan-giai', 'TEXT', NULL, 2, TRUE, TRUE),
(1, 'Công nghệ màn hình', 'cong-nghe-man-hinh', 'TEXT', NULL, 3, TRUE, TRUE),
(1, 'Tần số quét', 'tan-so-quet', 'NUMBER', 'Hz', 4, TRUE, TRUE),
(1, 'Độ sáng tối đa', 'do-sang-toi-da', 'NUMBER', 'nits', 5, FALSE, TRUE),

-- Hiệu năng (group 2)
(2, 'Chip xử lý', 'chip-xu-ly', 'TEXT', NULL, 1, TRUE, TRUE),
(2, 'RAM', 'ram', 'NUMBER', 'GB', 2, TRUE, TRUE),
(2, 'Bộ nhớ trong', 'bo-nho-trong', 'NUMBER', 'GB', 3, TRUE, TRUE),
(2, 'GPU', 'gpu', 'TEXT', NULL, 4, FALSE, TRUE),
(2, 'AnTuTu Score', 'antutu-score', 'NUMBER', 'điểm', 5, FALSE, TRUE),

-- Camera (group 3)
(3, 'Camera chính', 'camera-chinh', 'TEXT', NULL, 1, FALSE, TRUE),
(3, 'Camera trước', 'camera-truoc', 'TEXT', NULL, 2, FALSE, TRUE),
(3, 'Quay video', 'quay-video', 'TEXT', NULL, 3, FALSE, TRUE),
(3, 'Tính năng camera', 'tinh-nang-camera', 'TEXT', NULL, 4, FALSE, TRUE),

-- Pin & Sạc (group 4)
(4, 'Dung lượng pin', 'dung-luong-pin', 'NUMBER', 'mAh', 1, TRUE, TRUE),
(4, 'Công suất sạc', 'cong-suat-sac', 'NUMBER', 'W', 2, TRUE, TRUE),
(4, 'Sạc không dây', 'sac-khong-day', 'BOOLEAN', NULL, 3, TRUE, TRUE),
(4, 'Sạc ngược không dây', 'sac-nguoc-khong-day', 'BOOLEAN', NULL, 4, FALSE, TRUE),

-- Kết nối (group 5)
(5, 'Mạng di động', 'mang-di-dong', 'TEXT', NULL, 1, TRUE, TRUE),
(5, 'SIM', 'sim', 'TEXT', NULL, 2, TRUE, TRUE),
(5, 'WiFi', 'wifi', 'TEXT', NULL, 3, FALSE, TRUE),
(5, 'Bluetooth', 'bluetooth', 'TEXT', NULL, 4, FALSE, TRUE),
(5, 'NFC', 'nfc', 'BOOLEAN', NULL, 5, TRUE, TRUE),
(5, 'Cổng kết nối', 'cong-ket-noi', 'TEXT', NULL, 6, FALSE, TRUE),

-- Thiết kế (group 6)
(6, 'Kích thước', 'kich-thuoc', 'TEXT', NULL, 1, FALSE, TRUE),
(6, 'Trọng lượng', 'trong-luong', 'NUMBER', 'gram', 2, FALSE, TRUE),
(6, 'Chất liệu', 'chat-lieu', 'TEXT', NULL, 3, FALSE, TRUE),
(6, 'Chuẩn kháng nước', 'chuan-khang-nuoc', 'TEXT', NULL, 4, TRUE, TRUE),

-- Tính năng khác (group 7)
(7, 'Hệ điều hành', 'he-dieu-hanh', 'TEXT', NULL, 1, TRUE, TRUE),
(7, 'Bảo mật', 'bao-mat', 'TEXT', NULL, 2, FALSE, TRUE),
(7, 'Tính năng đặc biệt', 'tinh-nang-dac-biet', 'TEXT', NULL, 3, FALSE, TRUE);
