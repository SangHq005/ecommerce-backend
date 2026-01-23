# TÀI LIỆU MÔ TẢ CƠ SỞ DỮ LIỆU

## 1. BẢNG role (Vai trò)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của vai trò |
| 2 | code | VARCHAR(32) | NOT NULL, UNIQUE | Mã vai trò (ADMIN, SELLER, CLIENT) |
| 3 | name | VARCHAR(64) | NOT NULL | Tên vai trò |

---

## 2. BẢNG app_user (Người dùng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của người dùng |
| 2 | email | VARCHAR(191) | NULL, UNIQUE | Email đăng nhập (nullable cho OAuth) |
| 3 | password_hash | VARCHAR(255) | NULL | Mật khẩu đã hash (nullable cho OAuth) |
| 4 | full_name | VARCHAR(191) | NOT NULL | Họ và tên đầy đủ |
| 5 | status | VARCHAR(32) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái tài khoản |
| 6 | google_sub | VARCHAR(191) | NULL, UNIQUE | Google OAuth subject ID |
| 7 | phone_number | VARCHAR(20) | NULL, UNIQUE | Số điện thoại |
| 8 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 9 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 3. BẢNG user_role (Vai trò người dùng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | user_id | BIGINT | PRIMARY KEY, FOREIGN KEY → app_user(id) | ID người dùng |
| 2 | role_id | BIGINT | PRIMARY KEY, FOREIGN KEY → role(id) | ID vai trò |

---

## 4. BẢNG user_profile (Hồ sơ người dùng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | user_id | BIGINT | PRIMARY KEY, FOREIGN KEY → app_user(id) | ID người dùng |
| 2 | phone | VARCHAR(32) | NULL | Số điện thoại |
| 3 | gender | VARCHAR(10) | NULL | Giới tính (MALE, FEMALE, OTHER) |
| 4 | date_of_birth | DATE | NULL | Ngày sinh |
| 5 | avatar_url | VARCHAR(512) | NULL | URL ảnh đại diện |
| 6 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 7 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 5. BẢNG user_address (Địa chỉ người dùng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của địa chỉ |
| 2 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | receiver_name | VARCHAR(191) | NOT NULL | Tên người nhận |
| 4 | receiver_phone | VARCHAR(32) | NOT NULL | Số điện thoại người nhận |
| 5 | line1 | VARCHAR(255) | NOT NULL | Địa chỉ dòng 1 |
| 6 | line2 | VARCHAR(255) | NULL | Địa chỉ dòng 2 |
| 7 | ward | VARCHAR(128) | NULL | Phường/Xã |
| 8 | district | VARCHAR(128) | NULL | Quận/Huyện |
| 9 | province | VARCHAR(128) | NULL | Tỉnh/Thành phố |
| 10 | postal_code | VARCHAR(32) | NULL | Mã bưu điện |
| 11 | address_type | VARCHAR(32) | NULL | Loại địa chỉ (HOME, WORK, etc.) |
| 12 | is_default | BOOLEAN | NOT NULL, DEFAULT FALSE | Địa chỉ mặc định |
| 13 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 14 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 15 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 6. BẢNG seller_shop (Cửa hàng người bán)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của cửa hàng |
| 2 | seller_user_id | BIGINT | NOT NULL, UNIQUE, FOREIGN KEY → app_user(id) | ID người bán |
| 3 | shop_name | VARCHAR(191) | NOT NULL | Tên cửa hàng |
| 4 | shop_slug | VARCHAR(191) | NOT NULL, UNIQUE | Slug URL của cửa hàng |
| 5 | description | LONGTEXT | NULL | Mô tả cửa hàng |
| 6 | logo_url | VARCHAR(512) | NULL | URL logo cửa hàng |
| 7 | banner_url | VARCHAR(512) | NULL | URL banner cửa hàng |
| 8 | status | VARCHAR(32) | NOT NULL, DEFAULT 'PENDING' | Trạng thái (DRAFT/PENDING_REVIEW/ACTIVE/SUSPENDED) |
| 9 | city | VARCHAR(100) | NULL | Thành phố |
| 10 | verified_at | DATETIME | NULL | Thời gian xác minh |
| 11 | suspended_reason | VARCHAR(255) | NULL | Lý do bị đình chỉ |
| 12 | contact_name | VARCHAR(100) | NULL | Tên người liên hệ |
| 13 | contact_phone | VARCHAR(20) | NULL | Số điện thoại liên hệ |
| 14 | contact_email | VARCHAR(100) | NULL | Email liên hệ |
| 15 | identity_code | VARCHAR(50) | NULL | Mã định danh |
| 16 | tax_code | VARCHAR(50) | NULL | Mã số thuế |
| 17 | bank_name | VARCHAR(100) | NULL | Tên ngân hàng |
| 18 | bank_account_number | VARCHAR(50) | NULL | Số tài khoản ngân hàng |
| 19 | bank_account_name | VARCHAR(100) | NULL | Tên chủ tài khoản |
| 20 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 21 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 22 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 7. BẢNG seller_profile (Hồ sơ người bán)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL, UNIQUE, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | status | VARCHAR(32) | NOT NULL, DEFAULT 'PENDING_VERIFICATION' | Trạng thái xác minh |
| 4 | seller_type | VARCHAR(20) | NOT NULL, DEFAULT 'INDIVIDUAL' | Loại người bán (INDIVIDUAL/BUSINESS) |
| 5 | full_name | VARCHAR(100) | NULL | Họ tên đầy đủ |
| 6 | id_type | VARCHAR(30) | NULL | Loại giấy tờ (CCCD, PASSPORT, BUSINESS_LICENSE) |
| 7 | id_number | VARCHAR(50) | NULL | Số giấy tờ |
| 8 | id_image_front | VARCHAR(512) | NULL | URL ảnh mặt trước CMND/CCCD |
| 9 | id_image_back | VARCHAR(512) | NULL | URL ảnh mặt sau CMND/CCCD |
| 10 | tax_code | VARCHAR(50) | NULL | Mã số thuế |
| 11 | contact_phone | VARCHAR(20) | NULL | Số điện thoại liên hệ |
| 12 | contact_email | VARCHAR(100) | NULL | Email liên hệ |
| 13 | city | VARCHAR(100) | NULL | Thành phố |
| 14 | address | VARCHAR(255) | NULL | Địa chỉ |
| 15 | submitted_at | DATETIME | NULL | Thời gian nộp hồ sơ |
| 16 | verified_at | DATETIME | NULL | Thời gian xác minh |
| 17 | rejected_at | DATETIME | NULL | Thời gian từ chối |
| 18 | rejected_reason | VARCHAR(500) | NULL | Lý do từ chối |
| 19 | verified_by | BIGINT | NULL | ID admin xác minh |
| 20 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 21 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 22 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 8. BẢNG category (Danh mục)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của danh mục |
| 2 | name | VARCHAR(191) | NOT NULL | Tên danh mục |
| 3 | slug | VARCHAR(191) | NOT NULL, UNIQUE | Slug URL |
| 4 | parent_id | BIGINT | NULL, FOREIGN KEY → category(id) | ID danh mục cha |
| 5 | path | VARCHAR(512) | NOT NULL | Đường dẫn phân cấp |
| 6 | is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Trạng thái hoạt động |
| 7 | sort_order | INT | NOT NULL, DEFAULT 0 | Thứ tự sắp xếp |
| 8 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 9 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 10 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 9. BẢNG brand (Thương hiệu)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của thương hiệu |
| 2 | name | VARCHAR(191) | NOT NULL, UNIQUE | Tên thương hiệu |
| 3 | slug | VARCHAR(191) | NOT NULL, UNIQUE | Slug URL |
| 4 | logo_url | VARCHAR(512) | NULL | URL logo |
| 5 | is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Trạng thái hoạt động |
| 6 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 7 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 8 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 10. BẢNG product (Sản phẩm)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của sản phẩm |
| 2 | shop_id | BIGINT | NOT NULL, FOREIGN KEY → seller_shop(id) | ID cửa hàng |
| 3 | category_id | BIGINT | NOT NULL, FOREIGN KEY → category(id) | ID danh mục |
| 4 | brand_id | BIGINT | NULL, FOREIGN KEY → brand(id) | ID thương hiệu |
| 5 | name | VARCHAR(255) | NOT NULL | Tên sản phẩm |
| 6 | slug | VARCHAR(255) | NOT NULL | Slug URL (unique với shop_id) |
| 7 | description | LONGTEXT | NULL | Mô tả sản phẩm |
| 8 | status | VARCHAR(32) | NOT NULL, DEFAULT 'DRAFT' | Trạng thái (DRAFT/PENDING/ACTIVE/REJECTED) |
| 9 | main_image_url | VARCHAR(512) | NULL | URL ảnh chính |
| 10 | price | BIGINT | NOT NULL, DEFAULT 0 | Giá bán (VND) |
| 11 | original_price | DECIMAL(15,2) | NULL | Giá gốc (để hiển thị giảm giá) |
| 12 | stock_quantity | INT | NOT NULL, DEFAULT 0 | Số lượng tồn kho |
| 13 | sku | VARCHAR(100) | NULL | Mã SKU |
| 14 | currency | VARCHAR(8) | NOT NULL, DEFAULT 'VND' | Đơn vị tiền tệ |
| 15 | seller_user_id | BIGINT | NOT NULL, DEFAULT 0 | ID người bán |
| 16 | average_rating | DECIMAL(2,1) | NOT NULL, DEFAULT 0.0 | Đánh giá trung bình |
| 17 | review_count | INT | NOT NULL, DEFAULT 0 | Số lượng đánh giá |
| 18 | sold_count | INT | NOT NULL, DEFAULT 0 | Số lượng đã bán |
| 19 | quality_score | INT | NULL | Điểm chất lượng listing (0-100) |
| 20 | published_at | DATETIME | NULL | Thời gian công khai lần đầu |
| 21 | hidden_at | DATETIME | NULL | Thời gian ẩn sản phẩm |
| 22 | rejected_at | DATETIME | NULL | Thời gian bị từ chối |
| 23 | rejected_by | BIGINT | NULL | ID admin từ chối |
| 24 | is_featured | TINYINT(1) | NOT NULL, DEFAULT 0 | Sản phẩm nổi bật |
| 25 | weight_grams | INT | NULL | Trọng lượng (gram) |
| 26 | shipping_fee_type | VARCHAR(20) | DEFAULT 'STANDARD' | Loại phí vận chuyển |
| 27 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 28 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 29 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 11. BẢNG product_sku (SKU sản phẩm)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của SKU |
| 2 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 3 | sku_code | VARCHAR(64) | NOT NULL | Mã SKU (unique với product_id) |
| 4 | price | BIGINT | NOT NULL | Giá bán (VND) |
| 5 | stock_on_hand | INT | NOT NULL, DEFAULT 0 | Tồn kho thực tế |
| 6 | reserved_stock | INT | NOT NULL, DEFAULT 0 | Tồn kho đã đặt |
| 7 | option_signature | VARCHAR(255) | NOT NULL, DEFAULT '' | Chữ ký tùy chọn |
| 8 | option_signature_hash | VARCHAR(64) | NOT NULL, DEFAULT '' | Hash chữ ký (unique với product_id) |
| 9 | compare_at_price | BIGINT | NULL | Giá so sánh |
| 10 | image_url | VARCHAR(512) | NULL | URL ảnh SKU |
| 11 | is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Trạng thái hoạt động |
| 12 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |
| 13 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 14 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 12. BẢNG product_image (Ảnh sản phẩm)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của ảnh |
| 2 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 3 | image_url | VARCHAR(512) | NOT NULL | URL ảnh |
| 4 | sort_order | INT | NOT NULL, DEFAULT 0 | Thứ tự sắp xếp (unique với product_id) |
| 5 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 13. BẢNG product_option_group (Nhóm tùy chọn sản phẩm)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của nhóm |
| 2 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 3 | name | VARCHAR(64) | NOT NULL | Tên nhóm (unique với product_id) |
| 4 | sort_order | INT | NOT NULL, DEFAULT 0 | Thứ tự sắp xếp |
| 5 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 14. BẢNG product_option_value (Giá trị tùy chọn)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của giá trị |
| 2 | option_group_id | BIGINT | NOT NULL, FOREIGN KEY → product_option_group(id) | ID nhóm tùy chọn |
| 3 | value | VARCHAR(64) | NOT NULL | Giá trị (unique với option_group_id) |
| 4 | sort_order | INT | NOT NULL, DEFAULT 0 | Thứ tự sắp xếp |
| 5 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 15. BẢNG attribute_group (Nhóm thuộc tính)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | name | VARCHAR(100) | NOT NULL | Tên nhóm (Màn hình, Hiệu năng...) |
| 3 | slug | VARCHAR(100) | NOT NULL, UNIQUE | Slug URL |
| 4 | description | VARCHAR(500) | NULL | Mô tả nhóm |
| 5 | sort_order | INT | NOT NULL, DEFAULT 0 | Thứ tự hiển thị |
| 6 | is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Trạng thái hoạt động |
| 7 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 8 | updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 16. BẢNG attribute (Thuộc tính)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | attribute_group_id | BIGINT | NOT NULL, FOREIGN KEY → attribute_group(id) | ID nhóm thuộc tính |
| 3 | name | VARCHAR(100) | NOT NULL | Tên thuộc tính (RAM, CPU...) |
| 4 | slug | VARCHAR(100) | NOT NULL, UNIQUE | Slug URL |
| 5 | data_type | ENUM | NOT NULL, DEFAULT 'TEXT' | Loại dữ liệu (TEXT/NUMBER/BOOLEAN/ENUM) |
| 6 | unit | VARCHAR(50) | NULL | Đơn vị (GB, inch, mAh...) |
| 7 | description | VARCHAR(500) | NULL | Mô tả |
| 8 | sort_order | INT | NOT NULL, DEFAULT 0 | Thứ tự trong nhóm |
| 9 | is_filterable | BOOLEAN | NOT NULL, DEFAULT FALSE | Có thể lọc |
| 10 | is_comparable | BOOLEAN | NOT NULL, DEFAULT TRUE | Hiển thị khi so sánh |
| 11 | is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Trạng thái hoạt động |
| 12 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 13 | updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 17. BẢNG category_attribute (Mapping thuộc tính theo danh mục)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | category_id | BIGINT | NOT NULL, FOREIGN KEY → category(id) | ID danh mục |
| 3 | attribute_id | BIGINT | NOT NULL, FOREIGN KEY → attribute(id) | ID thuộc tính |
| 4 | is_required | BOOLEAN | NOT NULL, DEFAULT FALSE | Bắt buộc nhập |
| 5 | sort_order | INT | NOT NULL, DEFAULT 0 | Thứ tự trong danh mục |
| 6 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 18. BẢNG product_attribute_value (Giá trị thuộc tính sản phẩm)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 3 | attribute_id | BIGINT | NOT NULL, FOREIGN KEY → attribute(id) | ID thuộc tính |
| 4 | value_text | VARCHAR(1000) | NULL | Giá trị dạng text |
| 5 | value_number | DECIMAL(15,4) | NULL | Giá trị số (để so sánh) |
| 6 | value_boolean | BOOLEAN | NULL | Giá trị boolean |
| 7 | display_value | VARCHAR(500) | NOT NULL | Giá trị hiển thị (có unit) |
| 8 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 9 | updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 19. BẢNG stock_movement (Biến động tồn kho)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | sku_id | BIGINT | NOT NULL, FOREIGN KEY → product_sku(id) | ID SKU |
| 3 | delta | INT | NOT NULL | Số lượng thay đổi (+/-) |
| 4 | reason | VARCHAR(64) | NOT NULL | Lý do thay đổi |
| 5 | actor_id | BIGINT | NULL | ID người thực hiện |
| 6 | idem_scope | VARCHAR(64) | NOT NULL | Phạm vi idempotency |
| 7 | idem_key | VARCHAR(128) | NOT NULL | Khóa idempotency (unique với scope) |
| 8 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 20. BẢNG stock_reservation (Đặt trước tồn kho)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | order_token | VARCHAR(64) | NOT NULL | Token đơn hàng |
| 3 | sku_id | BIGINT | NOT NULL, FOREIGN KEY → product_sku(id) | ID SKU |
| 4 | qty | INT | NOT NULL | Số lượng đặt trước |
| 5 | status | VARCHAR(16) | NOT NULL | Trạng thái (RESERVED/RELEASED/COMMITTED) |
| 6 | expires_at | TIMESTAMP | NULL | Thời gian hết hạn |
| 7 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 8 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 21. BẢNG orders (Đơn hàng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất của đơn hàng |
| 2 | order_code | VARCHAR(32) | NOT NULL, UNIQUE | Mã đơn hàng |
| 3 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người mua |
| 4 | shop_id | BIGINT | NOT NULL, DEFAULT 0 | ID cửa hàng |
| 5 | status | VARCHAR(32) | NOT NULL | Trạng thái đơn hàng |
| 6 | total_amount | BIGINT | NOT NULL, DEFAULT 0 | Tổng tiền (VND) |
| 7 | currency | VARCHAR(8) | NOT NULL, DEFAULT 'VND' | Đơn vị tiền tệ |
| 8 | address_id | BIGINT | NULL, FOREIGN KEY → user_address(id) | ID địa chỉ giao hàng |
| 9 | payment_method | VARCHAR(32) | NULL | Phương thức thanh toán |
| 10 | note | TEXT | NULL | Ghi chú |
| 11 | coupon_code | VARCHAR(64) | NULL | Mã coupon |
| 12 | discount_amount | BIGINT | NOT NULL, DEFAULT 0 | Số tiền giảm giá |
| 13 | shipping_fee | BIGINT | NOT NULL, DEFAULT 0 | Phí vận chuyển |
| 14 | seller_voucher_id | BIGINT | NULL | ID voucher của người bán |
| 15 | seller_voucher_discount | BIGINT | NOT NULL, DEFAULT 0 | Giảm giá từ voucher người bán |
| 16 | shipping_provider | VARCHAR(50) | NULL | Nhà vận chuyển (GHN, GHTK...) |
| 17 | shipping_tracking_url | VARCHAR(512) | NULL | URL theo dõi vận chuyển |
| 18 | shipped_at | DATETIME | NULL | Thời gian giao hàng |
| 19 | delivered_at | DATETIME | NULL | Thời gian nhận hàng |
| 20 | completed_at | DATETIME | NULL | Thời gian hoàn thành |
| 21 | estimated_delivery_date | DATETIME | NULL | Ngày dự kiến giao hàng |
| 22 | delivery_attempts | INT | NOT NULL, DEFAULT 0 | Số lần giao hàng |
| 23 | delivery_failed_reason | VARCHAR(255) | NULL | Lý do giao hàng thất bại |
| 24 | buyer_confirmed | BOOLEAN | NOT NULL, DEFAULT FALSE | Người mua đã xác nhận |
| 25 | buyer_confirmed_at | DATETIME | NULL | Thời gian xác nhận |
| 26 | auto_complete_at | DATETIME | NULL | Thời gian tự động hoàn thành |
| 27 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 28 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 22. BẢNG order_item (Chi tiết đơn hàng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | order_id | BIGINT | NOT NULL, FOREIGN KEY → orders(id) | ID đơn hàng |
| 3 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 4 | sku_id | BIGINT | NOT NULL, FOREIGN KEY → product_sku(id) | ID SKU |
| 5 | quantity | INT | NOT NULL | Số lượng |
| 6 | unit_price | BIGINT | NOT NULL | Giá đơn vị |
| 7 | total_price | BIGINT | NOT NULL | Tổng tiền |

---

## 23. BẢNG order_status_history (Lịch sử trạng thái đơn hàng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | order_id | BIGINT | NOT NULL, FOREIGN KEY → orders(id) | ID đơn hàng |
| 3 | from_status | VARCHAR(32) | NULL | Trạng thái trước |
| 4 | to_status | VARCHAR(32) | NOT NULL | Trạng thái mới |
| 5 | actor_type | VARCHAR(20) | NOT NULL | Loại người thực hiện (SYSTEM/BUYER/SELLER/ADMIN) |
| 6 | actor_id | BIGINT | NULL | ID người thực hiện |
| 7 | reason | VARCHAR(500) | NULL | Lý do thay đổi |
| 8 | note | TEXT | NULL | Ghi chú |
| 9 | metadata | JSON | NULL | Dữ liệu bổ sung |
| 10 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 24. BẢNG refund (Hoàn tiền)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | order_id | BIGINT | NOT NULL, FOREIGN KEY → orders(id) | ID đơn hàng |
| 3 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 4 | shop_id | BIGINT | NOT NULL, FOREIGN KEY → seller_shop(id) | ID cửa hàng |
| 5 | reason | VARCHAR(500) | NOT NULL | Lý do hoàn tiền |
| 6 | description | LONGTEXT | NULL | Mô tả chi tiết |
| 7 | refund_amount | BIGINT | NOT NULL | Số tiền hoàn |
| 8 | currency | VARCHAR(8) | NOT NULL, DEFAULT 'VND' | Đơn vị tiền tệ |
| 9 | status | VARCHAR(32) | NOT NULL | Trạng thái |
| 10 | refund_type | VARCHAR(20) | DEFAULT 'REFUND' | Loại (REFUND/RETURN) |
| 11 | return_tracking_number | VARCHAR(100) | NULL | Mã vận đơn trả hàng |
| 12 | return_shipping_provider | VARCHAR(50) | NULL | Nhà vận chuyển trả hàng |
| 13 | admin_note | VARCHAR(1000) | NULL | Ghi chú admin |
| 14 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 15 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 16 | processed_at | DATETIME | NULL | Thời gian xử lý |
| 17 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 25. BẢNG cart_item (Giỏ hàng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL | ID người dùng |
| 3 | shop_id | BIGINT | NOT NULL, DEFAULT 0 | ID cửa hàng |
| 4 | product_id | BIGINT | NOT NULL | ID sản phẩm |
| 5 | sku_id | BIGINT | NOT NULL | ID SKU |
| 6 | quantity | INT | NOT NULL, DEFAULT 1 | Số lượng |
| 7 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 8 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 26. BẢNG payments (Thanh toán)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | order_id | BIGINT | NOT NULL, FOREIGN KEY → orders(id) | ID đơn hàng |
| 3 | amount | BIGINT | NOT NULL | Số tiền (đơn vị nhỏ nhất) |
| 4 | currency | VARCHAR(3) | NOT NULL, DEFAULT 'VND' | Đơn vị tiền tệ |
| 5 | method | VARCHAR(50) | NOT NULL | Phương thức (VNPAY, MOMO, STRIPE...) |
| 6 | status | VARCHAR(20) | NOT NULL | Trạng thái (PENDING/PROCESSING/COMPLETED/FAILED...) |
| 7 | transaction_id | VARCHAR(255) | NULL | ID giao dịch từ gateway |
| 8 | gateway | VARCHAR(50) | NOT NULL | Tên payment gateway |
| 9 | gateway_response | JSON | NULL | Phản hồi đầy đủ từ gateway |
| 10 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 11 | updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 27. BẢNG password_reset_tokens (Token đặt lại mật khẩu)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | token | VARCHAR(255) | NOT NULL, UNIQUE | Token đặt lại mật khẩu |
| 4 | expires_at | TIMESTAMP | NOT NULL | Thời gian hết hạn |
| 5 | used | BOOLEAN | NOT NULL, DEFAULT FALSE | Đã sử dụng |
| 6 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 28. BẢNG review (Đánh giá)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 3 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 4 | order_id | BIGINT | NULL, FOREIGN KEY → orders(id) | ID đơn hàng |
| 5 | parent_id | BIGINT | NULL | ID đánh giá cha (cho reply) |
| 6 | rating | INT | NULL | Điểm đánh giá (1-5) |
| 7 | comment | LONGTEXT | NULL | Bình luận |
| 8 | images | JSON | NULL | Ảnh đánh giá |
| 9 | status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Trạng thái |
| 10 | helpful_count | INT | NOT NULL, DEFAULT 0 | Số lượt hữu ích |
| 11 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 29. BẢNG review_helpful (Đánh giá hữu ích)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | review_id | BIGINT | NOT NULL, FOREIGN KEY → review(id) | ID đánh giá |
| 3 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 4 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 30. BẢNG coupon (Mã giảm giá)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | code | VARCHAR(50) | NOT NULL, UNIQUE | Mã coupon |
| 3 | name | VARCHAR(255) | NOT NULL | Tên coupon |
| 4 | description | LONGTEXT | NULL | Mô tả |
| 5 | type | VARCHAR(20) | NOT NULL | Loại coupon |
| 6 | status | VARCHAR(20) | NOT NULL | Trạng thái |
| 7 | discount_value | BIGINT | NOT NULL | Giá trị giảm giá |
| 8 | max_discount_amount | BIGINT | NULL | Giảm giá tối đa |
| 9 | min_order_amount | BIGINT | NULL | Đơn hàng tối thiểu |
| 10 | start_date | DATETIME | NOT NULL | Ngày bắt đầu |
| 11 | end_date | DATETIME | NOT NULL | Ngày kết thúc |
| 12 | usage_limit | INT | NULL | Giới hạn sử dụng |
| 13 | usage_count | INT | NOT NULL, DEFAULT 0 | Số lần đã dùng |
| 14 | usage_limit_per_user | INT | NULL | Giới hạn mỗi người dùng |
| 15 | auto_apply | BOOLEAN | NOT NULL, DEFAULT FALSE | Tự động áp dụng |
| 16 | applicable_product_ids | JSON | NULL | Danh sách ID sản phẩm áp dụng |
| 17 | applicable_category_ids | JSON | NULL | Danh sách ID danh mục áp dụng |
| 18 | applicable_user_ids | JSON | NULL | Danh sách ID người dùng áp dụng |
| 19 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 20 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

---

## 31. BẢNG coupon_usage (Sử dụng coupon)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | coupon_id | BIGINT | NOT NULL, FOREIGN KEY → coupon(id) | ID coupon |
| 3 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 4 | order_id | BIGINT | NOT NULL | ID đơn hàng |
| 5 | discount_amount | BIGINT | NOT NULL | Số tiền giảm giá |
| 6 | used_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian sử dụng |

---

## 32. BẢNG seller_voucher (Voucher người bán)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | shop_id | BIGINT | NOT NULL, FOREIGN KEY → seller_shop(id) | ID cửa hàng |
| 3 | code | VARCHAR(50) | NOT NULL | Mã voucher (unique với shop_id) |
| 4 | name | VARCHAR(255) | NOT NULL | Tên voucher |
| 5 | description | LONGTEXT | NULL | Mô tả |
| 6 | discount_type | VARCHAR(20) | NOT NULL | Loại giảm giá (PERCENTAGE/FIXED_AMOUNT) |
| 7 | discount_value | BIGINT | NOT NULL | Giá trị giảm giá |
| 8 | max_discount_amount | BIGINT | NULL | Giảm giá tối đa |
| 9 | min_order_amount | BIGINT | NULL | Đơn hàng tối thiểu |
| 10 | start_date | DATETIME | NOT NULL | Ngày bắt đầu |
| 11 | end_date | DATETIME | NOT NULL | Ngày kết thúc |
| 12 | usage_limit | INT | NULL | Giới hạn sử dụng |
| 13 | usage_count | INT | NOT NULL, DEFAULT 0 | Số lần đã dùng |
| 14 | usage_limit_per_user | INT | NULL | Giới hạn mỗi người dùng |
| 15 | status | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | Trạng thái |
| 16 | applicable_product_ids | JSON | NULL | Danh sách ID sản phẩm áp dụng |
| 17 | applicable_category_ids | JSON | NULL | Danh sách ID danh mục áp dụng |
| 18 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 19 | updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |
| 20 | version | BIGINT | NOT NULL, DEFAULT 0 | Phiên bản cho optimistic locking |

---

## 33. BẢNG seller_voucher_usage (Sử dụng voucher người bán)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | voucher_id | BIGINT | NOT NULL, FOREIGN KEY → seller_voucher(id) | ID voucher |
| 3 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 4 | order_id | BIGINT | NOT NULL, FOREIGN KEY → orders(id) | ID đơn hàng |
| 5 | discount_amount | BIGINT | NOT NULL | Số tiền giảm giá thực tế |
| 6 | used_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian sử dụng |

---

## 34. BẢNG wishlist_item (Danh sách yêu thích)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 4 | added_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian thêm |
| 5 | note | VARCHAR(500) | NULL | Ghi chú |

---

## 35. BẢNG refresh_tokens (Token làm mới)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | jti | VARCHAR(64) | NOT NULL, UNIQUE | JWT ID |
| 4 | family_id | VARCHAR(64) | NOT NULL | ID gia đình token |
| 5 | issued_at | DATETIME | NOT NULL | Thời gian phát hành |
| 6 | expires_at | DATETIME | NOT NULL | Thời gian hết hạn |
| 7 | revoked_at | DATETIME | NULL | Thời gian thu hồi |
| 8 | replaced_by_jti | VARCHAR(64) | NULL | JTI thay thế |
| 9 | ip | VARCHAR(64) | NULL | Địa chỉ IP |
| 10 | user_agent | VARCHAR(255) | NULL | User agent |
| 11 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 36. BẢNG refresh_sessions (Phiên làm mới)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | session_root_jti | VARCHAR(64) | NOT NULL | JTI gốc của phiên |
| 4 | refresh_jti | VARCHAR(64) | NOT NULL, UNIQUE | JTI refresh token |
| 5 | status | VARCHAR(32) | NOT NULL | Trạng thái |
| 6 | user_agent | VARCHAR(255) | NULL | User agent |
| 7 | ip | VARCHAR(64) | NULL | Địa chỉ IP |
| 8 | expires_at | DATETIME(6) | NOT NULL | Thời gian hết hạn |
| 9 | created_at | DATETIME(6) | NOT NULL, DEFAULT CURRENT_TIMESTAMP(6) | Thời gian tạo |
| 10 | rotated_at | DATETIME(6) | NULL | Thời gian xoay |
| 11 | revoked_at | DATETIME(6) | NULL | Thời gian thu hồi |

---

## 37. BẢNG audit_log (Nhật ký kiểm toán)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | actor_id | BIGINT | NULL | ID người thực hiện |
| 3 | actor_type | VARCHAR(32) | NULL | Loại người thực hiện |
| 4 | action | VARCHAR(128) | NOT NULL | Hành động |
| 5 | resource_type | VARCHAR(64) | NOT NULL | Loại tài nguyên |
| 6 | resource_id | VARCHAR(64) | NULL | ID tài nguyên |
| 7 | metadata | JSON | NULL | Dữ liệu bổ sung |
| 8 | correlation_id | VARCHAR(64) | NULL | ID tương quan |
| 9 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 38. BẢNG idempotency_key (Khóa idempotency)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | idem_key | VARCHAR(128) | NOT NULL | Khóa idempotency |
| 3 | scope | VARCHAR(64) | NOT NULL | Phạm vi (unique với idem_key) |
| 4 | request_hash | VARCHAR(64) | NOT NULL | Hash request |
| 5 | response_code | INT | NULL | Mã phản hồi |
| 6 | response_body | JSON | NULL | Nội dung phản hồi |
| 7 | status | VARCHAR(32) | NOT NULL | Trạng thái |
| 8 | expires_at | TIMESTAMP | NOT NULL | Thời gian hết hạn |
| 9 | created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 39. BẢNG notification (Thông báo)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | type | VARCHAR(50) | NOT NULL | Loại thông báo |
| 4 | title | VARCHAR(255) | NOT NULL | Tiêu đề |
| 5 | message | LONGTEXT | NULL | Nội dung |
| 6 | reference_type | VARCHAR(50) | NULL | Loại tham chiếu |
| 7 | reference_id | BIGINT | NULL | ID tham chiếu |
| 8 | is_read | BOOLEAN | NOT NULL, DEFAULT FALSE | Đã đọc |
| 9 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| 10 | read_at | DATETIME | NULL | Thời gian đọc |

---

## 40. BẢNG trusted_device (Thiết bị tin cậy)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | user_id | BIGINT | NOT NULL, FOREIGN KEY → app_user(id) | ID người dùng |
| 3 | device_id | VARCHAR(100) | NOT NULL | ID thiết bị |
| 4 | trusted_at | DATETIME | NOT NULL | Thời gian tin cậy |
| 5 | expires_at | DATETIME | NOT NULL | Thời gian hết hạn |
| 6 | last_used_at | DATETIME | NULL | Thời gian sử dụng cuối |

---

## 41. BẢNG product_status_history (Lịch sử trạng thái sản phẩm)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | product_id | BIGINT | NOT NULL, FOREIGN KEY → product(id) | ID sản phẩm |
| 3 | shop_id | BIGINT | NOT NULL | ID cửa hàng |
| 4 | previous_status | VARCHAR(32) | NULL | Trạng thái trước |
| 5 | new_status | VARCHAR(32) | NOT NULL | Trạng thái mới |
| 6 | changed_by | BIGINT | NULL | ID người thay đổi |
| 7 | changed_by_type | VARCHAR(20) | NULL | Loại người thay đổi (SELLER/ADMIN/SYSTEM) |
| 8 | reason | VARCHAR(500) | NULL | Lý do thay đổi |
| 9 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## 42. BẢNG shop_status_history (Lịch sử trạng thái cửa hàng)

| STT | Thuộc tính | Kiểu dữ liệu | Ràng buộc | Diễn giải |
|-----|------------|--------------|-----------|-----------|
| 1 | id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | ID duy nhất |
| 2 | shop_id | BIGINT | NOT NULL, FOREIGN KEY → seller_shop(id) | ID cửa hàng |
| 3 | from_status | VARCHAR(32) | NULL | Trạng thái trước |
| 4 | to_status | VARCHAR(32) | NOT NULL | Trạng thái mới |
| 5 | actor_type | VARCHAR(20) | NOT NULL | Loại người thực hiện (SYSTEM/SELLER/ADMIN) |
| 6 | actor_id | BIGINT | NULL | ID người thực hiện |
| 7 | reason | VARCHAR(500) | NULL | Lý do thay đổi |
| 8 | note | TEXT | NULL | Ghi chú |
| 9 | metadata | JSON | NULL | Dữ liệu bổ sung |
| 10 | created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## TỔNG KẾT

Hệ thống có **42 bảng** chính, bao gồm:

- **Quản lý người dùng**: app_user, user_profile, user_role, user_address, role
- **Quản lý người bán**: seller_shop, seller_profile, shop_status_history
- **Danh mục sản phẩm**: category, brand, product, product_sku, product_image, product_option_group, product_option_value
- **Thuộc tính sản phẩm**: attribute_group, attribute, category_attribute, product_attribute_value
- **Tồn kho**: stock_movement, stock_reservation
- **Đơn hàng**: orders, order_item, order_status_history
- **Thanh toán**: payments, refund
- **Giỏ hàng & Yêu thích**: cart_item, wishlist_item
- **Đánh giá**: review, review_helpful
- **Khuyến mãi**: coupon, coupon_usage, seller_voucher, seller_voucher_usage
- **Xác thực**: refresh_tokens, refresh_sessions, password_reset_tokens, trusted_device
- **Hệ thống**: audit_log, idempotency_key, notification
