package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.order.CheckoutRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.order.OrderService;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserAddressJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.mapper.OrderDomainMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderJpaRepository orderRepo;
    @Mock private OrderItemJpaRepository itemRepo;
    @Mock private ProductJpaRepository productRepo;
    @Mock private SkuJpaRepository skuRepo;
    @Mock private UserAddressJpaRepository addressRepo;
    @Mock private ReservationService reservationService;
    @Mock private IdempotencyService idempotencyService;
    @Mock private CouponService couponService;
    @Mock private ObjectMapper om;
    @Mock private com.example.ecommerce.ecommerce_backend.infrastructure.config.OrderProperties orderProperties;
    @Mock private com.example.ecommerce.ecommerce_backend.application.service.order.OrderStatusHistoryService orderHistoryService;
    @Spy private OrderDomainMapper orderMapper = new OrderDomainMapper();

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
         lenient().when(orderProperties.getShippingFeeDefault()).thenReturn(30000L);
         lenient().when(orderProperties.getCurrencyDefault()).thenReturn("VND");
    }

    @Test
    void checkout_ShouldCreateOrder_WhenRequestIsValid() {
        // Arrange
        Long userId = 100L;
        String idemKey = "idem-123";
        Long addressId = 10L;
        Long shopId = 500L;
        Long productId = 1L;
        Long skuId = 11L;

        CheckoutRequest.Item itemDto = new CheckoutRequest.Item(productId, skuId, 2);
        CheckoutRequest req = new CheckoutRequest(List.of(itemDto), addressId, "COD", "Note", null);

        UserAddressEntity address = new UserAddressEntity();
        address.setId(addressId);
        address.setUserId(userId);

        // Create SKU mock data
        SkuEntity sku = new SkuEntity();
        try {
            var idField = SkuEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sku, skuId);
        } catch (Exception e) {
            fail("Failed to set SKU id");
        }
        sku.setProductId(productId);
        sku.setPrice(100000L);

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setShopId(shopId);

        // Mocks
        when(addressRepo.existsByIdAndUserId(addressId, userId)).thenReturn(true);
        when(idempotencyService.begin(any(), any(), any(), any())).thenReturn(null); // No cache hit

        // Batch fetching mocks
        when(skuRepo.findAllById(List.of(skuId))).thenReturn(List.of(sku));
        when(productRepo.findAllById(List.of(productId))).thenReturn(List.of(product));
        try {
            when(om.writeValueAsString(any())).thenReturn("[]");
        } catch (Exception e) { fail("Mock setup failed"); }

        // Act
        List<OrderResponse> responses = orderService.checkout(userId, idemKey, req);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        OrderResponse resp = responses.get(0);
        assertEquals("VND", resp.currency());
        assertEquals(OrderStatus.PROCESSING.name(), resp.status());

        // Verify batch calls were used
        verify(skuRepo).findAllById(anyList());
        verify(productRepo).findAllById(anyList());
        verify(skuRepo, never()).findById(anyLong()); // Ensure Loop N+1 is gone

        // Verify Order Saved
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepo, times(1)).save(orderCaptor.capture());
        OrderEntity savedOrder = orderCaptor.getValue();
        assertEquals(shopId, savedOrder.getShopId());
        assertEquals(30000L, savedOrder.getShippingFee()); // From injected value

        // Verify Reservation
        verify(reservationService).reserve(anyString(), eq(skuId), eq(2));

        // Verify Idempotency Complete
        verify(idempotencyService).complete(eq("order.checkout"), eq(idemKey), eq(200), anyString());
    }

    @Test
    @DisplayName("checkout should throw BadRequest when Idempotency-Key is missing")
    void checkout_ThrowsBadRequest_WhenIdempotencyKeyMissing() {
        // Arrange
        Long userId = 100L;
        CheckoutRequest req = new CheckoutRequest(List.of(), 1L, "COD", "Note", null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderService.checkout(userId, null, req));
        assertThrows(ApiException.class, () -> orderService.checkout(userId, "", req));
        assertThrows(ApiException.class, () -> orderService.checkout(userId, "  ", req));
    }

    @Test
    @DisplayName("checkout should throw NotFound when address does not belong to user")
    void checkout_ThrowsNotFound_WhenAddressInvalid() {
        // Arrange
        Long userId = 100L;
        String idemKey = "idem-123";
        Long addressId = 999L;
        CheckoutRequest.Item itemDto = new CheckoutRequest.Item(1L, 1L, 1);
        CheckoutRequest req = new CheckoutRequest(List.of(itemDto), addressId, "COD", "Note", null);

        when(addressRepo.existsByIdAndUserId(addressId, userId)).thenReturn(false);

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class,
            () -> orderService.checkout(userId, idemKey, req));
        assertEquals(404, ex.getStatus());
    }

    @Test
    @DisplayName("checkout should throw BadRequest when no items provided")
    void checkout_ThrowsBadRequest_WhenNoItems() {
        // Arrange
        Long userId = 100L;
        String idemKey = "idem-123";
        CheckoutRequest req = new CheckoutRequest(List.of(), 1L, "COD", "Note", null);

        when(addressRepo.existsByIdAndUserId(1L, userId)).thenReturn(true);
        when(idempotencyService.begin(any(), any(), any(), any())).thenReturn(null);

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class,
            () -> orderService.checkout(userId, idemKey, req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    @DisplayName("checkout should throw BadRequest when SKU not found")
    void checkout_ThrowsBadRequest_WhenSkuNotFound() {
        // Arrange
        Long userId = 100L;
        String idemKey = "idem-123";
        CheckoutRequest.Item itemDto = new CheckoutRequest.Item(1L, 999L, 1);
        CheckoutRequest req = new CheckoutRequest(List.of(itemDto), 1L, "COD", "Note", null);

        when(addressRepo.existsByIdAndUserId(1L, userId)).thenReturn(true);
        when(idempotencyService.begin(any(), any(), any(), any())).thenReturn(null);
        when(skuRepo.findAllById(List.of(999L))).thenReturn(List.of()); // SKU not found

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class,
            () -> orderService.checkout(userId, idemKey, req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    @DisplayName("checkout should throw BadRequest when SKU does not belong to product")
    void checkout_ThrowsBadRequest_WhenSkuProductMismatch() {
        // Arrange
        Long userId = 100L;
        String idemKey = "idem-123";
        Long productId = 1L;
        Long skuId = 11L;
        Long wrongProductId = 999L;

        CheckoutRequest.Item itemDto = new CheckoutRequest.Item(wrongProductId, skuId, 1);
        CheckoutRequest req = new CheckoutRequest(List.of(itemDto), 1L, "COD", "Note", null);

        SkuEntity sku = new SkuEntity();
        try {
            var idField = SkuEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sku, skuId);
        } catch (Exception e) { fail("Failed to set SKU id"); }
        sku.setProductId(productId); // SKU belongs to product 1, not 999

        when(addressRepo.existsByIdAndUserId(1L, userId)).thenReturn(true);
        when(idempotencyService.begin(any(), any(), any(), any())).thenReturn(null);
        when(skuRepo.findAllById(List.of(skuId))).thenReturn(List.of(sku));

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setShopId(1L);
        when(productRepo.findAllById(List.of(productId))).thenReturn(List.of(product));

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class,
            () -> orderService.checkout(userId, idemKey, req));
        assertEquals(400, ex.getStatus());
        assertTrue(ex.getMessage().contains("does not belong"));
    }

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("Should return paginated orders for user")
        void list_ReturnsPaginatedOrders() {
            // Arrange
            Long userId = 100L;
            Pageable pageable = PageRequest.of(0, 10);

            OrderEntity order = new OrderEntity();
            order.setId(1L);
            order.setOrderCode("OD123");
            order.setStatus("PAID");
            order.setTotalAmount(100000L);
            order.setCurrency("VND");

            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(1L);
            item.setSkuId(11L);
            item.setQuantity(2);
            item.setUnitPrice(50000L);
            item.setTotalPrice(100000L);

            Page<OrderEntity> orderPage = new PageImpl<>(List.of(order), pageable, 1);

            when(orderRepo.findByUserId(userId, pageable)).thenReturn(orderPage);
            when(itemRepo.findByOrderId(1L)).thenReturn(List.of(item));

            // Act
            Page<OrderResponse> result = orderService.list(userId, pageable);

            // Assert
            assertEquals(1, result.getTotalElements());
            OrderResponse resp = result.getContent().get(0);
            assertEquals("OD123", resp.orderCode());
            assertEquals("PAID", resp.status());
            assertEquals(100000L, resp.totalAmount());
            assertEquals(1, resp.items().size());
        }

        @Test
        @DisplayName("Should return empty page when user has no orders")
        void list_ReturnsEmptyPage_WhenNoOrders() {
            // Arrange
            Long userId = 100L;
            Pageable pageable = PageRequest.of(0, 10);

            when(orderRepo.findByUserId(userId, pageable)).thenReturn(Page.empty(pageable));

            // Act
            Page<OrderResponse> result = orderService.list(userId, pageable);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("get()")
    class GetTests {

        @Test
        @DisplayName("Should return order details for valid orderCode")
        void get_ReturnsOrder_WhenFound() {
            // Arrange
            Long userId = 100L;
            String orderCode = "OD123";

            OrderEntity order = new OrderEntity();
            order.setId(1L);
            order.setOrderCode(orderCode);
            order.setUserId(userId);
            order.setStatus("PAID");
            order.setTotalAmount(100000L);
            order.setCurrency("VND");

            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(1L);
            item.setSkuId(11L);
            item.setQuantity(2);
            item.setUnitPrice(50000L);
            item.setTotalPrice(100000L);

            when(orderRepo.findByOrderCode(orderCode)).thenReturn(Optional.of(order));
            when(itemRepo.findByOrderId(1L)).thenReturn(List.of(item));

            // Act
            OrderResponse result = orderService.get(userId, orderCode);

            // Assert
            assertEquals(orderCode, result.orderCode());
            assertEquals("PAID", result.status());
            assertEquals(1, result.items().size());
        }

        @Test
        @DisplayName("Should throw NotFound when order does not exist")
        void get_ThrowsNotFound_WhenOrderNotExists() {
            // Arrange
            Long userId = 100L;
            String orderCode = "INVALID";

            when(orderRepo.findByOrderCode(orderCode)).thenReturn(Optional.empty());

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class,
                () -> orderService.get(userId, orderCode));
            assertEquals(404, ex.getStatus());
        }

        @Test
        @DisplayName("Should throw NotFound when order belongs to different user")
        void get_ThrowsNotFound_WhenOrderBelongsToDifferentUser() {
            // Arrange
            Long userId = 100L;
            String orderCode = "OD123";

            OrderEntity order = new OrderEntity();
            order.setId(1L);
            order.setOrderCode(orderCode);
            order.setUserId(999L); // Different user

            when(orderRepo.findByOrderCode(orderCode)).thenReturn(Optional.of(order));

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class,
                () -> orderService.get(userId, orderCode));
            assertEquals(404, ex.getStatus());
        }
    }

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("Should cancel order and release reservations")
        void cancel_Success() {
            // Arrange
            Long userId = 100L;
            String orderCode = "OD123";

            OrderEntity order = new OrderEntity();
            order.setId(1L);
            order.setOrderCode(orderCode);
            order.setUserId(userId);
            order.setStatus("PAYMENT_PENDING");

            when(orderRepo.findByOrderCode(orderCode)).thenReturn(Optional.of(order));
            when(itemRepo.findByOrderId(1L)).thenReturn(List.of());

            // Act
            orderService.cancel(userId, orderCode);

            // Assert
            verify(reservationService).release(orderCode);
            verify(orderRepo).save(order);
        }

        @Test
        @DisplayName("Should throw NotFound when order does not exist")
        void cancel_ThrowsNotFound_WhenOrderNotExists() {
            // Arrange
            Long userId = 100L;
            String orderCode = "INVALID";

            when(orderRepo.findByOrderCode(orderCode)).thenReturn(Optional.empty());

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class,
                () -> orderService.cancel(userId, orderCode));
            assertEquals(404, ex.getStatus());
        }
    }
}
