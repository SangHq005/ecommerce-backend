package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.payment.CreatePaymentRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.payment.PaymentResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.payment.PaymentUrlResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.payment.MomoService;
import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentService;
import com.example.ecommerce.ecommerce_backend.application.service.payment.VNPayService;
import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayFactory;
import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayStrategy;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.VNPayConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PaymentEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for payment gateway operations.
 *
 * <h3>Design Pattern: Strategy via Factory</h3>
 * <p>
 * Each gateway endpoint resolves its {@link PaymentGatewayStrategy} through
 * {@link PaymentGatewayFactory} rather than injecting concrete service classes directly.
 * Adding a new gateway requires no changes here — only a new {@code @Service} that
 * implements {@link PaymentGatewayStrategy}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment", description = "Payment gateway endpoints")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService        paymentService;
    private final PaymentGatewayFactory gatewayFactory;
    private final VNPayConfig           vnPayConfig;
    private final OrderJpaRepository    orderRepository;

    // Kept for backward-compatible injection by Spring Boot; no longer used directly in business logic
    @SuppressWarnings("unused")
    private final VNPayService  vnPayService;
    @SuppressWarnings("unused")
    private final MomoService   momoService;

    public PaymentController(
            PaymentService paymentService,
            PaymentGatewayFactory gatewayFactory,
            VNPayConfig vnPayConfig,
            OrderJpaRepository orderRepository,
            VNPayService vnPayService,
            MomoService momoService
    ) {
        this.paymentService  = paymentService;
        this.gatewayFactory  = gatewayFactory;
        this.vnPayConfig     = vnPayConfig;
        this.orderRepository = orderRepository;
        this.vnPayService    = vnPayService;
        this.momoService     = momoService;
    }

    // =========================================================================
    // VNPay endpoints
    // =========================================================================

    @PostMapping("/vnpay/create")
    @Operation(summary = "Create VNPay payment URL", description = "Generate payment URL for VNPay gateway")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createVNPayPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = currentUserId();
        OrderEntity order = resolveOrderForPayment(request.orderCode(), userId);

        // Create / reuse payment record
        paymentService.createPayment(order, PaymentMethod.VNPAY);

        // Delegate URL creation to Strategy — controller has no VNPay-specific code
        PaymentGatewayStrategy strategy = gatewayFactory.getStrategy("VNPAY");
        String paymentUrl = strategy.createPaymentUrl(order, getClientIp(httpRequest));

        return ResponseHelper.ok(
                new PaymentUrlResponse(paymentUrl, order.getOrderCode(), order.getTotalAmount(), order.getCurrency()),
                "Payment URL generated successfully"
        );
    }

    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay payment callback", description = "Handle redirect callback from VNPay")
    public RedirectView vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("Received VNPay callback");
        try {
            // Validate signature via strategy (copy params — validateCallback may mutate the map)
            PaymentGatewayStrategy strategy = gatewayFactory.getStrategy("VNPAY");
            boolean isValid = strategy.validateCallback(new HashMap<>(params));

            if (!isValid) {
                log.error("Invalid VNPay callback signature");
                return new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/failed?reason=invalid_signature");
            }

            // Parse callback and persist result
            paymentService.applyCallbackResult(strategy.processCallback(params), "VNPAY");

            String responseCode = params.get("vnp_ResponseCode");
            String orderCode    = params.get("vnp_TxnRef");

            return "00".equals(responseCode)
                    ? new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/success?orderCode=" + orderCode)
                    : new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/failed?orderCode=" + orderCode + "&code=" + responseCode);

        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            return new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/error");
        }
    }

    // =========================================================================
    // MoMo endpoints
    // =========================================================================

    @PostMapping("/momo/create")
    @Operation(summary = "Create MoMo payment URL")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createMomoPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = currentUserId();
        OrderEntity order = resolveOrderForPayment(request.orderCode(), userId);

        paymentService.createPayment(order, PaymentMethod.MOMO);

        PaymentGatewayStrategy strategy = gatewayFactory.getStrategy("MOMO");
        String paymentUrl = strategy.createPaymentUrl(order, getClientIp(httpRequest));

        if (paymentUrl == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to generate MoMo payment URL");
        }

        return ResponseHelper.ok(
                new PaymentUrlResponse(paymentUrl, order.getOrderCode(), order.getTotalAmount(), order.getCurrency()),
                "MoMo payment URL generated successfully"
        );
    }

    @PostMapping("/momo/callback")
    @Operation(summary = "MoMo IPN callback")
    public ResponseEntity<Void> momoCallback(@RequestBody Map<String, String> params) {
        log.info("Received MoMo IPN callback");
        // processGatewayCallback validates + parses + persists in one call
        paymentService.processGatewayCallback("MOMO", params);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifies a MoMo payment from the frontend redirect params.
     * Used when IPN is not reachable (localhost development).
     */
    @PostMapping("/momo/verify")
    @Operation(
            summary = "Verify MoMo payment from redirect params",
            description = "Used when IPN callback is not available (dev environment)"
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyMomoPayment(
            @RequestBody Map<String, String> params
    ) {
        Long userId = currentUserId();

        String momoOrderId = params.get("orderId");
        String resultCode  = params.get("resultCode");
        String transId     = params.get("transId");

        if (momoOrderId == null || resultCode == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Missing orderId or resultCode");
        }

        // Recover orderCode via MomoService helper — no duplicated logic here
        MomoService momo = (MomoService) gatewayFactory.getStrategy("MOMO");
        String orderCode = momo.extractOrderCode(momoOrderId);

        log.info("Verifying MoMo payment — orderCode={}, momoOrderId={}, resultCode={}", orderCode, momoOrderId, resultCode);

        // Ownership check
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderCode));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to verify this payment");
        }

        if (!"0".equals(resultCode)) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Payment was not successful. ResultCode: " + resultCode);
        }

        paymentService.processMomoRedirectVerification(orderCode, transId, params);

        PaymentEntity payment = paymentService.getPaymentByOrderCode(orderCode);
        PaymentResponse response = new PaymentResponse(
                payment.getId(), orderCode, payment.getAmount(), payment.getCurrency(),
                payment.getMethod(), payment.getStatus(), payment.getTransactionId(), payment.getCreatedAt()
        );
        return ResponseHelper.ok(response, "Payment verified successfully");
    }

    // =========================================================================
    // Query
    // =========================================================================

    @GetMapping("/{orderCode}")
    @Operation(summary = "Get payment by order code")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderCode(@PathVariable String orderCode) {
        Long userId = currentUserId();

        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderCode));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to view this payment");
        }

        PaymentEntity payment = paymentService.getPaymentByOrderCode(orderCode);
        PaymentResponse response = new PaymentResponse(
                payment.getId(), orderCode, payment.getAmount(), payment.getCurrency(),
                payment.getMethod(), payment.getStatus(), payment.getTransactionId(), payment.getCreatedAt()
        );
        return ResponseHelper.ok(response);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid User ID in token");
        }
    }

    private OrderEntity resolveOrderForPayment(String orderCode, Long userId) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderCode));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to pay for this order");
        }
        if (!OrderStatus.PAYMENT_PENDING.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATE,
                    "Order is not in PAYMENT_PENDING status. Current: " + order.getStatus());
        }
        return order;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip != null ? ip : "127.0.0.1";
    }
}
