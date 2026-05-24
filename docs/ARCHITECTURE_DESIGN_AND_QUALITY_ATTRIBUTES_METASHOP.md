# Thiết kế Kiến trúc và Yêu cầu Thuộc tính Chất lượng

## 1. Ràng buộc thiết kế

- **Phạm vi nghiệp vụ:** Nền tảng hỗ trợ 3 vai trò chính: Khách hàng, Nhà bán hàng và Quản trị viên.
- **Kiểu kiến trúc:** Backend triển khai theo mô hình module/dịch vụ trên Spring Boot; frontend dùng Next.js App Router.
- **Khả năng mở rộng:** Hệ thống phải chịu tải đồng thời cao trong các đợt khuyến mãi, flash sale và mùa cao điểm.
- **Hiệu năng:** 95% request API phải hoàn thành trong 3 giây; 100% request trong 5 giây ở tải bình thường.
- **Bảo mật:** Dữ liệu nhạy cảm phải được bảo vệ khi truyền và khi lưu trữ. Xác thực/phân quyền là bắt buộc cho mọi tài nguyên bảo vệ.
- **Tính sẵn sàng:** Mục tiêu uptime tối thiểu 99,9%, có quy trình xử lý sự cố và khôi phục rõ ràng.
- **Khả năng tương tác:** Hệ thống phải tích hợp an toàn với dịch vụ ngoài (VNPay, MoMo, Google OAuth2, AI API, dịch vụ lưu trữ media).
- **Khả năng chỉnh sửa:** Quy tắc nghiệp vụ (coupon, voucher, luồng đơn hàng/hoàn tiền, chính sách kiểm duyệt) cần dễ thay đổi và ít rủi ro hồi quy.
- **Tuân thủ:** Tuân thủ yêu cầu an toàn thông tin và bảo vệ dữ liệu cá nhân áp dụng cho thương mại điện tử.

## 2. Yêu cầu thuộc tính chất lượng

### 2.1 Bảo mật

#### 2.1.1 Xác thực và phân quyền


| Thành phần         | Mô tả                                                                                                                                         |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Người dùng thực hiện đăng ký, đăng nhập, đặt lại mật khẩu, truy cập tài nguyên bảo vệ hoặc thao tác bị giới hạn theo vai trò.                 |
| Nguồn kích thích   | Khách hàng, nhà bán hàng, quản trị viên và client tự động.                                                                                    |
| Môi trường         | Vận hành bình thường trên frontend web và backend REST API.                                                                                   |
| Đối tượng tác động | Module xác thực, xử lý JWT/refresh token, cơ chế RBAC trong cấu hình bảo mật.                                                                 |
| Phản hồi hệ thống  | Mật khẩu được băm (BCrypt), JWT được kiểm tra hợp lệ, phân quyền theo endpoint, hành vi đăng nhập bất thường bị giới hạn tần suất và ghi log. |
| Chỉ số đo lường    | 100% endpoint bảo vệ yêu cầu xác thực hợp lệ; truy cập trái phép bị chặn và ghi log; thời gian phản hồi trung bình API đăng nhập < 2 giây.    |


#### 2.1.2 Bảo vệ dữ liệu


| Thành phần         | Mô tả                                                                                                                                |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------ |
| Kích thích         | Hệ thống lưu/truyền dữ liệu hồ sơ người dùng, địa chỉ, dữ liệu đơn hàng và tham chiếu thanh toán.                                    |
| Nguồn kích thích   | Khách hàng và nhà bán hàng trong các luồng tài khoản/đơn hàng/thanh toán.                                                            |
| Môi trường         | Toàn bộ vòng đời dữ liệu: nhập, xử lý, lưu trữ, sao lưu và truyền tải.                                                               |
| Đối tượng tác động | MySQL, tầng API, tầng logging, module tích hợp.                                                                                      |
| Phản hồi hệ thống  | Bắt buộc HTTPS/TLS, che thông tin nhạy cảm trên log, kiểm soát truy cập CSDL chặt chẽ, có chính sách sao lưu và lưu giữ dữ liệu.     |
| Chỉ số đo lường    | 100% lưu lượng production dùng HTTPS; không có secret dạng plaintext trong log; truy cập CSDL production được giới hạn theo vai trò. |


#### 2.1.3 Bảo mật thanh toán


| Thành phần         | Mô tả                                                                                                                                             |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Người dùng tạo yêu cầu thanh toán và cổng thanh toán gửi callback/webhook.                                                                        |
| Nguồn kích thích   | VNPay, MoMo, luồng checkout của khách hàng.                                                                                                       |
| Môi trường         | Luồng thanh toán trực tuyến và xử lý callback bất đồng bộ.                                                                                        |
| Đối tượng tác động | Payment service, callback handler, logic cập nhật trạng thái đơn hàng, bản ghi giao dịch.                                                         |
| Phản hồi hệ thống  | Bắt buộc kiểm tra chữ ký cổng thanh toán; callback xử lý idempotent; không lưu đầy đủ dữ liệu thẻ; mọi cập nhật trạng thái thanh toán được audit. |
| Chỉ số đo lường    | 100% callback được xác thực trước khi xử lý; callback lặp không gây hiệu ứng nghiệp vụ lặp; lỗi callback do hệ thống <= 0,1%.                     |


### 2.2 Hiệu năng

#### 2.2.1 Thời gian phản hồi nhanh


| Thành phần         | Mô tả                                                                                                             |
| ------------------ | ----------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Người dùng xem sản phẩm, tìm kiếm, thêm giỏ hàng, thanh toán và tra cứu đơn.                                      |
| Nguồn kích thích   | Khách hàng, nhà bán hàng, quản trị viên.                                                                          |
| Môi trường         | Tải bình thường và tải cao điểm theo chiến dịch.                                                                  |
| Đối tượng tác động | Spring Boot API, truy vấn MySQL, cache, frontend SSR/client fetch.                                                |
| Phản hồi hệ thống  | Phân trang/lọc dữ liệu ở API, tối ưu index, cache dữ liệu truy cập nóng, xử lý bất đồng bộ tác vụ không khẩn cấp. |
| Chỉ số đo lường    | >=95% API dưới 3 giây; trang catalog/tìm kiếm tải dưới 2 giây ở tải bình thường.                                  |


#### 2.2.2 Hiệu quả tìm kiếm và lọc


| Thành phần         | Mô tả                                                                                   |
| ------------------ | --------------------------------------------------------------------------------------- |
| Kích thích         | Người dùng tìm kiếm/lọc/sắp xếp theo từ khóa, danh mục, thương hiệu, giá và thuộc tính. |
| Nguồn kích thích   | Khách hàng và người vận hành nội bộ.                                                    |
| Môi trường         | Nhiều truy vấn đồng thời với tổ hợp bộ lọc đa dạng.                                     |
| Đối tượng tác động | Endpoint tìm kiếm, truy vấn repository, chiến lược index, endpoint gợi ý.               |
| Phản hồi hệ thống  | Tối ưu kế hoạch truy vấn, duy trì index cho bộ lọc phổ biến, response luôn phân trang.  |
| Chỉ số đo lường    | >=95% request tìm kiếm trả kết quả < 2 giây (tải bình thường).                          |


#### 2.2.3 Mở rộng cho người dùng đồng thời cao


| Thành phần         | Mô tả                                                                                                                |
| ------------------ | -------------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Lượng request đồng thời tăng mạnh trong các đợt sale.                                                                |
| Nguồn kích thích   | Số lượng lớn khách hàng truy cập cùng lúc.                                                                           |
| Môi trường         | Khung giờ cao điểm/chương trình khuyến mãi lớn.                                                                      |
| Đối tượng tác động | Instance ứng dụng, load balancer, connection pool DB, cache, phân phối tài nguyên tĩnh.                              |
| Phản hồi hệ thống  | Scale ngang instance, ưu tiên chiến lược cache cho luồng đọc nhiều, rate-limit để bảo vệ luồng nghiệp vụ quan trọng. |
| Chỉ số đo lường    | Thời gian phản hồi trung bình < 3 giây ở mức đồng thời mục tiêu; lỗi quá tải <= 0,5%.                                |


### 2.3 Tính dễ sử dụng

#### 2.3.1 Trải nghiệm người dùng mượt mà


| Thành phần         | Mô tả                                                                                                            |
| ------------------ | ---------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Người dùng đi xuyên suốt luồng mua hàng từ khám phá sản phẩm đến thanh toán thành công.                          |
| Nguồn kích thích   | Khách hàng và người vận hành gian hàng.                                                                          |
| Môi trường         | Trình duyệt web desktop và mobile.                                                                               |
| Đối tượng tác động | Trang Next.js, form nhập liệu, validation, xử lý lỗi API.                                                        |
| Phản hồi hệ thống  | Tối giản số bước thao tác, hiển thị lỗi form rõ ràng, phản hồi trạng thái ngay sau thao tác.                     |
| Chỉ số đo lường    | >=95% người dùng hoàn thành luồng mua hàng cốt lõi mà không cần hỗ trợ; thời gian hoàn tất checkout được tối ưu. |


#### 2.3.2 Giao diện dễ dùng


| Thành phần         | Mô tả                                                                                                |
| ------------------ | ---------------------------------------------------------------------------------------------------- |
| Kích thích         | Người dùng thao tác menu, bộ lọc, form, trang chi tiết sản phẩm và theo dõi đơn hàng.                |
| Nguồn kích thích   | Khách hàng, nhà bán hàng, quản trị viên.                                                             |
| Môi trường         | Trình duyệt hiện đại với layout responsive.                                                          |
| Đối tượng tác động | Bộ component dùng chung, token thiết kế, thành phần form.                                            |
| Phản hồi hệ thống  | UI nhất quán, ngôn ngữ rõ ràng, vị trí thao tác trực quan, hành vi trang dự đoán được.               |
| Chỉ số đo lường    | Tỷ lệ hoàn thành tác vụ trong test usability >=90%; số ticket hỗ trợ cho thao tác cơ bản ở mức thấp. |


### 2.4 Khả năng tương tác

#### 2.4.1 Tích hợp hệ thống bên thứ ba


| Thành phần         | Mô tả                                                                                                           |
| ------------------ | --------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Hệ thống gửi/nhận dữ liệu với cổng thanh toán, nhà cung cấp OAuth, dịch vụ AI và dịch vụ lưu trữ media.         |
| Nguồn kích thích   | VNPay, MoMo, Google OAuth2, nhà cung cấp AI API, nhà cung cấp ảnh/tệp.                                          |
| Môi trường         | Tương tác runtime với dịch vụ ngoài qua internet công cộng.                                                     |
| Đối tượng tác động | Integration client, callback controller, cơ chế retry/timeout, xử lý lỗi kết nối.                               |
| Phản hồi hệ thống  | Tích hợp REST chuẩn qua HTTPS, quản lý secret an toàn, retry có backoff, cấu hình timeout, logging có cấu trúc. |
| Chỉ số đo lường    | >=99,5% cuộc gọi tích hợp thành công (không tính lỗi từ phía đối tác); khôi phục lỗi tạm thời trong 5 phút.     |


#### 2.4.2 Giao tiếp nội bộ giữa các module


| Thành phần         | Mô tả                                                                                                                            |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Các module backend trao đổi dữ liệu trong luồng checkout, cập nhật tồn kho, xử lý hoàn tiền và gửi thông báo.                    |
| Nguồn kích thích   | Service class/component nội bộ.                                                                                                  |
| Môi trường         | Xử lý request đồng bộ và tác vụ bất đồng bộ.                                                                                     |
| Đối tượng tác động | Service interface, transaction boundary, cơ chế event/thông báo.                                                                 |
| Phản hồi hệ thống  | Hợp đồng dịch vụ rõ ràng, cập nhật dữ liệu an toàn theo transaction, event nghiệp vụ nhất quán cho tác vụ phụ (thông báo/audit). |
| Chỉ số đo lường    | Tỷ lệ lỗi gọi nội bộ <=0,1% ở môi trường ổn định; luồng quan trọng có đầy đủ khả năng truy vết.                                  |


### 2.5 Khả năng chỉnh sửa

#### 2.5.1 Hỗ trợ thay đổi quy tắc nghiệp vụ


| Thành phần         | Mô tả                                                                                                                    |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------ |
| Kích thích         | Nhóm sản phẩm thay đổi quy tắc kinh doanh (logic giảm giá, thời hạn hoàn tiền, luồng kiểm duyệt, chính sách vận chuyển). |
| Nguồn kích thích   | Stakeholder nghiệp vụ và product owner.                                                                                  |
| Môi trường         | Giai đoạn phát triển và bảo trì.                                                                                         |
| Đối tượng tác động | Tầng service, DTO/validation, màn hình cấu hình admin.                                                                   |
| Phản hồi hệ thống  | Logic quy tắc được đóng gói rõ ràng, API ưu tiên tương thích ngược, có test để giảm hồi quy.                             |
| Chỉ số đo lường    | Thay đổi chính sách nhỏ triển khai trong < 1 tuần; số module bị ảnh hưởng cho thay đổi phổ biến <=2.                     |


#### 2.5.2 Cập nhật không gián đoạn dịch vụ


| Thành phần         | Mô tả                                                                                               |
| ------------------ | --------------------------------------------------------------------------------------------------- |
| Kích thích         | Triển khai phiên bản mới, bản vá bảo mật hoặc cập nhật cấu hình.                                    |
| Nguồn kích thích   | Nhóm DevOps và kỹ thuật.                                                                            |
| Môi trường         | Hệ thống production đang có người dùng hoạt động.                                                   |
| Đối tượng tác động | Pipeline CI/CD, script triển khai, health check, chiến lược rollback.                               |
| Phản hồi hệ thống  | Áp dụng rolling/blue-green deployment, kiểm tra tự động trước/sau deploy, rollback an toàn khi lỗi. |
| Chỉ số đo lường    | >=99% lần deploy không gây gián đoạn thấy được với người dùng; rollback hoàn tất trong 10 phút.     |


### 2.6 Tính sẵn sàng

#### 2.6.1 Chịu lỗi và khôi phục hệ thống


| Thành phần         | Mô tả                                                                                                  |
| ------------------ | ------------------------------------------------------------------------------------------------------ |
| Kích thích         | Lỗi service/node/database hoặc phụ thuộc bên thứ ba bị gián đoạn.                                      |
| Nguồn kích thích   | Sự cố hạ tầng, lỗi phần mềm, outage từ nhà cung cấp.                                                   |
| Môi trường         | Runtime production.                                                                                    |
| Đối tượng tác động | Hạ tầng chạy ứng dụng, backup dữ liệu, quan sát hệ thống, health monitor.                              |
| Phản hồi hệ thống  | Fail-fast và suy giảm có kiểm soát khi cần, cảnh báo sự cố kịp thời, có kế hoạch sao lưu và khôi phục. |
| Chỉ số đo lường    | Uptime >=99,9%; MTTR <30 phút cho sự cố phổ biến; RPO/RTO được định nghĩa và kiểm thử định kỳ.         |


#### 2.6.2 Tự động xử lý phản hồi thanh toán


| Thành phần         | Mô tả                                                                                                                       |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------- |
| Kích thích         | Callback từ cổng thanh toán báo thành công/thất bại/chờ xử lý.                                                              |
| Nguồn kích thích   | Webhook/callback từ VNPay và MoMo.                                                                                          |
| Môi trường         | Xử lý bất đồng bộ sau checkout.                                                                                             |
| Đối tượng tác động | Callback handler thanh toán, state machine đơn hàng, luồng thông báo.                                                       |
| Phản hồi hệ thống  | Kết quả thanh toán được xử lý tự động và idempotent, trạng thái đơn cập nhật đúng một lần, người dùng được thông báo nhanh. |
| Chỉ số đo lường    | >=99,9% callback xử lý đúng trong 1 phút; lỗi trạng thái đơn do webhook <=0,01%.                                            |


## 3. Biểu diễn kiến trúc

### 3.1 Góc nhìn logic

Các phân hệ logic chính:

- **Định danh và truy cập:** đăng ký, đăng nhập, quản lý token/session, gán vai trò.
- **Catalog và khám phá:** danh mục, thương hiệu, duyệt sản phẩm, tìm kiếm, gợi ý.
- **Giỏ hàng và thanh toán:** quản lý giỏ, áp dụng voucher/coupon, tạo đơn.
- **Thanh toán:** tạo yêu cầu VNPay/MoMo và xử lý callback.
- **Đơn hàng và thực thi:** vòng đời đơn, xử lý phía seller, thông tin vận chuyển.
- **Tồn kho:** tồn SKU, giữ chỗ tồn kho, nhật ký biến động kho.
- **Tương tác khách hàng:** wishlist, đánh giá, thông báo, API chat.
- **Trung tâm nhà bán hàng:** hồ sơ shop, tạo/sửa sản phẩm, voucher, tài chính.
- **Trung tâm quản trị:** kiểm duyệt, quản trị danh mục, quản lý user/seller/refund, audit log.

### 3.2 Góc nhìn triển khai mã nguồn

- **Backend:** Java Spring Boot (kiến trúc phân lớp: controller -> service -> repository -> entity).
- **Frontend:** Next.js App Router + các module component React.
- **Lưu trữ:** MySQL (mô hình entity theo JPA/Hibernate).
- **Bảo mật:** Spring Security, JWT, RBAC.
- **Quan sát hệ thống:** application log, request correlation/tracing, metric hook.
- **Phân phối:** pipeline CI/CD theo các bước test và triển khai.

### 3.3 Góc nhìn triển khai hạ tầng

- Frontend và backend có thể triển khai độc lập.
- Backend chạy một hoặc nhiều instance phía sau load balancer.
- MySQL triển khai kèm chính sách backup và tùy chọn replication/read scaling.
- Phụ thuộc bên ngoài (payment, OAuth, AI, media) được tách qua integration module.
- Có thể bổ sung cache/reverse proxy cho endpoint đọc nhiều.

### 3.4 Góc nhìn dữ liệu

- Mô hình dữ liệu quan hệ lõi bao gồm: users, shops, products, SKUs, orders, payments, refunds, reviews, vouchers, notifications, inventory logs.
- Transaction boundary cho các luồng quan trọng:
  - tạo đơn hàng + giữ chỗ tồn kho
  - callback thanh toán + cập nhật trạng thái đơn
  - xử lý hoàn tiền + chuyển trạng thái
- Các bảng lịch sử trạng thái (order/product/shop) phục vụ truy vết và audit.

## 4. Checklist nghiệm thu

- 100% endpoint production chạy qua HTTPS.
- Auth + RBAC áp dụng cho toàn bộ route cần bảo vệ.
- Callback thanh toán có xác thực chữ ký và xử lý idempotent.
- Các luồng quan trọng có transaction boundary và audit log.
- Dashboard SLO hiệu năng và cảnh báo đã được cấu hình.
- Quy trình backup/khôi phục đã được tài liệu hóa và kiểm thử.
- Quy trình rollback khi deploy đã được xác minh.

