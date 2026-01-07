# API Test Guide - E-commerce Backend

Hướng dẫn test đầy đủ tất cả các chức năng API của hệ thống E-commerce Backend.

## Mục lục
- [1. Chuẩn bị](#1-chuẩn-bị)
- [2. Authentication & Authorization](#2-authentication--authorization)
- [3. User Profile & Address](#3-user-profile--address)
- [4. Catalog & Product Management](#4-catalog--product-management)
- [5. Shopping Cart & Checkout](#5-shopping-cart--checkout)
- [6. Orders & Payments](#6-orders--payments)
- [7. Reviews & Ratings](#7-reviews--ratings)
- [8. Wishlist](#8-wishlist)
- [9. Coupons & Discounts](#9-coupons--discounts)
- [10. Refunds](#10-refunds)
- [11. Recommendations](#11-recommendations)
- [12. Shop Management](#12-shop-management)
- [13. Admin Dashboard](#13-admin-dashboard)
- [14. Notifications](#14-notifications)
- [15. File Upload](#15-file-upload)

---

## 1. Chuẩn bị

### 1.1. Environment Setup
```bash
Base URL: http://localhost:8080
API Version: /api/v1
```

### 1.2. Authentication Header
Hầu hết các API yêu cầu JWT token:
```
Authorization: Bearer <your_access_token>
```

### 1.3. Test Data Requirements
- Email hợp lệ cho đăng ký
- Số điện thoại Việt Nam
- Địa chỉ đầy đủ (tỉnh, quận, phường)
- File ảnh (JPG/PNG) cho upload

---

## 2. Authentication & Authorization

### 2.1. Đăng ký tài khoản mới

**Endpoint:** `POST /api/v1/auth/register`

**Request:**
```json
{
  "email": "customer3@demo.local",
  "password": "Password123!",
  "fullName": "Nguyễn Văn An"
}
```

**Expected Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresInSeconds": 3600
}
```

**Test Cases:**
- ✅ Đăng ký thành công với thông tin hợp lệ
- ❌ Email đã tồn tại (Expect: 400/409)
- ❌ Email không hợp lệ (Expect: 400)
- ❌ Mật khẩu yếu (Expect: 400)
- ❌ Thiếu fullName (Expect: 400)

---

### 2.2. Đăng nhập

**Endpoint:** `POST /api/v1/auth/login`

**Request:**
```json
{
  "email": "customer3@demo.local",
  "password": "Password123!"
}
```

**Expected Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresInSeconds": 3600
}
```

**Test Cases:**
- ✅ Đăng nhập thành công
- ❌ Email không tồn tại (Expect: 401)
- ❌ Mật khẩu sai (Expect: 401)
- ❌ Thiếu email hoặc password (Expect: 400)

**Save for later tests:**
```javascript
// Lưu access token để dùng cho các request tiếp theo
const ACCESS_TOKEN = response.accessToken;
const REFRESH_TOKEN = response.refreshToken;
```

---

### 2.3. Refresh Token

**Endpoint:** `POST /api/v1/auth/refresh`

**Request:**
```json
{
  "refreshToken": "<refresh_token_from_login>"
}
```

**Expected Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresInSeconds": 3600
}
```

**Test Cases:**
- ✅ Refresh token thành công
- ❌ Refresh token không hợp lệ (Expect: 401)
- ❌ Refresh token đã hết hạn (Expect: 401)

---

### 2.4. Get Current User Info

**Endpoint:** `GET /api/v1/auth/me`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "userId": "5",
  "roles": ["CLIENT"]
}
```

**Test Cases:**
- ✅ Lấy thông tin user thành công
- ❌ Không có token (Expect: 401)
- ❌ Token không hợp lệ (Expect: 401)

---

### 2.5. Forgot Password

**Endpoint:** `POST /api/v1/auth/forgot-password`

**Request:**
```json
{
  "email": "testuser@example.com"
}
```

**Expected Response (200):**
```json
{
  "message": "Password reset email sent. Please check your inbox."
}
```

**Test Cases:**
- ✅ Gửi email reset password thành công
- ❌ Email không tồn tại (Expect: 404 hoặc 200 để tránh lộ thông tin)

---

### 2.6. Reset Password

**Endpoint:** `POST /api/v1/auth/reset-password`

**Request:**
```json
{
  "token": "<reset_token_from_email>",
  "newPassword": "NewPassword123!"
}
```

**Expected Response (200):**
```json
{
  "message": "Password has been reset successfully. Please login with your new password."
}
```

**Test Cases:**
- ✅ Reset password thành công
- ❌ Token không hợp lệ (Expect: 400)
- ❌ Token đã hết hạn (Expect: 400)
- ❌ Mật khẩu mới yếu (Expect: 400)

---

### 2.7. Logout

**Endpoint:** `POST /api/v1/auth/logout`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (204):**
(No content)

**Test Cases:**
- ✅ Logout thành công
- ❌ Không có token (Expect: 401)

---

## 3. User Profile & Address

### 3.1. Get User Profile

**Endpoint:** `GET /api/v1/users/me/profile`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "userId": "507f1f77bcf86cd799439011",
  "phone": "0901234567",
  "gender": "MALE",
  "dateOfBirth": "1990-01-01",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Test Cases:**
- ✅ Lấy profile thành công
- ❌ Không có token (Expect: 401)

---

### 3.2. Update User Profile

**Endpoint:** `PUT /api/v1/users/me/profile`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "phone": "0901234567",
  "gender": "MALE",
  "dateOfBirth": "1990-01-01"
}
```

**Expected Response (200):**
```json
{
  "userId": "507f1f77bcf86cd799439011",
  "phone": "0901234567",
  "gender": "MALE",
  "dateOfBirth": "1990-01-01",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Test Cases:**
- ✅ Update profile thành công
- ❌ Số điện thoại không hợp lệ (Expect: 400)
- ❌ Gender không hợp lệ (Expect: 400)
- ❌ DateOfBirth trong tương lai (Expect: 400)

---

### 3.3. Upload Avatar

**Endpoint:** `POST /api/v1/users/me/avatar`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <image_file.jpg>
```

**Expected Response (200):**
```json
{
  "userId": "507f1f77bcf86cd799439011",
  "phone": "0901234567",
  "gender": "MALE",
  "dateOfBirth": "1990-01-01",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**Test Cases:**
- ✅ Upload avatar thành công (JPG, PNG)
- ❌ File không phải ảnh (Expect: 400)
- ❌ File quá lớn (Expect: 413)
- ❌ Không có file (Expect: 400)

---

### 3.4. List User Addresses

**Endpoint:** `GET /api/v1/users/me/addresses`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "receiverName": "Nguyễn Văn A",
    "receiverPhone": "0901234567",
    "line1": "123 Đường ABC",
    "line2": "Căn hộ 456",
    "ward": "Phường 1",
    "district": "Quận 1",
    "province": "TP. Hồ Chí Minh",
    "postalCode": "700000",
    "isDefault": true
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách địa chỉ thành công
- ✅ Danh sách rỗng nếu chưa có địa chỉ

---

### 3.5. Create Address

**Endpoint:** `POST /api/v1/users/me/addresses`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "receiverName": "Nguyễn Văn A",
  "receiverPhone": "0901234567",
  "line1": "123 Đường ABC",
  "line2": "Căn hộ 456",
  "ward": "Phường 1",
  "district": "Quận 1",
  "province": "TP. Hồ Chí Minh",
  "postalCode": "700000"
}
```

**Expected Response (200):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "receiverName": "Nguyễn Văn A",
  "receiverPhone": "0901234567",
  "line1": "123 Đường ABC",
  "line2": "Căn hộ 456",
  "ward": "Phường 1",
  "district": "Quận 1",
  "province": "TP. Hồ Chí Minh",
  "postalCode": "700000",
  "isDefault": true
}
```

**Test Cases:**
- ✅ Tạo địa chỉ thành công
- ❌ Thiếu thông tin bắt buộc (Expect: 400)
- ❌ Số điện thoại không hợp lệ (Expect: 400)

**Save for later:**
```javascript
const ADDRESS_ID = response.id;
```

---

### 3.6. Update Address

**Endpoint:** `PUT /api/v1/users/me/addresses/{addressId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "receiverName": "Nguyễn Văn B",
  "receiverPhone": "0907654321",
  "line1": "456 Đường XYZ",
  "line2": "Tầng 7",
  "ward": "Phường 2",
  "district": "Quận 2",
  "province": "TP. Hồ Chí Minh",
  "postalCode": "700000"
}
```

**Expected Response (200):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "receiverName": "Nguyễn Văn B",
  "receiverPhone": "0907654321",
  "line1": "456 Đường XYZ",
  "line2": "Tầng 7",
  "ward": "Phường 2",
  "district": "Quận 2",
  "province": "TP. Hồ Chí Minh",
  "postalCode": "700000",
  "isDefault": true
}
```

**Test Cases:**
- ✅ Cập nhật địa chỉ thành công
- ❌ Address ID không tồn tại (Expect: 404)
- ❌ Cập nhật địa chỉ của user khác (Expect: 403)

---

### 3.7. Set Default Address

**Endpoint:** `POST /api/v1/users/me/addresses/{addressId}/default`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Default address updated successfully"
}
```

**Test Cases:**
- ✅ Đặt địa chỉ mặc định thành công
- ❌ Address ID không tồn tại (Expect: 404)

---

### 3.8. Delete Address

**Endpoint:** `DELETE /api/v1/users/me/addresses/{addressId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Address deleted successfully"
}
```

**Test Cases:**
- ✅ Xóa địa chỉ thành công
- ❌ Xóa địa chỉ mặc định cuối cùng (Expect: 400)
- ❌ Address ID không tồn tại (Expect: 404)

---

## 4. Catalog & Product Management

### 4.1. List Categories (Public)

**Endpoint:** `GET /api/v1/catalog/categories`

**Expected Response (200):**
```json
[
  {
    "id": "cat1",
    "name": "Điện thoại",
    "slug": "dien-thoai",
    "description": "Điện thoại di động",
    "imageUrl": "https://example.com/category.jpg",
    "parentId": null
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách categories thành công
- ✅ Endpoint public (không cần token)

---

### 4.2. List Brands (Public)

**Endpoint:** `GET /api/v1/catalog/brands`

**Expected Response (200):**
```json
[
  {
    "id": "brand1",
    "name": "Apple",
    "slug": "apple",
    "description": "Apple Inc.",
    "logoUrl": "https://example.com/apple-logo.jpg"
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách brands thành công
- ✅ Endpoint public

---

### 4.3. List Products (Public, Paginated)

**Endpoint:** `GET /api/v1/catalog/products?page=0&size=20&categoryId=cat1&brandId=brand1`

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 20)
- `categoryId` (optional)
- `brandId` (optional)

**Expected Response (200):**
```json
{
  "content": [
    {
      "id": "prod1",
      "name": "iPhone 15 Pro",
      "slug": "iphone-15-pro",
      "description": "Flagship smartphone",
      "categoryId": "cat1",
      "brandId": "brand1",
      "shopId": "shop1",
      "images": ["url1", "url2"],
      "basePrice": 29990000,
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

**Test Cases:**
- ✅ Lấy danh sách products thành công
- ✅ Filter theo categoryId
- ✅ Filter theo brandId
- ✅ Pagination hoạt động đúng
- ✅ Endpoint public

**Save for later:**
```javascript
const PRODUCT_ID = response.content[0].id;
```

---

### 4.4. Get Product Details (Public)

**Endpoint:** `GET /api/v1/catalog/products/{productId}`

**Expected Response (200):**
```json
{
  "id": "prod1",
  "name": "iPhone 15 Pro",
  "slug": "iphone-15-pro",
  "description": "Flagship smartphone with A17 Pro chip",
  "categoryId": "cat1",
  "brandId": "brand1",
  "shopId": "shop1",
  "images": ["url1", "url2"],
  "basePrice": 29990000,
  "options": [
    {
      "name": "Color",
      "values": ["Black", "White", "Blue"]
    },
    {
      "name": "Storage",
      "values": ["128GB", "256GB", "512GB"]
    }
  ],
  "skus": [
    {
      "id": "sku1",
      "sku": "IP15P-BK-128",
      "optionValues": ["Black", "128GB"],
      "price": 29990000,
      "stock": 50
    }
  ],
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Lấy chi tiết product thành công
- ❌ Product ID không tồn tại (Expect: 404)
- ✅ Endpoint public

---

### 4.5. Advanced Product Search (Public)

**Endpoint:** `GET /api/v1/search/products?q=iphone&minPrice=20000000&maxPrice=30000000&inStock=true&page=0&size=20`

**Query Parameters:**
- `q` (search query)
- `categoryId` (optional)
- `brandId` (optional)
- `shopId` (optional)
- `minPrice` (optional)
- `maxPrice` (optional)
- `inStock` (optional, boolean)
- `sort` (optional: "price_asc", "price_desc", "newest", "popular")
- `page` (default: 0)
- `size` (default: 20)

**Expected Response (200):**
```json
{
  "items": [
    {
      "id": "prod1",
      "name": "iPhone 15 Pro",
      "slug": "iphone-15-pro",
      "description": "Flagship smartphone",
      "categoryId": "cat1",
      "brandId": "brand1",
      "shopId": "shop1",
      "images": ["url1"],
      "minPrice": 29990000,
      "maxPrice": 35990000,
      "inStock": true,
      "score": 0.95
    }
  ],
  "total": 10,
  "page": 0,
  "size": 20,
  "totalPages": 1
}
```

**Test Cases:**
- ✅ Tìm kiếm theo từ khóa
- ✅ Filter theo khoảng giá
- ✅ Filter theo tồn kho
- ✅ Sort theo giá tăng/giảm
- ✅ Kết hợp nhiều filter
- ✅ Endpoint public

---

### 4.6. Get Search Suggestions (Public)

**Endpoint:** `GET /api/v1/search/suggestions?q=iph&limit=10`

**Expected Response (200):**
```json
[
  "iphone 15",
  "iphone 15 pro",
  "iphone 14",
  "iphone case"
]
```

**Test Cases:**
- ✅ Lấy suggestions thành công
- ✅ Limit suggestions
- ✅ Endpoint public

---

### 4.7. Get Popular Searches (Public)

**Endpoint:** `GET /api/v1/search/popular?limit=10`

**Expected Response (200):**
```json
[
  "iphone 15",
  "samsung galaxy s24",
  "airpods pro",
  "macbook air"
]
```

**Test Cases:**
- ✅ Lấy popular searches thành công
- ✅ Endpoint public

---

### 4.8. Create Product (Seller)

**Endpoint:** `POST /api/v1/seller/products`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request:**
```json
{
  "name": "New Product",
  "description": "Product description",
  "categoryId": "cat1",
  "brandId": "brand1",
  "shopId": "shop1",
  "basePrice": 1000000,
  "images": ["url1", "url2"]
}
```

**Expected Response (200):**
```json
{
  "id": "prod2",
  "name": "New Product",
  "slug": "new-product",
  "description": "Product description",
  "categoryId": "cat1",
  "brandId": "brand1",
  "shopId": "shop1",
  "images": ["url1", "url2"],
  "basePrice": 1000000,
  "status": "DRAFT",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Tạo product draft thành công (SELLER role)
- ❌ Không có SELLER role (Expect: 403)
- ❌ Category không tồn tại (Expect: 400/404)
- ❌ Thiếu thông tin bắt buộc (Expect: 400)

**Save for later:**
```javascript
const SELLER_PRODUCT_ID = response.id;
```

---

### 4.9. Update Product (Seller)

**Endpoint:** `PUT /api/v1/seller/products/{productId}`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request:**
```json
{
  "name": "Updated Product Name",
  "description": "Updated description",
  "categoryId": "cat1",
  "brandId": "brand1",
  "basePrice": 1200000
}
```

**Expected Response (200):**
```json
{
  "id": "prod2",
  "name": "Updated Product Name",
  "slug": "updated-product-name",
  "description": "Updated description",
  "categoryId": "cat1",
  "brandId": "brand1",
  "shopId": "shop1",
  "images": ["url1", "url2"],
  "basePrice": 1200000,
  "status": "DRAFT",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Update product thành công
- ❌ Update product của shop khác (Expect: 403)
- ❌ Product ID không tồn tại (Expect: 404)

---

### 4.10. Upload Product Images (Seller)

**Endpoint:** `POST /api/v1/seller/products/{productId}/images?sortOrder=1`

**Headers:**
```
Authorization: Bearer <seller_access_token>
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <image_file.jpg>
```

**Expected Response (200):**
```json
{
  "imageUrl": "https://example.com/product-image.jpg",
  "sortOrder": 1
}
```

**Test Cases:**
- ✅ Upload ảnh thành công
- ❌ File không phải ảnh (Expect: 400)
- ❌ File quá lớn (Expect: 413)

---

### 4.11. Set Product Options (Seller)

**Endpoint:** `PUT /api/v1/seller/products/{productId}/options`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request:**
```json
[
  {
    "name": "Color",
    "values": ["Red", "Blue", "Green"]
  },
  {
    "name": "Size",
    "values": ["S", "M", "L", "XL"]
  }
]
```

**Expected Response (200):**
```json
{
  "message": "Product options updated successfully"
}
```

**Test Cases:**
- ✅ Set options thành công
- ❌ Options trống (Expect: 400)
- ❌ Duplicate option names (Expect: 400)

---

### 4.12. Create/Update Product SKUs (Seller)

**Endpoint:** `PUT /api/v1/seller/products/{productId}/skus`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request:**
```json
[
  {
    "sku": "PROD-RED-S",
    "optionValues": ["Red", "S"],
    "price": 1000000,
    "stock": 100
  },
  {
    "sku": "PROD-BLUE-M",
    "optionValues": ["Blue", "M"],
    "price": 1200000,
    "stock": 50
  }
]
```

**Expected Response (200):**
```json
[
  {
    "id": "sku1",
    "sku": "PROD-RED-S",
    "optionValues": ["Red", "S"],
    "price": 1000000,
    "stock": 100
  },
  {
    "id": "sku2",
    "sku": "PROD-BLUE-M",
    "optionValues": ["Blue", "M"],
    "price": 1200000,
    "stock": 50
  }
]
```

**Test Cases:**
- ✅ Tạo SKUs thành công
- ❌ SKU code trùng (Expect: 400)
- ❌ Option values không khớp với options (Expect: 400)
- ❌ Giá hoặc stock âm (Expect: 400)

**Save for later:**
```javascript
const SKU_ID = response[0].id;
```

---

### 4.13. Submit Product for Review (Seller)

**Endpoint:** `POST /api/v1/seller/products/{productId}/submit`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "id": "prod2",
  "name": "Updated Product Name",
  "status": "PENDING_REVIEW",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Submit thành công
- ❌ Product chưa đầy đủ thông tin (Expect: 400)
- ❌ Product đã được submit (Expect: 400)

---

### 4.14. Approve Product (Admin)

**Endpoint:** `POST /api/v1/admin/products/{productId}/approve`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "id": "prod2",
  "name": "Updated Product Name",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Approve product thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Product không ở trạng thái PENDING_REVIEW (Expect: 400)

---

### 4.15. Reject Product (Admin)

**Endpoint:** `POST /api/v1/admin/products/{productId}/reject`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "id": "prod2",
  "name": "Updated Product Name",
  "status": "REJECTED",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Reject product thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)

---

### 4.16. Deactivate Product (Seller)

**Endpoint:** `POST /api/v1/seller/products/{productId}/deactivate`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "id": "prod2",
  "name": "Updated Product Name",
  "status": "INACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Deactivate thành công
- ❌ Deactivate product của shop khác (Expect: 403)

---

## 5. Shopping Cart & Checkout

### 5.1. Get Cart

**Endpoint:** `GET /api/v1/cart`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "userId": "user1",
  "items": [
    {
      "id": "item1",
      "productId": "prod1",
      "skuId": "sku1",
      "productName": "iPhone 15 Pro",
      "skuName": "Black - 128GB",
      "price": 29990000,
      "quantity": 2,
      "imageUrl": "https://example.com/image.jpg"
    }
  ],
  "totalItems": 2,
  "subtotal": 59980000
}
```

**Test Cases:**
- ✅ Lấy giỏ hàng thành công
- ✅ Giỏ hàng rỗng nếu chưa có sản phẩm

---

### 5.2. Add to Cart

**Endpoint:** `POST /api/v1/cart`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "productId": "prod1",
  "skuId": "sku1",
  "quantity": 2
}
```

**Expected Response (200):**
```json
{
  "message": "Item added to cart successfully"
}
```

**Test Cases:**
- ✅ Thêm sản phẩm vào giỏ thành công
- ✅ Tăng số lượng nếu sản phẩm đã có trong giỏ
- ❌ Product/SKU không tồn tại (Expect: 404)
- ❌ Quantity vượt quá stock (Expect: 400)
- ❌ Quantity <= 0 (Expect: 400)

**Save for later:**
```javascript
// Get cart to get item ID
const CART_ITEM_ID = "item1";
```

---

### 5.3. Update Cart Item Quantity

**Endpoint:** `PUT /api/v1/cart/items/{itemId}?quantity=5`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Cart item updated successfully"
}
```

**Test Cases:**
- ✅ Cập nhật số lượng thành công
- ❌ Quantity vượt quá stock (Expect: 400)
- ❌ Quantity <= 0 (Expect: 400)
- ❌ Item ID không tồn tại (Expect: 404)

---

### 5.4. Remove Cart Item

**Endpoint:** `DELETE /api/v1/cart/items/{itemId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Cart item removed successfully"
}
```

**Test Cases:**
- ✅ Xóa sản phẩm khỏi giỏ thành công
- ❌ Item ID không tồn tại (Expect: 404)

---

### 5.5. Checkout

**Endpoint:** `POST /api/v1/checkout`

**Headers:**
```
Authorization: Bearer <access_token>
Idempotency-Key: unique-key-123 (optional)
```

**Request:**
```json
{
  "addressId": "addr1",
  "paymentMethod": "VNPAY",
  "note": "Giao hàng giờ hành chính",
  "couponCode": "NEWYEAR2024"
}
```

**Expected Response (200):**
```json
[
  {
    "orderCode": "ORD-2024-001",
    "shopId": "shop1",
    "shopName": "Apple Store",
    "items": [
      {
        "productId": "prod1",
        "skuId": "sku1",
        "productName": "iPhone 15 Pro",
        "skuName": "Black - 128GB",
        "price": 29990000,
        "quantity": 2
      }
    ],
    "subtotal": 59980000,
    "shippingFee": 30000,
    "discount": 1000000,
    "total": 59010000,
    "status": "PENDING_PAYMENT",
    "shippingAddress": {
      "receiverName": "Nguyễn Văn A",
      "receiverPhone": "0901234567",
      "line1": "123 Đường ABC",
      "ward": "Phường 1",
      "district": "Quận 1",
      "province": "TP. Hồ Chí Minh"
    },
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

**Test Cases:**
- ✅ Checkout thành công
- ✅ Tạo nhiều orders nếu có sản phẩm từ nhiều shops
- ✅ Áp dụng coupon thành công
- ❌ Giỏ hàng rỗng (Expect: 400)
- ❌ Address không tồn tại (Expect: 404)
- ❌ Coupon không hợp lệ (Expect: 400)
- ❌ Sản phẩm hết hàng (Expect: 400)
- ✅ Idempotency key ngăn duplicate orders

**Save for later:**
```javascript
const ORDER_CODE = response[0].orderCode;
```

---

## 6. Orders & Payments

### 6.1. Get Order Details

**Endpoint:** `GET /api/v1/orders/{orderCode}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "orderCode": "ORD-2024-001",
  "shopId": "shop1",
  "shopName": "Apple Store",
  "items": [
    {
      "productId": "prod1",
      "skuId": "sku1",
      "productName": "iPhone 15 Pro",
      "skuName": "Black - 128GB",
      "price": 29990000,
      "quantity": 2,
      "imageUrl": "https://example.com/image.jpg"
    }
  ],
  "subtotal": 59980000,
  "shippingFee": 30000,
  "discount": 1000000,
  "total": 59010000,
  "status": "PENDING_PAYMENT",
  "paymentMethod": "VNPAY",
  "paymentStatus": "PENDING",
  "shippingAddress": {
    "receiverName": "Nguyễn Văn A",
    "receiverPhone": "0901234567",
    "line1": "123 Đường ABC",
    "ward": "Phường 1",
    "district": "Quận 1",
    "province": "TP. Hồ Chí Minh"
  },
  "note": "Giao hàng giờ hành chính",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Lấy chi tiết order thành công
- ❌ Order không tồn tại (Expect: 404)
- ❌ Xem order của user khác (Expect: 403)

---

### 6.2. Cancel Order

**Endpoint:** `POST /api/v1/orders/{orderCode}/cancel`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Order cancelled successfully"
}
```

**Test Cases:**
- ✅ Hủy order thành công (PENDING_PAYMENT, PENDING)
- ❌ Hủy order đã ship (Expect: 400)
- ❌ Hủy order của user khác (Expect: 403)
- ❌ Order không tồn tại (Expect: 404)

---

### 6.3. Create VNPay Payment

**Endpoint:** `POST /api/v1/payment/vnpay/create`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "orderCode": "ORD-2024-001"
}
```

**Expected Response (200):**
```json
{
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
  "orderCode": "ORD-2024-001",
  "amount": 59010000,
  "currency": "VND"
}
```

**Test Cases:**
- ✅ Tạo payment URL thành công
- ❌ Order không tồn tại (Expect: 404)
- ❌ Order đã thanh toán (Expect: 400)
- ❌ Order của user khác (Expect: 403)

**Manual Test:**
```
1. Copy paymentUrl vào browser
2. Thực hiện thanh toán trên sandbox VNPay
3. Verify callback được gọi
```

---

### 6.4. VNPay Callback

**Endpoint:** `GET /api/v1/payment/vnpay/callback?vnp_Amount=5901000000&vnp_BankCode=NCB&vnp_ResponseCode=00&...`

**Note:** Endpoint này được VNPay gọi sau khi thanh toán

**Expected Response (302):**
```
Redirect to: http://localhost:3000/payment/success?orderCode=ORD-2024-001
```

**Test Cases:**
- ✅ Callback thành công → Order status = PAID
- ✅ Callback thất bại → Order status không đổi
- ❌ Invalid signature (Expect: 400)

---

### 6.5. Get Payment Details

**Endpoint:** `GET /api/v1/payment/{orderCode}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "orderCode": "ORD-2024-001",
  "amount": 59010000,
  "paymentMethod": "VNPAY",
  "status": "COMPLETED",
  "transactionId": "VNP-20240101-123456",
  "paidAt": "2024-01-01T10:30:00Z"
}
```

**Test Cases:**
- ✅ Lấy thông tin payment thành công
- ❌ Order không tồn tại (Expect: 404)
- ❌ Xem payment của user khác (Expect: 403)

---

### 6.6. Get Shop Orders (Seller)

**Endpoint:** `GET /api/v1/seller/orders?shopId=shop1&page=0&size=20&sortBy=createdAt&sortDirection=DESC`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "content": [
    {
      "orderCode": "ORD-2024-001",
      "customerName": "Nguyễn Văn A",
      "total": 59010000,
      "status": "PAID",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

**Test Cases:**
- ✅ Lấy danh sách orders thành công
- ✅ Pagination hoạt động đúng
- ✅ Sort hoạt động đúng
- ❌ Xem orders của shop khác (Expect: 403)

---

### 6.7. Get Orders by Status (Seller)

**Endpoint:** `GET /api/v1/seller/orders/status/PAID?shopId=shop1&page=0&size=20`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "content": [
    {
      "orderCode": "ORD-2024-001",
      "customerName": "Nguyễn Văn A",
      "total": 59010000,
      "status": "PAID",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

**Test Cases:**
- ✅ Filter theo status thành công
- ✅ Các status: PENDING_PAYMENT, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED

---

### 6.8. Get Order Detail (Seller)

**Endpoint:** `GET /api/v1/seller/orders/{orderId}?shopId=shop1`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "orderCode": "ORD-2024-001",
  "customerId": "user1",
  "customerName": "Nguyễn Văn A",
  "items": [
    {
      "productId": "prod1",
      "skuId": "sku1",
      "productName": "iPhone 15 Pro",
      "skuName": "Black - 128GB",
      "price": 29990000,
      "quantity": 2
    }
  ],
  "subtotal": 59980000,
  "shippingFee": 30000,
  "discount": 1000000,
  "total": 59010000,
  "status": "PAID",
  "paymentMethod": "VNPAY",
  "shippingAddress": {
    "receiverName": "Nguyễn Văn A",
    "receiverPhone": "0901234567",
    "line1": "123 Đường ABC",
    "ward": "Phường 1",
    "district": "Quận 1",
    "province": "TP. Hồ Chí Minh"
  },
  "note": "Giao hàng giờ hành chính",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Lấy chi tiết order thành công
- ❌ Order không thuộc shop (Expect: 403/404)

---

### 6.9. Update Order Status (Seller)

**Endpoint:** `PUT /api/v1/seller/orders/{orderId}/status?shopId=shop1`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request:**
```json
{
  "status": "PROCESSING",
  "note": "Đơn hàng đang được chuẩn bị"
}
```

**Expected Response (200):**
```json
{
  "orderCode": "ORD-2024-001",
  "status": "PROCESSING",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

**Test Cases:**
- ✅ Update status thành công
- ✅ Flow: PAID → PROCESSING → SHIPPED → DELIVERED
- ❌ Update status không hợp lệ (VD: PAID → DELIVERED) (Expect: 400)
- ❌ Update order của shop khác (Expect: 403)

---

### 6.10. Get Order Stats (Seller)

**Endpoint:** `GET /api/v1/seller/orders/stats?shopId=shop1`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "totalOrders": 150,
  "pendingOrders": 10,
  "processingOrders": 25,
  "shippedOrders": 30,
  "deliveredOrders": 80,
  "cancelledOrders": 5,
  "totalRevenue": 500000000,
  "thisMonthRevenue": 50000000
}
```

**Test Cases:**
- ✅ Lấy thống kê thành công
- ❌ Stats của shop khác (Expect: 403)

---

### 6.11. Cancel Order (Seller)

**Endpoint:** `POST /api/v1/seller/orders/{orderId}/cancel?shopId=shop1&reason=Out of stock`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "message": "Order cancelled successfully"
}
```

**Test Cases:**
- ✅ Hủy order thành công
- ❌ Hủy order đã ship (Expect: 400)
- ❌ Hủy order của shop khác (Expect: 403)

---

## 7. Reviews & Ratings

### 7.1. Create Product Review

**Endpoint:** `POST /api/v1/products/{productId}/reviews`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "rating": 5,
  "comment": "Sản phẩm rất tốt, giao hàng nhanh!",
  "images": [
    "https://example.com/review-img1.jpg",
    "https://example.com/review-img2.jpg"
  ]
}
```

**Expected Response (200):**
```json
{
  "id": 1,
  "productId": 1,
  "userId": 5,
  "orderId": 10,
  "rating": 5,
  "comment": "Sản phẩm rất tốt, giao hàng nhanh!",
  "images": [
    "https://example.com/review-img1.jpg",
    "https://example.com/review-img2.jpg"
  ],
  "status": "PENDING",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Tạo review thành công
- ❌ Chưa mua sản phẩm (Expect: 400/403)
- ❌ Rating không hợp lệ (<1 hoặc >5) (Expect: 400)
- ❌ Comment quá dài (Expect: 400)
- ❌ Đã review sản phẩm này rồi (Expect: 400)

**Save for later:**
```javascript
const REVIEW_ID = response.id;
```

---

### 7.2. List Product Reviews (Public)

**Endpoint:** `GET /api/v1/products/{productId}/reviews?rating=5`

**Query Parameters:**
- `rating` (optional): Filter theo số sao (1-5)

**Expected Response (200):**
```json
[
  {
    "id": 1,
    "productId": 1,
    "userId": 5,
    "orderId": 10,
    "rating": 5,
    "comment": "Sản phẩm rất tốt, giao hàng nhanh!",
    "images": ["https://example.com/review-img1.jpg"],
    "status": "APPROVED",
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách reviews thành công
- ✅ Filter theo rating
- ✅ Chỉ hiện reviews APPROVED
- ✅ Endpoint public

---

### 7.3. Update Review

**Endpoint:** `PUT /api/v1/reviews/{reviewId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "rating": 4,
  "comment": "Cập nhật đánh giá sau 1 tháng sử dụng",
  "images": ["https://example.com/new-review-img.jpg"]
}
```

**Expected Response (200):**
```json
{
  "id": "review1",
  "productId": "prod1",
  "userId": "user1",
  "userName": "Nguyễn Văn A",
  "rating": 4,
  "comment": "Cập nhật đánh giá sau 1 tháng sử dụng",
  "images": ["https://example.com/new-review-img.jpg"],
  "status": "PENDING",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-02-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Update review thành công
- ❌ Update review của user khác (Expect: 403)
- ❌ Review ID không tồn tại (Expect: 404)

---

### 7.4. Delete Review

**Endpoint:** `DELETE /api/v1/reviews/{reviewId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Review deleted successfully"
}
```

**Test Cases:**
- ✅ Xóa review thành công
- ❌ Xóa review của user khác (Expect: 403)
- ❌ Review ID không tồn tại (Expect: 404)

---

### 7.5. List Reviews by Status (Admin)

**Endpoint:** `GET /api/v1/admin/reviews?status=PENDING`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": 1,
    "productId": 1,
    "userId": 5,
    "orderId": 10,
    "rating": 5,
    "comment": "Sản phẩm rất tốt!",
    "images": [],
    "status": "PENDING",
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách reviews thành công (ADMIN role)
- ✅ Filter theo status: PENDING, APPROVED, REJECTED
- ❌ Không có ADMIN role (Expect: 403)

---

### 7.6. Approve Review (Admin)

**Endpoint:** `POST /api/v1/admin/reviews/{reviewId}/approve`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "id": "review1",
  "status": "APPROVED",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Approve review thành công
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Review không ở trạng thái PENDING (Expect: 400)

---

### 7.7. Reject Review (Admin)

**Endpoint:** `POST /api/v1/admin/reviews/{reviewId}/reject`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "id": "review1",
  "status": "REJECTED",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Reject review thành công
- ❌ Không có ADMIN role (Expect: 403)

---

## 8. Wishlist

### 8.1. Add to Wishlist

**Endpoint:** `POST /api/v1/wishlist`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "productId": "prod1",
  "note": "Mua khi có khuyến mãi"
}
```

**Expected Response (200):**
```json
{
  "id": 1,
  "productId": 1,
  "productName": "iPhone 15 Pro",
  "productSlug": "iphone-15-pro",
  "mainImageUrl": "https://example.com/image.jpg",
  "minPrice": 29990000,
  "status": "ACTIVE",
  "note": "Mua khi có khuyến mãi",
  "addedAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Thêm vào wishlist thành công
- ❌ Product đã có trong wishlist (Expect: 400)
- ❌ Product không tồn tại (Expect: 404)

**Save for later:**
```javascript
const WISHLIST_PRODUCT_ID = response.productId;
```

---

### 8.2. Get All Wishlist Items

**Endpoint:** `GET /api/v1/wishlist`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "wish1",
    "productId": "prod1",
    "productName": "iPhone 15 Pro",
    "productImage": "https://example.com/image.jpg",
    "price": 29990000,
    "inStock": true,
    "note": "Mua khi có khuyến mãi",
    "addedAt": "2024-01-01T00:00:00Z"
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách wishlist thành công
- ✅ Danh sách rỗng nếu chưa có item

---

### 8.3. Check Product in Wishlist

**Endpoint:** `GET /api/v1/wishlist/check/{productId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "inWishlist": true
}
```

**Test Cases:**
- ✅ Check thành công (true/false)
- ❌ Product không tồn tại (Expect: 404)

---

### 8.4. Get Wishlist Count

**Endpoint:** `GET /api/v1/wishlist/count`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "count": 5
}
```

**Test Cases:**
- ✅ Lấy số lượng wishlist thành công

---

### 8.5. Remove from Wishlist

**Endpoint:** `DELETE /api/v1/wishlist/{productId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Item removed from wishlist successfully"
}
```

**Test Cases:**
- ✅ Xóa khỏi wishlist thành công
- ❌ Product không có trong wishlist (Expect: 404)

---

### 8.6. Clear Wishlist

**Endpoint:** `DELETE /api/v1/wishlist`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Wishlist cleared successfully"
}
```

**Test Cases:**
- ✅ Xóa toàn bộ wishlist thành công

---

## 9. Coupons & Discounts

### 9.1. Validate Coupon

**Endpoint:** `POST /api/v1/coupons/validate`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "couponCode": "NEWYEAR2024",
  "orderTotal": 5000000,
  "productIds": ["prod1", "prod2"],
  "categoryIds": ["cat1"]
}
```

**Expected Response (200):**
```json
{
  "valid": true,
  "couponCode": "NEWYEAR2024",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "discountAmount": 500000,
  "maxDiscount": 1000000,
  "message": "Coupon applied successfully"
}
```

**Test Cases:**
- ✅ Validate coupon thành công
- ❌ Coupon không tồn tại (valid: false)
- ❌ Coupon đã hết hạn (valid: false)
- ❌ Đơn hàng chưa đạt min order value (valid: false)
- ❌ Sản phẩm không áp dụng được (valid: false)
- ❌ Đã hết lượt sử dụng (valid: false)

---

### 9.2. Get Active Coupons

**Endpoint:** `GET /api/v1/coupons/active`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "coupon1",
    "code": "NEWYEAR2024",
    "description": "Giảm 10% cho đơn hàng từ 5 triệu",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "maxDiscount": 1000000,
    "minOrderValue": 5000000,
    "startDate": "2024-01-01T00:00:00Z",
    "endDate": "2024-01-31T23:59:59Z",
    "usageLimit": 1000,
    "usedCount": 250
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách coupons active thành công
- ✅ Chỉ hiện coupons còn hạn

---

### 9.3. Get Coupon by Code

**Endpoint:** `GET /api/v1/coupons/{code}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "id": "coupon1",
  "code": "NEWYEAR2024",
  "description": "Giảm 10% cho đơn hàng từ 5 triệu",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "maxDiscount": 1000000,
  "minOrderValue": 5000000,
  "applicableProducts": ["prod1", "prod2"],
  "applicableCategories": ["cat1"],
  "startDate": "2024-01-01T00:00:00Z",
  "endDate": "2024-01-31T23:59:59Z",
  "usageLimit": 1000,
  "usedCount": 250
}
```

**Test Cases:**
- ✅ Lấy thông tin coupon thành công
- ❌ Coupon không tồn tại (Expect: 404)

---

### 9.4. Create Coupon (Admin)

**Endpoint:** `POST /api/v1/admin/coupons`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Request:**
```json
{
  "code": "SUMMER2024",
  "description": "Giảm 200k cho đơn từ 2 triệu",
  "discountType": "FIXED_AMOUNT",
  "discountValue": 200000,
  "minOrderValue": 2000000,
  "applicableProducts": [],
  "applicableCategories": [],
  "startDate": "2024-06-01T00:00:00Z",
  "endDate": "2024-08-31T23:59:59Z",
  "usageLimit": 500,
  "perUserLimit": 1
}
```

**Expected Response (200):**
```json
{
  "id": "coupon2",
  "code": "SUMMER2024",
  "description": "Giảm 200k cho đơn từ 2 triệu",
  "discountType": "FIXED_AMOUNT",
  "discountValue": 200000,
  "minOrderValue": 2000000,
  "startDate": "2024-06-01T00:00:00Z",
  "endDate": "2024-08-31T23:59:59Z",
  "usageLimit": 500,
  "usedCount": 0,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Tạo coupon thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Code đã tồn tại (Expect: 400)
- ❌ startDate > endDate (Expect: 400)
- ❌ discountValue <= 0 (Expect: 400)

**Save for later:**
```javascript
const COUPON_ID = response.id;
```

---

### 9.5. Update Coupon (Admin)

**Endpoint:** `PUT /api/v1/admin/coupons/{couponId}`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Request:**
```json
{
  "description": "Giảm 250k cho đơn từ 2 triệu (Updated)",
  "discountValue": 250000,
  "usageLimit": 1000
}
```

**Expected Response (200):**
```json
{
  "id": "coupon2",
  "code": "SUMMER2024",
  "description": "Giảm 250k cho đơn từ 2 triệu (Updated)",
  "discountType": "FIXED_AMOUNT",
  "discountValue": 250000,
  "usageLimit": 1000,
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Update coupon thành công
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Coupon ID không tồn tại (Expect: 404)

---

### 9.6. Delete Coupon (Admin)

**Endpoint:** `DELETE /api/v1/admin/coupons/{couponId}`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "message": "Coupon deleted successfully"
}
```

**Test Cases:**
- ✅ Xóa coupon thành công
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Coupon đã được sử dụng (Expect: 400)

---

## 10. Refunds

### 10.1. Create Refund Request (Customer)

**Endpoint:** `POST /api/v1/customer/refunds`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "orderCode": "ORD-2024-001",
  "reason": "DEFECTIVE",
  "description": "Sản phẩm bị lỗi màn hình",
  "images": [
    "https://example.com/defect1.jpg",
    "https://example.com/defect2.jpg"
  ],
  "requestedAmount": 29990000
}
```

**Expected Response (200):**
```json
{
  "id": "refund1",
  "refundCode": "RF-2024-001",
  "orderCode": "ORD-2024-001",
  "userId": "user1",
  "shopId": "shop1",
  "reason": "DEFECTIVE",
  "description": "Sản phẩm bị lỗi màn hình",
  "images": ["https://example.com/defect1.jpg"],
  "requestedAmount": 29990000,
  "approvedAmount": 0,
  "status": "PENDING",
  "createdAt": "2024-01-05T00:00:00Z"
}
```

**Test Cases:**
- ✅ Tạo refund request thành công
- ❌ Order không tồn tại (Expect: 404)
- ❌ Order chưa delivered (Expect: 400)
- ❌ Đã quá thời hạn đổi trả (Expect: 400)
- ❌ Đã tạo refund cho order này (Expect: 400)

**Save for later:**
```javascript
const REFUND_ID = response.id;
const REFUND_CODE = response.refundCode;
```

---

### 10.2. Get Customer Refunds (Paginated)

**Endpoint:** `GET /api/v1/customer/refunds?page=0&size=20&sortBy=createdAt&sortDirection=DESC`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "content": [
    {
      "id": "refund1",
      "refundCode": "RF-2024-001",
      "orderCode": "ORD-2024-001",
      "reason": "DEFECTIVE",
      "requestedAmount": 29990000,
      "status": "PENDING",
      "createdAt": "2024-01-05T00:00:00Z"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Test Cases:**
- ✅ Lấy danh sách refunds thành công
- ✅ Pagination hoạt động đúng

---

### 10.3. Get Refund Detail (Customer)

**Endpoint:** `GET /api/v1/customer/refunds/{refundId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "id": "refund1",
  "refundCode": "RF-2024-001",
  "orderCode": "ORD-2024-001",
  "userId": "user1",
  "shopId": "shop1",
  "shopName": "Apple Store",
  "reason": "DEFECTIVE",
  "description": "Sản phẩm bị lỗi màn hình",
  "images": ["https://example.com/defect1.jpg"],
  "requestedAmount": 29990000,
  "approvedAmount": 29990000,
  "status": "APPROVED",
  "sellerNote": "Đã xác nhận sản phẩm lỗi",
  "createdAt": "2024-01-05T00:00:00Z",
  "updatedAt": "2024-01-06T00:00:00Z"
}
```

**Test Cases:**
- ✅ Lấy chi tiết refund thành công
- ❌ Refund ID không tồn tại (Expect: 404)
- ❌ Xem refund của user khác (Expect: 403)

---

### 10.4. Cancel Refund Request (Customer)

**Endpoint:** `POST /api/v1/customer/refunds/{refundId}/cancel`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Refund request cancelled successfully"
}
```

**Test Cases:**
- ✅ Hủy refund request thành công (PENDING)
- ❌ Hủy refund đã processed (Expect: 400)
- ❌ Refund của user khác (Expect: 403)

---

### 10.5. Get Shop Refunds (Seller)

**Endpoint:** `GET /api/v1/seller/refunds?shopId=shop1&page=0&size=20&sortBy=createdAt&sortDirection=DESC`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "content": [
    {
      "id": "refund1",
      "refundCode": "RF-2024-001",
      "orderCode": "ORD-2024-001",
      "customerName": "Nguyễn Văn A",
      "reason": "DEFECTIVE",
      "requestedAmount": 29990000,
      "status": "PENDING",
      "createdAt": "2024-01-05T00:00:00Z"
    }
  ],
  "totalElements": 20,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Test Cases:**
- ✅ Lấy danh sách refunds thành công
- ❌ Xem refunds của shop khác (Expect: 403)

---

### 10.6. Get Refunds by Status (Seller)

**Endpoint:** `GET /api/v1/seller/refunds/status/PENDING?shopId=shop1&page=0&size=20`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "content": [
    {
      "id": "refund1",
      "refundCode": "RF-2024-001",
      "orderCode": "ORD-2024-001",
      "customerName": "Nguyễn Văn A",
      "reason": "DEFECTIVE",
      "requestedAmount": 29990000,
      "status": "PENDING",
      "createdAt": "2024-01-05T00:00:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Test Cases:**
- ✅ Filter theo status thành công
- ✅ Statuses: PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED

---

### 10.7. Get Refund Detail (Seller)

**Endpoint:** `GET /api/v1/seller/refunds/{refundId}?shopId=shop1`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "id": "refund1",
  "refundCode": "RF-2024-001",
  "orderCode": "ORD-2024-001",
  "userId": "user1",
  "customerName": "Nguyễn Văn A",
  "customerPhone": "0901234567",
  "reason": "DEFECTIVE",
  "description": "Sản phẩm bị lỗi màn hình",
  "images": ["https://example.com/defect1.jpg"],
  "requestedAmount": 29990000,
  "status": "PENDING",
  "createdAt": "2024-01-05T00:00:00Z"
}
```

**Test Cases:**
- ✅ Lấy chi tiết refund thành công
- ❌ Refund không thuộc shop (Expect: 403/404)

---

### 10.8. Process Refund (Seller)

**Endpoint:** `PUT /api/v1/seller/refunds/{refundId}/process?shopId=shop1`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request (Approve):**
```json
{
  "action": "APPROVE",
  "approvedAmount": 29990000,
  "note": "Đã xác nhận sản phẩm lỗi, chấp nhận hoàn tiền"
}
```

**Request (Reject):**
```json
{
  "action": "REJECT",
  "note": "Sản phẩm không có dấu hiệu lỗi kỹ thuật"
}
```

**Expected Response (200):**
```json
{
  "id": "refund1",
  "refundCode": "RF-2024-001",
  "status": "APPROVED",
  "approvedAmount": 29990000,
  "sellerNote": "Đã xác nhận sản phẩm lỗi, chấp nhận hoàn tiền",
  "updatedAt": "2024-01-06T00:00:00Z"
}
```

**Test Cases:**
- ✅ Approve refund thành công
- ✅ Reject refund thành công
- ❌ Approved amount > requested amount (Expect: 400)
- ❌ Process refund của shop khác (Expect: 403)
- ❌ Refund không ở trạng thái PENDING (Expect: 400)

---

## 11. Recommendations

### 11.1. Track User Interaction Event

**Endpoint:** `POST /api/v1/recommendations/events?productId=prod1&eventType=VIEW`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `productId`: Product ID
- `eventType`: VIEW, ADD_TO_CART, PURCHASE

**Expected Response (200):**
```json
{
  "message": "Event tracked successfully"
}
```

**Test Cases:**
- ✅ Track VIEW event
- ✅ Track ADD_TO_CART event
- ✅ Track PURCHASE event
- ❌ Product không tồn tại (Expect: 404)

---

### 11.2. Get Trending Products

**Endpoint:** `GET /api/v1/recommendations/trending`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "prod1",
    "name": "iPhone 15 Pro",
    "slug": "iphone-15-pro",
    "images": ["url1"],
    "basePrice": 29990000,
    "viewCount": 1500,
    "purchaseCount": 250
  }
]
```

**Test Cases:**
- ✅ Lấy trending products thành công
- ✅ Sắp xếp theo view count / purchase count

---

### 11.3. Get Personalized Recommendations

**Endpoint:** `GET /api/v1/recommendations/personalized?limit=10`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "prod5",
    "name": "AirPods Pro",
    "slug": "airpods-pro",
    "images": ["url1"],
    "basePrice": 5990000,
    "recommendationScore": 0.85
  }
]
```

**Test Cases:**
- ✅ Lấy personalized recommendations thành công
- ✅ Dựa trên lịch sử xem/mua của user
- ✅ Limit số lượng kết quả

---

### 11.4. Get Similar Products

**Endpoint:** `GET /api/v1/recommendations/similar/{productId}?limit=6`

**Expected Response (200):**
```json
[
  {
    "id": "prod2",
    "name": "iPhone 15",
    "slug": "iphone-15",
    "images": ["url1"],
    "basePrice": 24990000,
    "similarityScore": 0.92
  }
]
```

**Test Cases:**
- ✅ Lấy similar products thành công
- ✅ Dựa trên category, brand, price range
- ✅ Endpoint public
- ❌ Product không tồn tại (Expect: 404)

---

### 11.5. Get Frequently Bought Together

**Endpoint:** `GET /api/v1/recommendations/bought-together/{productId}?limit=4`

**Expected Response (200):**
```json
[
  {
    "id": "prod10",
    "name": "iPhone Case",
    "slug": "iphone-case",
    "images": ["url1"],
    "basePrice": 299000,
    "boughtTogetherCount": 180
  }
]
```

**Test Cases:**
- ✅ Lấy frequently bought together products
- ✅ Dựa trên order history
- ✅ Endpoint public

---

## 12. Shop Management

### 12.1. Get Seller's Shop

**Endpoint:** `GET /api/v1/seller/shop`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "id": "shop1",
  "sellerId": "user2",
  "shopName": "Apple Store Official",
  "slug": "apple-store-official",
  "description": "Cửa hàng chính hãng Apple",
  "logoUrl": "https://example.com/shop-logo.jpg",
  "bannerUrl": "https://example.com/shop-banner.jpg",
  "status": "ACTIVE",
  "rating": 4.8,
  "totalProducts": 150,
  "totalOrders": 5000,
  "createdAt": "2023-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Lấy thông tin shop thành công (SELLER role)
- ❌ Không có SELLER role (Expect: 403)
- ❌ Chưa tạo shop (Expect: 404)

**Save for later:**
```javascript
const SHOP_ID = response.id;
```

---

### 12.2. Create Shop

**Endpoint:** `POST /api/v1/seller/shop`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request:**
```json
{
  "shopName": "My New Shop",
  "description": "Chuyên cung cấp điện thoại chính hãng"
}
```

**Expected Response (200):**
```json
{
  "id": "shop2",
  "sellerId": "user3",
  "shopName": "My New Shop",
  "slug": "my-new-shop",
  "description": "Chuyên cung cấp điện thoại chính hãng",
  "status": "DRAFT",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Test Cases:**
- ✅ Tạo shop thành công (SELLER role)
- ❌ Không có SELLER role (Expect: 403)
- ❌ Đã có shop rồi (Expect: 400)
- ❌ Tên shop trùng (Expect: 400)

---

### 12.3. Update Shop

**Endpoint:** `PUT /api/v1/seller/shop`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Request:**
```json
{
  "shopName": "My Updated Shop Name",
  "description": "Cập nhật mô tả shop"
}
```

**Expected Response (200):**
```json
{
  "id": "shop2",
  "sellerId": "user3",
  "shopName": "My Updated Shop Name",
  "slug": "my-updated-shop-name",
  "description": "Cập nhật mô tả shop",
  "status": "DRAFT",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Update shop thành công
- ❌ Không có shop (Expect: 404)

---

### 12.4. Upload Shop Logo

**Endpoint:** `POST /api/v1/seller/shop/logo`

**Headers:**
```
Authorization: Bearer <seller_access_token>
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <logo_file.jpg>
```

**Expected Response (200):**
```json
{
  "id": "shop2",
  "shopName": "My Updated Shop Name",
  "logoUrl": "https://example.com/shop-logo-new.jpg",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Upload logo thành công
- ❌ File không phải ảnh (Expect: 400)
- ❌ File quá lớn (Expect: 413)

---

### 12.5. Upload Shop Banner

**Endpoint:** `POST /api/v1/seller/shop/banner`

**Headers:**
```
Authorization: Bearer <seller_access_token>
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <banner_file.jpg>
```

**Expected Response (200):**
```json
{
  "id": "shop2",
  "shopName": "My Updated Shop Name",
  "bannerUrl": "https://example.com/shop-banner-new.jpg",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Upload banner thành công
- ❌ File không phải ảnh (Expect: 400)

---

### 12.6. Submit Shop for Review

**Endpoint:** `POST /api/v1/seller/shop/submit`

**Headers:**
```
Authorization: Bearer <seller_access_token>
```

**Expected Response (200):**
```json
{
  "id": "shop2",
  "shopName": "My Updated Shop Name",
  "status": "PENDING_REVIEW",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

**Test Cases:**
- ✅ Submit shop thành công
- ❌ Shop chưa đầy đủ thông tin (Expect: 400)
- ❌ Shop đã được submit (Expect: 400)

---

### 12.7. List Shops by Status (Admin)

**Endpoint:** `GET /api/v1/admin/shops?status=PENDING_REVIEW`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "shop2",
    "sellerId": "user3",
    "shopName": "My Updated Shop Name",
    "description": "Cập nhật mô tả shop",
    "status": "PENDING_REVIEW",
    "createdAt": "2024-01-01T00:00:00Z",
    "submittedAt": "2024-01-02T00:00:00Z"
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách shops thành công (ADMIN role)
- ✅ Filter theo status: DRAFT, PENDING_REVIEW, ACTIVE, SUSPENDED
- ❌ Không có ADMIN role (Expect: 403)

---

### 12.8. Approve Shop (Admin)

**Endpoint:** `POST /api/v1/admin/shops/{shopId}/approve`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "id": "shop2",
  "shopName": "My Updated Shop Name",
  "status": "ACTIVE",
  "approvedAt": "2024-01-03T00:00:00Z"
}
```

**Test Cases:**
- ✅ Approve shop thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Shop không ở trạng thái PENDING_REVIEW (Expect: 400)

---

### 12.9. Suspend Shop (Admin)

**Endpoint:** `POST /api/v1/admin/shops/{shopId}/suspend`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Request:**
```json
{
  "reason": "Vi phạm chính sách bán hàng"
}
```

**Expected Response (200):**
```json
{
  "id": "shop1",
  "shopName": "Apple Store Official",
  "status": "SUSPENDED",
  "suspendReason": "Vi phạm chính sách bán hàng",
  "suspendedAt": "2024-01-03T00:00:00Z"
}
```

**Test Cases:**
- ✅ Suspend shop thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Thiếu lý do suspend (Expect: 400)

---

## 13. Admin Dashboard

### 13.1. Get Dashboard Stats

**Endpoint:** `GET /api/v1/admin/dashboard/stats`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "totalUsers": 10000,
  "newUsersToday": 50,
  "totalShops": 500,
  "activeShops": 450,
  "totalProducts": 50000,
  "activeProducts": 45000,
  "totalOrders": 100000,
  "ordersToday": 200,
  "totalRevenue": 10000000000,
  "revenueToday": 50000000,
  "pendingReviews": 25,
  "pendingRefunds": 10
}
```

**Test Cases:**
- ✅ Lấy dashboard stats thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)

---

### 13.2. Get User Analytics

**Endpoint:** `GET /api/v1/admin/dashboard/analytics/users`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "totalUsers": 10000,
  "activeUsers": 8500,
  "newUsersLast30Days": 1200,
  "usersByRole": {
    "USER": 9000,
    "SELLER": 900,
    "ADMIN": 100
  },
  "userGrowthChart": [
    {
      "date": "2024-01-01",
      "count": 100
    },
    {
      "date": "2024-01-02",
      "count": 150
    }
  ]
}
```

**Test Cases:**
- ✅ Lấy user analytics thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)

---

### 13.3. Get Sales Analytics

**Endpoint:** `GET /api/v1/admin/dashboard/analytics/sales`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "totalOrders": 100000,
  "totalRevenue": 10000000000,
  "averageOrderValue": 100000,
  "ordersByStatus": {
    "PENDING_PAYMENT": 100,
    "PAID": 200,
    "PROCESSING": 150,
    "SHIPPED": 300,
    "DELIVERED": 99000,
    "CANCELLED": 250
  },
  "revenueChart": [
    {
      "date": "2024-01-01",
      "revenue": 50000000
    },
    {
      "date": "2024-01-02",
      "revenue": 60000000
    }
  ],
  "topCategories": [
    {
      "categoryId": "cat1",
      "categoryName": "Điện thoại",
      "revenue": 5000000000
    }
  ],
  "topProducts": [
    {
      "productId": "prod1",
      "productName": "iPhone 15 Pro",
      "soldCount": 5000,
      "revenue": 150000000000
    }
  ]
}
```

**Test Cases:**
- ✅ Lấy sales analytics thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)

---

### 13.4. List All Users (Admin)

**Endpoint:** `GET /api/v1/admin/users`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "user1",
    "email": "testuser@example.com",
    "fullName": "Nguyễn Văn A",
    "roles": ["USER"],
    "status": "ACTIVE",
    "createdAt": "2023-01-01T00:00:00Z"
  }
]
```

**Test Cases:**
- ✅ Lấy danh sách users thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)

---

### 13.5. Disable User (Admin)

**Endpoint:** `POST /api/v1/admin/users/{userId}/disable`

**Headers:**
```
Authorization: Bearer <admin_access_token>
```

**Expected Response (200):**
```json
{
  "message": "User disabled successfully"
}
```

**Test Cases:**
- ✅ Disable user thành công (ADMIN role)
- ❌ Không có ADMIN role (Expect: 403)
- ❌ Disable chính mình (Expect: 400)

---

## 14. Notifications

### 14.1. Get Notifications (Paginated)

**Endpoint:** `GET /api/v1/notifications?page=0&size=20&sortBy=createdAt&sortDirection=DESC`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "content": [
    {
      "id": "notif1",
      "userId": "user1",
      "type": "ORDER_STATUS",
      "title": "Đơn hàng đã được giao",
      "message": "Đơn hàng ORD-2024-001 đã được giao thành công",
      "data": {
        "orderCode": "ORD-2024-001"
      },
      "isRead": false,
      "createdAt": "2024-01-05T10:00:00Z"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

**Test Cases:**
- ✅ Lấy notifications thành công
- ✅ Pagination hoạt động đúng

**Save for later:**
```javascript
const NOTIFICATION_ID = response.content[0].id;
```

---

### 14.2. Get Unread Notifications

**Endpoint:** `GET /api/v1/notifications/unread`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
[
  {
    "id": "notif1",
    "type": "ORDER_STATUS",
    "title": "Đơn hàng đã được giao",
    "message": "Đơn hàng ORD-2024-001 đã được giao thành công",
    "isRead": false,
    "createdAt": "2024-01-05T10:00:00Z"
  }
]
```

**Test Cases:**
- ✅ Lấy unread notifications thành công
- ✅ Chỉ hiện notifications chưa đọc

---

### 14.3. Get Unread Count

**Endpoint:** `GET /api/v1/notifications/unread/count`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "count": 5
}
```

**Test Cases:**
- ✅ Lấy unread count thành công

---

### 14.4. Mark Notification as Read

**Endpoint:** `PUT /api/v1/notifications/{notificationId}/read`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Notification marked as read"
}
```

**Test Cases:**
- ✅ Mark as read thành công
- ❌ Notification không tồn tại (Expect: 404)
- ❌ Mark notification của user khác (Expect: 403)

---

### 14.5. Mark All as Read

**Endpoint:** `PUT /api/v1/notifications/read-all`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "All notifications marked as read"
}
```

**Test Cases:**
- ✅ Mark all as read thành công

---

### 14.6. Delete Notification

**Endpoint:** `DELETE /api/v1/notifications/{notificationId}`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Expected Response (200):**
```json
{
  "message": "Notification deleted successfully"
}
```

**Test Cases:**
- ✅ Xóa notification thành công
- ❌ Xóa notification của user khác (Expect: 403)

---

## 15. File Upload

### 15.1. Upload Product Image

**Endpoint:** `POST /api/v1/upload/product`

**Headers:**
```
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <product_image.jpg>
```

**Expected Response (200):**
```json
{
  "fileUrl": "https://example.com/uploads/product-123456.jpg",
  "fileName": "product-123456.jpg",
  "fileSize": 245680,
  "contentType": "image/jpeg"
}
```

**Test Cases:**
- ✅ Upload ảnh thành công (JPG, PNG, WEBP)
- ❌ File không phải ảnh (Expect: 400)
- ❌ File quá lớn (>5MB) (Expect: 413)

---

### 15.2. Upload Multiple Product Images

**Endpoint:** `POST /api/v1/upload/product/multiple`

**Headers:**
```
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
files: [<image1.jpg>, <image2.jpg>, <image3.jpg>]
```

**Expected Response (200):**
```json
[
  {
    "fileUrl": "https://example.com/uploads/product-123456.jpg",
    "fileName": "product-123456.jpg",
    "fileSize": 245680,
    "contentType": "image/jpeg"
  },
  {
    "fileUrl": "https://example.com/uploads/product-123457.jpg",
    "fileName": "product-123457.jpg",
    "fileSize": 189420,
    "contentType": "image/jpeg"
  }
]
```

**Test Cases:**
- ✅ Upload nhiều ảnh thành công
- ❌ Quá số lượng cho phép (>10 ảnh) (Expect: 400)

---

### 15.3. Upload User Avatar

**Endpoint:** `POST /api/v1/upload/avatar`

**Headers:**
```
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <avatar.jpg>
```

**Expected Response (200):**
```json
{
  "fileUrl": "https://example.com/uploads/avatar-user123.jpg",
  "fileName": "avatar-user123.jpg",
  "fileSize": 89450,
  "contentType": "image/jpeg"
}
```

**Test Cases:**
- ✅ Upload avatar thành công
- ❌ File quá lớn (>2MB) (Expect: 413)

---

### 15.4. Upload Shop Logo

**Endpoint:** `POST /api/v1/upload/shop/logo`

**Headers:**
```
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <logo.png>
```

**Expected Response (200):**
```json
{
  "fileUrl": "https://example.com/uploads/shop-logo-shop123.png",
  "fileName": "shop-logo-shop123.png",
  "fileSize": 45200,
  "contentType": "image/png"
}
```

**Test Cases:**
- ✅ Upload logo thành công
- ❌ File không phải PNG (Expect: 400)

---

### 15.5. Upload Category Image

**Endpoint:** `POST /api/v1/upload/category`

**Headers:**
```
Content-Type: multipart/form-data
```

**Request (Form Data):**
```
file: <category.jpg>
```

**Expected Response (200):**
```json
{
  "fileUrl": "https://example.com/uploads/category-cat123.jpg",
  "fileName": "category-cat123.jpg",
  "fileSize": 156780,
  "contentType": "image/jpeg"
}
```

**Test Cases:**
- ✅ Upload category image thành công

---

### 15.6. Delete Image

**Endpoint:** `DELETE /api/v1/upload?fileUrl=https://example.com/uploads/product-123456.jpg`

**Expected Response (200):**
```json
{
  "message": "File deleted successfully"
}
```

**Test Cases:**
- ✅ Xóa file thành công
- ❌ File không tồn tại (Expect: 404)
- ❌ File URL không hợp lệ (Expect: 400)

---

## Appendix

### A. Common Error Responses

#### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "Email must be valid",
    "Password must be at least 8 characters"
  ],
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/auth/register"
}
```

#### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/users/me/profile"
}
```

#### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "You don't have permission to access this resource",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/admin/users"
}
```

#### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Product not found",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/catalog/products/invalid-id"
}
```

#### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/..."
}
```

---

### B. Test Flow Recommendations

#### Complete User Flow Test
```
1. Register → Login
2. Update Profile → Upload Avatar
3. Create Address
4. Browse Products → Add to Cart
5. Checkout → Payment
6. Track Order
7. Create Review
8. Add to Wishlist
9. Use Coupon
10. Request Refund (if needed)
```

#### Complete Seller Flow Test
```
1. Register → Login (SELLER role)
2. Create Shop
3. Upload Shop Logo/Banner
4. Submit Shop for Review
5. (Admin) Approve Shop
6. Create Product
7. Upload Product Images
8. Set Product Options & SKUs
9. Submit Product for Review
10. (Admin) Approve Product
11. Manage Orders
12. Process Refunds
```

#### Complete Admin Flow Test
```
1. Login (ADMIN role)
2. View Dashboard Stats
3. View User/Sales Analytics
4. Approve/Reject Products
5. Approve/Suspend Shops
6. Approve/Reject Reviews
7. Manage Coupons
8. Disable Users (if needed)
```

---

### C. Postman Collection Tips

**Environment Variables:**
```
BASE_URL: http://localhost:8080
ACCESS_TOKEN: {{access_token}}
REFRESH_TOKEN: {{refresh_token}}
USER_ID: {{user_id}}
PRODUCT_ID: {{product_id}}
ORDER_CODE: {{order_code}}
```

**Pre-request Script (Auto Token):**
```javascript
if (pm.environment.get("ACCESS_TOKEN")) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + pm.environment.get("ACCESS_TOKEN")
    });
}
```

**Test Script (Save Token):**
```javascript
if (pm.response.json().accessToken) {
    pm.environment.set("ACCESS_TOKEN", pm.response.json().accessToken);
    pm.environment.set("REFRESH_TOKEN", pm.response.json().refreshToken);
}
```

---

### D. Testing Tools

**Recommended Tools:**
- **Postman**: API testing & collection
- **Thunder Client** (VS Code): Lightweight API testing
- **curl**: Command-line testing
- **Swagger UI**: Interactive API documentation (usually at `/swagger-ui.html`)
- **JMeter**: Load testing

**Example curl command:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}'
```

---

## Kết luận

Document này cung cấp hướng dẫn test đầy đủ cho **tất cả 147+ API endpoints** của hệ thống E-commerce Backend, bao gồm:

- ✅ 27 Controllers
- ✅ 15 Modules chính
- ✅ Authentication & Authorization
- ✅ User, Seller, Admin flows
- ✅ Payment integration
- ✅ Complete test cases cho mỗi endpoint
- ✅ Expected responses & error cases

**Happy Testing! 🚀**
