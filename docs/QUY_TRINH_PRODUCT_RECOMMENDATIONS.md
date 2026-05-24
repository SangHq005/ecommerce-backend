# BÁO CÁO KHÓA LUẬN: HỆ THỐNG GỢI Ý SẢN PHẨM TRONG THƯƠNG MẠI ĐIỆN TỬ

## 1. Tổng quan về Hệ thống Product Recommendations

Trong kỷ nguyên số, khi số lượng sản phẩm trên các nền tảng thương mại điện tử tăng trưởng theo cấp số nhân, người dùng thường xuyên đối mặt với trạng thái "quá tải thông tin" (Information Overload), dẫn đến khó khăn trong việc ra quyết định mua sắm. Để giải quyết thách thức này, Hệ thống Gợi ý Sản phẩm (Product Recommendation System) được nghiên cứu và phát triển như một thành phần cốt lõi của hạ tầng công nghệ. Không chỉ dừng lại ở việc lọc thông tin, hệ thống đóng vai trò như một trợ lý ảo thông minh, có khả năng thấu hiểu hành vi, sở thích và ngữ cảnh của người dùng để chủ động đề xuất các sản phẩm phù hợp nhất.

Hệ thống được xây dựng dựa trên phương pháp tiếp cận lai (Hybrid Approach), kết hợp ưu điểm của các mô hình: Theo dõi hành vi (Behavioral Tracking), Lọc cộng tác (Collaborative Filtering) và Lọc dựa trên nội dung (Content-based Filtering). Điểm đột phá của hệ thống nằm ở khả năng xử lý dữ liệu thời gian thực (Real-time Processing), cho phép cập nhật hồ sơ sở thích của người dùng ngay lập tức sau mỗi tương tác. Mục tiêu cuối cùng là cá nhân hóa tối đa trải nghiệm người dùng, từ đó tối ưu hóa các chỉ số kinh doanh quan trọng như Tỷ lệ chuyển đổi (Conversion Rate), Giá trị đơn hàng trung bình (AOV) và sự trung thành của khách hàng.

## 2. Các mô hình Gợi ý Sản phẩm được triển khai

Hệ thống triển khai bốn chiến lược gợi ý chuyên biệt, được thiết kế để bao phủ trọn vẹn các điểm chạm (touchpoints) trong hành trình khách hàng:

**2.1. Gợi ý Cá nhân hóa (Personalized Recommendations)**
Đây là mô hình trung tâm của hệ thống, hoạt động dựa trên việc khai thác lịch sử tương tác cá nhân. Thay vì áp dụng một giao diện "one-size-fits-all", hệ thống sử dụng thuật toán tính điểm "Category Affinity Score" để định lượng mức độ quan tâm của người dùng đối với từng nhóm hàng. Kết quả là một danh sách sản phẩm được "may đo" riêng, ưu tiên hiển thị những mặt hàng thuộc các danh mục mà người dùng có xu hướng tương tác nhiều nhất trong quá khứ gần.

**2.2. Sản phẩm Tương tự (Similar Products)**
Nhằm hỗ trợ người dùng trong giai đoạn cân nhắc và so sánh, mô hình này sử dụng kỹ thuật Content-based Filtering. Hệ thống phân tích các đặc trưng nội tại của sản phẩm đang xem (như thông số kỹ thuật, thương hiệu, phân khúc giá) để tìm kiếm các sản phẩm thay thế có độ tương đồng cao. Chiến lược này giúp giữ chân người dùng trong luồng trải nghiệm mua sắm ngay cả khi sản phẩm hiện tại chưa thỏa mãn hoàn toàn nhu cầu của họ.

**2.3. Thường được mua cùng nhau (Frequently Bought Together)**
Dựa trên nguyên lý "Khai phá luật kết hợp" (Association Rule Mining), chiến lược này phân tích dữ liệu lịch sử đơn hàng của toàn bộ hệ thống để tìm ra các mẫu sản phẩm thường xuất hiện cùng nhau (co-occurrence patterns). Thông qua việc gợi ý các sản phẩm bổ trợ (như điện thoại đi kèm ốp lưng hay tai nghe), hệ thống kích thích nhu cầu mua sắm thêm (cross-selling) một cách tự nhiên và hữu ích.

**2.4. Sản phẩm Xu hướng (Trending Products)**
Để giải quyết bài toán "Cold Start" (Khởi động lạnh) cho người dùng mới chưa có lịch sử hành vi, hệ thống sử dụng mô hình Popularity-based. Bằng cách thống kê và xếp hạng các sản phẩm có lượng tương tác cao nhất trong cửa sổ thời gian trượt (sliding window) 7 ngày gần nhất, hệ thống đảm bảo luôn hiển thị được các nội dung hấp dẫn, bắt kịp xu hướng thị trường để thu hút người dùng ngay từ lần truye cập đầu tiên.

## 3. Quy trình Xử lý và Thuật toán Chi tiết

### 3.1. Phân hệ Thu thập và Phân tích Sự kiện (Event Tracking)
Hệ thống vận hành theo cơ chế hướng sự kiện (Event-driven). Mọi tương tác của người dùng trên giao diện Frontend – từ việc xem chi tiết sản phẩm (VIEW), thêm vào giỏ hàng (ADD_TO_CART), mua hàng (PURCHASE) đến lưu vào danh sách yêu thích (WISHLIST_ADD) – đều được định danh và gửi về Backend thông qua API Event Tracking.

Do đặc thù dữ liệu sự kiện có tốc độ sinh ra lớn (High Velocity) và khối lượng khổng lồ (High Volume), hệ thống sử dụng MongoDB – một cơ sở dữ liệu NoSQL hướng văn bản – làm kho lưu trữ chính (Event Store). Mỗi sự kiện được lưu trữ dưới dạng một document JSON chứa đầy đủ ngữ cảnh: định danh người dùng (`userId`), mã sản phẩm (`productId`), loại sự kiện (`eventType`) và dấu thời gian (`timestamp`). Kiến trúc này không chỉ đảm bảo hiệu năng ghi (write throughput) cao mà còn tạo tiền đề thuận lợi cho các truy vấn phân tích phức tạp sau này.

### 3.2. Động cơ Tính toán Sở thích (Affinity Engine)
Trái tim của hệ thống cá nhân hóa là "Affinity Engine", module chịu trách nhiệm chuyển đổi dữ liệu hành vi thô thành hồ sơ sở thích có ý nghĩa. Hệ thống sử dụng chỉ số **Category Affinity Score** để lượng hóa mức độ quan tâm của người dùng đối với từng danh mục sản phẩm.

Thuật toán tính điểm được xây dựng dựa trên mô hình trọng số hành vi:
> **Affinity Score = (View Count × $w_{view}$) + (Purchase Count × $w_{purchase}$)**

Trong đó, trọng số $w_{purchase}$ được thiết lập với giá trị 10, trong khi $w_{view}$ là 1. Tỷ lệ 10:1 này phản ánh giả định rằng hành vi mua hàng thể hiện sự quan tâm và cam kết mạnh mẽ hơn nhiều so với việc chỉ xem. Ngay khi một sự kiện mới được ghi nhận, hệ thống tính toán lại điểm số này và cập nhật vào collection `user_category_affinity` trong thời gian thực. Từ đây, hệ thống trích xuất được danh sách "Top Categories" – tập hợp các danh mục có điểm số cao nhất, đóng vai trò là vector đặc trưng cho sở thích người dùng.

### 3.3. Cơ chế Sinh Gợi ý (Recommendation Generation)

Quy trình sinh gợi ý là quy trình đa bước, được tùy biến cho từng loại hình recommendation:

#### 3.3.1. Thuật toán Gợi ý Cá nhân hóa
Quy trình bắt đầu bằng việc truy vấn Top 3 danh mục có điểm Affinity cao nhất từ hồ sơ người dùng. Với mỗi danh mục này, hệ thống truy xuất danh sách các sản phẩm đang hoạt động (Active status) từ cơ sở dữ liệu quan hệ (MySQL).
Tuy nhiên, để đảm bảo tính mới mẻ và tránh gây nhàm chán, hệ thống áp dụng một "Bộ lọc phủ định" (Negation Filter). Bộ lọc này đối chiếu danh sách ứng viên với lịch sử xem của người dùng trong 30 ngày gần nhất (truy xuất từ MongoDB) để loại bỏ các sản phẩm đã tương tác. Danh sách kết quả cuối cùng là tập hợp các sản phẩm "mới" nhưng thuộc đúng các danh mục người dùng yêu thích nhất.

#### 3.3.2. Thuật toán Lọc Nội dung cho Sản phẩm Tương tự
Để xác định độ tương đồng giữa sản phẩm A (đang xem) và sản phẩm B (ứng viên), hệ thống sử dụng hàm tính điểm tương đồng (Similarity Scoring Function) dựa trên các thuộc tính định danh:
*   **Tiêu chí Danh mục (+40 điểm):** Nếu A và B cùng thuộc một Category.
*   **Tiêu chí Thương hiệu (+30 điểm):** Nếu A và B cùng Brand.
*   **Tiêu chí Giá (+20 điểm):** Nếu giá của B nằm trong khoảng chênh lệch ±20% so với giá của A.

Tổng điểm Similarity Score càng cao, sản phẩm B càng được coi là thay thế phù hợp cho A. Danh sách gợi ý được sắp xếp giảm dần theo điểm số này.

#### 3.3.3. Thuật toán Lọc Cộng tác cho Sản phẩm Mua cùng
Hệ thống thực hiện phân tích tần suất xuất hiện đồng thời (Co-occurrence Analysis) trên tập dữ liệu đơn hàng (`order_items`). Đối với một sản phẩm đích, thuật toán quét toàn bộ các đơn hàng chứa sản phẩm đó, sau đó liệt kê và đếm số lần xuất hiện của các sản phẩm đi kèm.
Để đảm bảo độ tin cậy thống kê và loại bỏ các nhiễu ngẫu nhiên, hệ thống áp dụng ngưỡng lọc (Threshold Filtering): chỉ những cặp sản phẩm có tần suất xuất hiện chung $\ge 3$ lần mới được đưa vào danh sách ứng viên. Kết quả trả về là các sản phẩm có xác suất được mua kèm cao nhất.

## 4. Đánh giá Hiệu quả và Kết luận

### 4.1. Đánh giá về mặt Kỹ thuật
Hệ thống Product Recommendations đã giải quyết thành công các thách thức kỹ thuật lớn:
*   **Khả năng đáp ứng thời gian thực:** Nhờ kiến trúc Event-driven và việc tối ưu hóa chỉ mục (indexing) trên MongoDB, hệ thống có thể cập nhật sở thích người dùng và điều chỉnh gợi ý chỉ trong mili-giây, mang lại trải nghiệm mượt mà không độ trễ.
*   **Giải pháp cho vấn đề Cold Start:** Việc tích hợp mô hình Trending Products như một cơ chế dự phòng (fallback) đảm bảo hệ thống luôn hoạt động hiệu quả ngay cả với người dùng vãng lai (guest users).
*   **Hiệu năng và Khả năng mở rộng:** Việc phân tách trách nhiệm lưu trữ (Polyglot Persistence) – dùng MongoDB cho Log/Event và MySQL cho dữ liệu nghiệp vụ – giúp hệ thống dễ dàng mở rộng theo chiều ngang khi lượng truy cập tăng vọt.

### 4.2. Kết luận
Việc tích hợp hệ thống Product Recommendations vào nền tảng thương mại điện tử không chỉ là một nâng cấp về mặt tính năng mà là một bước chuyển mình quan trọng sang mô hình kinh doanh lấy dữ liệu làm trung tâm (Data-driven). Hệ thống đã chứng minh được khả năng cá nhân hóa trải nghiệm người dùng một cách sâu sắc, giúp doanh nghiệp thấu hiểu khách hàng hơn và tối đa hóa giá trị vòng đời khách hàng.

Trong tương lai, hệ thống có thể được phát triển mở rộng theo hướng tích hợp các mô hình Học sâu (Deep Learning) như Neural Collaborative Filtering để nắm bắt các mối quan hệ phi tuyến tính phức tạp hơn, hoặc bổ sung các yếu tố ngữ cảnh (Context-aware) như thời gian, địa điểm và thiết bị truy cập để nâng cao hơn nữa độ chính xác của các gợi ý.
