# Phân Tích Nghiệp Vụ Đăng Ký Seller

## Tổng Quan

Hệ thống đăng ký seller được thiết kế theo quy trình 2 bước:
1. **Xác thực Seller Profile** - Xác minh danh tính người bán
2. **Tạo và Duyệt Shop** - Tạo cửa hàng và chờ admin duyệt

---

## Quy Trình Chi Tiết

### Bước 1: Đăng Nhập & Nâng Cấp Role

**Frontend:** `app/seller-signup/page.tsx`

1. User đăng nhập bằng:
   - OTP (số điện thoại)
   - Google OAuth

2. Nâng cấp tài khoản lên SELLER role:
   ```typescript
   // Frontend: services/auth.service.ts
   AuthService.registerAsSeller()
   ```
   
   **Backend:** `POST /api/v1/auth/register-seller`
   - Controller: `AuthController.registerAsSeller()`
   - Service: `AuthService.registerSeller()`
   - Logic:
     - Kiểm tra user có status = ACTIVE
     - Thêm role SELLER vào user (nếu chưa có)
     - Trả về token mới với role SELLER

### Bước 2: Submit Seller Profile

**Frontend:** `app/seller-signup/page.tsx` - `handleFinishOnboarding()`

1. User điền form:
   - Thông tin shop (tên, email, phone, địa chỉ)
   - Loại seller (Cá nhân/Doanh nghiệp)
   - Số CCCD/MST
   - Thông tin ngân hàng

2. Submit profile:
   ```typescript
   SellerService.submitProfile(profilePayload)
   ```
   
   **Backend:** `POST /api/v1/seller/profile`
   - Controller: `SellerProfileController.submitProfile()`
   - Service: `SellerProfileService.createOrUpdateProfile()`
   - Logic:
     - Tạo hoặc cập nhật SellerProfile
     - Status: `PENDING_VERIFICATION`
     - Lưu thông tin: fullName, sellerType, idNumber, taxCode, contact info
     - **Không cho phép** sửa profile đã ACTIVE

### Bước 3: Admin Duyệt Profile

**Backend:** `AdminSellerController`

1. Admin xem danh sách profiles chờ duyệt:
   - `GET /api/v1/admin/seller/profiles?status=PENDING_VERIFICATION`

2. Admin duyệt/từ chối:
   - **Approve:** `POST /api/v1/admin/seller/profiles/{id}/approve`
     - Service: `SellerProfileService.approve()`
     - Status → `ACTIVE`
     - **Tự động tạo Shop DRAFT** với thông tin từ profile
     - Gửi notification cho seller
   
   - **Reject:** `POST /api/v1/admin/seller/profiles/{id}/reject`
     - Status → `REJECTED`
     - Lưu lý do từ chối
     - Gửi notification cho seller

### Bước 4: Hoàn Thiện Shop (Sau Khi Profile Được Duyệt)

**Frontend:** `app/seller-signup/page.tsx` - `handleCompleteShopInfo()`

1. Khi profile được approve, shop đã được tạo tự động với status `DRAFT`
2. Seller hoàn thiện thông tin ngân hàng:
   ```typescript
   SellerService.updateShop(shopPayload)
   SellerService.submitShop()
   ```
   
   **Backend:**
   - `PUT /api/v1/seller/shop` - Cập nhật shop
   - `POST /api/v1/seller/shop/submit` - Gửi shop để duyệt
     - Status: `DRAFT` → `PENDING_REVIEW`

### Bước 5: Admin Duyệt Shop

**Backend:** `AdminShopController`

1. Admin duyệt shop:
   - `POST /api/v1/admin/shops/{id}/approve`
   - Status: `PENDING_REVIEW` → `ACTIVE`
   - Shop có thể bắt đầu bán hàng

---

## Các Trạng Thái (Status)

### Seller Profile Status
- `PENDING_VERIFICATION` - Chờ admin duyệt
- `ACTIVE` - Đã được duyệt, có thể tạo shop
- `REJECTED` - Bị từ chối, có thể cập nhật và gửi lại
- `SUSPENDED` - Bị tạm khóa bởi admin

### Shop Status
- `DRAFT` - Shop mới tạo, chưa hoàn thiện
- `PENDING_REVIEW` - Đã gửi để admin duyệt
- `ACTIVE` - Đã được duyệt, có thể bán hàng
- `SUSPENDED` - Bị tạm khóa
- `CLOSED` - Đã đóng

---

## Business Rules

### 1. Quy Tắc Tạo Shop
- ✅ **Chỉ seller có profile ACTIVE mới được tạo shop**
- ✅ Shop được tự động tạo khi profile được approve
- ✅ Mỗi seller chỉ có 1 shop (UNIQUE constraint trên `seller_user_id`)

### 2. Quy Tắc Cập Nhật Profile
- ✅ Chỉ cho phép sửa khi status = `PENDING_VERIFICATION` hoặc `REJECTED`
- ❌ Không cho phép sửa profile đã `ACTIVE`

### 3. Quy Tắc Cập Nhật Shop
- ✅ Status `DRAFT` hoặc `PENDING_REVIEW`: Có thể sửa tất cả thông tin
- ✅ Status `ACTIVE`: Chỉ sửa được description, address, bank info
- ❌ Không sửa được khi status = `SUSPENDED` hoặc `CLOSED`

### 4. Validation
- ✅ Seller phải có profile trước khi tạo shop
- ✅ Profile phải ACTIVE trước khi tạo shop
- ✅ Shop phải có đầy đủ thông tin ngân hàng trước khi submit

---

## API Endpoints

### Seller Endpoints
- `POST /api/v1/auth/register-seller` - Nâng cấp role
- `GET /api/v1/seller/profile` - Xem profile của mình
- `POST /api/v1/seller/profile` - Submit profile
- `GET /api/v1/seller/profile/status` - Kiểm tra trạng thái
- `GET /api/v1/seller/shop` - Xem shop của mình
- `POST /api/v1/seller/shop` - Tạo shop
- `PUT /api/v1/seller/shop` - Cập nhật shop
- `POST /api/v1/seller/shop/submit` - Gửi shop để duyệt

### Admin Endpoints
- `GET /api/v1/admin/seller/profiles` - Danh sách profiles
- `POST /api/v1/admin/seller/profiles/{id}/approve` - Duyệt profile
- `POST /api/v1/admin/seller/profiles/{id}/reject` - Từ chối profile
- `POST /api/v1/admin/shops/{id}/approve` - Duyệt shop

---

## Frontend Flow

### Trạng Thái UI (onboardingStep)
1. `welcome` - Màn hình chào mừng
2. `shop-info` - Nhập thông tin shop
3. `shipping` - Chọn phương thức vận chuyển (UI only, không lưu)
4. `identity` - Nhập thông tin định danh
5. `bank` - Nhập thông tin ngân hàng
6. `verification-pending` - Chờ admin duyệt profile
7. `verification-rejected` - Profile bị từ chối
8. `complete-shop-info` - Hoàn thiện shop sau khi profile được duyệt
9. `done` - Hoàn tất

### Logic Kiểm Tra Trạng Thái
```typescript
// Frontend: app/seller-signup/page.tsx - useEffect checkStatus()
1. Kiểm tra user đã đăng nhập chưa
2. Kiểm tra user có role SELLER chưa
3. Kiểm tra verification status:
   - PENDING_VERIFICATION → hiển thị "verification-pending"
   - REJECTED → hiển thị "verification-rejected" với lý do
   - SUSPENDED → hiển thị "verification-rejected"
   - ACTIVE → kiểm tra shop:
     - Shop ACTIVE/PENDING_REVIEW → "done"
     - Shop DRAFT → "complete-shop-info"
     - Không có shop → "shop-info"
```

---

## Vấn Đề Tiềm Ẩn & Cải Thiện

### ✅ Điểm Mạnh
1. Quy trình rõ ràng, tách biệt profile và shop
2. Tự động tạo shop khi profile được duyệt
3. Validation chặt chẽ ở backend
4. Notification cho seller khi có thay đổi

### ⚠️ Vấn Đề Cần Lưu Ý

1. **Shipping Methods không được lưu**
   - Frontend có step "shipping" nhưng không gọi API lưu
   - Chỉ là UI simulation

2. **Thiếu validation format**
   - Số CCCD: Không validate format (12 số)
   - Số tài khoản ngân hàng: Không validate
   - Email: Có validation nhưng có thể cải thiện

3. **Error Handling**
   - Frontend có try-catch nhưng có thể cải thiện error messages
   - Backend có ApiException nhưng cần đảm bảo consistency

4. **Race Condition**
   - Nếu seller submit profile nhiều lần nhanh, có thể tạo nhiều profile
   - Cần idempotency check

5. **Auto-create Shop có thể fail**
   - Nếu auto-create shop fail, seller vẫn có thể tạo shop thủ công
   - Nhưng có thể gây confusion

### 💡 Đề Xuất Cải Thiện

1. **Thêm validation format:**
   ```java
   // Backend: Validate CCCD format (12 digits)
   if (sellerType == INDIVIDUAL && !idNumber.matches("^\\d{12}$")) {
       throw ApiException.badRequest("CCCD must be 12 digits");
   }
   ```

2. **Thêm idempotency:**
   ```java
   // Check if profile already exists and is PENDING
   if (profile.getId() != null && profile.getStatus() == PENDING_VERIFICATION) {
       // Return existing instead of creating new
   }
   ```

3. **Lưu shipping methods:**
   - Tạo bảng `shop_shipping_methods`
   - Lưu khi seller chọn

4. **Cải thiện error messages:**
   - Thêm error codes cụ thể
   - Frontend hiển thị messages thân thiện hơn

5. **Thêm retry logic:**
   - Nếu auto-create shop fail, có job retry
   - Hoặc có button "Tạo shop" nếu chưa có

---

## Test Cases Đề Xuất

### Test Case 1: Happy Path
1. User đăng nhập
2. Upgrade to SELLER
3. Submit profile
4. Admin approve → shop auto-created
5. Seller complete shop info
6. Seller submit shop
7. Admin approve shop
8. ✅ Shop ACTIVE

### Test Case 2: Profile Rejected
1. User submit profile
2. Admin reject với lý do
3. Seller xem lý do
4. Seller update và resubmit
5. Admin approve
6. ✅ Shop được tạo

### Test Case 3: Duplicate Submission
1. User submit profile
2. User submit lại nhanh (race condition)
3. ✅ Chỉ có 1 profile được tạo

### Test Case 4: Shop Update Rules
1. Shop DRAFT → có thể sửa tất cả
2. Shop ACTIVE → chỉ sửa được description, address, bank
3. ✅ Validation đúng

### Test Case 5: Auto-create Shop Failure
1. Admin approve profile
2. Auto-create shop fail (simulate)
3. Seller vẫn có thể tạo shop thủ công
4. ✅ Không block seller

---

## Kết Luận

Nghiệp vụ đăng ký seller được thiết kế tốt với quy trình rõ ràng. Có một số điểm cần cải thiện về validation và error handling, nhưng không có vấn đề nghiêm trọng về logic nghiệp vụ.

**Đánh giá:** ⭐⭐⭐⭐ (4/5)
