# Database Scripts

## Seed Data

### Tự động (Flyway Migrations)

Seed data được tạo tự động thông qua các Flyway migrations khi ứng dụng khởi động:

| Migration | Mô tả |
|-----------|-------|
| `V0080__seed_sample_data.sql` | Dữ liệu cơ bản: roles, users, shops, categories, brands, products |
| `V0081__enhanced_sample_data.sql` | Dữ liệu bổ sung: thêm users, products, orders, reviews, coupons |
| `V0084__fix_password_hashes.sql` | Sửa password hash cho các demo users |

### Thủ công (Standalone Script)

File `seed-data.sql` là script độc lập để tạo seed data:

```bash
# Windows (PowerShell)
Get-Content scripts/seed-data.sql | docker exec -i ecommerce-mysql mysql -u ecommerce -p<password> ecommerce

# Linux/Mac
docker exec -i ecommerce-mysql mysql -u ecommerce -p<password> ecommerce < scripts/seed-data.sql
```

## Tài khoản Test

Tất cả tài khoản sử dụng password: **`Password123!`**

| Email | Vai trò | Mô tả |
|-------|---------|-------|
| `admin@demo.local` | ADMIN | Quản trị viên hệ thống |
| `seller1@demo.local` | SELLER, CLIENT | Apple Store Vietnam |
| `seller2@demo.local` | SELLER, CLIENT | Samsung Official Store |
| `seller3@demo.local` | SELLER | TechWorld Vietnam |
| `seller4@demo.local` | SELLER | Fashion House |
| `client1@demo.local` | CLIENT | Khách hàng test 1 |
| `client2@demo.local` | CLIENT | Khách hàng test 2 |
| `customer3@demo.local` | CLIENT | Khách hàng test 3 |
| `customer4@demo.local` | CLIENT | Khách hàng test 4 |
| `customer5@demo.local` | CLIENT | Khách hàng test 5 |
| `customer6@demo.local` | CLIENT | Khách hàng test 6 |

## Dữ liệu Mẫu

### Shops (4)
- Apple Store Official
- Samsung Hub  
- TechWorld Vietnam
- Fashion House

### Categories (25)
- **Electronics**: Phones, Laptops, Tablets, Headphones, Smartwatches
- **Fashion**: Men Clothing, Women Clothing, Shoes
- **Home & Living**
- **Sports & Outdoors**
- **Books & Media**

### Brands (11)
Apple, Samsung, Sony, Dell, Xiaomi, Canon, Nike, Adidas, Zara, LG, Uniqlo

### Products (19 active)
- iPhone 15 Pro Max, MacBook Pro 14, iPad Pro, AirPods Pro
- Galaxy S24 Ultra, Galaxy Tab S9, Galaxy Buds2 Pro
- Dell XPS 15, Sony WH-1000XM5, Xiaomi Redmi Note 13 Pro
- Nike Air Max 270, Adidas Ultraboost, Zara Suit, Zara Dress

### SKUs (34 active)
Mỗi sản phẩm có nhiều biến thể theo màu sắc, dung lượng, kích cỡ

### Coupons (9 active)
| Code | Loại | Giảm giá | Điều kiện |
|------|------|----------|-----------|
| WELCOME10 | Percentage | 10% (max 100k) | Đơn từ 200k |
| SAVE500K | Fixed | 500,000đ | Đơn từ 5M |
| FLASH20 | Percentage | 20% (max 200k) | Đơn từ 500k |
| VIP1M | Fixed | 1,000,000đ | Đơn từ 10M |
| FREESHIP | Fixed | 30,000-50,000đ | Mọi đơn |
| TECH2026 | Percentage | 15% | Sản phẩm tech |
| FASHION20 | Percentage | 20% | Thời trang |
| MEGA1M | Fixed | 1,000,000đ | Đơn từ 10M |

## Migrations Overview

```
V0001-V0003  : Core (roles, users, user_role)
V0010-V0013  : Auth (refresh_tokens, refresh_sessions)
V0011-V0012  : Infrastructure (audit_log, idempotency_key)
V0020-V0022  : User (profile, address, seller_shop)
V0025        : Notification
V0030-V0034  : Catalog (category, brand, product, sku, images, variants)
V0040-V0041  : Inventory (stock_movement, stock_reservation)
V0050-V0055  : Orders (orders, order_item, refund)
V0060        : Cart
V0070-V0077  : Features (payments, password_reset, reviews, coupons, wishlist, ratings, indexes)
V0080-V0084  : Seed data & fixes
```

## Reset Database

```bash
# Drop và tạo lại database
docker exec -i ecommerce-mysql mysql -u root -p<root_password> -e "DROP DATABASE IF EXISTS ecommerce; CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Restart ứng dụng để chạy migrations
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
