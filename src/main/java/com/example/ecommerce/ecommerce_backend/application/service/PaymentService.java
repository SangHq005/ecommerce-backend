package com.example.ecommerce.ecommerce_backend.application.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PaymentEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PaymentJpaRepository;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentJpaRepository paymentRepository;
    private final OrderJpaRepository orderRepository;
    private final ReservationService reservationService;
    private final EmailService emailService;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository userRepository;
    private final OrderStatusHistoryService orderStatusHistoryService;

    public PaymentService(
            PaymentJpaRepository paymentRepository,
            OrderJpaRepository orderRepository,
            ReservationService reservationService,
            EmailService emailService,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository userRepository,
            OrderStatusHistoryService orderStatusHistoryService
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.reservationService = reservationService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.orderStatusHistoryService = orderStatusHistoryService;
    }

    /**
     * Create payment record for order
     */
    @Transactional
    public PaymentEntity createPayment(OrderEntity order, PaymentMethod method) {
        // Check if payment already exists for this order
        var existingPayment = paymentRepository.findByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING);
        if (existingPayment.isPresent()) {
            log.info("Payment already exists for order: {}", order.getOrderCode());
            return existingPayment.get();
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGateway(method.name());

        PaymentEntity savedPayment = paymentRepository.save(payment);
        log.info("Created payment {} for order {}", savedPayment.getId(), order.getOrderCode());

        return savedPayment;
    }

    /**
     * Process VNPay callback
     */
    @Transactional
    public void processVNPayCallback(Map<String, String> vnpParams) {
        String txnRef = vnpParams.get("vnp_TxnRef"); // This is order code
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String amount = vnpParams.get("vnp_Amount");

        log.info("Processing VNPay callback for order: {}, responseCode: {}", txnRef, responseCode);

        // Find order
        OrderEntity order = orderRepository.findByOrderCode(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + txnRef));

        // Find payment
        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + txnRef));

        // Check idempotency - if already processed, skip
        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            log.warn("Payment already processed for order: {}, current status: {}", txnRef, payment.getStatus());
            return;
        }

        // Store gateway response
        Map<String, Object> gatewayResponse = new HashMap<>();
        vnpParams.forEach((key, value) -> gatewayResponse.put(key, value));
        payment.setGatewayResponse(gatewayResponse);
        payment.setTransactionId(transactionNo);

        // Check response code
        if ("00".equals(responseCode)) {
            // Payment successful
            payment.setStatus(PaymentStatus.COMPLETED);
            order.setStatus(OrderStatus.PAID.name());

            // Commit stock reservation
            reservationService.commit(txnRef);

            // Send order confirmation email
            userRepository.findById(order.getUserId()).ifPresent(user -> {
                emailService.sendOrderConfirmationEmail(user, order);
            });

            log.info("Payment successful for order: {}, transaction: {}", txnRef, transactionNo);
        } else {
            // Payment failed
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED.name());

            // Release stock reservation
            reservationService.release(txnRef);

            log.warn("Payment failed for order: {}, responseCode: {}", txnRef, responseCode);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    /**
     * Process MoMo callback
     */
    @Transactional
    public void processMomoCallback(Map<String, String> params) {
        // MoMo orderId format: orderCode_timestamp (to ensure uniqueness on retries)
        String momoOrderId = params.get("orderId");
        String transId = params.get("transId");
        String resultCode = params.get("resultCode");
        String message = params.get("message");
        
        // Extract original orderCode from MoMo orderId (format: orderCode_timestamp)
        // Find the last underscore followed by timestamp (numeric)
        String orderCode = extractOrderCodeFromMomoOrderId(momoOrderId);

        log.info("Processing MoMo callback for order: {} (momoOrderId: {}), resultCode: {}", orderCode, momoOrderId, resultCode);

        // Find order
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

        // Find payment
        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderCode));

        // Check idempotency
        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            log.warn("Payment already processed for order: {}, current status: {}", orderCode, payment.getStatus());
            return;
        }

        Map<String, Object> gatewayResponse = new HashMap<>(params);
        payment.setGatewayResponse(gatewayResponse);
        payment.setTransactionId(transId);

        if ("0".equals(resultCode)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            order.setStatus(OrderStatus.PAID.name());
            
            // DEBUG LOGGING
            System.out.println("DEBUG: PaymentService - Updating Order " + orderCode + " to PAID");

            reservationService.commit(orderCode);
            
            userRepository.findById(order.getUserId()).ifPresent(user -> {
                emailService.sendOrderConfirmationEmail(user, order);
            });
            
            log.info("Payment successful for order: {}, transaction: {}", orderCode, transId);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED.name());
            reservationService.release(orderCode);
            log.warn("Payment failed for order: {}, resultCode: {}, message: {}", orderCode, resultCode, message);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }
    
    /**
     * Process MoMo payment verification from frontend redirect.
     * Used when IPN callback doesn't work (e.g., localhost development).
     */
    @Transactional
    public void processMomoRedirectVerification(String orderCode, String transId, Map<String, String> params) {
        log.info("Processing MoMo redirect verification for order: {}", orderCode);
        
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));
        
        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderCode));
        
        // Skip if already processed
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Payment already marked as COMPLETED for order: {}", orderCode);
            return;
        }
        
        // Skip if order is not in PAYMENT_PENDING
        if (!OrderStatus.PAYMENT_PENDING.name().equals(order.getStatus())) {
            log.info("Order {} is not in PAYMENT_PENDING status, current status: {}", orderCode, order.getStatus());
            return;
        }
        
        // Update payment and order status
        String previousStatus = order.getStatus();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(transId);
        order.setStatus(OrderStatus.PAID.name());
        
        // Record status change
        orderStatusHistoryService.recordSystemChange(
            order.getId(),
            previousStatus,
            OrderStatus.PAID.name(),
            "Payment verified via MoMo redirect (transId: " + transId + ")"
        );
        
        paymentRepository.save(payment);
        orderRepository.save(order);
        
        log.info("Payment verified successfully for order: {}, transId: {}", orderCode, transId);
    }
    
    /**
     * Extract original orderCode from MoMo orderId
     * MoMo orderId format: orderCode_timestamp (e.g., OD123_456_1_1234567890123)
     * We need to find the last underscore followed by pure digits (timestamp)
     */
    private String extractOrderCodeFromMomoOrderId(String momoOrderId) {
        if (momoOrderId == null || momoOrderId.isEmpty()) {
            return momoOrderId;
        }
        
        // Find the last underscore
        int lastUnderscore = momoOrderId.lastIndexOf('_');
        if (lastUnderscore == -1) {
            return momoOrderId; // No underscore, return as-is
        }
        
        // Check if everything after the last underscore is numeric (timestamp)
        String suffix = momoOrderId.substring(lastUnderscore + 1);
        if (suffix.matches("\\d+") && suffix.length() >= 10) {
            // It's a timestamp, extract the orderCode (everything before)
            return momoOrderId.substring(0, lastUnderscore);
        }
        
        // Not a timestamp suffix, return as-is (fallback for old format)
        return momoOrderId;
    }

    /**
     * Get payment by order code
     */
    public PaymentEntity getPaymentByOrderCode(String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

        return paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderCode));
    }
}
