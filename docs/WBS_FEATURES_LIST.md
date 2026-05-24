# Bảng Đặc tả Chức năng & Phân rã Công việc (WBS / Feature List)
## Nền tảng Thương mại Điện tử ShopMart (Meta-Shop)

Tài liệu này phân rã toàn bộ chức năng của hệ thống ShopMart dựa trên tài liệu thiết kế kiến trúc giải pháp (SAD) và tình trạng thực tế của dự án. Bảng chức năng được chuẩn hóa theo đúng cấu trúc template được yêu cầu.

### Bảng Feature List / WBS Chi Tiết

| ID | Nhóm chức năng (Name) | Chức năng con (Sub functions) | User | Buyer | Seller | Admin | CS | PC | Mobile | Levels | Etd Time |
| :--- | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **F-01** | **Xác thực & Phân quyền (Auth)** | | | | | | | | | | |
| F-01.01 | | Đăng ký tài khoản bằng Email/Mật khẩu | ✓ | - | - | - | - | ✓ | ✓ | L1 | 1.5d |
| F-01.02 | | Đăng nhập tài khoản bằng Email/Mật khẩu | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L1 | 1.0d |
| F-01.03 | | Đăng nhập bằng tài khoản mạng xã hội (Google OAuth2) | ✓ | ✓ | ✓ | - | - | ✓ | ✓ | L1 | 2.0d |
| F-01.04 | | Gửi email xác thực & Kích hoạt tài khoản (Verify Link) | ✓ | - | - | - | - | ✓ | ✓ | L1 | 1.0d |
| F-01.05 | | Yêu cầu đặt lại mật khẩu (Quên mật khẩu / Reset Password) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L1 | 1.5d |
| F-01.06 | | Thay đổi mật khẩu & Cập nhật thông tin cá nhân | - | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L2 | 1.0d |
| F-01.07 | | Đăng xuất & Thu hồi phiên đăng nhập (Revoke Refresh Token) | - | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L1 | 0.5d |
| F-01.08 | | Kiểm soát truy cập dựa trên vai trò (RBAC Security Filter) | - | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L1 | 1.5d |
| **F-02** | **Danh mục & Tìm kiếm (Catalog)** | | | | | | | | | | |
| F-02.01 | | Xem cây danh mục sản phẩm đa cấp (Category tree) | ✓ | ✓ | - | - | - | ✓ | ✓ | L1 | 1.0d |
| F-02.02 | | Tìm kiếm sản phẩm theo từ khóa (Fulltext Search) | ✓ | ✓ | - | - | - | ✓ | ✓ | L1 | 1.5d |
| F-02.03 | | Lọc sản phẩm (Theo danh mục, thương hiệu, mức giá, rating) | ✓ | ✓ | - | - | - | ✓ | ✓ | L1 | 1.5d |
| F-02.04 | | Sắp xếp sản phẩm (Theo giá, ngày đăng, bán chạy nhất) | ✓ | ✓ | - | - | - | ✓ | ✓ | L2 | 0.5d |
| F-02.05 | | Xem chi tiết sản phẩm (Thông tin chung, hình ảnh, mô tả) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L1 | 1.5d |
| F-02.06 | | Lựa chọn các biến thể sản phẩm (SKUs: Màu sắc, kích thước...) | ✓ | ✓ | - | - | - | ✓ | ✓ | L1 | 1.5d |
| **F-03** | **Giỏ hàng & Đặt hàng (Cart/Checkout)**| | | | | | | | | | |
| F-03.01 | | Thêm sản phẩm/biến thể SKU vào giỏ hàng | - | ✓ | - | - | - | ✓ | ✓ | L1 | 1.0d |
| F-03.02 | | Cập nhật số lượng / Xóa sản phẩm khỏi giỏ hàng | - | ✓ | - | - | - | ✓ | ✓ | L1 | 0.5d |
| F-03.03 | | Áp dụng mã giảm giá (Coupon toàn sàn hoặc Voucher của Shop) | - | ✓ | - | - | - | ✓ | ✓ | L1 | 2.0d |
| F-03.04 | | Xem trước đơn hàng, tính toán tạm tính & phí vận chuyển | - | ✓ | - | - | - | ✓ | ✓ | L1 | 1.5d |
| F-03.05 | | Đặt hàng & Tạo đơn hàng đa shop (Multi-shop Order Split) | - | ✓ | - | - | - | ✓ | ✓ | L1 | 2.5d |
| **F-04** | **Cổng thanh toán & Giao dịch** | | | | | | | | | | |
| F-04.01 | | Lựa chọn phương thức thanh toán (COD hoặc Online) | - | ✓ | - | - | - | ✓ | ✓ | L1 | 0.5d |
| F-04.02 | | Tạo link thanh toán online chuyển hướng qua VNPay | - | ✓ | - | - | - | ✓ | ✓ | L1 | 1.5d |
| F-04.03 | | Tạo link thanh toán online chuyển hướng qua MoMo | - | ✓ | - | - | - | ✓ | ✓ | L1 | 1.5d |
| F-04.04 | | Xử lý callback kết quả thanh toán từ VNPay/MoMo (Idempotent) | - | ✓ | - | - | - | ✓ | ✓ | L1 | 2.0d |
| F-04.05 | | Tra cứu lịch sử thanh toán & Đối soát giao dịch tự động | - | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L2 | 2.0d |
| **F-05** | **Quản lý đơn hàng (Order Lifecycle)** | | | | | | | | | | |
| F-05.01 | | Xem danh sách đơn hàng đã đặt (Lọc theo trạng thái) | - | ✓ | - | - | - | ✓ | ✓ | L1 | 1.0d |
| F-05.02 | | Xem chi tiết đơn hàng & Lịch sử trạng thái đơn (Status History) | - | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | L1 | 1.5d |
| F-05.03 | | Khách hàng hủy đơn hàng (Khi đơn ở trạng thái PENDING/CONFIRMED)| - | ✓ | - | - | - | ✓ | ✓ | L1 | 1.0d |
| F-05.04 | | Khách hàng gửi yêu cầu trả hàng / hoàn tiền (Refund Request) | - | ✓ | - | - | - | ✓ | ✓ | L2 | 2.0d |
| F-05.05 | | Tự động hủy đơn hàng thanh toán online nếu quá hạn (Cron task) | - | - | - | - | - | - | - | L1 | 1.5d |
| **F-06** | **Quản lý tồn kho (Inventory)** | | | | | | | | | | |
| F-06.01 | | Giữ tồn kho tạm thời (Stock Reservation) khi tạo đơn hàng | - | - | - | - | - | - | - | L1 | 1.5d |
| F-06.02 | | Giải phóng tồn kho khi đơn hàng bị hủy hoặc hết hạn thanh toán | - | - | - | - | - | - | - | L1 | 1.0d |
| F-06.03 | | Cập nhật số lượng tồn kho khả dụng của các SKU | - | - | ✓ | ✓ | - | ✓ | ✓ | L1 | 1.0d |
| F-06.04 | | Ghi nhận nhật ký biến động tồn kho (Stock Movement Log) | - | - | ✓ | ✓ | - | ✓ | ✓ | L2 | 1.5d |
| **F-07** | **Kênh Người bán (Seller Center)** | | | | | | | | | | |
| F-07.01 | | Đăng ký thông tin Shop mới (Seller Onboarding Profile) | - | ✓ | - | - | - | ✓ | ✓ | L1 | 2.0d |
| F-07.02 | | Cập nhật hồ sơ Shop (Tên shop, logo, banner, địa chỉ, SĐT) | - | - | ✓ | - | - | ✓ | ✓ | L1 | 1.5d |
| F-07.03 | | CRUD Sản phẩm (Đăng bán mới, xem, sửa thông tin, xóa sản phẩm) | - | - | ✓ | - | - | ✓ | ✓ | L1 | 3.0d |
| F-07.04 | | CRUD Biến thể sản phẩm (Thiết lập SKU, giá bán, tồn kho riêng) | - | - | ✓ | - | - | ✓ | ✓ | L1 | 2.0d |
| F-07.05 | | Xem danh sách đơn hàng gửi đến Shop & Xử lý đơn (Chuẩn bị hàng) | - | - | ✓ | - | - | ✓ | ✓ | L1 | 2.0d |
| F-07.06 | | Quản lý mã giảm giá của Shop (Tạo, sửa, tắt/bật Seller Voucher) | - | - | ✓ | - | - | ✓ | ✓ | L2 | 2.5d |
| F-07.07 | | Báo cáo doanh thu Shop & Analytics (Doanh số, số đơn thành công)| - | - | ✓ | - | - | ✓ | ✓ | L2 | 3.0d |
| **F-08** | **Kênh Quản trị (Admin Center)** | | | | | | | | | | |
| F-08.01 | | Dashboard thống kê toàn sàn (Users, shops, orders, revenue charts) | - | - | - | ✓ | - | ✓ | - | L1 | 2.5d |
| F-08.02 | | Duyệt/Từ chối hồ sơ đăng ký Shop mới (Approve/Reject Seller) | - | - | - | ✓ | ✓ | ✓ | - | L1 | 1.5d |
| F-08.03 | | Duyệt/Từ chối sản phẩm mới đăng của Shop (Approve/Reject Product)| - | - | - | ✓ | ✓ | ✓ | - | L1 | 1.5d |
| F-08.04 | | Quản lý người dùng (Tìm kiếm, xem thông tin, bật/vô hiệu hóa tài khoản)| - | - | - | ✓ | ✓ | ✓ | - | L1 | 2.0d |
| F-08.05 | | Cập nhật vai trò người dùng (Update Roles/Permissions) | - | - | - | ✓ | - | ✓ | - | L2 | 1.0d |
| F-08.06 | | CRUD Danh mục & Thương hiệu toàn hệ thống (Categories/Brands) | - | - | - | ✓ | - | ✓ | - | L1 | 2.0d |
| F-08.07 | | CRUD Mã giảm giá toàn sàn (Admin Coupons) | - | - | - | ✓ | - | ✓ | - | L2 | 2.5d |
| F-08.08 | | Tiếp nhận, xử lý khiếu nại & Phê duyệt Hoàn tiền (Refund Decisions)| - | - | - | ✓ | ✓ | ✓ | - | L2 | 2.5d |
| **F-09** | **Đánh giá & Tương tác** | | | | | | | | | | |
| F-09.01 | | Viết đánh giá sản phẩm (Chọn số sao, viết bình luận, đính kèm ảnh) | - | ✓ | - | - | - | ✓ | ✓ | L2 | 2.0d |
| F-09.02 | | Quản lý danh sách sản phẩm yêu thích (Wishlist) | - | ✓ | - | - | - | ✓ | ✓ | L3 | 1.0d |
| F-09.03 | | Đánh dấu đánh giá hữu ích (Useful review vote) | - | ✓ | - | - | - | ✓ | ✓ | L3 | 0.5d |
| F-09.04 | | Kiểm duyệt nội dung đánh giá của khách hàng (Review Moderation)| - | - | - | ✓ | ✓ | ✓ | - | L2 | 1.5d |
| **F-10** | **Hệ thống thông báo (Notification)** | | | | | | | | | | |
| F-10.01 | | Gửi Email tự động (Xác thực, OTP, hóa đơn mua hàng) | - | ✓ | ✓ | - | - | - | - | L1 | 1.5d |
| F-10.02 | | Gửi thông báo in-app cho Buyer khi đơn hàng đổi trạng thái | - | ✓ | - | - | - | ✓ | ✓ | L2 | 1.0d |
| F-10.03 | | Gửi thông báo in-app cho Seller khi nhận đơn hàng mới | - | - | ✓ | - | - | ✓ | ✓ | L2 | 1.0d |
| F-10.04 | | Gửi thông báo in-app cho Seller khi sản phẩm được duyệt/bị từ chối | - | - | ✓ | - | - | ✓ | ✓ | L2 | 1.0d |
| F-10.05 | | Gửi thông báo in-app cho Admin khi có Shop mới hoặc SP cần duyệt| - | - | - | ✓ | ✓ | ✓ | - | L2 | 1.0d |

---

### Ghi chú thuật ngữ các vai trò (Actors) và môi trường
*   **User**: Người truy cập vãng lai chưa đăng ký hoặc chưa đăng nhập.
*   **Buyer**: Người mua hàng (đã đăng nhập).
*   **Seller**: Nhà bán hàng (đã có gian hàng hoạt động).
*   **Admin**: Quản trị viên hệ thống.
*   **CS (Customer Service)**: Bộ phận Chăm sóc khách hàng & Hỗ trợ vận hành.
*   **PC**: Giao diện chạy trên máy tính (Desktop/Laptop web).
*   **Mobile**: Giao diện chạy trên điện thoại di động (Responsive web trên Mobile browser).
*   **Levels (Độ ưu tiên)**:
    *   **L1**: Core features (Bắt buộc phải có để hệ thống vận hành cơ bản).
    *   **L2**: Standard features (Tính năng tiêu chuẩn nâng cao trải nghiệm & vận hành).
    *   **L3**: Nice-to-have features (Tính năng mở rộng có thể phát triển sau).
*   **Etd Time**: Thời gian ước lượng thực hiện (d: ngày công lập trình viên).
