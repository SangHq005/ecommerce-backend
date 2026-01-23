# Fix: Admin Notification Khi Seller Submit Profile

## Vấn Đề

Khi seller đăng ký và gửi hồ sơ thành công, admin không nhận được thông báo và cũng không thấy hồ sơ để duyệt.

## Nguyên Nhân

1. **Không có notification cho admin:** Khi seller submit profile, chỉ có event log, không có notification gửi cho admin
2. **Admin dashboard không auto-refresh:** Admin phải refresh thủ công để thấy hồ sơ mới

## Giải Pháp Đã Triển Khai

### 1. Backend Changes

#### ✅ UserJpaRepository.java
- Thêm method `findAllAdmins()` để lấy tất cả admin users

```java
@Query("SELECT DISTINCT u FROM UserEntity u JOIN u.roles r WHERE r.code = 'ADMIN' AND u.status = 'ACTIVE'")
List<UserEntity> findAllAdmins();
```

#### ✅ NotificationService.java
- Inject `UserJpaRepository`
- Thêm method `notifyAdminsNewSellerProfile()` để gửi notification cho tất cả admin

```java
@Transactional
public void notifyAdminsNewSellerProfile(Long profileId, String sellerName, String sellerType) {
    List<UserEntity> admins = userRepo.findAllAdmins();
    String message = String.format("Hồ sơ người bán mới từ %s (%s) đang chờ xác thực", 
            sellerName, sellerType.equals("INDIVIDUAL") ? "Cá nhân" : "Doanh nghiệp");
    
    for (UserEntity admin : admins) {
        createNotification(
                admin.getId(),
                "SELLER_PROFILE_PENDING",
                "Hồ sơ người bán mới",
                message,
                "SELLER_PROFILE",
                profileId
        );
    }
}
```

#### ✅ SellerProfileService.java
- Gọi `notifyAdminsNewSellerProfile()` sau khi save profile

```java
// Notify all admins about new seller profile submission
try {
    notificationService.notifyAdminsNewSellerProfile(
            saved.getId(),
            saved.getFullName(),
            saved.getSellerType().name()
    );
} catch (Exception e) {
    log.warn("Failed to notify admins about new seller profile: {}", e.getMessage());
}
```

### 2. Frontend Changes

#### ✅ seller-applications/page.tsx
- Thêm auto-refresh mỗi 30 giây để tự động load lại danh sách

```typescript
useEffect(() => {
  loadProfiles()
  loadPendingCount()
  
  // Auto-refresh every 30 seconds to check for new applications
  const interval = setInterval(() => {
    loadPendingCount()
    loadProfiles()
  }, 30000) // 30 seconds
  
  return () => clearInterval(interval)
}, [])
```

## Kết Quả

### ✅ Admin Sẽ Nhận Được:
1. **Notification:** Khi có seller submit profile mới, tất cả admin sẽ nhận notification
   - Type: `SELLER_PROFILE_PENDING`
   - Title: "Hồ sơ người bán mới"
   - Message: "Hồ sơ người bán mới từ [Tên] ([Loại]) đang chờ xác thực"
   - Reference: Link đến profile để duyệt

2. **Auto-refresh:** Admin dashboard tự động refresh mỗi 30 giây
   - Tự động cập nhật pending count
   - Tự động load lại danh sách profiles

3. **Real-time Updates:** Nếu admin đang mở seller-applications page, sẽ thấy hồ sơ mới ngay lập tức (trong vòng 30 giây)

## Testing

### Test Case 1: Notification
1. Seller submit profile
2. ✅ Tất cả admin nhận notification
3. ✅ Notification có đầy đủ thông tin

### Test Case 2: Auto-refresh
1. Admin mở seller-applications page
2. Seller submit profile mới
3. ✅ Trong vòng 30 giây, admin thấy hồ sơ mới xuất hiện
4. ✅ Pending count tự động cập nhật

### Test Case 3: Multiple Admins
1. Có 3 admin users
2. Seller submit profile
3. ✅ Cả 3 admin đều nhận notification

## Files Changed

### Backend
- `UserJpaRepository.java` - Thêm method findAllAdmins()
- `NotificationService.java` - Thêm method notifyAdminsNewSellerProfile()
- `SellerProfileService.java` - Gọi notification khi submit profile

### Frontend
- `app/admin/(dashboard)/seller-applications/page.tsx` - Thêm auto-refresh

## Next Steps (Optional)

1. **WebSocket Real-time:** Thay polling bằng WebSocket để real-time hơn
2. **Dashboard Widget:** Thêm widget hiển thị pending seller count trong admin dashboard
3. **Email Notification:** Gửi email cho admin khi có hồ sơ mới (nếu cần)

## Summary

✅ **Đã fix:**
- Admin nhận notification khi có seller submit profile
- Admin dashboard auto-refresh mỗi 30 giây
- Tất cả admin users đều nhận notification

✅ **Kết quả:**
- Admin sẽ biết ngay khi có hồ sơ mới
- Không cần refresh thủ công
- UX tốt hơn cho admin
