# Vấn Đề: Admin Không Nhận Notification Khi Seller Tạo Sản Phẩm

## Nguyên Nhân

### Flow Hiện Tại

1. **Seller tạo sản phẩm:**
   - API: `POST /api/v1/seller/products`
   - Method: `CatalogService.sellerCreateDraft()`
   - Status: `DRAFT`
   - **KHÔNG có notification** (đúng behavior)

2. **Seller submit sản phẩm:**
   - API: `POST /api/v1/seller/products/{id}/submit`
   - Method: `CatalogService.sellerSubmit()`
   - Status: `DRAFT` → `PENDING_REVIEW`
   - **CÓ notification** gửi tới admin

### Vấn Đề

**Notification chỉ được gửi khi seller SUBMIT sản phẩm, không phải khi TẠO sản phẩm.**

Nếu seller chỉ tạo sản phẩm (DRAFT) mà chưa submit → Admin không nhận notification.

## Giải Pháp

### Option 1: Giữ nguyên (Khuyến nghị)
- Seller tạo sản phẩm → DRAFT (chưa gửi notification)
- Seller submit sản phẩm → PENDING_REVIEW (gửi notification)
- **Lý do:** Sản phẩm DRAFT có thể chưa hoàn chỉnh, chỉ nên gửi notification khi seller đã sẵn sàng submit

### Option 2: Gửi notification ngay khi tạo
- Thêm notification vào `sellerCreateDraft()`
- **Nhược điểm:** Có thể spam admin với sản phẩm chưa hoàn chỉnh

## Kiểm Tra

### 1. Xác nhận seller đã submit
```sql
SELECT id, name, status, created_at, updated_at 
FROM product 
WHERE seller_user_id = ? 
ORDER BY updated_at DESC;
```

Nếu status = `DRAFT` → Seller chưa submit
Nếu status = `PENDING_REVIEW` → Đã submit, kiểm tra notification

### 2. Kiểm tra notification trong database
```sql
SELECT * FROM notification 
WHERE type = 'PRODUCT_PENDING' 
ORDER BY created_at DESC 
LIMIT 10;
```

### 3. Kiểm tra log
Tìm trong log file:
- `"Notifying X admins about new product submission"`
- `"Successfully notified admins about product"`
- `"Failed to notify admins about product"`

### 4. Kiểm tra có admin users
```sql
SELECT u.id, u.username, u.status 
FROM user u 
JOIN user_role ur ON u.id = ur.user_id 
JOIN role r ON ur.role_id = r.id 
WHERE r.code = 'ADMIN' AND u.status = 'ACTIVE';
```

Nếu không có admin → Notification không được gửi tới ai

## Debug Steps

1. **Kiểm tra seller có submit không:**
   - Xem trong database: `SELECT status FROM product WHERE id = ?`
   - Nếu `DRAFT` → Seller chưa submit, cần submit mới có notification

2. **Kiểm tra notification có được tạo không:**
   - Query database: `SELECT * FROM notification WHERE reference_type = 'PRODUCT' AND reference_id = ?`

3. **Kiểm tra log:**
   - Tìm log: `"Notifying X admins about new product submission"`
   - Nếu không thấy → Notification không được gọi hoặc có lỗi

4. **Kiểm tra có admin users:**
   - Nếu không có admin → Notification không được gửi

## Code Reference

### Notification được gửi ở đâu:
- File: `CatalogService.java`
- Method: `sellerSubmit()` (dòng 366-376)
- Chỉ gọi khi status = `DRAFT` → `PENDING_REVIEW`

### Notification method:
- File: `NotificationService.java`
- Method: `notifyAdminsNewProduct()` (dòng 290-316)
- Gửi tới tất cả admin có role ADMIN và status ACTIVE

## Kết Luận

**Nếu seller chỉ tạo sản phẩm (DRAFT) mà chưa submit → Đây là behavior đúng, admin không nhận notification.**

**Để admin nhận notification, seller phải:**
1. Tạo sản phẩm (DRAFT)
2. Thêm đầy đủ thông tin (images, SKUs, etc.)
3. **Submit sản phẩm** (`POST /api/v1/seller/products/{id}/submit`)

Sau khi submit, status chuyển sang `PENDING_REVIEW` và admin sẽ nhận notification.
