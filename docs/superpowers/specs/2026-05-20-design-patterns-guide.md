# ShopMart Design Patterns Guide

**Ngày soạn:** 2026-05-20  
**Phiên bản:** 1.0  
**Trạng thái:** Approved  
**Phạm vi:** Backend Spring Boot — `ecommerce-backend`

---

## Mục đích tài liệu

Domain Vấn đề hiện tại Pattern áp dụng Mức độ ưu tiên
Payment
VNPayService và MomoService là class cụ thể, không có interface chung — vi phạm Open/Closed Principle, khó thêm gateway mới
Strategy + Factory
🔴 Cao
Order
Sau khi đổi trạng thái đơn, side effects (notification, inventory, audit) bị hard-code trong service — tăng coupling
Observer / Domain Event
🔴 Cao
Discount & Promotion
Logic tính discount (coupon toàn sàn vs voucher shop) có thể bị trộn trong OrderService/CheckoutController
Strategy
🟡 Trung bình
Notification
NotificationService vừa lưu DB, vừa push WebSocket, vừa gửi Email trong một class — quá nhiều trách nhiệm
Observer + Chain of Responsibility
🟡 Trung bình
Storage
Đã có CloudStorageService interface + CloudinaryStorageService/LocalStorageService — pattern đúng rồi, chỉ cần document
Strategy (đã có)
✅ Done
Service Layer
61 service file, OrderService inject thẳng 8+ repository và service — khó đọc, khó test unit
Facade / Orchestrator
🟡 Trung bình
Tài liệu này là **design guide kép** — phục vụ đồng thời hai mục đích:

1. **Refactoring roadmap** — mô tả thứ tự ưu tiên cải thiện từng domain qua các sprint.
2. **Code review checklist** — reviewer dùng để kiểm tra PR có tuân thủ pattern hay không.

Nguyên tắc refactor: **"refactor có kiểm soát"** — mỗi module chỉ được refactor khi đã có test coverage cho behavior hiện tại. Không thay đổi API contract (HTTP endpoints, DTO) trong quá trình refactor.

---

## Kiến trúc tổng thể hiện tại

```
HTTP Request
     │
     ▼
Controller (api/controller/)
     │  (chỉ gọi Service, không gọi Repository)
     ▼
Service (application/service/)
     │  (chứa business logic + transaction boundary)
     ▼
Repository (infrastructure/persistence/mysql/repository/)
     │  (Spring Data JPA — không có business logic)
     ▼
Database (MySQL)
```

Kiến trúc phân tầng này **giữ nguyên**. Các pattern trong guide này áp dụng **bên trong** các tầng, không thay đổi chiều phụ thuộc giữa chúng.

---

## Domain 1: Payment — Strategy + Factory

### Vấn đề hiện tại

`VNPayService` và `MomoService` là các class cụ thể không có interface chung. `PaymentService` và `CheckoutController` phụ thuộc trực tiếp vào từng class — vi phạm Open/Closed Principle. Thêm gateway mới (ZaloPay, VNPT Money) đòi hỏi sửa nhiều file hiện tại.

```java
// Hiện tại — BAD: biết rõ implementation cụ thể
@Autowired VNPayService vnpayService;
@Autowired MomoService momoService;

if (method == VNPAY) {
    url = vnpayService.createPaymentUrl(order, ip);
} else if (method == MOMO) {
    url = momoService.createPaymentUrl(order);
}
```

### Target State

```
«interface»
PaymentGateway
  + createPaymentUrl(order: OrderEntity, ipAddress: String): String
  + verifyCallback(params: Map<String,String>): CallbackResult
  + supports(method: PaymentMethod): boolean
       ▲                  ▲                   ▲
VNPayGateway         MomoGateway         MockPaymentGateway
(refactor từ         (refactor từ        (refactor từ
 VNPayService)        MomoService)        MockVNPayService)


PaymentGatewayRegistry
  - List<PaymentGateway> gateways   ← Spring inject tất cả implementations
  + resolve(method: PaymentMethod): PaymentGateway
```

### File cần tạo / sửa

| Hành động | File                                                                    |
| --------- | ----------------------------------------------------------------------- |
| Tạo mới   | `application/service/payment/PaymentGateway.java`                       |
| Tạo mới   | `application/service/payment/CallbackResult.java`                       |
| Tạo mới   | `application/service/payment/PaymentGatewayRegistry.java`               |
| Refactor  | `VNPayService` → implements `PaymentGateway`                            |
| Refactor  | `MomoService` → implements `PaymentGateway`                             |
| Refactor  | `MockVNPayService` → implements `PaymentGateway`                        |
| Cập nhật  | `PaymentService` — inject `PaymentGatewayRegistry` thay vì từng service |

### Code sau khi refactor

```java
// PaymentGateway.java
public interface PaymentGateway {
    String createPaymentUrl(OrderEntity order, String ipAddress);
    CallbackResult verifyCallback(Map<String, String> params);
    boolean supports(PaymentMethod method);
}

// PaymentGatewayRegistry.java
@Component
public class PaymentGatewayRegistry {
    private final List<PaymentGateway> gateways;

    public PaymentGatewayRegistry(List<PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway resolve(PaymentMethod method) {
        return gateways.stream()
            .filter(g -> g.supports(method))
            .findFirst()
            .orElseThrow(() -> ApiException.badRequest("Unsupported payment method: " + method));
    }
}

// Trong PaymentService.java — sau refactor
PaymentGateway gateway = registry.resolve(order.getPaymentMethod());
String url = gateway.createPaymentUrl(order, ipAddress);
```

### Thêm gateway mới (ZaloPay)

Chỉ cần:

1. Tạo `ZaloPayGateway implements PaymentGateway`
2. Annotate `@Service` → Spring tự đăng ký vào `PaymentGatewayRegistry`
3. **Không đụng** `PaymentService`, `CheckoutController`, hay bất kỳ file hiện tại nào

### PR Checklist

- [ ] Không có `if/else` rẽ nhánh theo `PaymentMethod` trong service layer
- [ ] Mọi gateway đều implement `PaymentGateway` interface
- [ ] `PaymentService` chỉ biết `PaymentGatewayRegistry`, không biết gateway cụ thể
- [ ] Có unit test cho `PaymentGatewayRegistry.resolve()` với mọi payment method

---

## Domain 2: Order — Domain Event + Observer

### Vấn đề hiện tại

Sau khi đổi trạng thái đơn hàng, các side effects (gửi notification, giải phóng stock khi CANCELLED, ghi audit log) được gọi trực tiếp trong service method — tạo coupling chặt giữa Order domain và Notification/Inventory domain.

```java
// Hiện tại — BAD: Order service biết về Notification và Inventory
order.transitionTo(OrderStatus.CANCELLED);
notificationService.createNotification(...);      // coupling với Notification
reservationService.release(order.getOrderCode()); // coupling với Inventory
auditService.log(...);                             // coupling với Audit
orderRepo.save(orderEntity);
```

### Target State

```
OrderStatusChangedEvent {orderId, fromStatus, toStatus, actorId, occurredAt}

«interface»
ApplicationListener<OrderStatusChangedEvent>
       ▲                      ▲                    ▲
OrderNotification         OrderInventory        OrderAudit
Listener                  Listener              Listener
(gửi thông báo            (release stock        (ghi
 theo loại event)          khi CANCELLED)        audit log)
```

### File cần tạo / sửa

| Hành động | File                                                                    |
| --------- | ----------------------------------------------------------------------- |
| Tạo mới   | `domain/event/OrderStatusChangedEvent.java`                             |
| Tạo mới   | `application/event/OrderNotificationListener.java`                      |
| Tạo mới   | `application/event/OrderInventoryListener.java`                         |
| Tạo mới   | `application/event/OrderAuditListener.java`                             |
| Cập nhật  | `SellerOrderService`, `OrderService` — dùng `ApplicationEventPublisher` |

### Code sau khi refactor

```java
// OrderStatusChangedEvent.java
public record OrderStatusChangedEvent(
    Long orderId,
    String orderCode,
    OrderStatus fromStatus,
    OrderStatus toStatus,
    Long actorId,
    String actorRole,
    Instant occurredAt
) {}

// Trong SellerOrderService.java — sau refactor
order.transitionTo(OrderStatus.CANCELLED);
orderRepo.save(orderEntity);
eventPublisher.publishEvent(new OrderStatusChangedEvent(
    order.getId(), order.getOrderCode(),
    fromStatus, OrderStatus.CANCELLED,
    actorId, "SELLER", Instant.now()
));
// Không gọi notificationService, reservationService trực tiếp nữa

// OrderInventoryListener.java
@Component
public class OrderInventoryListener implements ApplicationListener<OrderStatusChangedEvent> {
    @Override
    public void onApplicationEvent(OrderStatusChangedEvent event) {
        if (event.toStatus() == OrderStatus.CANCELLED) {
            reservationService.release(event.orderCode());
        }
    }
}
```

> **Lưu ý:** Dùng Spring `ApplicationEventPublisher` — synchronous, trong cùng transaction. Đủ dùng cho giai đoạn hiện tại. Khi cần async, thêm `@Async` vào listener mà **không** sửa service đang publish.

### PR Checklist

- [ ] Sau `transitionTo()`, service chỉ gọi `eventPublisher.publishEvent()` — không gọi trực tiếp notification/inventory/audit
- [ ] Mỗi listener chỉ xử lý một loại side effect
- [ ] Listener có test riêng, độc lập với service đã publish event
- [ ] `Order.java` domain object không import bất kỳ service nào

---

## Domain 3: Discount & Promotion — Strategy

### Vấn đề hiện tại

Logic tính discount có hai hành vi khác nhau: Coupon (toàn sàn, validate và ghi usage riêng) vs Voucher (theo shop, validate theo shop_id). Nếu trộn trong `OrderService`/`CheckoutService`, việc thêm loại discount mới (flash sale, tier discount) sẽ khiến các service đó ngày càng phình to.

### Target State

```
«interface»
DiscountStrategy
  + isApplicable(order: OrderContext, request: DiscountRequest): boolean
  + calculate(order: OrderContext, request: DiscountRequest): DiscountResult
       ▲                              ▲
CouponDiscountStrategy         VoucherDiscountStrategy
(validate + apply coupon)      (validate + apply voucher)
```

### File cần tạo

| Hành động | File                                                                 |
| --------- | -------------------------------------------------------------------- |
| Tạo mới   | `application/service/discount/DiscountStrategy.java`                 |
| Tạo mới   | `application/service/discount/DiscountContext.java`                  |
| Tạo mới   | `application/service/discount/DiscountResult.java`                   |
| Tạo mới   | `application/service/discount/CouponDiscountStrategy.java`           |
| Tạo mới   | `application/service/discount/VoucherDiscountStrategy.java`          |
| Cập nhật  | `OrderService` / `CheckoutService` — inject `List<DiscountStrategy>` |

### Code sau khi refactor

```java
// DiscountStrategy.java
public interface DiscountStrategy {
    boolean isApplicable(DiscountContext ctx);
    DiscountResult calculate(DiscountContext ctx);
}

// DiscountContext.java
public record DiscountContext(
    Long userId, Long shopId,
    Long totalAmount, String couponCode, String voucherCode
) {}

// DiscountResult.java
public record DiscountResult(long discountAmount, String description) {
    public static DiscountResult zero() { return new DiscountResult(0, ""); }
}

// Trong CheckoutService / OrderService
List<DiscountStrategy> strategies; // inject tất cả strategies

long totalDiscount = strategies.stream()
    .filter(s -> s.isApplicable(ctx))
    .mapToLong(s -> s.calculate(ctx).discountAmount())
    .sum();
```

### PR Checklist

- [ ] Không có `if (couponCode != null)` / `if (voucherCode != null)` rẽ nhánh trong service checkout
- [ ] Mỗi loại discount có strategy riêng, test độc lập
- [ ] Thêm loại discount mới không cần sửa `OrderService`

---

## Domain 4: Notification — Chain of Responsibility

### Vấn đề hiện tại

`NotificationService` hiện đảm nhận 3 trách nhiệm trong một class: (1) lưu `NotificationEntity` vào DB, (2) push real-time qua WebSocket (`SimpMessagingTemplate`), (3) ủy quyền gửi email. Vi phạm Single Responsibility Principle — khó test từng kênh, khó thêm kênh mới (FCM push notification).

### Target State

```
«interface»
NotificationChannel
  + supports(type: NotificationType): boolean
  + send(payload: NotificationPayload): void
       ▲                    ▲                   ▲
InAppChannel           EmailChannel        PushChannel
(lưu DB +              (gọi                (tương lai: FCM)
 WebSocket push)        EmailService)


NotificationDispatcher
  - List<NotificationChannel> channels  ← Spring inject tất cả
  + dispatch(NotificationPayload): void
      → duyệt channels, gọi send() nếu supports()
```

### File cần tạo / sửa

| Hành động | File                                                             |
| --------- | ---------------------------------------------------------------- |
| Tạo mới   | `application/service/notification/NotificationChannel.java`      |
| Tạo mới   | `application/service/notification/NotificationPayload.java`      |
| Tạo mới   | `application/service/notification/InAppNotificationChannel.java` |
| Tạo mới   | `application/service/notification/EmailNotificationChannel.java` |
| Tạo mới   | `application/service/notification/NotificationDispatcher.java`   |
| Refactor  | `NotificationService` — delegate sang `NotificationDispatcher`   |

### Code sau khi refactor

```java
// NotificationChannel.java
public interface NotificationChannel {
    boolean supports(String notificationType);
    void send(NotificationPayload payload);
}

// NotificationPayload.java
public record NotificationPayload(
    Long userId, String type,
    String title, String message,
    String referenceType, Long referenceId
) {}

// NotificationDispatcher.java
@Service
public class NotificationDispatcher {
    private final List<NotificationChannel> channels;

    public void dispatch(NotificationPayload payload) {
        channels.stream()
            .filter(c -> c.supports(payload.type()))
            .forEach(c -> {
                try { c.send(payload); }
                catch (Exception e) { log.warn("Channel {} failed: {}", c.getClass().getSimpleName(), e.getMessage()); }
            });
    }
}
```

> **Lưu ý:** Lỗi ở một kênh (ví dụ email fail) **không** ảnh hưởng đến kênh khác (in-app vẫn lưu DB). Đây là graceful degradation đã được yêu cầu trong SAD.

### PR Checklist

- [ ] `NotificationService` không chứa logic gửi mail hay WebSocket push trực tiếp
- [ ] Mỗi `NotificationChannel` có unit test độc lập, không cần Spring context
- [ ] Thêm kênh mới (FCM) chỉ cần tạo class mới implements `NotificationChannel`

---

## Domain 5: Storage — Strategy (Đã có, cần document)

### Trạng thái hiện tại: ✅ Pattern đúng, giữ nguyên

Codebase đã implement đúng Strategy pattern cho storage:

```
«interface»
CloudStorageService
  + upload(file, folder): String
  + delete(publicId): void
       ▲                        ▲
CloudinaryStorageService   LocalStorageService
(@ConditionalOnProperty    (@ConditionalOnProperty
 cloudinary)                local)
```

### PR Checklist

- [ ] Không inject `CloudinaryStorageService` hay `LocalStorageService` trực tiếp — luôn inject qua interface `CloudStorageService`
- [ ] Thêm storage provider mới (AWS S3): tạo class mới + `@ConditionalOnProperty`, không sửa code hiện tại

---

## Domain 6: Service Layer — Facade

### Vấn đề hiện tại

`CatalogService` inject 10+ repository trong constructor. `OrderService` inject 8+ service và repository. Các class này quá lớn, khó đọc và khó test unit vì phải mock quá nhiều dependency.

### Target State (CatalogService)

```
CatalogController
      │
      ▼
CatalogFacade  ← entry point, inject 3 sub-services
  ├── ProductQueryService   (read: search, filter, detail, paginate)
  ├── ProductWriteService   (write: create, update, publish, delete)
  └── ProductDataAggregatorService  ← đã có sẵn
```

### Target State (OrderService)

```
OrderController / CheckoutController
      │
      ▼
OrderFacade  ← entry point
  ├── CheckoutService      (tạo đơn, validate cart, apply discount)
  ├── OrderStatusService   (đổi trạng thái, publish event)
  └── OrderQueryService    (read: list, detail, history)
```

### Nguyên tắc tách

- Mỗi sub-service chỉ inject repository/service nó **thực sự cần**
- `Facade` không chứa business logic — chỉ ủy quyền và có thể orchestrate transaction
- Controller chỉ biết `Facade`, không biết sub-service

### PR Checklist

- [ ] Constructor của service mới không có quá 5 dependency
- [ ] Sub-service chỉ inject những repository/service nó thực sự sử dụng
- [ ] Facade không chứa `if/else` business logic

---

## Cross-cutting Concerns (Đã có AOP — giữ nguyên)

| Concern                | Mechanism        | File                                          |
| ---------------------- | ---------------- | --------------------------------------------- |
| Rate limiting          | `@RateLimit` AOP | `api/aspect/RateLimitAspect.java`             |
| Performance monitoring | AOP              | `api/aspect/PerformanceMonitoringAspect.java` |
| Audit trail            | `AuditService`   | `application/service/AuditService.java`       |

**PR Checklist AOP:**

- [ ] Endpoint nhạy cảm có `@RateLimit`
- [ ] Thao tác thay đổi dữ liệu quan trọng (approve shop, change role...) được gọi `AuditService`

---

## Tổng hợp Pattern Map

| Pattern                     | Domain áp dụng             | Vị trí trong code                                        |
| --------------------------- | -------------------------- | -------------------------------------------------------- |
| **Strategy**                | Payment gateway            | `application/service/payment/`                           |
| **Strategy**                | Discount calculation       | `application/service/discount/`                          |
| **Strategy**                | Storage provider           | `application/service/CloudStorageService` _(đã có)_      |
| **Factory / Registry**      | Payment gateway resolution | `PaymentGatewayRegistry`                                 |
| **State Machine**           | Order lifecycle            | `domain/order/Order.java` _(đã có, tốt)_                 |
| **Observer / Domain Event** | Order side effects         | `domain/event/` + `application/event/`                   |
| **Chain of Responsibility** | Multi-channel notification | `application/service/notification/`                      |
| **Facade**                  | Catalog service            | `application/service/catalog/CatalogFacade`              |
| **Facade**                  | Order service              | `application/service/order/OrderFacade`                  |
| **Repository**              | Data access                | `infrastructure/persistence/mysql/repository/` _(đã có)_ |
| **AOP**                     | Rate limiting, monitoring  | `api/aspect/` _(đã có)_                                  |

---

## Thứ tự ưu tiên refactoring

| Sprint   | Domain                           | Lý do ưu tiên                                                        |
| -------- | -------------------------------- | -------------------------------------------------------------------- |
| Sprint 1 | **Payment — Strategy + Factory** | Rủi ro cao nhất khi thêm gateway mới; thay đổi nhỏ, impact lớn       |
| Sprint 1 | **Order — Domain Event**         | Giảm coupling ngay, chuẩn bị cho async notification sau này          |
| Sprint 2 | **Notification — Chain**         | Cần test cover `NotificationService` trước; refactor phức tạp hơn    |
| Sprint 2 | **Discount — Strategy**          | Cần đọc toàn bộ checkout flow trước khi refactor                     |
| Sprint 3 | **Facade — CatalogService**      | Class lớn nhất, cần thời gian; ưu tiên sau khi 2 sprint trên ổn định |
| Sprint 3 | **Facade — OrderService**        | Tương tự CatalogService                                              |

---

## Nguyên tắc "Refactor Có Kiểm Soát"

Trước khi refactor bất kỳ service nào:

1. **Viết characterization test** — test mô tả behavior hiện tại, không phải behavior mong muốn
2. **Chạy test → PASS** với code cũ
3. **Refactor** từng bước nhỏ
4. **Chạy test → vẫn PASS** — đảm bảo behavior không thay đổi
5. **Commit sau mỗi bước nhỏ** hoạt động

> **Quy tắc vàng:** Nếu test pass trước khi refactor nhưng fail sau khi refactor → revert ngay, không tiếp tục.

---

_Design Guide v1.0 — ShopMart Backend — 2026-05-20_
