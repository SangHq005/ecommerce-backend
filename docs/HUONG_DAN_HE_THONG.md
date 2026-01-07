# 📘 Hướng Dẫn Hệ Thống E-Commerce Backend

Chào mừng bạn đến với tài liệu hướng dẫn chi tiết về hệ thống E-commerce Backend. Tài liệu này được biên soạn để giúp bạn hiểu rõ về cấu trúc, cách cài đặt, các tính năng cốt lõi và quy trình phát triển của dự án.

---

## 📑 Mục lục
1. [Giới thiệu hệ thống](#1-giới-thiệu-hệ-thống)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Hướng dẫn cài đặt & Khởi chạy](#3-hướng-dẫn-cài-đặt--khởi-chạy)
4. [Các Module tính năng chính](#4-các-module-tính-năng-chính)
5. [Dữ liệu mẫu & Tài khoản thử nghiệm](#5-dữ-liệu-mẫu--tài-khoản-thử-nghiệm)
6. [Hướng dẫn kiểm thử API](#6-hướng-dẫn-kiểm-thử-api)
7. [Kiến trúc dữ liệu & Database](#7-kiến-trúc-dữ-liệu--database)

---

## 1. Giới thiệu hệ thống
Hệ thống là một nền tảng Backend thương mại điện tử mạnh mẽ, hỗ trợ mô hình **Multi-Vendor** (Đa nhà bán hàng). Hệ thống được thiết kế theo kiến trúc hiện đại, đảm bảo tính bảo mật, hiệu năng cao và khả năng mở rộng.

**Các luồng quy trình chính:**
- **Khách hàng (Client):** Xem sản phẩm, tìm kiếm, giỏ hàng, đặt hàng, thanh toán qua VNPay, đánh giá sản phẩm.
- **Người bán (Seller):** Quản lý cửa hàng, đăng sản phẩm, quản lý kho, xử lý đơn hàng.
- **Quản trị viên (Admin):** Quản lý người dùng, duyệt shop, quản lý mã giảm giá, xem báo cáo thống kê.

---

## 2. Công nghệ sử dụng
Hệ thống sử dụng các công nghệ mới nhất để đảm bảo tính ổn định:

| Thành phần | Công nghệ |
|------------|-----------|
| **Ngôn ngữ** | Java 21 |
| **Framework** | Spring Boot 3.4.1 |
| **Bảo mật** | Spring Security, JWT, OAuth2 (Google) |
| **Database chính** | MySQL 8.0 (Lưu trữ quan hệ, đơn hàng, người dùng) |
| **Database NoSQL** | MongoDB (Lưu trữ logs, event người dùng, recommendations) |
| **Caching** | Redis (Tăng tốc độ truy xuất dữ liệu) |
| **Migration** | Flyway (Quản lý phiên bản database) |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) |

---

## 3. Hướng dẫn cài đặt & Khởi chạy

### 3.1. Chuẩn bị môi trường
- Cài đặt **JDK 21**.
- Chuẩn bị các service: **MySQL**, **MongoDB**, **Redis**.
- Cài đặt **Maven** (để build project).

### 3.2. Cấu hình Biến môi trường
Tạo file `.env` tại thư mục gốc từ file mẫu `.env.example`:
```bash
# Ví dụ cấu hình DB đơn giản
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce
DB_USERNAME=root
DB_PASSWORD=your_password
```

### 3.3. Khởi tạo Database & Chạy Migration
Sử dụng script PowerShell đã chuẩn bị sẵn để nạp schema và dữ liệu mẫu:
```powershell
./manual-migrate.ps1
```

### 3.4. Chạy ứng dụng
```powershell
./run-dev.ps1
```
Ứng dụng sẽ khởi chạy tại: `http://localhost:8080`

---

## 4. Các Module tính năng chính

### 4.1. Xác thực & Phân quyền (Auth)
- **Cơ chế:** Sử dụng JWT (Access Token & Refresh Token).
- **Phân quyền:** Dựa trên Role (ADMIN, SELLER, CLIENT).
- **Tích hợp:** Hỗ trợ đăng nhập Google.

### 4.2. Quản lý Sản phẩm (Catalog)
- **Đa cấp:** Hỗ trợ danh mục sản phẩm lồng nhau (Categories tree).
- **SKU:** Quản lý biến thể (Màu sắc, kích cỡ) với giá và tồn kho riêng biệt.
- **Kho hàng:** Tự động giữ chỗ (Reservation) khi đặt hàng và hoàn lại khi hủy đơn.

### 4.3. Đặt hàng & Thanh toán (Orders)
- **Xử lý đơn hàng:** Hỗ trợ tách đơn tự động theo cửa hàng (multi-vendor split).
- **VNPay:** Tích hợp cổng thanh toán VNPay (môi trường thử nghiệm).
- **Workflow:** Quy trình trạng thái đơn hàng nghiêm ngặt (PAID -> PROCESSING -> SHIPPED -> COMPLETED).

### 4.4. Hệ thống Khuyến mãi (Promotion)
- **Mã giảm giá (Coupon):** Hỗ trợ giảm theo %, giảm số tiền cố định, miễn phí vận chuyển.
- **Ràng buộc:** Giới hạn theo sản phẩm, danh mục, giá trị đơn hàng tối thiểu hoặc đối tượng người dùng.

### 4.5. Hệ thống Khuyến nghị (Recommendation)
- Sử dụng MongoDB để theo dõi hành vi người dùng (VIEW, ADD_TO_CART, PURCHASE).
- Cung cấp API gợi ý sản phẩm liên quan và sản phẩm cá nhân hóa.

---

## 5. Dữ liệu mẫu & Tài khoản thử nghiệm

Dưới đây là danh sách các tài khoản đã được nạp sẵn để bạn trải nghiệm:

| Email | Mật khẩu | Vai trò | Mô tả |
|-------|----------|---------|-------|
| `admin@demo.local` | `Password123!` | **ADMIN** | Quản trị viên toàn hệ thống |
| `seller1@demo.local` | `Password123!` | **SELLER** | Shop Apple Store Official |
| `customer3@demo.local` | `Password123!` | **CLIENT** | Khách hàng: Nguyễn Văn An |
| `customer4@demo.local` | `Password123!` | **CLIENT** | Khách hàng: Trần Thị Bình |

*Lưu ý: Tất cả tài khoản mẫu đều dùng mật khẩu chung là `Password123!`*

---

## 6. Hướng dẫn kiểm thử API

Hệ thống cung cấp đầy đủ công cụ để bạn test các endpoint:

1.  **Swagger UI:** Truy cập `http://localhost:8080/swagger-ui.html` để xem và chạy thử API trực tiếp.
2.  **API Test Guide:** Xem chi tiết hàng trăm kịch bản test tại thư mục `docs/`.
3.  **Kịch bản phổ biến:**
    - `POST /api/v1/auth/login`: Lấy Token.
    - `GET /api/v1/products`: Danh sách sản phẩm công khai.
    - `POST /api/v1/cart/items`: Thêm vào giỏ hàng (yêu cầu Login).
    - `POST /api/v1/checkout`: Đặt hàng.

---

## 7. Kiến trúc dữ liệu & Database

Hệ thống quản lý dữ liệu chặt chẽ qua Flyway:
- `V0001` -> `V0013`: Schema cốt lõi (User, Roles, Refresh Session).
- `V0020` -> `V0022`: Profile & Shop Management.
- `V0030` -> `V0034`: Catalog, Products, SKU, Variants.
- `V0040` -> `V0045`: Cart & Inventory.
- `V0050` -> `V0055`: Orders & Payments.
- `V0071` -> `V0074`: Coupon, Review, Wishlist.
- `V0080` -> `V0081`: Dữ liệu mẫu Tiếng Việt chuẩn.

---

**Chúc bạn thành công với dự án! 🚀**
*(Mọi thắc mắc xin vui lòng kiểm tra thêm các file chi tiết hơn trong folder `docs/`)*
