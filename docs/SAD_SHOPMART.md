# Tài liệu Kiến trúc Giải pháp (SAD)
# ShopMart E-Commerce Platform

---

## 1. Thông tin chung

| Trường              | Nội dung                                      |
| ------------------- | --------------------------------------------- |
| **Tên hệ thống**    | ShopMart – Nền tảng thương mại điện tử đa vai trò |
| **Phiên bản tài liệu** | 2.0                                        |
| **Ngày soạn**       | 18/05/2026                                    |
| **Người soạn**      | Nhóm Kiến trúc / Tech Lead                   |
| **Người kiểm tra**  | Product Owner, Senior Developer, DevOps Lead  |
| **Người phê duyệt** | Trưởng nhóm kỹ thuật                         |
| **Trạng thái**      | Baseline – Đang kiểm tra nội bộ              |

### Lịch sử phiên bản

| Phiên bản | Ngày       | Người thực hiện    | Mô tả thay đổi                                              |
| --------- | ---------- | ------------------ | ----------------------------------------------------------- |
| 0.1       | 18/05/2026 | Architecture Team  | Khởi tạo cấu trúc tài liệu                                 |
| 1.0       | 18/05/2026 | Architecture Team  | Hoàn thiện nội dung theo chuẩn IEEE SAD                    |
| 2.0       | 18/05/2026 | Architecture Team  | Chuẩn hóa lại theo template SAD doanh nghiệp tiếng Việt   |

---

## 2. Giới thiệu

### 2.1 Mục tiêu tài liệu

Tài liệu Kiến trúc Giải pháp (SAD) này được soạn nhằm các mục tiêu sau:

1. **Thống nhất nhận thức kiến trúc** giữa tất cả các bên liên quan: Product, Engineering, QA,
   DevOps và các stakeholder kinh doanh — đảm bảo mọi người cùng hiểu một hệ thống.
2. **Cung cấp căn cứ kỹ thuật** cho việc phát triển, vận hành, bảo trì và mở rộng hệ thống
   ShopMart trong suốt vòng đời dự án.
3. **Lưu trữ các quyết định kiến trúc (ADR)** cùng lý do lựa chọn và trade-off, tránh tranh luận
   lại những vấn đề đã được giải quyết.
4. **Hỗ trợ phân tích chất lượng** bằng cách truy vết các yêu cầu phi chức năng đến cấu trúc
   kiến trúc tương ứng.
5. **Onboarding thành viên mới** — tài liệu này là điểm vào chính để hiểu hệ thống từ góc nhìn
   kiến trúc.

Tài liệu này được sử dụng:
- **Khi thiết kế tính năng mới**: đối chiếu xem tính năng thuộc module nào, ảnh hưởng đến
  component nào.
- **Khi ra quyết định kỹ thuật**: tham khảo ADR để không mâu thuẫn với kiến trúc hiện có.
- **Khi onboarding**: là tài liệu đọc đầu tiên cho thành viên mới gia nhập nhóm.
- **Khi review kiến trúc**: làm cơ sở đánh giá tính nhất quán và chất lượng kiến trúc theo chu kỳ.

### 2.2 Phạm vi

#### Trong phạm vi (In Scope)

- **Frontend web** phục vụ 3 vai trò: Khách hàng (Customer), Nhà bán hàng (Seller), Quản trị
  viên (Admin) — xây dựng bằng Next.js/React/TypeScript.
- **Backend REST API** (Spring Boot, kiến trúc monolith module hóa) xử lý toàn bộ logic nghiệp vụ.
- **Cơ sở dữ liệu quan hệ** MySQL — mô hình dữ liệu, chiến lược index, transaction boundary.
- **Tích hợp bên thứ ba**: Google OAuth2, VNPay, MoMo, Email service, Media/CDN storage.
- **Hạ tầng triển khai** và pipeline CI/CD.
- **Quan sát hệ thống** (logging, metrics, health check, alerting).

#### Ngoài phạm vi (Out of Scope)

- Ứng dụng mobile native (iOS/Android).
- Kiến trúc data warehouse, BI pipeline hoặc hệ thống phân tích nâng cao.
- Chi tiết thiết kế cấp class/method (xem component design documents hoặc source code).
- Đặc tả API chi tiết (xem tài liệu OpenAPI/Swagger).
- Cấu hình hạ tầng cloud cụ thể, sizing và chi phí.

### 2.3 Đối tượng người đọc

| Đối tượng                     | Mục đích sử dụng tài liệu này                                                     | Phần quan trọng nhất         |
| ----------------------------- | --------------------------------------------------------------------------------- | ----------------------------- |
| **Kiến trúc sư giải pháp**    | Đánh giá tổng thể, ra quyết định kiến trúc, phân tích trade-off                   | 4, 5, 8, 9                    |
| **Lập trình viên Backend**    | Hiểu module boundary, quy tắc phân tầng, transaction, data model                  | 5.3, 6.1, 6.2, 6.4            |
| **Lập trình viên Frontend**   | Hiểu cấu trúc route, hợp đồng API, luồng xác thực, xử lý lỗi                     | 5.3, 6.1, 6.2, 7              |
| **Kiểm thử viên (QA)**        | Xây dựng test strategy, hiểu runtime flow, đo lường quality attributes             | 4.2, 6.2, 9                   |
| **Quản lý dự án**             | Hiểu phạm vi, rủi ro, ràng buộc, hướng phát triển                                | 2, 3, 10                      |
| **DevOps / Vận hành (SRE)**   | Triển khai, giám sát, backup/rollback, incident response                           | 6.3, 9.2, 10                  |
| **Kỹ sư bảo mật**             | Đánh giá auth/authz, bảo vệ dữ liệu, payment security, audit logging              | 4.2, 6.2, 7, 8 (ADR-03, 04)   |
| **Quản trị CSDL (DBA)**       | Mô hình entity, index strategy, transaction boundary, backup/replication           | 6.4, 8 (ADR-02)               |
| **Thành viên mới (Onboarding)**| Định hướng tổng quan: mục đích, cấu trúc, nguyên tắc, quyết định chính            | 2, 3, 5, 6.1                  |

---

## 3. Bối cảnh và tổng quan hệ thống

### 3.1 Bối cảnh kinh doanh

**ShopMart** được xây dựng để giải quyết nhu cầu có một nền tảng thương mại điện tử đa vai trò,
nơi người tiêu dùng có thể mua sắm, người bán hàng có thể tự vận hành gian hàng, và đội ngũ vận
hành có công cụ để quản trị toàn bộ hệ thống.

#### Vấn đề kinh doanh cần giải quyết

| ID    | Vấn đề                                                                          | Mức độ ưu tiên |
| ----- | ------------------------------------------------------------------------------- | -------------- |
| BP-01 | Người mua không có kênh hiệu quả để khám phá, đánh giá và mua sản phẩm online | Cực kỳ quan trọng |
| BP-02 | Người bán thiếu công cụ tự vận hành gian hàng, quản lý sản phẩm/đơn hàng      | Cực kỳ quan trọng |
| BP-03 | Đội vận hành không có giao diện thống nhất để kiểm duyệt, quản lý và theo dõi | Quan trọng cao |
| BP-04 | Chưa có luồng thanh toán tích hợp các cổng thanh toán nội địa (VNPay, MoMo)   | Cực kỳ quan trọng |
| BP-05 | Tồn kho dễ bị sai lệch khi nhiều người đặt hàng cùng lúc                      | Quan trọng cao |
| BP-06 | Chưa có cơ chế khuyến mãi linh hoạt (coupon toàn sàn, voucher theo shop)       | Quan trọng vừa |
| BP-07 | Hệ thống không đủ năng lực xử lý khi có đợt sale lớn, flash sale               | Quan trọng cao |
| BP-08 | Chưa định nghĩa rõ bảo mật, để lộ dữ liệu người dùng và luồng thanh toán      | Cực kỳ quan trọng |

#### Mục tiêu kinh doanh

1. Cung cấp trải nghiệm mua sắm online hoàn chỉnh: tìm kiếm → xem sản phẩm → đặt hàng → thanh
   toán → theo dõi đơn.
2. Cho phép người bán tự vận hành: đăng sản phẩm, quản lý tồn kho, xử lý đơn, xem doanh thu.
3. Đảm bảo đội quản trị có đầy đủ công cụ kiểm soát, kiểm duyệt và quan sát hệ thống.
4. Xây dựng nền tảng có khả năng mở rộng, sẵn sàng phát triển tính năng mới trong các sprint
   tiếp theo.

### 3.2 Mô tả tổng quan hệ thống

ShopMart là **nền tảng thương mại điện tử đa vai trò** hoạt động trên web, hỗ trợ đầy đủ vòng đời
giao dịch từ khám phá sản phẩm đến thanh toán và hậu mãi. Hệ thống phục vụ ba nhóm người dùng
chính với các quyền và giao diện riêng biệt:

| Vai trò              | Mô tả                                                                                   |
| -------------------- | --------------------------------------------------------------------------------------- |
| **Customer**         | Duyệt sản phẩm, thêm vào giỏ, đặt hàng, thanh toán, theo dõi đơn, đánh giá sản phẩm   |
| **Seller**           | Mở shop, quản lý sản phẩm/tồn kho, xử lý đơn hàng, tạo voucher, xem doanh thu         |
| **Admin**            | Duyệt shop, kiểm duyệt sản phẩm, quản lý người dùng, quản trị danh mục, xem analytics  |

**Giá trị cốt lõi:**

- **Với người mua**: tìm kiếm nhanh, mua hàng dễ, thanh toán an toàn, theo dõi minh bạch.
- **Với người bán**: tự chủ vận hành, kiểm soát sản phẩm và đơn hàng, theo dõi doanh thu thực
  thời.
- **Với vận hành**: kiểm soát toàn sàn, kiểm duyệt nội dung, phát hiện vi phạm, quan sát hiệu
  năng hệ thống.

### 3.3 Sơ đồ bối cảnh hệ thống (System Context)

```
                    ┌─────────────────────────────────────────────────────┐
                    │               SHOPMART PLATFORM                     │
                    │                                                     │
                    │   ┌──────────────┐    ┌──────────────────────┐     │
                    │   │  Next.js     │◄──►│  Spring Boot         │     │
                    │   │  Frontend    │    │  Backend API         │     │
                    │   │  (Web App)   │    │  (REST/JSON)         │     │
                    │   └──────────────┘    └──────────┬───────────┘     │
                    │                                  │                 │
                    │                        ┌─────────▼──────┐         │
                    │                        │  MySQL RDBMS   │         │
                    │                        └────────────────┘         │
                    └──────────────────────────────┬──────────────────────┘
                                                   │
              ┌───────────────────┬────────────────┼──────────────────┬──────────────────┐
              │                   │                │                  │                  │
     ┌────────▼──────┐  ┌─────────▼──────┐  ┌─────▼──────┐  ┌───────▼───────┐  ┌───────▼──────┐
     │   Customer    │  │    Seller      │  │   Admin    │  │ Google OAuth2 │  │  VNPay/MoMo  │
     │   (Browser)   │  │   (Browser)   │  │  (Browser) │  │  (Auth SSO)   │  │  (Payment)   │
     └───────────────┘  └───────────────┘  └────────────┘  └───────────────┘  └──────────────┘
                                                                        ┌────────────────────┐
                                                                        │   Email Service    │
                                                                        │  (Transactional)   │
                                                                        └────────────────────┘
                                                                        ┌────────────────────┐
                                                                        │  Media Storage/CDN │
                                                                        │  (Product Images)  │
                                                                        └────────────────────┘
```

#### Mô tả các tác nhân bên ngoài

| Tác nhân              | Loại   | Tương tác với ShopMart                                                    |
| --------------------- | ------ | ------------------------------------------------------------------------- |
| **Customer**          | Người  | Duyệt, tìm kiếm, mua hàng, đánh giá qua web app                          |
| **Seller**            | Người  | Quản lý gian hàng, sản phẩm, đơn hàng qua seller portal                  |
| **Admin**             | Người  | Kiểm duyệt, quản trị, xem báo cáo qua admin portal                       |
| **Google OAuth2**     | Hệ thống | Cung cấp token xác thực khi đăng nhập bằng Google                      |
| **VNPay / MoMo**      | Hệ thống | Nhận yêu cầu thanh toán; gửi callback kết quả về backend                |
| **Email Service**     | Hệ thống | Nhận yêu cầu gửi mail; phân phối email xác thực, thông báo              |
| **Media Storage/CDN** | Hệ thống | Lưu trữ hình ảnh sản phẩm; phân phối asset tối ưu đến frontend          |

---

## 4. Yêu cầu kiến trúc

### 4.1 Yêu cầu chức năng ở mức cao

| Nhóm chức năng               | Mô tả                                                                                      |
| ----------------------------- | ------------------------------------------------------------------------------------------ |
| **Xác thực & Phân quyền**    | Đăng ký, đăng nhập email, OAuth2 Google, xác thực email, quên mật khẩu, RBAC 3 vai trò   |
| **Catalog & Tìm kiếm**       | Duyệt danh mục, tìm kiếm sản phẩm, lọc/sắp xếp, xem chi tiết sản phẩm và biến thể SKU   |
| **Giỏ hàng & Checkout**      | Quản lý giỏ hàng, áp dụng coupon/voucher, tạo đơn đa shop, tính phí vận chuyển           |
| **Thanh toán**                | Thanh toán COD và online (VNPay/MoMo), xử lý callback idempotent, đối soát trạng thái    |
| **Quản lý đơn hàng**         | Vòng đời đơn hàng (state machine), theo dõi trạng thái, hủy đơn, xử lý hoàn tiền         |
| **Quản lý tồn kho**          | Cập nhật tồn kho SKU, reservation khi đặt hàng, giải phóng khi hủy, log biến động        |
| **Seller Center**             | Đăng ký/hồ sơ shop, CRUD sản phẩm/SKU, xử lý đơn seller, voucher shop, dashboard doanh thu |
| **Admin Center**              | Duyệt shop, kiểm duyệt sản phẩm, quản lý user/seller, quản trị danh mục/brand, analytics |
| **Tương tác khách hàng**     | Wishlist, đánh giá sản phẩm (rating + review + ảnh), vote hữu ích                         |
| **Thông báo**                 | Thông báo trạng thái đơn, duyệt shop, email xác thực — in-app và email                    |
| **Quan sát hệ thống**        | Logging có cấu trúc, metrics, health check, audit trail cho thao tác nhạy cảm             |

### 4.2 Yêu cầu phi chức năng (Quality Attributes)

#### Hiệu năng (Performance)

| Thuộc tính           | Chỉ tiêu                                                                    |
| -------------------- | --------------------------------------------------------------------------- |
| Thời gian phản hồi API | ≥95% request hoàn thành trong 3 giây; 100% trong 5 giây ở tải bình thường |
| Tìm kiếm sản phẩm   | ≥95% request trả kết quả trong 2 giây (tải bình thường)                   |
| Trang catalog        | Tải trong vòng 2 giây ở điều kiện mạng thông thường                       |
| Thanh toán callback  | ≥99,9% callback xử lý xong trong 1 phút                                   |

#### Khả năng mở rộng (Scalability)

| Thuộc tính               | Chỉ tiêu                                                                 |
| ------------------------ | ------------------------------------------------------------------------ |
| Đồng thời người dùng     | Hệ thống phải duy trì SLA hiệu năng ở mức tải đồng thời mục tiêu        |
| Thời gian cao điểm       | Hỗ trợ scale ngang (horizontal scaling) backend khi có flash sale        |
| Tỷ lệ lỗi quá tải        | Lỗi do quá tải ≤0,5% ở mức tải mục tiêu                                |

#### Bảo mật (Security)

| Thuộc tính               | Chỉ tiêu                                                                 |
| ------------------------ | ------------------------------------------------------------------------ |
| Xác thực API             | 100% endpoint bảo vệ yêu cầu JWT hợp lệ; truy cập trái phép bị chặn và ghi log |
| Bảo vệ dữ liệu           | 100% traffic production qua HTTPS/TLS; không có secret dạng plaintext trong log |
| Bảo mật thanh toán       | 100% callback được xác thực chữ ký trước khi xử lý; idempotent           |
| Mật khẩu                 | Hash bằng BCrypt; không bao giờ lưu/log plaintext password               |

#### Khả dụng (Availability)

| Thuộc tính    | Chỉ tiêu                                              |
| ------------- | ----------------------------------------------------- |
| Uptime        | ≥99,9% mỗi tháng calendar                            |
| MTTR          | <30 phút cho sự cố phổ biến                          |
| RPO / RTO     | Được định nghĩa và kiểm thử định kỳ theo chính sách backup |
| Rollback      | Hoàn thành trong vòng 10 phút khi cần rollback        |

#### Khả năng bảo trì (Maintainability)

| Thuộc tính             | Chỉ tiêu                                                                  |
| ---------------------- | ------------------------------------------------------------------------- |
| Thay đổi chính sách    | Thay đổi chính sách nhỏ (coupon, refund, order flow) triển khai trong <1 sprint |
| Phạm vi ảnh hưởng      | Thay đổi thông thường ảnh hưởng tối đa 2 module                          |
| Phân lớp               | Quy tắc phân tầng (controller → service → repository) được tuân thủ 100%  |

#### Khả năng quan sát / Giám sát (Observability)

| Thuộc tính           | Chỉ tiêu                                                                    |
| -------------------- | --------------------------------------------------------------------------- |
| Logging              | Tất cả request có correlation ID; log có cấu trúc JSON; không log thông tin nhạy cảm |
| Metrics              | Theo dõi latency, error rate, saturation, business KPI qua Prometheus/Grafana |
| Health Check         | Endpoint `/actuator/health` phục vụ load balancer và deployment gate        |
| Audit Trail          | Mọi thao tác nhạy cảm (thay đổi quyền, thanh toán, duyệt/từ chối) được ghi audit log |

### 4.3 Ràng buộc và giả định

#### Ràng buộc kỹ thuật

| ID     | Ràng buộc                                                                            |
| ------ | ------------------------------------------------------------------------------------ |
| CON-01 | Backend bắt buộc dùng **Java + Spring Boot** (chuẩn kỹ thuật của tổ chức)           |
| CON-02 | Frontend bắt buộc dùng **Next.js + React + TypeScript**                              |
| CON-03 | Cơ sở dữ liệu bắt buộc là **MySQL**                                                 |
| CON-04 | Toàn bộ traffic production bắt buộc qua **HTTPS/TLS**                               |
| CON-05 | Tích hợp thanh toán phải hỗ trợ **VNPay và MoMo** (cổng nội địa Việt Nam)           |
| CON-06 | Đăng nhập mạng xã hội phải hỗ trợ **Google OAuth2** ngay từ đầu                     |

#### Ràng buộc tổ chức

| ID     | Ràng buộc                                                                            |
| ------ | ------------------------------------------------------------------------------------ |
| CON-07 | Quy mô team nhỏ, kiến trúc phải tối thiểu hóa overhead vận hành                     |
| CON-08 | Áp lực time-to-market; ưu tiên tốc độ deliver hơn tối ưu hóa kiến trúc sớm          |
| CON-09 | Mọi quyết định kiến trúc lớn phải được ghi thành ADR và review team                 |

#### Giả định (Assumptions)

| ID     | Giả định                                                                             |
| ------ | ------------------------------------------------------------------------------------ |
| ASM-01 | Hệ thống nhắm đến thị trường Việt Nam; cổng thanh toán là VNPay và MoMo             |
| ASM-02 | Ứng dụng mobile native nằm ngoài phạm vi release này                                |
| ASM-03 | Triển khai một region duy nhất là đủ cho giai đoạn đầu                              |
| ASM-04 | KYC của seller là quy trình thủ công/vận hành; nền tảng chỉ xử lý onboarding số    |
| ASM-05 | Traffic đọc (browse catalog, search) lớn hơn nhiều so với traffic ghi (order, payment) |
| ASM-06 | Hệ thống không xử lý, lưu trữ thông tin thẻ thanh toán đầy đủ (tokenized via gateway) |

---

## 5. Kiến trúc tổng thể

### 5.1 Cách tiếp cận kiến trúc

#### Kiểu kiến trúc được chọn: **Modular Monolith + Layered Architecture**

ShopMart sử dụng kiến trúc **monolith module hóa** ở backend và kiến trúc **phân tầng nghiêm ngặt**
(layered) trong từng module. Frontend sử dụng **Server-Side Rendering (SSR)** với Next.js App Router.

| Khía cạnh            | Lựa chọn                                 | Lý do chính                                                              |
| -------------------- | ---------------------------------------- | ------------------------------------------------------------------------ |
| Kiến trúc backend    | Modular Monolith                         | Giảm độ phức tạp vận hành; ACID transaction natively; phù hợp team nhỏ  |
| Phân tầng nội bộ     | Layered (Controller → Service → Repository) | Tách biệt concern rõ ràng; dễ test từng tầng; giảm coupling              |
| API style            | REST/JSON over HTTPS                     | Chuẩn phổ biến; dễ tích hợp frontend/mobile/partner; team quen thuộc    |
| Frontend rendering   | SSR + Client-side hydration              | Tốt cho SEO trang sản phẩm/catalog; trải nghiệm mượt sau khi load       |
| Auth                 | JWT + Refresh Token                      | Stateless API auth; scalable; revocable session                          |

#### Nguyên tắc kiến trúc nền tảng

1. **Domain-Driven Package Structure** — mỗi domain nghiệp vụ là một module cohesive.
2. **API-First Contract Design** — REST contract là giao diện chuẩn; frontend/backend phát triển song song.
3. **Security by Default** — Auth/RBAC áp dụng cho mọi tài nguyên bảo vệ ngay từ đầu.
4. **Explicit Transaction Boundaries** — thao tác quan trọng (đặt hàng + giữ tồn kho, callback +
   cập nhật đơn) có transaction rõ ràng.
5. **Observable by Design** — logging cấu trúc, metrics, correlation ID là built-in, không phải
   thêm sau.
6. **Fail-Fast, Degrade Gracefully** — tích hợp ngoài có timeout/retry; lỗi internal surface sớm.

### 5.2 Sơ đồ kiến trúc tổng thể

Sơ đồ dưới đây mô tả kiến trúc theo mô hình **C4 Container Level**:

```
╔══════════════════════════════════════════════════════════════════════════════════╗
║                          SHOPMART SYSTEM (C4 Container)                          ║
║                                                                                  ║
║  ┌──────────────────────────────────────────────────────────────────────────┐    ║
║  │                         USER INTERFACES                                  │    ║
║  │  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────────┐     │    ║
║  │  │  Customer Web   │  │   Seller Portal  │  │    Admin Portal     │     │    ║
║  │  │  (Next.js SSR)  │  │   (Next.js SSR)  │  │    (Next.js SSR)    │     │    ║
║  │  └────────┬────────┘  └────────┬─────────┘  └──────────┬──────────┘     │    ║
║  └───────────┼───────────────────┼────────────────────────┼────────────────┘    ║
║              │     HTTPS/JSON    │                        │                     ║
║  ┌───────────▼───────────────────▼────────────────────────▼────────────────┐    ║
║  │                    SPRING BOOT BACKEND API                               │    ║
║  │  ┌─────────────┐ ┌─────────────┐ ┌───────────────┐ ┌─────────────────┐ │    ║
║  │  │  Identity   │ │  Catalog &  │ │  Cart/Checkout│ │  Order &        │ │    ║
║  │  │  & Access   │ │  Discovery  │ │  & Payment    │ │  Fulfillment    │ │    ║
║  │  └─────────────┘ └─────────────┘ └───────────────┘ └─────────────────┘ │    ║
║  │  ┌─────────────┐ ┌─────────────┐ ┌───────────────┐ ┌─────────────────┐ │    ║
║  │  │  Inventory  │ │   Seller    │ │  Promotions   │ │  Admin Center   │ │    ║
║  │  │             │ │   Center    │ │               │ │                 │ │    ║
║  │  └─────────────┘ └─────────────┘ └───────────────┘ └─────────────────┘ │    ║
║  │  ┌─────────────┐ ┌─────────────────────────────────────────────────┐   │    ║
║  │  │ Notification│ │         Spring Security / JWT / RBAC             │   │    ║
║  │  └─────────────┘ └─────────────────────────────────────────────────┘   │    ║
║  └─────────────────────────────┬────────────────────────────────────────────┘   ║
║                                │  JPA/Hibernate                                 ║
║  ┌─────────────────────────────▼────────────────────────────────────────────┐   ║
║  │                          MySQL Database                                   │   ║
║  └───────────────────────────────────────────────────────────────────────────┘  ║
║                                                                                  ║
╚══════════════════════════════════════════════════════════════════════════════════╝

       ↕ HTTPS                ↕ HTTPS             ↕ HTTPS              ↕ HTTPS
  ┌────────────┐       ┌──────────────┐     ┌─────────────┐    ┌──────────────────┐
  │ Google     │       │ VNPay / MoMo │     │   Email     │    │ Media Storage    │
  │ OAuth2     │       │   Payment    │     │   Service   │    │    / CDN         │
  └────────────┘       └──────────────┘     └─────────────┘    └──────────────────┘
```

### 5.3 Thành phần chính và trách nhiệm

| Thành phần              | Trách nhiệm chính                                                                | Giao tiếp với                          |
| ----------------------- | -------------------------------------------------------------------------------- | --------------------------------------- |
| **Customer Web**        | Giao diện mua sắm: catalog, cart, checkout, tracking, wishlist, review           | Backend API (REST/JSON)                 |
| **Seller Portal**       | Giao diện quản lý: sản phẩm, đơn hàng, voucher, tồn kho, doanh thu              | Backend API (REST/JSON)                 |
| **Admin Portal**        | Giao diện quản trị: user, seller, sản phẩm, danh mục, analytics                 | Backend API (REST/JSON)                 |
| **Identity & Access**   | Đăng ký, đăng nhập, OAuth2, session, RBAC                                        | MySQL, Google OAuth2, Email Service     |
| **Catalog & Discovery** | Quản lý sản phẩm, danh mục, brand, tìm kiếm, lọc                                | MySQL                                   |
| **Cart & Checkout**     | Tính giá, áp voucher, tạo đơn multi-shop                                         | MySQL, Inventory, Promotions, Payment   |
| **Payment**             | Tạo yêu cầu thanh toán, xử lý callback idempotent                               | VNPay/MoMo, MySQL (IdempotencyKey)      |
| **Order & Fulfillment** | State machine vòng đời đơn, lịch sử trạng thái, hoàn tiền                       | MySQL, Notification, Inventory          |
| **Inventory**           | Quản lý tồn kho SKU, reservation, log biến động                                  | MySQL                                   |
| **Seller Center**       | Hồ sơ shop, CRUD sản phẩm/SKU, dashboard seller                                  | MySQL, Inventory, Catalog               |
| **Promotions**          | Coupon toàn sàn, voucher theo shop, validate và ghi nhận sử dụng                 | MySQL                                   |
| **Admin Center**        | Duyệt shop, kiểm duyệt sản phẩm, quản lý user, analytics                        | MySQL                                   |
| **Notification**        | Gửi thông báo in-app và email theo sự kiện nghiệp vụ                             | MySQL, Email Service                    |
| **MySQL Database**      | Lưu trữ toàn bộ dữ liệu quan hệ của hệ thống                                    | Backend API (qua JPA/Hibernate)         |

---

## 6. Các view kiến trúc chi tiết

### 6.1 View logic (Logical View)

#### Mục đích
View này trả lời câu hỏi: **"Hệ thống được phân chia thành các module logic nào? Mỗi module chịu
trách nhiệm gì? Quy tắc phụ thuộc giữa các module là gì?"**

#### Cấu trúc phân tầng backend

Mỗi domain module tuân thủ kiến trúc phân tầng **nghiêm ngặt một chiều**:

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────┐
│         api/controller/                 │  ← Nhận và validate HTTP request
│         api/dto/                        │  ← Request/Response data contracts
│         api/security/                   │  ← JWT filter, OAuth2 handlers
└──────────────────┬──────────────────────┘
                   │ (gọi Service, không bao giờ Repository)
                   ▼
┌─────────────────────────────────────────┐
│       application/service/              │  ← Nghiệp vụ, transaction boundary
└──────────────────┬──────────────────────┘
                   │ (gọi Repository, không bao giờ Controller)
                   ▼
┌─────────────────────────────────────────┐
│   infrastructure/persistence/mysql/     │
│         entity/                         │  ← JPA entity mappings
│         repository/                     │  ← Spring Data JPA interfaces
└──────────────────┬──────────────────────┘
                   │ JPA/Hibernate
                   ▼
              [MySQL Database]
```

#### Quy tắc phụ thuộc bắt buộc

| Quy tắc | Mô tả                                                                               |
| ------- | ----------------------------------------------------------------------------------- |
| R-01    | `controller` chỉ gọi `service` — không gọi `repository` hoặc `controller` khác    |
| R-02    | `service` chứa toàn bộ logic nghiệp vụ và transaction boundary                     |
| R-03    | `repository` chỉ tương tác với JPA entity — không có logic nghiệp vụ               |
| R-04    | `dto` chỉ tồn tại ở tầng `controller` — entity không được lộ ra ngoài API layer    |
| R-05    | `shared/util` không phụ thuộc lên class domain-specific bất kỳ                     |

#### Cấu trúc package backend

```
com.example.ecommerce.ecommerce_backend/
├── api/
│   ├── config/SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── PublicCatalogController.java
│   │   ├── AdminCatalogController.java
│   │   ├── SellerVoucherController.java
│   │   ├── AdminOrderController.java
│   │   └── CustomErrorController.java
│   ├── dto/
│   │   ├── admin/DashboardStatsResponse.java
│   │   └── voucher/DuplicateVoucherRequest.java
│   └── security/
│       ├── OAuth2SuccessHandler.java
│       └── OAuth2FailureHandler.java
├── application/service/
│   ├── AuthService.java
│   ├── CatalogService.java
│   ├── ShopService.java
│   ├── SellerOrderService.java
│   ├── SellerVoucherService.java
│   └── AdminDashboardService.java
├── domain/order/Order.java
├── infrastructure/
│   ├── bootstrap/
│   │   ├── SellerDataSeeder.java
│   │   └── ShopDataSeeder.java
│   └── persistence/mysql/
│       ├── entity/          ← 30+ JPA entity classes
│       └── repository/      ← Spring Data JPA repositories
└── shared/util/ResponseFormatter.java
```

#### Cấu trúc package frontend

```
Frontend/
├── app/
│   ├── products/[slug]/        ← Trang chi tiết sản phẩm (Customer)
│   ├── shop/[id]/              ← Trang gian hàng
│   ├── seller/                 ← Seller portal routes
│   │   ├── dashboard/
│   │   ├── products/
│   │   ├── orders/
│   │   ├── vouchers/
│   │   └── finance/revenue/
│   ├── admin/(dashboard)/      ← Admin portal routes
│   │   ├── dashboard/
│   │   ├── users/
│   │   ├── sellers/
│   │   ├── products/
│   │   ├── catalog/
│   │   ├── analytics/
│   │   └── orders/
│   └── oauth2/callback/        ← OAuth2 redirect handler
├── components/                 ← Reusable React components
├── services/                   ← API service abstraction layer
└── lib/                        ← auth-context, axios instance, utils
```

### 6.2 View xử lý / tiến trình (Process / Runtime View)

#### Mục đích
View này trả lời câu hỏi: **"Các component tương tác với nhau như thế nào khi hệ thống chạy?
Các luồng nghiệp vụ chính diễn ra theo trình tự nào?"**

#### Luồng 1: Đăng ký tài khoản và xác thực email

```
Trình duyệt          Frontend              AuthController         AuthService          EmailService
    │                    │                      │                      │                    │
    │──POST /register────►│                      │                      │                    │
    │                    │──POST /auth/register──►│                      │                    │
    │                    │                      │── validateInput()────►│                    │
    │                    │                      │                      │── checkEmailExist() │
    │                    │                      │                      │── createUser()      │
    │                    │                      │                      │── hashBCrypt()      │
    │                    │                      │                      │── genVerifyToken()  │
    │                    │                      │                      │────────────────────►│
    │                    │                      │                      │                    │── sendEmail()
    │                    │◄─201 Created─────────│                      │                    │
    │◄─ Thông báo kiểm email─│                  │                      │                    │
    │                    │                      │                      │                    │
    │──GET /verify?token──►│                     │                      │                    │
    │                    │──GET /auth/verify────►│                      │                    │
    │                    │                      │── verifyToken()──────►│                    │
    │                    │                      │                      │── activateUser()    │
    │◄─ 200 OK (Đã xác thực) ─────────────────│                      │                    │
```

#### Luồng 2: Checkout và thanh toán online (VNPay/MoMo)

```
Customer     Frontend      Backend API       DB (MySQL)      Payment Gateway     Notification
    │            │               │                │                 │                  │
    │──Checkout──►│               │                │                 │                  │
    │            │──POST /order──►│                │                 │                  │
    │            │               │─validateCart()─►│                 │                  │
    │            │               │─applyVoucher()─►│                 │                  │
    │            │               │                 │                 │                  │
    │            │               │─ BEGIN TRANSACTION ──────────────────────────────   │
    │            │               │─createOrder()──►│                 │                  │
    │            │               │─reserveStock()─►│                 │                  │
    │            │               │─ COMMIT ────────────────────────────────────────    │
    │            │               │                 │                 │                  │
    │            │               │─createPaymentRequest()────────────►│                 │
    │            │               │◄──paymentUrl────────────────────│                  │
    │            │◄─200 {url}────│                 │                 │                  │
    │◄──redirect to gateway──────│                 │                 │                  │
    │                            │                 │                 │                  │
    │─── [Người dùng thanh toán trên gateway] ─────────────────────►│                  │
    │                            │                 │                 │── POST /callback─►│
    │                            │                 │                 │  (tới backend)   │
    │                            │◄────────────────────────────────── callback         │
    │                            │─verifySignature()                 │                  │
    │                            │─checkIdempotencyKey()──────────►│                  │
    │                            │─ BEGIN TRANSACTION ─────────────────────────────   │
    │                            │─updateOrder(CONFIRMED)──────────►│                  │
    │                            │─saveIdempotencyKey()────────────►│                  │
    │                            │─ COMMIT ──────────────────────────────────────      │
    │                            │─────────────────────────────────────────────────►│
    │                            │                 │                 │  notify Customer  │
    │                            │                 │                 │  notify Seller    │
```

#### Luồng 3: Seller xử lý đơn hàng

```
Seller Portal      Backend API         OrderService         NotificationService
    │                   │                   │                       │
    │──GET /seller/orders────────────────►│                       │
    │◄──[Danh sách đơn]──────────────────│                       │
    │                   │                   │                       │
    │──PUT /orders/{id}/confirm──────────►│                       │
    │                   │─validateOwner()──►│                       │
    │                   │─validateTransition(→CONFIRMED)            │
    │                   │─updateStatus()───►│                       │
    │                   │─logHistory()──────►│                       │
    │                   │─────────────────────────────────────────►│
    │                   │                   │   notifyCustomer()    │
    │◄──200 OK──────────│                   │                       │
```

#### Luồng 4: Xử lý đăng nhập thất bại

```
Client                Spring Security Filter          Log / Monitor
  │                            │                            │
  │── Request (JWT hết hạn) ──►│                            │
  │                            │─ validateToken() ─► FAIL   │
  │                            │────────────────────────────►│
  │                            │   logSecurityEvent()        │
  │◄── HTTP 401 Unauthorized ──│                            │
```

#### Xử lý Concurrency và Background Jobs

| Cơ chế                      | Mô tả                                                                          |
| --------------------------- | ------------------------------------------------------------------------------ |
| **Transaction isolation**   | `READ_COMMITTED` cho phần lớn luồng; transaction scope tối thiểu và rõ ràng   |
| **Stock reservation lock**  | Reservation tạo trong cùng transaction với Order — rollback tự động nếu lỗi   |
| **Async notification**      | Gửi thông báo sau khi commit transaction chính — lỗi notification không rollback đơn hàng |
| **Idempotency key check**   | Kiểm tra trước khi xử lý callback — đảm bảo at-most-once execution           |
| **Payment callback retry**  | Gateway có thể retry callback; idempotency key ngăn xử lý trùng              |

### 6.3 View triển khai (Deployment View)

#### Mục đích
View này trả lời câu hỏi: **"Hệ thống được triển khai ở đâu? Topology hạ tầng như thế nào?
Pipeline từ code đến production ra sao?"**

#### Topology triển khai mục tiêu

```
                  ┌────────────────────────────────────────────────┐
                  │            Internet / DNS / CDN                │
                  └─────────────────────┬──────────────────────────┘
                                        │ HTTPS
                          ┌─────────────▼──────────────┐
                          │        Load Balancer        │
                          │  (HTTPS termination, L7)    │
                          │  Health check → /health     │
                          └────┬─────────────────┬──────┘
                               │                 │
               ┌───────────────▼──┐         ┌────▼──────────────────┐
               │ Backend App #1   │   ...   │ Backend App #N        │
               │ Spring Boot :8080│         │ Spring Boot :8080      │
               │ /actuator/health │         │ /actuator/health       │
               └────────┬─────────┘         └────────────┬──────────┘
                        │                               │
                        └──────────────┬────────────────┘
                                       │ JDBC/TLS
                          ┌────────────▼────────────┐
                          │     MySQL Primary        │
                          │  (với Read Replica       │
                          │   nếu cần scale đọc)     │
                          └─────────────────────────┘

               ┌──────────────────────────────────────┐
               │  Next.js Frontend                    │
               │  (SSR Server hoặc Static + CDN)      │
               └──────────────────────────────────────┘

               ┌──────────────────────────────────────┐
               │         Observability Stack           │
               │  Logs ──► Log Aggregator (ELK/equiv) │
               │  Metrics ──► Prometheus + Grafana     │
               │  Traces ──► Correlation ID logging   │
               └──────────────────────────────────────┘
```

#### Chiến lược môi trường

| Môi trường      | Mục đích                                   | Dữ liệu                    | Trigger triển khai              |
| --------------- | ------------------------------------------ | -------------------------- | -------------------------------- |
| **Development** | Phát triển cục bộ, unit test               | Local MySQL / test data    | Developer tự chạy               |
| **Staging**     | Integration test, UAT, kiểm tra pre-release | Dữ liệu ẩn danh hóa       | Merge vào nhánh `develop`        |
| **Production**  | Phục vụ người dùng thực                    | Dữ liệu production thực    | Merge vào `main` + manual gate   |

#### CI/CD Pipeline

```
┌───────────────────────────────────────────────────────────────────────┐
│                            CI/CD PIPELINE                             │
│                                                                       │
│  [1] Code Commit / PR      [2] Build & Test          [3] Code Quality │
│   └─ git push             ──► └─ mvn clean test      ──► └─ SAST     │
│   └─ PR raised                └─ npm test                └─ lint     │
│                                                                       │
│  [4] Build Artifact        [5] Deploy Staging        [6] Smoke Test  │
│   └─ mvn package          ──► └─ DB migration       ──► └─ /health   │
│   └─ docker build              └─ rolling deploy         └─ API test │
│                                                                       │
│  [7] Manual Approval Gate  [8] Deploy Production    [9] Post-Deploy  │
│   └─ security review      ──► └─ blue-green /       ──► └─ monitors │
│   └─ team sign-off              rolling                └─ alerts    │
│                                                                       │
│  [AUTO ROLLBACK] ◄──────── health check fail bất kỳ bước nào        │
└───────────────────────────────────────────────────────────────────────┘
```

#### Chiến lược Rollback

| Loại Rollback          | Cơ chế                                                                  | SLO thời gian       |
| ---------------------- | ----------------------------------------------------------------------- | ------------------- |
| **Application**        | Revert về artifact/image version trước; redeploy                        | <10 phút            |
| **Database schema**    | Script Flyway rollback hoặc forward-only migration với compatibility    | <15 phút            |
| **Feature flag**       | Toggle tắt tính năng mà không cần redeploy (cho high-risk feature)      | <2 phút             |

### 6.4 View dữ liệu (Data View)

#### Mục đích
View này trả lời câu hỏi: **"Dữ liệu của hệ thống được cấu trúc như thế nào? Ai sở hữu dữ liệu
nào? Transaction boundary nằm ở đâu?"**

#### Các nhóm thực thể chính

**Nhóm Định danh và Phiên**
```
User (1)─────────── (1) UserProfile
User (1)─────────── (*) Role              [many-to-many: user_roles]
User (1)─────────── (*) RefreshSession
User (1)─────────── (*) TrustedDevice
User (1)─────────── (*) UserAddress
User (1)─────────── (1) SellerProfile
                         └──(1) SellerShop
                                    └──(*) ShopStatusHistory
```

**Nhóm Catalog**
```
Category (tự tham chiếu parent-child)
    └──(*) CategoryAttributeEntity    ── (*) AttributeGroup ── (*) Attribute
Brand (1)──────── (*) Product
Product (1)─────── (*) SKU
Product (1)─────── (*) ProductImage
Product (1)─────── (*) ProductAttributeValue
Product (1)─────── (*) ProductStatusHistory
SKU (1)──────────── (*) OptionValue      [via sku_option_values]
OptionGroup (1)─────(*) OptionValue
```

**Nhóm Đơn hàng và Thanh toán**
```
Order (1)──────── (*) OrderItem ─── (*)──(1) SKU
Order (1)──────── (*) OrderStatusHistory
Order (1)──────── (*) Refund
IdempotencyKey                        [dedup payment callbacks]
StockReservation (*) ── (1) SKU
InventoryLog     (*) ── (1) SKU
```

**Nhóm Khuyến mãi**
```
Coupon (1)──────── (*) CouponUsage
SellerVoucher (1)── (*) SellerVoucherUsage
SellerVoucher (*) ─ (1) SellerShop
```

**Nhóm Tương tác khách hàng**
```
Review (*) ── (1) Product
Review (*) ── (1) User
ReviewHelpful     [pivot: user × review]
WishlistItem (*) ── (1) User
WishlistItem (*) ── (1) Product
Notification (*) ── (1) User
```

#### Transaction Boundary

| ID      | Thao tác trong transaction                                          | Lý do cần atomic                                   |
| ------- | ------------------------------------------------------------------- | --------------------------------------------------- |
| TXN-01  | Tạo Order + tạo StockReservation + trừ tồn kho SKU                 | Tránh overselling; consistency giữa đơn và kho     |
| TXN-02  | Nhận callback + Lưu IdempotencyKey + Cập nhật trạng thái Order     | Đảm bảo exactly-once; tránh trạng thái không nhất quán |
| TXN-03  | Hủy Order + Giải phóng StockReservation + Ghi InventoryLog         | Kho phải được hoàn lại đúng khi đơn bị hủy        |
| TXN-04  | Xử lý Refund + Cập nhật trạng thái Order + Ghi Refund record       | Tính nhất quán của trạng thái hoàn tiền            |

#### Chiến lược Index

| Bảng                   | Cột được index                               | Mục đích                            |
| ---------------------- | -------------------------------------------- | ----------------------------------- |
| `products`             | `name`, `category_id`, `brand_id`, `status`  | Tìm kiếm và lọc sản phẩm           |
| `orders`               | `user_id`, `seller_shop_id`, `status`, `created_at` | Liệt kê đơn theo user/seller  |
| `stock_reservations`   | `sku_id`, `order_id`                         | Tra cứu reservation theo SKU        |
| `seller_vouchers`      | `shop_id`, `code`, `is_active`               | Validate voucher khi checkout       |
| `refresh_sessions`     | `user_id`, `token_hash`                      | Tra cứu session khi refresh token   |
| `idempotency_keys`     | `key_value`                                  | Dedup payment callback              |

#### Audit Trail

Các bảng lịch sử sau lưu toàn bộ biến đổi trạng thái phục vụ audit và truy vết:

| Bảng                        | Domain               | Ghi lại                                          |
| --------------------------- | -------------------- | ------------------------------------------------- |
| `order_status_history`      | Order                | Mọi thay đổi trạng thái đơn: ai, khi nào, từ→đến |
| `product_status_history`    | Catalog              | Mọi thay đổi trạng thái kiểm duyệt sản phẩm     |
| `shop_status_history`       | Seller               | Duyệt/từ chối/đình chỉ gian hàng                 |
| `inventory_log`             | Inventory            | Mọi biến động tồn kho: lý do, tham chiếu         |

---

## 7. Tích hợp với hệ thống khác

### 7.1 Hệ thống tích hợp

| Hệ thống            | Mục đích tích hợp                                                                |
| ------------------- | -------------------------------------------------------------------------------- |
| **Google OAuth2**   | Cung cấp xác thực bên thứ ba (Social Login) cho người dùng                      |
| **VNPay**           | Xử lý thanh toán online qua cổng VNPay; nhận callback kết quả giao dịch         |
| **MoMo**            | Xử lý thanh toán online qua ví điện tử MoMo; nhận callback kết quả giao dịch    |
| **Email Service**   | Gửi email giao dịch: xác thực đăng ký, đặt lại mật khẩu, thông báo đơn hàng    |
| **Media Storage**   | Lưu trữ và phân phối hình ảnh sản phẩm; CDN để tối ưu tốc độ load ảnh          |

### 7.2 Giao diện tích hợp

| Hệ thống          | Giao thức     | Chiều dữ liệu      | Định dạng    | Tần suất        | Cơ chế bảo mật                          |
| ----------------- | ------------- | ------------------ | ------------ | --------------- | ---------------------------------------- |
| **Google OAuth2** | HTTPS/OAuth2  | 2 chiều            | JSON (JWT)   | Mỗi lần login   | PKCE/Authorization Code Flow; verify token với Google JWKS |
| **VNPay**         | HTTPS/REST    | 2 chiều            | JSON         | Mỗi giao dịch   | HMAC-SHA512 signature trên mọi request/callback |
| **MoMo**          | HTTPS/REST    | 2 chiều            | JSON         | Mỗi giao dịch   | RSA/HMAC signature; IP whitelist callback  |
| **Email Service** | HTTPS/SMTP    | 1 chiều (outbound) | HTML/Text    | Event-based     | API key; TLS transport                   |
| **Media Storage** | HTTPS/REST    | 2 chiều            | Binary/JSON  | Upload (Seller) | Signed URL; ACL bucket policy            |

#### Nguyên tắc tích hợp

1. **Timeout và Retry có Backoff** — tất cả lời gọi ra ngoài đều có timeout cấu hình; retry tối đa
   3 lần với exponential backoff.
2. **Xác thực chữ ký** — mọi callback từ payment gateway phải verify HMAC/RSA signature trước khi
   xử lý bất kỳ logic nghiệp vụ nào.
3. **Idempotency** — endpoint nhận callback được thiết kế idempotent; duplicate call không gây
   side effect nghiệp vụ.
4. **Secret Management** — API key, client secret, signing key quản lý qua biến môi trường/secret
   manager; không bao giờ hardcode trong source code.
5. **Graceful Degradation** — nếu Email Service hoặc Media Storage không available, core flow
   (đặt hàng, thanh toán) vẫn hoạt động; side effects được retry hoặc queue.

---

## 8. Các quyết định kiến trúc (ADR)

### 8.1 Monolith module hóa thay vì Microservices

| Trường              | Nội dung                                                                        |
| ------------------- | ------------------------------------------------------------------------------- |
| **Ngày**            | 18/05/2026                                                                      |
| **Trạng thái**      | Được chấp nhận                                                                  |
| **Bối cảnh**        | Cần deliver nền tảng e-commerce đầy đủ tính năng với team nhỏ và áp lực time-to-market. Có nhiều domain nghiệp vụ liên quan chặt chẽ (order, inventory, payment). |
| **Quyết định**      | Triển khai backend dưới dạng một Spring Boot application, tổ chức nội bộ thành các domain module riêng biệt. |
| **Các phương án đã cân nhắc** | (1) Microservices — loại vì overhead vận hành và distributed transaction; (2) Serverless — loại vì cold start latency và team chưa quen. |
| **Lý do lựa chọn**  | Loại bỏ hoàn toàn độ phức tạp distributed systems (service discovery, network calls, distributed tracing, saga); ACID transaction natively; deployment đơn giản; team nhỏ phù hợp. |
| **Hệ quả (+)**      | Tốc độ phát triển cao; transaction đơn giản; single deployment; dễ debug.      |
| **Hệ quả (–)**      | Toàn bộ ứng dụng phải scale cùng nhau; cần discipline để giữ module boundary.  |

---

### 8.2 MySQL làm kho dữ liệu chính

| Trường              | Nội dung                                                                        |
| ------------------- | ------------------------------------------------------------------------------- |
| **Ngày**            | 18/05/2026                                                                      |
| **Trạng thái**      | Được chấp nhận                                                                  |
| **Bối cảnh**        | Hệ thống xử lý dữ liệu giao dịch phức tạp: đặt hàng, thanh toán, tồn kho. Tính nhất quán dữ liệu là yêu cầu bắt buộc. |
| **Quyết định**      | Dùng MySQL 8.x làm kho dữ liệu quan hệ duy nhất cho tất cả domain.            |
| **Các phương án đã cân nhắc** | (1) PostgreSQL — năng lực tương đương, MySQL được chọn vì team đã quen; (2) MongoDB — loại vì không đáp ứng ACID cho order/payment. |
| **Lý do lựa chọn**  | ACID đầy đủ cho multi-table transaction; hỗ trợ JPA/Hibernate tốt; team có kinh nghiệm vận hành. |
| **Hệ quả (+)**      | Đảm bảo consistency; rich query capability; mature tooling.                     |
| **Hệ quả (–)**      | Write scaling ngang phức tạp hơn (sharding); schema migration tăng overhead khi release. |

---

### 8.3 JWT + Refresh Token cho xác thực

| Trường              | Nội dung                                                                        |
| ------------------- | ------------------------------------------------------------------------------- |
| **Ngày**            | 18/05/2026                                                                      |
| **Trạng thái**      | Được chấp nhận                                                                  |
| **Bối cảnh**        | API REST phục vụ nhiều client (web, tương lai là mobile); cần stateless auth scalable. |
| **Quyết định**      | Dùng JWT ngắn hạn (access token) kết hợp refresh token dài hạn lưu trong `RefreshSessionEntity`. |
| **Các phương án đã cân nhắc** | (1) Server-side session (Redis) — cần thêm infrastructure dependency; (2) Opaque token — không có lợi ích stateless. |
| **Lý do lựa chọn**  | Stateless JWT không cần shared session store giữa các instance; refresh token cho phép revoke session. |
| **Hệ quả (+)**      | Scale ngang không cần shared state; session có thể thu hồi.                    |
| **Hệ quả (–)**      | Access token không thể invalidate ngay khi hết hạn; rotation JWT secret làm mất toàn bộ session đang hoạt động. |

---

### 8.4 Xử lý Callback Thanh toán Idempotent

| Trường              | Nội dung                                                                        |
| ------------------- | ------------------------------------------------------------------------------- |
| **Ngày**            | 18/05/2026                                                                      |
| **Trạng thái**      | Được chấp nhận                                                                  |
| **Bối cảnh**        | VNPay và MoMo có thể gửi callback nhiều lần do network retry. Mỗi callback có thể trigger cập nhật trạng thái đơn hàng. |
| **Quyết định**      | Mỗi callback được gắn `IdempotencyKeyEntity` duy nhất; kiểm tra key trước khi thực hiện bất kỳ thay đổi nghiệp vụ nào. |
| **Các phương án đã cân nhắc** | (1) Last-write-wins — không chấp nhận vì ảnh hưởng tài chính; (2) Message queue với dedup — ngoài phạm vi hiện tại. |
| **Lý do lựa chọn**  | Bảo toàn tính nhất quán tài chính là yêu cầu bắt buộc không thương lượng.     |
| **Hệ quả (+)**      | Đảm bảo exactly-once execution cho callback; an toàn về tài chính.             |
| **Hệ quả (–)**      | Thêm một DB read mỗi callback; cần chiến lược dọn dẹp key hết hạn.            |

---

### 8.5 State Machine cho vòng đời đơn hàng

| Trường              | Nội dung                                                                        |
| ------------------- | ------------------------------------------------------------------------------- |
| **Ngày**            | 18/05/2026                                                                      |
| **Trạng thái**      | Được chấp nhận                                                                  |
| **Bối cảnh**        | Đơn hàng có nhiều trạng thái; nhiều actor (customer, seller, admin) có thể tác động; cần ngăn chuyển trạng thái không hợp lệ. |
| **Quyết định**      | Trạng thái đơn hàng được quản lý bằng finite state machine tường minh trong service layer. Mọi transition đều được log vào `OrderStatusHistory`. |
| **Sơ đồ trạng thái** | `PENDING → CONFIRMED → PREPARING → SHIPPED → DELIVERED` / `→ CANCELLED` / `→ REFUND_REQUESTED → REFUNDED` |
| **Lý do lựa chọn**  | Ngăn transition bất hợp lệ; cung cấp audit trail đầy đủ; logic rõ ràng cho mọi actor. |
| **Hệ quả (+)**      | Rõ ràng nghiệp vụ; audit đầy đủ; dễ test.                                     |
| **Hệ quả (–)**      | Service layer phải cập nhật mỗi khi thêm trạng thái mới.                       |

---

### 8.6 Phân tầng nghiêm ngặt (Controller → Service → Repository)

| Trường              | Nội dung                                                                        |
| ------------------- | ------------------------------------------------------------------------------- |
| **Ngày**            | 18/05/2026                                                                      |
| **Trạng thái**      | Được chấp nhận                                                                  |
| **Bối cảnh**        | Codebase đang phát triển với nhiều developer; cần ngăn coupling chéo tầng gây khó test và bảo trì. |
| **Quyết định**      | Bắt buộc chiều phụ thuộc Controller → Service → Repository; DTO chỉ ở tầng API; entity không được lộ ra ngoài API layer. |
| **Lý do lựa chọn**  | Cho phép test độc lập từng tầng; thay đổi persistence không ảnh hưởng API contract; dễ onboarding. |
| **Hệ quả (+)**      | Dễ test; dễ bảo trì; rõ ràng cho developer mới.                                |
| **Hệ quả (–)**      | Có thể sinh boilerplate mapping DTO ↔ Entity; cần discipline tuân thủ quy tắc. |

---

## 9. Đảm bảo chất lượng kiến trúc

### 9.1 Cách kiến trúc đáp ứng yêu cầu

#### Hiệu năng

| Tactic áp dụng                    | Đáp ứng QAR nào              | Cơ chế kỹ thuật                                              |
| --------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| Phân trang bắt buộc               | QAR-PERF-01, QAR-PERF-02     | Mọi endpoint list đều trả về page/size; không bao giờ trả toàn bộ |
| Composite index trên bảng sản phẩm | QAR-PERF-02                 | Index trên `name`, `category_id`, `brand_id`, `status`       |
| Async notification                | QAR-PERF-01                  | Gửi thông báo sau commit transaction; không block response   |
| N+1 query prevention              | QAR-PERF-01                  | Sử dụng JPA JOIN FETCH cho các liên kết được load trong request |

#### Bảo mật

| Tactic áp dụng                    | Đáp ứng QAR nào              | Cơ chế kỹ thuật                                              |
| --------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| JWT filter chain toàn cục         | QAR-SEC-01                   | Spring Security `SecurityFilterChain`; mọi request qua filter |
| RBAC tại service layer            | QAR-SEC-01                   | `@PreAuthorize("hasRole('SELLER')")` trên service method     |
| HMAC signature verification       | QAR-SEC-02                   | Verify signature trước mọi logic trong callback handler      |
| BCrypt password hashing           | QAR-SEC-01                   | Cost factor ≥10; không bao giờ log password                  |
| Secret management                 | QAR-SEC-02                   | API key, JWT secret qua environment variable / secret store  |

#### Khả dụng

| Tactic áp dụng                    | Đáp ứng QAR nào              | Cơ chế kỹ thuật                                              |
| --------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| Load balancer + multiple instances | QAR-AVAIL-01                 | Backend scale ngang; stateless (không lưu session in-memory) |
| Health check endpoint             | QAR-AVAIL-01                 | Spring Actuator `/actuator/health`; load balancer kiểm tra   |
| Circuit breaker ngoài (optional)  | QAR-AVAIL-01                 | Timeout + retry cho external call; fallback graceful         |
| Auto rollback CI/CD               | QAR-AVAIL-01                 | Pipeline rollback khi health check fail sau deploy           |

#### Khả năng bảo trì

| Tactic áp dụng                    | Đáp ứng QAR nào              | Cơ chế kỹ thuật                                              |
| --------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| Domain module separation          | QAR-MOD-01                   | Package theo domain; không cross-domain dependency trực tiếp |
| Business rule trong service layer | QAR-MOD-01                   | Controller zero-logic; repository zero-logic                 |
| DTO at API boundary               | QAR-MOD-01                   | Entity thay đổi không ảnh hưởng API contract                 |

### 9.2 Đánh giá và kiểm chứng

#### Kế hoạch kiểm chứng kiến trúc

| Hoạt động                   | Mục tiêu kiểm chứng                               | Trạng thái    |
| --------------------------- | -------------------------------------------------- | ------------- |
| Unit test cho Service layer | Nghiệp vụ đúng; transaction rollback đúng          | Đang thực hiện |
| Integration test checkout flow | Luồng đặt hàng + payment callback end-to-end   | Kế hoạch      |
| Load test (k6 / JMeter)     | Xác nhận SLO latency ở mức tải mục tiêu           | Kế hoạch      |
| Security review             | JWT implementation; payment signature; RBAC gaps   | Kế hoạch      |
| Database query review       | Phát hiện N+1; xác nhận index coverage             | Kế hoạch      |
| Penetration test (basic)    | OWASP Top 10 trên các endpoint chính               | Tương lai      |

#### Rủi ro còn tồn đọng

| Rủi ro                                          | Khả năng xảy ra | Mức ảnh hưởng | Biện pháp giảm thiểu                             |
| ----------------------------------------------- | --------------- | ------------- | ------------------------------------------------- |
| N+1 query trong product listing chưa được review | Trung bình     | Cao           | Query review bắt buộc trước production release    |
| JWT secret rotation làm mất session toàn bộ     | Thấp           | Cao           | Lên quy trình rotation có thông báo trước         |
| Monolith trở thành bottleneck khi traffic tăng  | Trung bình      | Trung bình    | Thiết kế module boundary sẵn sàng extract service |

---

## 10. Rủi ro, hạn chế và hạng mục mở

### 10.1 Rủi ro kiến trúc

| ID    | Rủi ro                                                    | Khả năng | Ảnh hưởng | Biện pháp giảm thiểu                                        |
| ----- | --------------------------------------------------------- | -------- | --------- | ------------------------------------------------------------ |
| R-01  | Quá tải trong flash sale / promotional campaign           | Cao      | Cao       | Scale ngang backend; cache read-heavy endpoint; rate limiting |
| R-02  | Sai lệch trạng thái thanh toán do callback trùng/mất     | Trung bình | Rất cao  | Idempotency key; verify signature; đối soát định kỳ         |
| R-03  | Overselling do race condition khi inventory reservation   | Trung bình | Cao      | Explicit lock trong transaction TXN-01; test concurrent checkout |
| R-04  | Module boundary bị vi phạm theo thời gian (architectural decay) | Cao  | Trung bình | Code review checklist; architecture fitness function test    |
| R-05  | Dependency bên thứ ba (VNPay, MoMo) không available       | Thấp     | Rất cao   | Timeout/retry với backoff; graceful error message cho user   |
| R-06  | Nợ kỹ thuật tích lũy khi backlog tăng nhanh              | Cao      | Trung bình | Test tự động cho luồng cốt lõi; refactor định kỳ theo sprint |

### 10.2 Hạn chế đã chấp nhận

| Hạn chế                                                        | Lý do chấp nhận                                                  |
| -------------------------------------------------------------- | ------------------------------------------------------------------ |
| Toàn bộ backend phải scale cùng nhau (không scale độc lập module) | Trade-off của monolith; chấp nhận ở giai đoạn đầu để giảm complexity |
| Không có real-time full-text search (Elasticsearch)            | MySQL full-text đủ cho giai đoạn đầu; Elasticsearch trong roadmap |
| Không có event-driven async messaging (Kafka/RabbitMQ)         | Notification dispatch in-process đủ cho scale hiện tại           |
| Không có Redis cache tầng cao                                  | Chưa cần thiết; thêm vào khi profiling cho thấy bottleneck        |
| JWT secret rotation làm mất toàn bộ active session            | Acceptable với quy trình thông báo trước; cải thiện khi có thời gian |

### 10.3 Hạng mục mở (Open Issues)

| ID    | Câu hỏi / Vấn đề còn mở                                                        | Ưu tiên | Dự kiến giải quyết |
| ----- | ------------------------------------------------------------------------------- | ------- | ------------------- |
| OI-01 | Chiến lược caching cụ thể cho catalog/search: Redis vs in-memory vs CDN?        | Cao     | Sprint sau          |
| OI-02 | Cần message queue (Kafka) cho notification không? Khi nào trigger?             | Trung bình | Q3/2026          |
| OI-03 | Cơ chế refresh JWT với rotation có cần triển khai ngay không?                  | Trung bình | Sprint sau       |
| OI-04 | Elasticsearch cho search có cần trong roadmap 6 tháng tới không?               | Trung bình | Cần quyết định   |
| OI-05 | Khi nào tách Payment service ra thành independent service?                      | Thấp    | Khi traffic tăng    |
| OI-06 | Chiến lược database migration tool cụ thể: Flyway hay Liquibase?                | Thấp    | Sprint này          |
| OI-07 | Multi-region deployment có trong roadmap không?                                 | Thấp    | Năm sau             |

---

## 11. Phụ lục

### 11.1 Thuật ngữ và từ viết tắt

| Thuật ngữ              | Giải nghĩa                                                                                  |
| ---------------------- | ------------------------------------------------------------------------------------------- |
| **ADR**                | Architecture Decision Record — tài liệu ghi lại một quyết định kiến trúc quan trọng        |
| **ACID**               | Atomicity, Consistency, Isolation, Durability — thuộc tính giao dịch database quan hệ       |
| **BCrypt**             | Thuật toán hash mật khẩu thích nghi với cost factor có thể điều chỉnh                       |
| **COD**                | Cash on Delivery — thanh toán khi nhận hàng                                                 |
| **DTO**                | Data Transfer Object — đối tượng truyền dữ liệu giữa tầng API và tầng service               |
| **Idempotency Key**    | Khóa duy nhất đảm bảo một thao tác chỉ được xử lý đúng một lần dù gửi nhiều lần            |
| **JWT**                | JSON Web Token — chuẩn token compact cho authentication                                     |
| **MTTR**               | Mean Time to Recovery — thời gian trung bình khôi phục sau sự cố                           |
| **OAuth2**             | Open Authorization 2.0 — giao thức ủy quyền tiêu chuẩn cho social login                   |
| **RBAC**               | Role-Based Access Control — kiểm soát truy cập dựa trên vai trò                            |
| **REST**               | Representational State Transfer — kiểu kiến trúc API                                        |
| **RPO**                | Recovery Point Objective — lượng dữ liệu tối đa có thể mất khi có sự cố                    |
| **RTO**                | Recovery Time Objective — thời gian tối đa hệ thống được phép ngừng hoạt động              |
| **SAST**               | Static Application Security Testing — phân tích bảo mật tĩnh trên source code              |
| **SKU**                | Stock Keeping Unit — đơn vị biến thể sản phẩm có tồn kho riêng                             |
| **SLI / SLO**          | Service Level Indicator / Objective — chỉ tiêu và mục tiêu mức dịch vụ                     |
| **SSR**                | Server-Side Rendering — render HTML trên server trước khi gửi về client                     |
| **TLS**                | Transport Layer Security — giao thức mã hóa truyền thông mạng                              |
| **Transaction Boundary** | Phạm vi các thao tác DB được coi là một đơn vị nguyên tử                                 |

### 11.2 Tài liệu tham khảo

| STT | Tài liệu / Nguồn                                                                                 |
| --- | ------------------------------------------------------------------------------------------------ |
| 1   | `docs/ARCHITECTURE_DESIGN_AND_QUALITY_ATTRIBUTES_METASHOP.md` (nội bộ)                          |
| 2   | ShopMart Product Backlog (nội bộ)                                                               |
| 3   | IEEE Std 42010-2011: Systems and software engineering — Architecture description                |
| 4   | Clements et al., *Documenting Software Architectures: Views and Beyond*, 2nd Ed., SEI Press     |
| 5   | Spring Boot Reference Documentation — https://docs.spring.io/spring-boot                       |
| 6   | Spring Security Reference — https://docs.spring.io/spring-security                             |
| 7   | Next.js Documentation — https://nextjs.org/docs                                                 |
| 8   | VNPay Integration Documentation (tài liệu partner nội bộ)                                      |
| 9   | MoMo Payment API Documentation (tài liệu partner nội bộ)                                       |
| 10  | Google OAuth 2.0 — https://developers.google.com/identity/protocols/oauth2                     |
| 11  | OWASP Top 10 — https://owasp.org/www-project-top-ten/                                          |
| 12  | Hohpe & Woolf, *Enterprise Integration Patterns*, Addison-Wesley                               |

### 11.3 Mapping Yêu cầu ↔ Kiến trúc (Traceability Matrix)

| Business Req.       | Module Logic    | Package Backend                          | Thực thể DB chính            | ADR liên quan  |
| ------------------- | --------------- | ---------------------------------------- | ----------------------------- | -------------- |
| Đăng ký/đăng nhập  | Identity        | `controller/AuthController`              | `users`, `refresh_sessions`   | ADR-03         |
| OAuth2 Google       | Identity        | `security/OAuth2SuccessHandler`          | `users`, `refresh_sessions`   | ADR-03         |
| Tìm kiếm sản phẩm  | Catalog         | `controller/PublicCatalogController`     | `products`, `categories`      | ADR-02         |
| Đặt hàng + tồn kho | Cart/Order/Inventory | `application/service/CatalogService` | `orders`, `stock_reservations` | ADR-01, ADR-02 |
| Thanh toán online   | Payment         | Payment integration service              | `orders`, `idempotency_keys`  | ADR-04         |
| Theo dõi đơn hàng  | Order           | `service/SellerOrderService`             | `order_status_history`        | ADR-05         |
| Seller quản lý SP  | Seller Center   | `controller/AdminCatalogController`      | `products`, `skus`            | ADR-06         |
| Admin analytics     | Admin Center    | `service/AdminDashboardService`          | Aggregation queries           | ADR-01         |
| Voucher seller      | Promotions      | `controller/SellerVoucherController`     | `seller_vouchers`             | ADR-06         |

---

*Kết thúc Tài liệu Kiến trúc Giải pháp — ShopMart v2.0*

*Document ID: SAD-SHOPMART-v2.0 | Cập nhật lần cuối: 18/05/2026*
