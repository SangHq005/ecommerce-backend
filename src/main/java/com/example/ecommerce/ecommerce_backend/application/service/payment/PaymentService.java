package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentCallbackResult;
import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayFactory;
import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayStrategy;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PaymentEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PaymentJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.notification.EmailService;
import com.example.ecommerce.ecommerce_backend.application.service.order.OrderStatusHistoryService;

import java.util.HashMap;
import java.util.Map;
import java.time.Duration;
/**
 * Application-layer service that orchestrates payment lifecycle operations.
 *
 * <h3>Design Pattern: Strategy + Factory</h3>
 * <p>
 * All gateway-specific logic (URL generation, signature validation, callback parsing)
 * is delegated to the appropriate {@link PaymentGatewayStrategy} resolved by
 * {@link PaymentGatewayFactory}.  This class is completely unaware of VNPay,
 * MoMo, or any other gateway's wire format.
 * </p>
 *
 * <h3>Adding a new gateway</h3>
 * <ol>
 *   <li>Implement {@link PaymentGatewayStrategy} and annotate with {@code @Service}.</li>
 *   <li>No changes needed here or in {@link PaymentGatewayFactory}.</li>
 * </ol>
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentJpaRepository        paymentRepository;
    private final OrderJpaRepository          orderRepository;
    private final ReservationService          reservationService;
    private final EmailService                emailService;
    private final UserJpaRepository           userRepository;
    private final OrderStatusHistoryService   orderStatusHistoryService;
    private final PaymentGatewayFactory       gatewayFactory;
    private final StringRedisTemplate         redis;
    private final TransactionTemplate         transactionTemplate;

    public PaymentService(
            PaymentJpaRepository paymentRepository,
            OrderJpaRepository orderRepository,
            ReservationService reservationService,
            EmailService emailService,
            UserJpaRepository userRepository,
            OrderStatusHistoryService orderStatusHistoryService,
            PaymentGatewayFactory gatewayFactory,
            StringRedisTemplate redis,
            PlatformTransactionManager transactionManager
    ) {
        this.paymentRepository      = paymentRepository;
        this.orderRepository        = orderRepository;
        this.reservationService     = reservationService;
        this.emailService           = emailService;
        this.userRepository         = userRepository;
        this.orderStatusHistoryService = orderStatusHistoryService;
        this.gatewayFactory         = gatewayFactory;
        this.redis                  = redis;
        this.transactionTemplate    = new TransactionTemplate(transactionManager);
    }

    // =========================================================================
    // Payment creation
    // =========================================================================

    /**
     * Creates (or returns an existing) PENDING payment record for an order.
     *
     * @param order  the order being paid
     * @param method the payment method chosen by the buyer
     * @return saved {@link PaymentEntity}
     */
    @Transactional
    public PaymentEntity createPayment(OrderEntity order, PaymentMethod method) {
        var existing = paymentRepository.findByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING);
        if (existing.isPresent()) {
            log.info("Reusing existing PENDING payment for order: {}", order.getOrderCode());
            return existing.get();
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGateway(method.name());

        PaymentEntity saved = paymentRepository.save(payment);
        log.info("Created PENDING payment {} for order {}", saved.getId(), order.getOrderCode());
        return saved;
    }

    // =========================================================================
    // Gateway URL creation (delegates to Strategy)
    // =========================================================================

    /**
     * Generates the payment redirect URL for the given gateway.
     *
     * @param order         order to pay for
     * @param gatewayName   e.g. "VNPAY", "MOMO" (case-insensitive)
     * @param clientIp      caller IP address
     * @return gateway payment URL
     */
    public String createPaymentUrl(OrderEntity order, String gatewayName, String clientIp) {
        PaymentGatewayStrategy strategy = gatewayFactory.getStrategy(gatewayName);
        String url = strategy.createPaymentUrl(order, clientIp);
        log.info("Payment URL created via {} for order {}", gatewayName, order.getOrderCode());
        return url;
    }

    // =========================================================================
    // Unified callback processing (Strategy pattern — no gateway if/else)
    // =========================================================================

    /**
     * Validates and processes a payment gateway callback in a gateway-agnostic way.
     * <p>
     * Flow:
     * <ol>
     *   <li>Resolve the correct {@link PaymentGatewayStrategy} from the factory</li>
     *   <li>Validate the callback signature</li>
     *   <li>Parse the callback into a {@link PaymentCallbackResult}</li>
     *   <li>Persist result and update order status</li>
     * </ol>
     * </p>
     *
     * @param gatewayName e.g. "VNPAY", "MOMO"
     * @param params      raw key-value pairs from the gateway
     */
    public void processGatewayCallback(String gatewayName, Map<String, String> params) {
        PaymentGatewayStrategy strategy = gatewayFactory.getStrategy(gatewayName);

        // 1. Validate signature
        if (!strategy.validateCallback(new java.util.HashMap<>(params))) {
            log.error("{} callback has invalid signature — ignoring", gatewayName);
            throw new IllegalArgumentException("Invalid " + gatewayName + " callback signature");
        }

        // 2. Parse into normalized result
        PaymentCallbackResult result = strategy.processCallback(params);

        // 3. Persist
        applyCallbackResult(result, gatewayName);
    }

    /**
     * Low-level method that persists a {@link PaymentCallbackResult} without
     * re-validating the signature.  Used by gateway-specific controller endpoints
     * that validate the signature themselves (e.g. VNPay redirect callback).
     *
     * @param result      already-parsed callback result
     * @param gatewayName name used only for logging
     */
    public void applyCallbackResult(PaymentCallbackResult result, String gatewayName) {
        String orderCode = result.orderCode();
        String lockKey = "lock:payment:" + orderCode;

        Boolean acquired = redis.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10));
        if (acquired == null || !acquired) {
            log.warn("Acquisition of lock {} failed. A payment callback for order {} is already in progress.", lockKey, orderCode);
            throw new IllegalStateException("Duplicate payment callback request in progress for order " + orderCode);
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                OrderEntity order = orderRepository.findByOrderCode(orderCode)
                        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

                PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderCode));

                // Idempotency guard — skip if already finalised
                if (payment.getStatus() == PaymentStatus.COMPLETED
                        || payment.getStatus() == PaymentStatus.FAILED) {
                    log.warn("{} callback ignored — payment for order {} already in terminal status: {}",
                            gatewayName, orderCode, payment.getStatus());
                    return;
                }

                payment.setGatewayResponse(result.rawResponse());
                payment.setTransactionId(result.transactionId());

                if (result.success()) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    order.setStatus(OrderStatus.PAID.name());

                    reservationService.commit(orderCode);

                    userRepository.findById(order.getUserId()).ifPresent(user ->
                            emailService.sendOrderConfirmationEmail(user, order)
                    );

                    log.info("{} payment COMPLETED for order {}, transactionId={}",
                            gatewayName, orderCode, result.transactionId());
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    order.setStatus(OrderStatus.CANCELLED.name());

                    reservationService.release(orderCode);

                    log.warn("{} payment FAILED for order {}", gatewayName, orderCode);
                }

                paymentRepository.save(payment);
                orderRepository.save(order);
            });
        } finally {
            redis.delete(lockKey);
        }
    }

    // =========================================================================
    // MoMo redirect verification (dev/staging without IPN)
    // =========================================================================

    /**
     * Verifies a MoMo payment using the redirect params sent by the frontend.
     * Used when the IPN callback is not reachable (e.g. localhost development).
     *
     * @param orderCode platform order code
     * @param transId   MoMo transaction ID from the redirect
     * @param params    all redirect params (for auditing)
     */
    public void processMomoRedirectVerification(String orderCode, String transId, Map<String, String> params) {
        log.info("Processing MoMo redirect verification for order: {}", orderCode);

        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));

        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderCode));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Payment already COMPLETED for order: {}", orderCode);
            return;
        }

        if (!OrderStatus.PAYMENT_PENDING.name().equals(order.getStatus())) {
            log.info("Order {} is not in PAYMENT_PENDING — current: {}", orderCode, order.getStatus());
            return;
        }

        String previousStatus = order.getStatus();

        Map<String, Object> rawResponse = new HashMap<>();
        if (params != null) {
            rawResponse.putAll(params);
        }

        PaymentCallbackResult result = PaymentCallbackResult.success(transId, orderCode, rawResponse);
        applyCallbackResult(result, "MOMO_REDIRECT");

        orderStatusHistoryService.recordSystemChange(
                order.getId(),
                previousStatus,
                OrderStatus.PAID.name(),
                "Payment verified via MoMo redirect (transId: " + transId + ")"
        );

        log.info("MoMo redirect verification SUCCESS for order: {}, transId: {}", orderCode, transId);
    }

    // =========================================================================
    // Queries
    // =========================================================================

    /**
     * Retrieves the payment record for an order.
     *
     * @param orderCode platform order code
     * @return the payment entity
     * @throws IllegalArgumentException if the order or payment is not found
     */
    public PaymentEntity getPaymentByOrderCode(String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderCode));
        return paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderCode));
    }

    // =========================================================================
    // Legacy bridge methods (kept for backward compatibility)
    // These delegate to applyCallbackResult() so all persistence logic stays in one place.
    // =========================================================================

    /**
     * @deprecated Use {@link #processGatewayCallback(String, Map)} with gatewayName="VNPAY" instead.
     */
    @Deprecated
    @Transactional
    public void processVNPayCallback(Map<String, String> vnpParams) {
        PaymentGatewayStrategy strategy = gatewayFactory.getStrategy("VNPAY");
        PaymentCallbackResult result = strategy.processCallback(vnpParams);
        applyCallbackResult(result, "VNPAY");
    }

    /**
     * @deprecated Use {@link #processGatewayCallback(String, Map)} with gatewayName="MOMO" instead.
     */
    @Deprecated
    @Transactional
    public void processMomoCallback(Map<String, String> params) {
        PaymentGatewayStrategy strategy = gatewayFactory.getStrategy("MOMO");
        PaymentCallbackResult result = strategy.processCallback(params);
        applyCallbackResult(result, "MOMO");
    }
}
