# Kiểm Tra Luồng Phê Duyệt Admin

## Tổng Quan

Tài liệu này kiểm tra xem các đối tượng (seller profile, shop, product) có được gửi thông báo tới admin và sau khi admin approve thì có hiển thị cho user hay không.

---

## 1. Seller Profile (Hồ sơ người bán)

### ✅ Gửi tới Admin
- **Có thông báo:** Khi seller submit profile, hệ thống gọi `notifyAdminsNewSellerProfile()`
- **File:** `SellerProfileService.java` (dòng 195)
- **Notification Type:** `SELLER_PROFILE_PENDING`
- **Message:** "Hồ sơ người bán mới từ [tên] ([loại]) đang chờ xác thực"
- **Gửi tới:** Tất cả admin có role ADMIN và status ACTIVE

### ✅ Sau Khi Admin Approve
- **Status:** `PENDING_VERIFICATION` → `ACTIVE`
- **Tự động tạo Shop:** Shop được tạo tự động với status `DRAFT`
- **Notification cho Seller:** Có thông báo "Chúc mừng! Hồ sơ người bán của bạn đã được xác thực"
- **File:** `SellerProfileService.java` (dòng 241-329)

### ✅ Hiển Thị Cho User
- Seller profile không hiển thị trực tiếp cho user (chỉ dùng để verify seller)
- Sau khi approve, seller có thể tạo shop và bán hàng

**Kết luận:** ✅ **HOẠT ĐỘNG ĐÚNG**

---

## 2. Shop (Cửa hàng)

### ❌ Gửi tới Admin
- **KHÔNG có thông báo:** Khi shop được submit (status → `PENDING_REVIEW`), hệ thống chỉ log event, KHÔNG gửi notification tới admin
- **File:** `ShopService.java` (dòng 251-265)
- **Event Log:** `SHOP_SUBMITTED` (chỉ log, không notify)
- **Vấn đề:** Admin không được thông báo khi có shop mới chờ duyệt

### ✅ Sau Khi Admin Approve
- **Status:** `PENDING_REVIEW` → `ACTIVE`
- **File:** `AdminShopService.java` (dòng 41-58)
- **Event Log:** `SHOP_STATUS_CHANGED` với status `ACTIVE`

### ✅ Hiển Thị Cho User
- **Chỉ ACTIVE shops hiển thị:** Trong `PublicCatalogController.java` (dòng 103)
  ```java
  if (!"ACTIVE".equals(shop.getStatus())) {
      throw new IllegalArgumentException("Shop not available");
  }
  ```
- **API:** `GET /api/v1/catalog/public/shops/{shopId}`
- **Chỉ đếm ACTIVE products:** `productRepository.countByShopIdAndStatus(shopId, "ACTIVE")`

**Kết luận:** 
- ✅ Sau khi approve → Shop hiển thị cho user **ĐÚNG**
- ❌ **THIẾU:** Không có notification gửi tới admin khi shop được submit

---

## 3. Product (Sản phẩm)

### ❌ Gửi tới Admin
- **KHÔNG có thông báo:** Khi product được submit (status → `PENDING_REVIEW`), hệ thống chỉ log event, KHÔNG gửi notification tới admin
- **File:** `CatalogService.java` (dòng 356-362)
- **Event Log:** `CATALOG_PRODUCT_SUBMITTED` (chỉ log, không notify)
- **Vấn đề:** Admin không được thông báo khi có product mới chờ duyệt

### ✅ Sau Khi Admin Approve
- **Status:** `PENDING_REVIEW` → `ACTIVE`
- **File:** `CatalogService.java` (dòng 385-394)
- **Event Log:** `CATALOG_PRODUCT_STATUS_CHANGED` với status `ACTIVE`

### ✅ Hiển Thị Cho User
- **Chỉ ACTIVE products hiển thị:** Trong `CatalogService.java` (dòng 95-99)
  ```java
  public Page<ProductEntity> listActiveProducts(...) {
      if (categoryId != null && brandId != null) {
          return productRepo.findByStatusAndCategoryIdAndBrandId("ACTIVE", ...);
      }
      return productRepo.findByStatus("ACTIVE", pageable);
  }
  ```
- **API:** `GET /api/v1/catalog/public/products`
- **Search:** Chỉ tìm ACTIVE products (SearchQueryRepository.java, dòng 49)
- **Product Detail:** Chỉ lấy ACTIVE products (CatalogService.java, dòng 109)

**Kết luận:**
- ✅ Sau khi approve → Product hiển thị cho user **ĐÚNG**
- ❌ **THIẾU:** Không có notification gửi tới admin khi product được submit

---

## Tóm Tắt

| Đối Tượng | Gửi Thông Báo Tới Admin? | Sau Approve Hiển Thị User? | Trạng Thái |
|-----------|-------------------------|---------------------------|------------|
| **Seller Profile** | ✅ Có (`notifyAdminsNewSellerProfile`) | ✅ Đúng (seller có thể tạo shop) | ✅ **HOẠT ĐỘNG ĐÚNG** |
| **Shop** | ❌ Không (chỉ log event) | ✅ Đúng (chỉ ACTIVE shops hiển thị) | ⚠️ **THIẾU NOTIFICATION** |
| **Product** | ❌ Không (chỉ log event) | ✅ Đúng (chỉ ACTIVE products hiển thị) | ⚠️ **THIẾU NOTIFICATION** |

---

## Vấn Đề Cần Sửa

### 1. Shop Submission - Thiếu Notification
**File:** `ShopService.java` - method `submitForReview()`

**Hiện tại:**
```java
eventRepo.save(new EventLogDocument("SHOP_SUBMITTED", ...));
// Không có notification
```

**Cần thêm:**
```java
// Notify all admins about new shop submission
notificationService.notifyAdminsNewShop(
    saved.getId(),
    saved.getShopName(),
    saved.getSellerUserId()
);
```

### 2. Product Submission - Thiếu Notification
**File:** `CatalogService.java` - method `submitForReview()`

**Hiện tại:**
```java
eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_SUBMITTED", ...));
// Không có notification
```

**Cần thêm:**
```java
// Notify all admins about new product submission
notificationService.notifyAdminsNewProduct(
    saved.getId(),
    saved.getName(),
    saved.getShopId()
);
```

---

## Đề Xuất Cải Thiện

### 1. Thêm Method Notification Cho Shop
Trong `NotificationService.java`, thêm method:
```java
public void notifyAdminsNewShop(Long shopId, String shopName, Long sellerUserId) {
    List<UserEntity> admins = userRepo.findAllAdmins();
    String message = String.format("Shop mới '%s' đang chờ duyệt", shopName);
    
    for (UserEntity admin : admins) {
        createNotification(
            admin.getId(),
            "SHOP_PENDING",
            "Shop mới chờ duyệt",
            message,
            "SHOP",
            shopId
        );
    }
}
```

### 2. Thêm Method Notification Cho Product
Trong `NotificationService.java`, thêm method:
```java
public void notifyAdminsNewProduct(Long productId, String productName, Long shopId) {
    List<UserEntity> admins = userRepo.findAllAdmins();
    String message = String.format("Sản phẩm mới '%s' đang chờ duyệt", productName);
    
    for (UserEntity admin : admins) {
        createNotification(
            admin.getId(),
            "PRODUCT_PENDING",
            "Sản phẩm mới chờ duyệt",
            message,
            "PRODUCT",
            productId
        );
    }
}
```

---

## Kết Luận

✅ **Sau khi admin approve, shop và product ĐÃ được hiển thị cho user** (chỉ ACTIVE status)

❌ **Vấn đề:** Shop và Product không gửi notification tới admin khi được submit, chỉ có Seller Profile mới gửi notification.

**Khuyến nghị:** Thêm notification cho Shop và Product submission để admin biết có items mới cần duyệt.
