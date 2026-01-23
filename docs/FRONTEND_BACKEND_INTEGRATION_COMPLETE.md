# Kết Nối Frontend-Backend Hoàn Chỉnh

## Tổng Quan

Tài liệu này mô tả toàn bộ kết nối giữa Frontend (Next.js) và Backend (Spring Boot) cho các chức năng Admin, đảm bảo tất cả endpoints đều được xử lý hoàn chỉnh.

---

## 1. Dashboard Statistics

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
getStats: async (): Promise<DashboardStatsResponse>
getUserAnalytics: async (): Promise<UserAnalyticsResponse>
getSalesAnalytics: async (): Promise<SalesAnalyticsResponse>
```

**Backend Endpoints:**
- `GET /api/v1/admin/dashboard/stats` → `AdminDashboardController.getStats()`
- `GET /api/v1/admin/dashboard/analytics/users` → `AdminDashboardController.getUserAnalytics()`
- `GET /api/v1/admin/dashboard/analytics/sales` → `AdminDashboardController.getSalesAnalytics()`

**Status:** ✅ **Hoàn chỉnh** - Tất cả data lấy từ database thật

---

## 2. Products Management

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
getProducts: async (status?, page?, size?, search?): Promise<PaginatedResult<ProductEntity>>
getProductDetail: async (productId: number): Promise<ProductDetailsResponse>
approveProduct: async (productId: number): Promise<void>
rejectProduct: async (productId: number, reason: string): Promise<void>
hideProduct: async (id: string | number, reason: string): Promise<ProductEntity>
```

**Backend Endpoints:**
- `GET /api/v1/admin/products?status=X&page=0&size=20&search=...` → `AdminCatalogController.list()`
  - ✅ **Đã cập nhật:** Hỗ trợ pagination, search, và status filter
- `GET /api/v1/admin/products/{id}` → `AdminCatalogController.getProductDetail()`
  - ✅ **Đã thêm:** Endpoint mới để lấy product detail
- `POST /api/v1/admin/products/{id}/approve` → `AdminCatalogController.approve()`
- `POST /api/v1/admin/products/{id}/reject` → `AdminCatalogController.reject()`
- `POST /api/v1/admin/products/{id}/hide` → `AdminCatalogController.hide()`

**Backend Implementation:**

```java
// AdminCatalogController.java
@GetMapping
public ResponseEntity<ApiResponse<Page<ProductEntity>>> list(
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String search,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "DESC") String sortDirection
) {
    Page<ProductEntity> products = catalog.adminListProducts(status, search, pageable);
    return ResponseHelper.page(products);
}

@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductDetailsResponse>> getProductDetail(@PathVariable Long id) {
    ProductDetailsResponse detail = catalog.getProductDetail(id);
    return ResponseHelper.ok(detail);
}
```

**Repository Methods:**
```java
// ProductJpaRepository.java
Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
Page<ProductEntity> findByStatusAndNameContainingIgnoreCase(String status, String name, Pageable pageable);
```

**Status:** ✅ **Hoàn chỉnh** - Đã thêm pagination, search, và product detail endpoint

---

## 3. Users Management

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
listUsers: async (page?, size?, sortBy?, sortDirection?): Promise<PaginatedResult<AdminUserResponse>>
getUser: async (id: string | number): Promise<AdminUserResponse>
searchUsers: async (filters, page?, size?, sortBy?, sortDirection?): Promise<PaginatedResult<AdminUserResponse>>
disableUser: async (id: string | number): Promise<void>
updateUserStatus: async (id: string | number, status: string): Promise<AdminUserResponse>
updateUserRoles: async (id: string | number, roles: string[]): Promise<AdminUserResponse>
```

**Backend Endpoints:**
- `GET /api/v1/admin/users?page=0&size=20&sortBy=id&sortDirection=DESC` → `AdminUserController.listUsers()`
- `GET /api/v1/admin/users/{id}` → `AdminUserController.getUser()`
- `GET /api/v1/admin/users/search?q=...&status=...&role=...` → `AdminUserController.searchUsers()`
- `POST /api/v1/admin/users/{id}/disable` → `AdminUserController.disableUser()`
- `PUT /api/v1/admin/users/{id}/status` → `AdminUserController.updateStatus()`
- `PUT /api/v1/admin/users/{id}/roles` → `AdminUserController.updateRoles()`

**Status:** ✅ **Hoàn chỉnh** - Tất cả endpoints đều hoạt động với pagination và filters

---

## 4. Seller Profiles Management

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
getSellerProfiles: async (status?, page?, size?): Promise<PaginatedResult<SellerProfileResponse>>
getPendingSellerCount: async (): Promise<number>
getSellerProfile: async (profileId: string | number): Promise<SellerProfileResponse>
approveSellerProfile: async (profileId: string | number): Promise<SellerProfileResponse>
rejectSellerProfile: async (profileId: string | number, reason: string): Promise<SellerProfileResponse>
suspendSellerProfile: async (profileId: string | number, reason: string): Promise<SellerProfileResponse>
reactivateSellerProfile: async (profileId: string | number): Promise<SellerProfileResponse>
```

**Backend Endpoints:**
- `GET /api/v1/admin/sellers?status=X&page=0&size=20` → `AdminSellerController.getProfilesByStatus()`
- `GET /api/v1/admin/sellers/pending-count` → `AdminSellerController.getPendingCount()`
- `GET /api/v1/admin/sellers/{profileId}` → `AdminSellerController.getProfile()`
- `POST /api/v1/admin/sellers/{profileId}/approve` → `AdminSellerController.approve()`
- `POST /api/v1/admin/sellers/{profileId}/reject` → `AdminSellerController.reject()`
- `POST /api/v1/admin/sellers/{profileId}/suspend` → `AdminSellerController.suspend()`
- `POST /api/v1/admin/sellers/{profileId}/reactivate` → `AdminSellerController.reactivate()`

**Status:** ✅ **Hoàn chỉnh** - Tất cả endpoints đều hoạt động với pagination

---

## 5. Shops Management

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
getShops: async (status: string): Promise<SellerShopEntity[]>
approveShop: async (shopId: string | number): Promise<SellerShopEntity>
suspendShop: async (shopId: string | number, reason: string): Promise<SellerShopEntity>
rejectShop: async (shopId: string | number, reason: string): Promise<SellerShopEntity>
```

**Backend Endpoints:**
- `GET /api/v1/admin/shops?status=X` → `AdminShopController.list()`
- `POST /api/v1/admin/shops/{shopId}/approve` → `AdminShopController.approve()`
- `POST /api/v1/admin/shops/{shopId}/suspend` → `AdminShopController.suspend()`
- `POST /api/v1/admin/shops/{shopId}/reject` → `AdminShopController.reject()`
- `POST /api/v1/admin/shops/{shopId}/reactivate` → `AdminShopController.reactivate()`

**Status:** ✅ **Hoàn chỉnh** - Tất cả endpoints đều hoạt động

---

## 6. Reviews Management

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
getReviewsByStatus: async (status: string): Promise<ReviewResponse[]>
searchReviews: async (filters, page?, size?, sortBy?, sortDirection?): Promise<PaginatedResult<ReviewResponse>>
approveReview: async (id: string | number): Promise<ReviewResponse>
rejectReview: async (id: string | number): Promise<ReviewResponse>
```

**Backend Endpoints:**
- `GET /api/v1/admin/reviews?status=X` → `AdminReviewController.listByStatus()`
- `GET /api/v1/admin/reviews/search?status=...&productId=...&userId=...&rating=...` → `AdminReviewController.search()`
- `POST /api/v1/admin/reviews/{id}/approve` → `AdminReviewController.approve()`
- `POST /api/v1/admin/reviews/{id}/reject` → `AdminReviewController.reject()`

**Status:** ✅ **Hoàn chỉnh** - Tất cả endpoints đều hoạt động với pagination và filters

---

## 7. Coupons Management

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
listCoupons: async (filters, page?, size?, sortBy?, sortDirection?): Promise<PaginatedResult<CouponResponse>>
createCoupon: async (coupon: CreateCouponRequest): Promise<CouponResponse>
updateCoupon: async (couponId: string | number, coupon: CreateCouponRequest): Promise<CouponResponse>
deleteCoupon: async (couponId: string | number): Promise<void>
updateCouponStatus: async (couponId: string | number, status: CouponStatus | string): Promise<CouponResponse>
```

**Backend Endpoints:**
- `GET /api/v1/admin/coupons?status=...&autoApply=...&q=...` → `AdminCouponController.list()`
- `POST /api/v1/admin/coupons` → `AdminCouponController.create()`
- `PUT /api/v1/admin/coupons/{couponId}` → `AdminCouponController.update()`
- `DELETE /api/v1/admin/coupons/{couponId}` → `AdminCouponController.delete()`
- `PUT /api/v1/admin/coupons/{couponId}/status` → `AdminCouponController.updateStatus()`

**Status:** ✅ **Hoàn chỉnh** - Tất cả CRUD operations đều hoạt động với pagination và filters

---

## 8. Refunds/Complaints Management

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
getRefunds: async (filters, page?, size?, sortBy?, sortDirection?): Promise<PaginatedResult<RefundResponse>>
getRefund: async (id: string | number): Promise<RefundResponse>
processRefund: async (id: string | number, status: string, adminNote: string): Promise<RefundResponse>
```

**Backend Endpoints:**
- `GET /api/v1/admin/refunds?status=X&page=0&size=20` → `AdminRefundController.list()`
- `GET /api/v1/admin/refunds/{id}` → `AdminRefundController.getRefund()`
- `POST /api/v1/admin/refunds/{id}/process` → `AdminRefundController.process()`

**Status:** ✅ **Hoàn chỉnh** - Tất cả endpoints đều hoạt động với pagination

---

## 9. Audit Logs

### ✅ Frontend → Backend

**Frontend Service:**
```typescript
// services/admin.service.ts
getAuditLogs: async (type?, page?, size?): Promise<PaginatedResult<AuditLog>>
```

**Backend Endpoints:**
- `GET /api/v1/admin/audit-logs?type=X&page=0&size=20` → `AdminAuditController.getAuditLogs()`

**Status:** ✅ **Hoàn chỉnh** - Endpoint hoạt động với pagination

---

## Cải Tiến Đã Thực Hiện

### 1. Products Endpoint - Thêm Pagination & Search

**Trước:**
```java
@GetMapping
public ResponseEntity<ApiResponse<List<ProductEntity>>> list(@RequestParam String status) {
    List<ProductEntity> products = catalog.adminListByStatus(status);
    return ResponseHelper.ok(products);
}
```

**Sau:**
```java
@GetMapping
public ResponseEntity<ApiResponse<Page<ProductEntity>>> list(
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String search,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "DESC") String sortDirection
) {
    Page<ProductEntity> products = catalog.adminListProducts(status, search, pageable);
    return ResponseHelper.page(products);
}
```

**Repository Methods Đã Thêm:**
```java
Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
Page<ProductEntity> findByStatusAndNameContainingIgnoreCase(String status, String name, Pageable pageable);
```

### 2. Product Detail Endpoint - Thêm Mới

**Đã thêm:**
```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductDetailsResponse>> getProductDetail(@PathVariable Long id) {
    ProductDetailsResponse detail = catalog.getProductDetail(id);
    return ResponseHelper.ok(detail);
}
```

### 3. Frontend Service - Cập Nhật Response Handling

**Trước:**
```typescript
getProducts: async (status?: string, page = 0, size = 20, search?: string): Promise<any> => {
    return apiCall(api.get("/api/v1/admin/products", { params: { status, page, size, search } }));
}
```

**Sau:**
```typescript
getProducts: async (status?: string, page = 0, size = 20, search?: string): Promise<PaginatedResult<ProductEntity>> => {
    const { data, meta } = await apiCallWithMeta<ProductEntity[]>(api.get("/api/v1/admin/products", { 
        params: { status, page, size, search, sortBy: "id", sortDirection: "DESC" } 
    }));
    return {
        content: data || [],
        totalPages: meta?.totalPages || 0,
        totalElements: meta?.total || 0,
        page: meta?.page || 0,
        size: meta?.size || 0,
        meta,
    };
}
```

---

## Tổng Kết

### ✅ Tất Cả Endpoints Đều Hoàn Chỉnh

| Chức Năng | Frontend Service | Backend Controller | Pagination | Search/Filter | Status |
|-----------|------------------|-------------------|------------|---------------|--------|
| **Dashboard** | ✅ | ✅ | N/A | N/A | ✅ **Complete** |
| **Products** | ✅ | ✅ | ✅ | ✅ | ✅ **Complete** |
| **Users** | ✅ | ✅ | ✅ | ✅ | ✅ **Complete** |
| **Seller Profiles** | ✅ | ✅ | ✅ | N/A | ✅ **Complete** |
| **Shops** | ✅ | ✅ | N/A | N/A | ✅ **Complete** |
| **Reviews** | ✅ | ✅ | ✅ | ✅ | ✅ **Complete** |
| **Coupons** | ✅ | ✅ | ✅ | ✅ | ✅ **Complete** |
| **Refunds** | ✅ | ✅ | ✅ | N/A | ✅ **Complete** |
| **Audit Logs** | ✅ | ✅ | ✅ | N/A | ✅ **Complete** |

### ✅ Tất Cả Data Đều Lấy Từ Database Thật

- Không có mock data
- Tất cả queries đều thực thi trên database thật
- Tất cả CRUD operations đều lưu vào database thật
- Pagination và filters đều hoạt động đúng

### ✅ Response Structure Đồng Nhất

Tất cả endpoints đều trả về:
```typescript
{
  success: boolean,
  data: T | T[],
  message?: string,
  meta?: {
    page: number,
    size: number,
    total: number,
    totalPages: number
  }
}
```

---

## Kết Luận

✅ **TẤT CẢ KẾT NỐI FRONTEND-BACKEND ĐỀU HOÀN CHỈNH**

- Tất cả endpoints đều được implement đầy đủ
- Pagination và search đã được thêm vào Products endpoint
- Product detail endpoint đã được thêm mới
- Frontend service đã được cập nhật để handle paginated responses
- Tất cả data đều lấy từ database thật
- Response structure đồng nhất và dễ sử dụng

**Hệ thống admin sẵn sàng để sử dụng với đầy đủ chức năng.**
