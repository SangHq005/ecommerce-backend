package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PaymentEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.StockReservationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PaymentJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.StockReservationJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PaymentService Integration Tests")
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentJpaRepository paymentRepository;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private SkuJpaRepository skuRepository;

    @Autowired
    private StockReservationJpaRepository stockReservationRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @MockitoBean
    private EmailService emailService; // Mock to avoid sending actual emails

    private OrderEntity testOrder;
    private SkuEntity testSku;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Mock email service
        doNothing().when(emailService).sendOrderConfirmationEmail(any(), any());

        // Create test user
        testUser = new UserEntity();
        testUser.setEmail("test-payment@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setFullName("Test User");
        testUser.setStatus("ACTIVE");
        testUser = userRepository.save(testUser);

        // Create test SKU for reservation tests
        testSku = new SkuEntity();
        testSku.setProductId(1L);
        testSku.setSkuCode("TEST-SKU-PAY-001");
        testSku.setOptionSignature("test-option");
        testSku.setOptionSignatureHash("test-hash");
        testSku.setStockOnHand(100);
        testSku.setReservedStock(10);
        testSku.setPrice(50000L);
        testSku = skuRepository.save(testSku);

        // Create test order
        testOrder = new OrderEntity();
        testOrder.setOrderCode("PAY-TEST-001");
        testOrder.setUserId(testUser.getId());
        testOrder.setShopId(1L);
        testOrder.setAddressId(1L);
        testOrder.setPaymentMethod("VNPAY");
        testOrder.setStatus(OrderStatus.PAYMENT_PENDING.name());
        testOrder.setTotalAmount(100000L);
        testOrder.setCurrency("VND");
        testOrder.setShippingFee(30000L);
        testOrder = orderRepository.save(testOrder);

        // Create stock reservation
        StockReservationEntity reservation = new StockReservationEntity();
        reservation.setOrderToken(testOrder.getOrderCode());
        reservation.setSkuId(testSku.getId());
        reservation.setQty(2);
        reservation.setStatus("RESERVED");
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        stockReservationRepository.save(reservation);
    }

    @Nested
    @DisplayName("createPayment()")
    class CreatePaymentTests {

        @Test
        @DisplayName("Should create payment for order successfully")
        void createPayment_Success() {
            // Act
            PaymentEntity payment = paymentService.createPayment(testOrder, PaymentMethod.VNPAY);

            // Assert
            assertNotNull(payment);
            assertNotNull(payment.getId());
            assertEquals(testOrder.getId(), payment.getOrderId());
            assertEquals(testOrder.getTotalAmount(), payment.getAmount());
            assertEquals(testOrder.getCurrency(), payment.getCurrency());
            assertEquals(PaymentMethod.VNPAY, payment.getMethod());
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
        }

        @Test
        @DisplayName("Should return existing payment if already exists")
        void createPayment_ReturnsExisting_WhenPaymentExists() {
            // Arrange - Create first payment
            PaymentEntity firstPayment = paymentService.createPayment(testOrder, PaymentMethod.VNPAY);

            // Act - Try to create again
            PaymentEntity secondPayment = paymentService.createPayment(testOrder, PaymentMethod.VNPAY);

            // Assert - Should return same payment
            assertEquals(firstPayment.getId(), secondPayment.getId());
        }
    }

    @Nested
    @DisplayName("processVNPayCallback()")
    class ProcessVNPayCallbackTests {

        @Test
        @DisplayName("Should process successful payment and commit stock")
        void processVNPayCallback_Success() {
            // Arrange
            PaymentEntity payment = paymentService.createPayment(testOrder, PaymentMethod.VNPAY);

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_TxnRef", testOrder.getOrderCode());
            vnpParams.put("vnp_TransactionNo", "TXN123456");
            vnpParams.put("vnp_ResponseCode", "00"); // Success
            vnpParams.put("vnp_Amount", "10000000"); // Amount in VNPay format (x100)

            // Act
            paymentService.processVNPayCallback(vnpParams);

            // Assert - Payment status updated
            PaymentEntity updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
            assertEquals(PaymentStatus.COMPLETED, updatedPayment.getStatus());
            assertEquals("TXN123456", updatedPayment.getTransactionId());
            assertNotNull(updatedPayment.getGatewayResponse());

            // Assert - Order status updated
            OrderEntity updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.PAID.name(), updatedOrder.getStatus());

            // Assert - Stock committed (reserved reduced, on_hand reduced)
            StockReservationEntity reservation = stockReservationRepository
                    .findByOrderTokenAndSkuId(testOrder.getOrderCode(), testSku.getId())
                    .orElseThrow();
            assertEquals("COMMITTED", reservation.getStatus());

            SkuEntity updatedSku = skuRepository.findById(testSku.getId()).orElseThrow();
            assertEquals(98, updatedSku.getStockOnHand()); // 100 - 2
            assertEquals(8, updatedSku.getReservedStock()); // 10 - 2
        }

        @Test
        @DisplayName("Should process failed payment and release stock")
        void processVNPayCallback_FailedPayment_ReleasesStock() {
            // Arrange
            paymentService.createPayment(testOrder, PaymentMethod.VNPAY);

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_TxnRef", testOrder.getOrderCode());
            vnpParams.put("vnp_TransactionNo", "TXN123456");
            vnpParams.put("vnp_ResponseCode", "24"); // Customer cancelled
            vnpParams.put("vnp_Amount", "10000000");

            // Act
            paymentService.processVNPayCallback(vnpParams);

            // Assert - Order cancelled
            OrderEntity updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.CANCELLED.name(), updatedOrder.getStatus());

            // Assert - Stock released
            StockReservationEntity reservation = stockReservationRepository
                    .findByOrderTokenAndSkuId(testOrder.getOrderCode(), testSku.getId())
                    .orElseThrow();
            assertEquals("RELEASED", reservation.getStatus());

            SkuEntity updatedSku = skuRepository.findById(testSku.getId()).orElseThrow();
            assertEquals(100, updatedSku.getStockOnHand()); // Unchanged
            assertEquals(8, updatedSku.getReservedStock()); // 10 - 2 released
        }

        @Test
        @DisplayName("Should be idempotent - skip already processed payment")
        void processVNPayCallback_Idempotent_SkipsAlreadyProcessed() {
            // Arrange - Process once
            paymentService.createPayment(testOrder, PaymentMethod.VNPAY);

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_TxnRef", testOrder.getOrderCode());
            vnpParams.put("vnp_TransactionNo", "TXN123456");
            vnpParams.put("vnp_ResponseCode", "00");
            vnpParams.put("vnp_Amount", "10000000");

            paymentService.processVNPayCallback(vnpParams);

            // Act - Process again (should be idempotent)
            paymentService.processVNPayCallback(vnpParams);

            // Assert - Stock should only be committed once
            SkuEntity updatedSku = skuRepository.findById(testSku.getId()).orElseThrow();
            assertEquals(98, updatedSku.getStockOnHand()); // 100 - 2, not 100 - 4
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void processVNPayCallback_ThrowsException_WhenOrderNotFound() {
            // Arrange
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_TxnRef", "INVALID-ORDER-CODE");
            vnpParams.put("vnp_TransactionNo", "TXN123456");
            vnpParams.put("vnp_ResponseCode", "00");
            vnpParams.put("vnp_Amount", "10000000");

            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> paymentService.processVNPayCallback(vnpParams));
        }
    }

    @Nested
    @DisplayName("getPaymentByOrderCode()")
    class GetPaymentByOrderCodeTests {

        @Test
        @DisplayName("Should return payment by order code")
        void getPaymentByOrderCode_Success() {
            // Arrange
            PaymentEntity created = paymentService.createPayment(testOrder, PaymentMethod.VNPAY);

            // Act
            PaymentEntity found = paymentService.getPaymentByOrderCode(testOrder.getOrderCode());

            // Assert
            assertNotNull(found);
            assertEquals(created.getId(), found.getId());
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void getPaymentByOrderCode_ThrowsException_WhenOrderNotFound() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> paymentService.getPaymentByOrderCode("INVALID-CODE"));
        }

        @Test
        @DisplayName("Should throw exception when payment not found for order")
        void getPaymentByOrderCode_ThrowsException_WhenPaymentNotFound() {
            // Act & Assert - Order exists but no payment
            assertThrows(IllegalArgumentException.class, 
                () -> paymentService.getPaymentByOrderCode(testOrder.getOrderCode()));
        }
    }
}
