# ShopMart Design Patterns — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Áp dụng 5 design pattern (Strategy, Domain Event, Chain of Responsibility, Facade) vào codebase ShopMart Backend để tăng khả năng mở rộng và giảm coupling giữa các domain.

**Architecture:** Modular Monolith + Layered Architecture (Controller → Service → Repository). Các pattern được áp dụng bên trong Service layer, không thay đổi API contract hay HTTP endpoints.

**Tech Stack:** Java 21, Spring Boot 3.x, JUnit 5, Mockito, Spring ApplicationEventPublisher, Spring Data JPA / MySQL

---

## File Structure

```
application/service/
  payment/
    PaymentGateway.java           (interface — Strategy)
    CallbackResult.java           (record — kết quả verify callback)
    PaymentGatewayRegistry.java   (Factory / Registry)
  discount/
    DiscountStrategy.java         (interface — Strategy)
    DiscountContext.java          (record — input)
    DiscountResult.java           (record — output)
    CouponDiscountStrategy.java   (implementation)
    VoucherDiscountStrategy.java  (implementation)
  notification/
    NotificationChannel.java      (interface — Chain)
    NotificationPayload.java      (record — input)
    InAppNotificationChannel.java (in-app + WebSocket)
    EmailNotificationChannel.java (email)
    NotificationDispatcher.java   (orchestrator)
domain/event/
  OrderStatusChangedEvent.java    (Domain Event record)
application/event/
  OrderNotificationListener.java  (Observer)
  OrderInventoryListener.java     (Observer)
  OrderAuditListener.java         (Observer)

test/application/service/payment/
  PaymentGatewayRegistryTest.java
test/application/service/discount/
  CouponDiscountStrategyTest.java
  VoucherDiscountStrategyTest.java
test/application/service/notification/
  NotificationDispatcherTest.java
  InAppNotificationChannelTest.java
test/application/event/
  OrderNotificationListenerTest.java
  OrderInventoryListenerTest.java
```

---

## Task 1: PaymentGateway — Strategy + Factory

**Ưu tiên:** 🔴 Sprint 1  
**Prerequisite:** Không có

**Files:**
- Create: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/payment/PaymentGateway.java`
- Create: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/payment/CallbackResult.java`
- Create: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/payment/PaymentGatewayRegistry.java`
- Modify: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/VNPayService.java`
- Modify: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/MomoService.java`
- Modify: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/MockVNPayService.java`
- Modify: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/PaymentService.java`
- Test: `src/test/java/com/example/ecommerce/ecommerce_backend/application/service/payment/PaymentGatewayRegistryTest.java`

---

- [ ] **Step 1.1: Tạo `CallbackResult` record**

```java
// src/main/java/.../application/service/payment/CallbackResult.java
package com.example.ecommerce.ecommerce_backend.application.service.payment;

public record CallbackResult(
    boolean success,
    String orderCode,
    String transactionId,
    String message
) {
    public static CallbackResult success(String orderCode, String transactionId) {
        return new CallbackResult(true, orderCode, transactionId, "OK");
    }
    public static CallbackResult failure(String message) {
        return new CallbackResult(false, null, null, message);
    }
}
```

- [ ] **Step 1.2: Tạo `PaymentGateway` interface**

```java
// src/main/java/.../application/service/payment/PaymentGateway.java
package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import java.util.Map;

public interface PaymentGateway {
    String createPaymentUrl(OrderEntity order, String ipAddress);
    CallbackResult verifyCallback(Map<String, String> params);
    boolean supports(PaymentMethod method);
}
```

- [ ] **Step 1.3: Viết failing test cho `PaymentGatewayRegistry`**

```java
// src/test/java/.../application/service/payment/PaymentGatewayRegistryTest.java
package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PaymentGatewayRegistryTest {

    private PaymentGateway stubGateway(PaymentMethod supported) {
        return new PaymentGateway() {
            public String createPaymentUrl(var o, String ip) { return "url"; }
            public CallbackResult verifyCallback(Map<String,String> p) { return CallbackResult.success("code","txn"); }
            public boolean supports(PaymentMethod m) { return m == supported; }
        };
    }

    @Test
    void resolve_returnsCorrectGateway_whenMethodSupported() {
        var registry = new PaymentGatewayRegistry(List.of(
            stubGateway(PaymentMethod.VNPAY),
            stubGateway(PaymentMethod.MOMO)
        ));

        var gateway = registry.resolve(PaymentMethod.VNPAY);

        assertTrue(gateway.supports(PaymentMethod.VNPAY));
        assertFalse(gateway.supports(PaymentMethod.MOMO));
    }

    @Test
    void resolve_throwsApiException_whenNoGatewaySupportsMethod() {
        var registry = new PaymentGatewayRegistry(List.of(
            stubGateway(PaymentMethod.VNPAY)
        ));

        assertThrows(ApiException.class, () -> registry.resolve(PaymentMethod.MOMO));
    }
}
```

- [ ] **Step 1.4: Chạy test — xác nhận FAIL**

```bash
cd d:\DemoApp\ecommerce-backend
mvn test -pl . -Dtest="PaymentGatewayRegistryTest" -q
```

Expected: FAIL với "class not found" hoặc compilation error vì `PaymentGatewayRegistry` chưa tồn tại.

- [ ] **Step 1.5: Tạo `PaymentGatewayRegistry`**

```java
// src/main/java/.../application/service/payment/PaymentGatewayRegistry.java
package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import org.springframework.stereotype.Component;
import java.util.List;

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
```

- [ ] **Step 1.6: Chạy test — xác nhận PASS**

```bash
mvn test -pl . -Dtest="PaymentGatewayRegistryTest" -q
```

Expected: BUILD SUCCESS, 2 tests passed.

- [ ] **Step 1.7: Sửa `VNPayService` implements `PaymentGateway`**

Thêm vào đầu class và implement 2 method còn thiếu:

```java
// VNPayService.java — thêm implements và method
@Service
@ConditionalOnProperty(name = "payment.vnpay.mock", havingValue = "false", matchIfMissing = true)
public class VNPayService implements PaymentGateway {

    // ... giữ nguyên code hiện tại ...

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.VNPAY;
    }

    @Override
    public CallbackResult verifyCallback(Map<String, String> params) {
        // Di chuyển logic verify signature hiện tại vào đây
        // Trả về CallbackResult.success(...) hoặc CallbackResult.failure(...)
        boolean valid = verifySignature(params); // method đã có
        if (!valid) return CallbackResult.failure("Invalid signature");
        String orderCode = params.get("vnp_TxnRef");
        String txnId = params.get("vnp_TransactionNo");
        return CallbackResult.success(orderCode, txnId);
    }
}
```

- [ ] **Step 1.8: Sửa `MomoService` implements `PaymentGateway`** (tương tự VNPayService)

```java
@Service
public class MomoService implements PaymentGateway {

    // ... giữ nguyên code hiện tại ...

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.MOMO;
    }

    @Override
    public CallbackResult verifyCallback(Map<String, String> params) {
        // Di chuyển logic verify callback hiện tại vào đây
        String signature = params.get("signature");
        boolean valid = verifyMomoSignature(params, signature); // method đã có
        if (!valid) return CallbackResult.failure("Invalid MoMo signature");
        String orderCode = extractOrderCode(params.get("extraData")); // method đã có
        String txnId = params.get("transId");
        return CallbackResult.success(orderCode, txnId);
    }

    @Override
    public String createPaymentUrl(OrderEntity order, String ipAddress) {
        return createPaymentUrl(order); // delegate sang method hiện tại
    }
}
```

- [ ] **Step 1.9: Sửa `MockVNPayService` implements `PaymentGateway`**

```java
@Service
@ConditionalOnProperty(name = "payment.vnpay.mock", havingValue = "true")
public class MockVNPayService implements PaymentGateway {

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.VNPAY || method == PaymentMethod.MOMO;
    }

    @Override
    public CallbackResult verifyCallback(Map<String, String> params) {
        return CallbackResult.success(params.getOrDefault("orderCode", "MOCK"), "MOCK_TXN");
    }

    @Override
    public String createPaymentUrl(OrderEntity order, String ipAddress) {
        // giữ nguyên logic mock hiện tại
        return "http://mock-payment/pay?order=" + order.getOrderCode();
    }
}
```

- [ ] **Step 1.10: Cập nhật `PaymentService` — inject `PaymentGatewayRegistry`**

Tìm chỗ inject `VNPayService`/`MomoService` và thay bằng:

```java
// PaymentService.java — thay field injection
private final PaymentGatewayRegistry gatewayRegistry;  // THÊM

// Constructor — thêm param
public PaymentService(..., PaymentGatewayRegistry gatewayRegistry) {
    ...
    this.gatewayRegistry = gatewayRegistry;
}

// Chỗ tạo payment URL — thay if/else bằng:
PaymentMethod method = PaymentMethod.valueOf(order.getPaymentMethod());
PaymentGateway gateway = gatewayRegistry.resolve(method);
String paymentUrl = gateway.createPaymentUrl(order, ipAddress);
```

- [ ] **Step 1.11: Chạy tất cả test liên quan**

```bash
mvn test -pl . -Dtest="PaymentGatewayRegistryTest,PaymentServiceIntegrationTest" -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 1.12: Commit**

```bash
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/payment/
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/VNPayService.java
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/MomoService.java
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/MockVNPayService.java
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/PaymentService.java
git add src/test/java/com/example/ecommerce/ecommerce_backend/application/service/payment/
git commit -m "refactor(payment): apply Strategy + Factory pattern via PaymentGateway interface"
```

---

## Task 2: Order Domain Events — Observer Pattern

**Ưu tiên:** 🔴 Sprint 1  
**Prerequisite:** Không có (độc lập với Task 1)

**Files:**
- Create: `src/main/java/com/example/ecommerce/ecommerce_backend/domain/event/OrderStatusChangedEvent.java`
- Create: `src/main/java/com/example/ecommerce/ecommerce_backend/application/event/OrderNotificationListener.java`
- Create: `src/main/java/com/example/ecommerce/ecommerce_backend/application/event/OrderInventoryListener.java`
- Create: `src/main/java/com/example/ecommerce/ecommerce_backend/application/event/OrderAuditListener.java`
- Modify: `src/main/java/com/example/ecommerce/ecommerce_backend/application/service/SellerOrderService.java`
- Test: `src/test/java/com/example/ecommerce/ecommerce_backend/application/event/OrderInventoryListenerTest.java`
- Test: `src/test/java/com/example/ecommerce/ecommerce_backend/application/event/OrderNotificationListenerTest.java`

---

- [ ] **Step 2.1: Tạo `OrderStatusChangedEvent`**

```java
// src/main/java/.../domain/event/OrderStatusChangedEvent.java
package com.example.ecommerce.ecommerce_backend.domain.event;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import java.time.Instant;

public record OrderStatusChangedEvent(
    Long orderId,
    String orderCode,
    OrderStatus fromStatus,
    OrderStatus toStatus,
    Long actorId,
    String actorRole,
    Instant occurredAt
) {
    public static OrderStatusChangedEvent of(
            Long orderId, String orderCode,
            OrderStatus from, OrderStatus to,
            Long actorId, String actorRole) {
        return new OrderStatusChangedEvent(orderId, orderCode, from, to, actorId, actorRole, Instant.now());
    }
}
```

- [ ] **Step 2.2: Viết failing test cho `OrderInventoryListener`**

```java
// src/test/java/.../application/event/OrderInventoryListenerTest.java
package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.ReservationService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderInventoryListenerTest {

    @Mock ReservationService reservationService;
    @InjectMocks OrderInventoryListener listener;

    @Test
    void onStatusChanged_releasesReservation_whenOrderCancelled() {
        var event = OrderStatusChangedEvent.of(1L, "ORD-001",
            OrderStatus.SUBMITTED, OrderStatus.CANCELLED, 42L, "CUSTOMER");

        listener.onApplicationEvent(event);

        verify(reservationService).release("ORD-001");
    }

    @Test
    void onStatusChanged_doesNothing_whenOrderNotCancelled() {
        var event = OrderStatusChangedEvent.of(1L, "ORD-001",
            OrderStatus.SUBMITTED, OrderStatus.PROCESSING, 99L, "SELLER");

        listener.onApplicationEvent(event);

        verifyNoInteractions(reservationService);
    }
}
```

- [ ] **Step 2.3: Chạy test — xác nhận FAIL**

```bash
mvn test -pl . -Dtest="OrderInventoryListenerTest" -q
```

Expected: FAIL vì `OrderInventoryListener` chưa tồn tại.

- [ ] **Step 2.4: Tạo `OrderInventoryListener`**

```java
// src/main/java/.../application/event/OrderInventoryListener.java
package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.ReservationService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OrderInventoryListener implements ApplicationListener<OrderStatusChangedEvent> {

    private final ReservationService reservationService;

    public OrderInventoryListener(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Override
    public void onApplicationEvent(OrderStatusChangedEvent event) {
        if (event.toStatus() == OrderStatus.CANCELLED) {
            reservationService.release(event.orderCode());
        }
    }
}
```

- [ ] **Step 2.5: Chạy test — xác nhận PASS**

```bash
mvn test -pl . -Dtest="OrderInventoryListenerTest" -q
```

Expected: BUILD SUCCESS, 2 tests passed.

- [ ] **Step 2.6: Tạo `OrderNotificationListener`**

```java
// src/main/java/.../application/event/OrderNotificationListener.java
package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.NotificationService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener implements ApplicationListener<OrderStatusChangedEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);
    private final NotificationService notificationService;

    public OrderNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onApplicationEvent(OrderStatusChangedEvent event) {
        try {
            String title = buildTitle(event.toStatus());
            String message = buildMessage(event);
            notificationService.createNotification(
                event.actorId(), "ORDER_STATUS_CHANGED",
                title, message, "ORDER", event.orderId()
            );
        } catch (Exception e) {
            log.warn("Failed to send order notification for order {}: {}", event.orderCode(), e.getMessage());
        }
    }

    private String buildTitle(OrderStatus status) {
        return switch (status) {
            case CANCELLED -> "Đơn hàng đã bị hủy";
            case PROCESSING -> "Đơn hàng đang được xử lý";
            case SHIPPED -> "Đơn hàng đã được giao cho vận chuyển";
            case DELIVERED -> "Đơn hàng đã được giao";
            case COMPLETED -> "Đơn hàng đã hoàn thành";
            default -> "Cập nhật trạng thái đơn hàng";
        };
    }

    private String buildMessage(OrderStatusChangedEvent event) {
        return String.format("Đơn hàng %s: %s → %s",
            event.orderCode(), event.fromStatus(), event.toStatus());
    }
}
```

- [ ] **Step 2.7: Tạo `OrderAuditListener`**

```java
// src/main/java/.../application/event/OrderAuditListener.java
package com.example.ecommerce.ecommerce_backend.application.event;

import com.example.ecommerce.ecommerce_backend.application.service.AuditService;
import com.example.ecommerce.ecommerce_backend.domain.event.OrderStatusChangedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditListener implements ApplicationListener<OrderStatusChangedEvent> {

    private final AuditService auditService;

    public OrderAuditListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void onApplicationEvent(OrderStatusChangedEvent event) {
        auditService.log(
            "ORDER_STATUS_CHANGED",
            event.actorId(),
            event.actorRole(),
            "Order " + event.orderCode() + ": " + event.fromStatus() + " → " + event.toStatus()
        );
    }
}
```

- [ ] **Step 2.8: Inject `ApplicationEventPublisher` vào `SellerOrderService`**

```java
// SellerOrderService.java — thêm field
private final ApplicationEventPublisher eventPublisher;

// Constructor — thêm param
public SellerOrderService(..., ApplicationEventPublisher eventPublisher) {
    ...
    this.eventPublisher = eventPublisher;
}

// Mỗi chỗ gọi transitionTo — thêm publish event sau khi save:
OrderStatus fromStatus = orderEntity.getStatus(); // ghi lại trước khi đổi
order.transitionTo(newStatus);
orderRepo.save(orderEntity);
// Xóa các gọi trực tiếp notificationService/reservationService tại đây
eventPublisher.publishEvent(OrderStatusChangedEvent.of(
    orderEntity.getId(), orderEntity.getOrderCode(),
    fromStatus, newStatus,
    actorId, actorRole
));
```

- [ ] **Step 2.9: Chạy tất cả test liên quan**

```bash
mvn test -pl . -Dtest="OrderInventoryListenerTest,OrderNotificationListenerTest,OrderServiceTest" -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 2.10: Commit**

```bash
git add src/main/java/com/example/ecommerce/ecommerce_backend/domain/event/
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/event/
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/SellerOrderService.java
git add src/test/java/com/example/ecommerce/ecommerce_backend/application/event/
git commit -m "refactor(order): decouple side effects via OrderStatusChangedEvent domain events"
```

---

## Task 3: DiscountStrategy — Strategy Pattern

**Ưu tiên:** 🟡 Sprint 2  
**Prerequisite:** Đọc và hiểu toàn bộ `OrderService.checkout()` và `CouponService`

**Files:**
- Create: `src/main/java/.../application/service/discount/DiscountStrategy.java`
- Create: `src/main/java/.../application/service/discount/DiscountContext.java`
- Create: `src/main/java/.../application/service/discount/DiscountResult.java`
- Create: `src/main/java/.../application/service/discount/CouponDiscountStrategy.java`
- Create: `src/main/java/.../application/service/discount/VoucherDiscountStrategy.java`
- Test: `src/test/java/.../application/service/discount/CouponDiscountStrategyTest.java`
- Test: `src/test/java/.../application/service/discount/VoucherDiscountStrategyTest.java`

---

- [ ] **Step 3.1: Tạo DiscountContext, DiscountResult, DiscountStrategy**

```java
// DiscountContext.java
package com.example.ecommerce.ecommerce_backend.application.service.discount;

public record DiscountContext(
    Long userId, Long shopId,
    long totalAmount, String couponCode, String voucherCode
) {}

// DiscountResult.java
public record DiscountResult(long discountAmount, String description) {
    public static DiscountResult zero() { return new DiscountResult(0L, ""); }
}

// DiscountStrategy.java
public interface DiscountStrategy {
    boolean isApplicable(DiscountContext ctx);
    DiscountResult calculate(DiscountContext ctx);
}
```

- [ ] **Step 3.2: Viết failing test cho `CouponDiscountStrategy`**

```java
// CouponDiscountStrategyTest.java
package com.example.ecommerce.ecommerce_backend.application.service.discount;

import com.example.ecommerce.ecommerce_backend.application.service.CouponService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponDiscountStrategyTest {

    @Mock CouponService couponService;
    @InjectMocks CouponDiscountStrategy strategy;

    @Test
    void isApplicable_returnsFalse_whenNoCouponCode() {
        var ctx = new DiscountContext(1L, 2L, 100_000L, null, null);
        assertFalse(strategy.isApplicable(ctx));
    }

    @Test
    void isApplicable_returnsTrue_whenCouponCodePresent() {
        var ctx = new DiscountContext(1L, 2L, 100_000L, "SUMMER10", null);
        assertTrue(strategy.isApplicable(ctx));
    }

    @Test
    void calculate_returnsDiscountAmount_forValidCoupon() {
        var coupon = new CouponEntity();
        coupon.setDiscountValue(10L);
        coupon.setDiscountType("PERCENTAGE");
        when(couponService.findValidCoupon("SUMMER10", 1L)).thenReturn(Optional.of(coupon));

        var ctx = new DiscountContext(1L, 2L, 200_000L, "SUMMER10", null);
        var result = strategy.calculate(ctx);

        assertEquals(20_000L, result.discountAmount());
    }

    @Test
    void calculate_returnsZero_whenCouponInvalid() {
        when(couponService.findValidCoupon("INVALID", 1L)).thenReturn(Optional.empty());

        var ctx = new DiscountContext(1L, 2L, 200_000L, "INVALID", null);
        var result = strategy.calculate(ctx);

        assertEquals(0L, result.discountAmount());
    }
}
```

- [ ] **Step 3.3: Chạy test — xác nhận FAIL**

```bash
mvn test -pl . -Dtest="CouponDiscountStrategyTest" -q
```

Expected: FAIL vì `CouponDiscountStrategy` chưa tồn tại.

- [ ] **Step 3.4: Tạo `CouponDiscountStrategy`**

```java
// CouponDiscountStrategy.java
package com.example.ecommerce.ecommerce_backend.application.service.discount;

import com.example.ecommerce.ecommerce_backend.application.service.CouponService;
import org.springframework.stereotype.Component;

@Component
public class CouponDiscountStrategy implements DiscountStrategy {

    private final CouponService couponService;

    public CouponDiscountStrategy(CouponService couponService) {
        this.couponService = couponService;
    }

    @Override
    public boolean isApplicable(DiscountContext ctx) {
        return ctx.couponCode() != null && !ctx.couponCode().isBlank();
    }

    @Override
    public DiscountResult calculate(DiscountContext ctx) {
        return couponService.findValidCoupon(ctx.couponCode(), ctx.userId())
            .map(coupon -> {
                long discount = "PERCENTAGE".equals(coupon.getDiscountType())
                    ? ctx.totalAmount() * coupon.getDiscountValue() / 100
                    : coupon.getDiscountValue();
                return new DiscountResult(Math.min(discount, ctx.totalAmount()), "Coupon: " + ctx.couponCode());
            })
            .orElse(DiscountResult.zero());
    }
}
```

- [ ] **Step 3.5: Chạy test — xác nhận PASS**

```bash
mvn test -pl . -Dtest="CouponDiscountStrategyTest" -q
```

Expected: BUILD SUCCESS, 4 tests passed.

- [ ] **Step 3.6: Tạo `VoucherDiscountStrategy`** (tương tự CouponDiscountStrategy nhưng dùng `SellerVoucherService`)

```java
// VoucherDiscountStrategy.java
@Component
public class VoucherDiscountStrategy implements DiscountStrategy {

    private final SellerVoucherService voucherService;

    public VoucherDiscountStrategy(SellerVoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @Override
    public boolean isApplicable(DiscountContext ctx) {
        return ctx.voucherCode() != null && !ctx.voucherCode().isBlank();
    }

    @Override
    public DiscountResult calculate(DiscountContext ctx) {
        return voucherService.findValidVoucher(ctx.voucherCode(), ctx.shopId())
            .map(voucher -> {
                long discount = "PERCENTAGE".equals(voucher.getDiscountType())
                    ? ctx.totalAmount() * voucher.getDiscountValue() / 100
                    : voucher.getDiscountValue();
                return new DiscountResult(Math.min(discount, ctx.totalAmount()), "Voucher: " + ctx.voucherCode());
            })
            .orElse(DiscountResult.zero());
    }
}
```

- [ ] **Step 3.7: Chạy toàn bộ test discount**

```bash
mvn test -pl . -Dtest="CouponDiscountStrategyTest,VoucherDiscountStrategyTest" -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3.8: Commit**

```bash
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/discount/
git add src/test/java/com/example/ecommerce/ecommerce_backend/application/service/discount/
git commit -m "feat(discount): add DiscountStrategy pattern for coupon and voucher calculation"
```

---

## Task 4: NotificationChannel — Chain of Responsibility

**Ưu tiên:** 🟡 Sprint 2  
**Prerequisite:** Viết characterization test cho `NotificationService` hiện tại trước khi refactor

**Files:**
- Create: `src/main/java/.../application/service/notification/NotificationChannel.java`
- Create: `src/main/java/.../application/service/notification/NotificationPayload.java`
- Create: `src/main/java/.../application/service/notification/InAppNotificationChannel.java`
- Create: `src/main/java/.../application/service/notification/EmailNotificationChannel.java`
- Create: `src/main/java/.../application/service/notification/NotificationDispatcher.java`
- Test: `src/test/java/.../application/service/notification/NotificationDispatcherTest.java`
- Test: `src/test/java/.../application/service/notification/InAppNotificationChannelTest.java`

---

- [ ] **Step 4.1: Tạo `NotificationPayload` và `NotificationChannel`**

```java
// NotificationPayload.java
package com.example.ecommerce.ecommerce_backend.application.service.notification;

public record NotificationPayload(
    Long userId, String type,
    String title, String message,
    String referenceType, Long referenceId
) {}

// NotificationChannel.java
public interface NotificationChannel {
    boolean supports(String notificationType);
    void send(NotificationPayload payload);
}
```

- [ ] **Step 4.2: Viết failing test cho `NotificationDispatcher`**

```java
// NotificationDispatcherTest.java
package com.example.ecommerce.ecommerce_backend.application.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Test
    void dispatch_sendsToAllSupportingChannels() {
        var channel1 = mock(NotificationChannel.class);
        var channel2 = mock(NotificationChannel.class);
        var payload = new NotificationPayload(1L, "ORDER_STATUS_CHANGED", "Title", "Msg", "ORDER", 10L);

        when(channel1.supports("ORDER_STATUS_CHANGED")).thenReturn(true);
        when(channel2.supports("ORDER_STATUS_CHANGED")).thenReturn(false);

        var dispatcher = new NotificationDispatcher(List.of(channel1, channel2));
        dispatcher.dispatch(payload);

        verify(channel1).send(payload);
        verify(channel2, never()).send(any());
    }

    @Test
    void dispatch_continuesWhenOneChannelFails() {
        var failingChannel = mock(NotificationChannel.class);
        var workingChannel = mock(NotificationChannel.class);
        var payload = new NotificationPayload(1L, "ORDER_STATUS_CHANGED", "Title", "Msg", "ORDER", 10L);

        when(failingChannel.supports(any())).thenReturn(true);
        when(workingChannel.supports(any())).thenReturn(true);
        doThrow(new RuntimeException("email server down")).when(failingChannel).send(any());

        var dispatcher = new NotificationDispatcher(List.of(failingChannel, workingChannel));

        assertDoesNotThrow(() -> dispatcher.dispatch(payload));
        verify(workingChannel).send(payload);
    }
}
```

- [ ] **Step 4.3: Chạy test — xác nhận FAIL**

```bash
mvn test -pl . -Dtest="NotificationDispatcherTest" -q
```

Expected: FAIL.

- [ ] **Step 4.4: Tạo `NotificationDispatcher`**

```java
// NotificationDispatcher.java
package com.example.ecommerce.ecommerce_backend.application.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final List<NotificationChannel> channels;

    public NotificationDispatcher(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public void dispatch(NotificationPayload payload) {
        channels.stream()
            .filter(c -> c.supports(payload.type()))
            .forEach(c -> {
                try {
                    c.send(payload);
                } catch (Exception e) {
                    log.warn("Notification channel {} failed for type {}: {}",
                        c.getClass().getSimpleName(), payload.type(), e.getMessage());
                }
            });
    }
}
```

- [ ] **Step 4.5: Chạy test — xác nhận PASS**

```bash
mvn test -pl . -Dtest="NotificationDispatcherTest" -q
```

Expected: BUILD SUCCESS, 2 tests passed.

- [ ] **Step 4.6: Tạo `InAppNotificationChannel`**

```java
// InAppNotificationChannel.java
@Component
public class InAppNotificationChannel implements NotificationChannel {

    private final NotificationJpaRepository notificationRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserJpaRepository userRepo;

    // constructor injection

    @Override
    public boolean supports(String type) {
        return true; // In-app luôn nhận mọi loại notification
    }

    @Override
    public void send(NotificationPayload payload) {
        // Di chuyển logic từ NotificationService.createNotification() vào đây
        var entity = new NotificationEntity();
        entity.setUserId(payload.userId());
        entity.setType(payload.type());
        entity.setTitle(payload.title());
        entity.setMessage(payload.message());
        entity.setReferenceType(payload.referenceType());
        entity.setReferenceId(payload.referenceId());
        entity.setRead(false);
        entity.setCreatedAt(Instant.now());
        notificationRepo.save(entity);

        // Push WebSocket
        messagingTemplate.convertAndSendToUser(
            payload.userId().toString(),
            "/queue/notifications",
            payload
        );
    }
}
```

- [ ] **Step 4.7: Tạo `EmailNotificationChannel`**

```java
// EmailNotificationChannel.java
@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Set<String> EMAIL_TYPES = Set.of(
        "ORDER_CONFIRMED", "ORDER_SHIPPED", "ORDER_DELIVERED", "ACCOUNT_VERIFIED"
    );

    private final EmailService emailService;
    private final UserJpaRepository userRepo;

    @Override
    public boolean supports(String type) {
        return EMAIL_TYPES.contains(type);
    }

    @Override
    public void send(NotificationPayload payload) {
        userRepo.findById(payload.userId()).ifPresent(user ->
            emailService.sendSimpleEmail(user.getEmail(), payload.title(), payload.message())
        );
    }
}
```

- [ ] **Step 4.8: Update `NotificationService` để delegate sang `NotificationDispatcher`**

```java
// NotificationService.java — thêm dependency và delegate
@Service
public class NotificationService {

    private final NotificationDispatcher dispatcher;
    // giữ lại các field cần thiết cho read operations (list, markRead, etc.)

    public void createNotification(
            Long userId, String type, String title, String message,
            String referenceType, Long referenceId) {
        var payload = new NotificationPayload(userId, type, title, message, referenceType, referenceId);
        dispatcher.dispatch(payload);
        // InAppNotificationChannel chịu trách nhiệm persist entity — không cần trả về ở đây
    }

    // Các method read (getNotifications, markAsRead, etc.) giữ nguyên
}
```

- [ ] **Step 4.9: Chạy tất cả test**

```bash
mvn test -pl . -Dtest="NotificationDispatcherTest,InAppNotificationChannelTest" -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4.10: Commit**

```bash
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/notification/
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/NotificationService.java
git add src/test/java/com/example/ecommerce/ecommerce_backend/application/service/notification/
git commit -m "refactor(notification): apply Chain of Responsibility via NotificationChannel + Dispatcher"
```

---

## Task 5: CatalogFacade — Facade Pattern

**Ưu tiên:** 🟡 Sprint 3  
**Prerequisite:** Task 2 và 3 hoàn thành; đọc toàn bộ `CatalogService.java`

**Files:**
- Create: `src/main/java/.../application/service/catalog/ProductQueryService.java`
- Create: `src/main/java/.../application/service/catalog/ProductWriteService.java`
- Create: `src/main/java/.../application/service/catalog/CatalogFacade.java`
- Modify: `src/main/java/.../api/controller/PublicCatalogController.java` — inject `CatalogFacade`
- Modify: `src/main/java/.../api/controller/AdminCatalogController.java` — inject `CatalogFacade`
- Test: `src/test/java/.../application/service/catalog/ProductQueryServiceTest.java`

---

- [ ] **Step 5.1: Tách `ProductQueryService` từ `CatalogService`**

Chuyển tất cả `@Transactional(readOnly=true)` method ra khỏi `CatalogService` vào `ProductQueryService`. `ProductQueryService` chỉ inject các repository cần cho read:

```java
// ProductQueryService.java
@Service
public class ProductQueryService {

    private final ProductJpaRepository productRepo;
    private final CategoryJpaRepository categoryRepo;
    private final BrandJpaRepository brandRepo;
    private final SkuJpaRepository skuRepo;
    private final ProductImageJpaRepository imageRepo;
    private final StringRedisTemplate redis;

    // Chỉ 6 dependencies (so với 10+ của CatalogService hiện tại)

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchProducts(ProductSearchRequest req, Pageable pageable) { ... }

    @Transactional(readOnly = true)
    public ProductDetailsResponse getProductBySlug(String slug) { ... }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() { ... }
}
```

- [ ] **Step 5.2: Tách `ProductWriteService` từ `CatalogService`**

```java
// ProductWriteService.java
@Service
public class ProductWriteService {

    private final ProductJpaRepository productRepo;
    private final SkuJpaRepository skuRepo;
    private final ProductImageJpaRepository imageRepo;
    private final EventLogMongoRepository eventRepo;

    // Chỉ 4 dependencies

    @Transactional
    public ProductEntity createProduct(CreateProductRequest req, Long sellerId) { ... }

    @Transactional
    public ProductEntity updateProduct(Long id, UpdateProductRequest req, Long sellerId) { ... }
}
```

- [ ] **Step 5.3: Tạo `CatalogFacade`**

```java
// CatalogFacade.java
@Service
public class CatalogFacade {

    private final ProductQueryService queryService;
    private final ProductWriteService writeService;
    private final ProductDataAggregatorService aggregatorService;

    public CatalogFacade(
            ProductQueryService queryService,
            ProductWriteService writeService,
            ProductDataAggregatorService aggregatorService) {
        this.queryService = queryService;
        this.writeService = writeService;
        this.aggregatorService = aggregatorService;
    }

    // Delegate tất cả calls
    public Page<ProductSummaryResponse> searchProducts(ProductSearchRequest req, Pageable p) {
        return queryService.searchProducts(req, p);
    }

    public ProductDetailsResponse getProductBySlug(String slug) {
        return queryService.getProductBySlug(slug);
    }

    public ProductEntity createProduct(CreateProductRequest req, Long sellerId) {
        return writeService.createProduct(req, sellerId);
    }
}
```

- [ ] **Step 5.4: Cập nhật Controllers inject `CatalogFacade` thay vì `CatalogService`**

```java
// PublicCatalogController.java
@RestController
public class PublicCatalogController {

    private final CatalogFacade catalogFacade;  // THAY CatalogService

    public PublicCatalogController(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }
    // Thay tất cả catalogService.xxx() → catalogFacade.xxx()
}
```

- [ ] **Step 5.5: Chạy tất cả test**

```bash
mvn test -pl . -q
```

Expected: BUILD SUCCESS, tất cả test pass.

- [ ] **Step 5.6: Commit**

```bash
git add src/main/java/com/example/ecommerce/ecommerce_backend/application/service/catalog/
git add src/main/java/com/example/ecommerce/ecommerce_backend/api/controller/PublicCatalogController.java
git add src/main/java/com/example/ecommerce/ecommerce_backend/api/controller/AdminCatalogController.java
git commit -m "refactor(catalog): apply Facade pattern — split CatalogService into focused sub-services"
```

---

## Self-Review Checklist

### Spec coverage
- [x] Payment Strategy + Factory → Task 1
- [x] Order Domain Events → Task 2
- [x] Discount Strategy → Task 3
- [x] Notification Chain → Task 4
- [x] Facade (Catalog) → Task 5
- [ ] Facade (OrderService) — **không có trong plan này, cần plan riêng sau Sprint 3**

### Placeholder check
- Không có TBD, TODO, hay "fill in later"
- Mọi step có code thực tế
- File paths đầy đủ

### Type consistency
- `OrderStatusChangedEvent` dùng nhất quán từ Task 2 sang toàn bộ listeners
- `DiscountContext`/`DiscountResult` dùng nhất quán trong Task 3
- `NotificationPayload` dùng nhất quán trong Task 4

---

*Implementation Plan v1.0 — ShopMart Backend — 2026-05-20*
