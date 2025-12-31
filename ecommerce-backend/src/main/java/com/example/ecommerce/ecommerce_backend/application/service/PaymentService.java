package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PaymentEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentJpaRepository paymentRepository;
    private final OrderJpaRepository orderRepository;
    private final ReservationService reservationService;
    private final EmailService emailService;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository userRepository;

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
     * Get payment by order code
     */
    public PaymentEntity getPaymentByOrderCode(String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

        return paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderCode));
    }
}
