package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.ecommerce.ecommerce_backend.api.dto.payment.CreatePaymentRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.payment.PaymentResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.payment.PaymentUrlResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.MomoService;
import com.example.ecommerce.ecommerce_backend.application.service.PaymentService;
import com.example.ecommerce.ecommerce_backend.application.service.VNPayService;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PaymentEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment", description = "Payment gateway endpoints")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final MomoService momoService;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.config.VNPayConfig vnPayConfig;
    private final OrderJpaRepository orderRepository;

    public PaymentController(
            PaymentService paymentService,
            VNPayService vnPayService,
            MomoService momoService,
            com.example.ecommerce.ecommerce_backend.infrastructure.config.VNPayConfig vnPayConfig,
            OrderJpaRepository orderRepository
    ) {
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
        this.momoService = momoService;
        this.vnPayConfig = vnPayConfig;
        this.orderRepository = orderRepository;
    }

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

    @PostMapping("/vnpay/create")
    @Operation(summary = "Create VNPay payment URL", description = "Generate payment URL for VNPay gateway")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createVNPayPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = currentUserId();

        // Find order
        OrderEntity order = orderRepository.findByOrderCode(request.orderCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + request.orderCode()));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to pay for this order");
        }

        // Verify order status
        if (!OrderStatus.PAYMENT_PENDING.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATE, "Order is not in PAYMENT_PENDING status. Current status: " + order.getStatus());
        }

        // Create payment record
        PaymentEntity payment = paymentService.createPayment(order, PaymentMethod.VNPAY);

        // Get client IP
        String ipAddress = getClientIp(httpRequest);

        // Generate payment URL
        String paymentUrl = vnPayService.createPaymentUrl(order, ipAddress);

        PaymentUrlResponse response = new PaymentUrlResponse(
                paymentUrl,
                order.getOrderCode(),
                order.getTotalAmount(),
                order.getCurrency()
        );

        return ResponseHelper.ok(response, "Payment URL generated successfully");
    }

    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay payment callback", description = "Handle callback from VNPay gateway")
    public RedirectView vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("Received VNPay callback with params: {}", params);

        try {
            // Validate callback signature
            boolean isValid = vnPayService.validateCallback(new HashMap<>(params));
            if (!isValid) {
                log.error("Invalid VNPay callback signature");
                return new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/failed?reason=invalid_signature");
            }

            // Process payment
            paymentService.processVNPayCallback(params);

            String responseCode = params.get("vnp_ResponseCode");
            String orderCode = params.get("vnp_TxnRef");

            if ("00".equals(responseCode)) {
                // Payment success
                return new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/success?orderCode=" + orderCode);
            } else {
                // Payment failed
                return new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/failed?orderCode=" + orderCode + "&code=" + responseCode);
            }
        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            return new RedirectView(vnPayConfig.getFrontendUrl() + "/payment/error");
        }
    }

    @PostMapping("/momo/create")
    @Operation(summary = "Create MoMo payment URL")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createMomoPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = currentUserId();

        OrderEntity order = orderRepository.findByOrderCode(request.orderCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + request.orderCode()));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to pay for this order");
        }

        if (!OrderStatus.PAYMENT_PENDING.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATE, "Order is not in PAYMENT_PENDING status");
        }

        PaymentEntity payment = paymentService.createPayment(order, PaymentMethod.MOMO);
        
        String paymentUrl = momoService.createPaymentUrl(order);
        
        if (paymentUrl == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to generate MoMo payment URL");
        }

        PaymentUrlResponse response = new PaymentUrlResponse(
                paymentUrl,
                order.getOrderCode(),
                order.getTotalAmount(),
                order.getCurrency()
        );

        return ResponseHelper.ok(response, "MoMo payment URL generated successfully");
    }

    @PostMapping("/momo/callback")
    @Operation(summary = "MoMo payment callback")
    public ResponseEntity<Void> momoCallback(@RequestBody Map<String, String> params) {
        log.info("Received MoMo callback: {}", params);
        if (momoService.validateCallback(params)) {
             paymentService.processMomoCallback(params);
             return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Verify MoMo payment from frontend redirect params.
     * This is used when IPN callback doesn't work (e.g., localhost development).
     * Frontend sends the redirect params to verify and update payment status.
     */
    @PostMapping("/momo/verify")
    @Operation(summary = "Verify MoMo payment from redirect params", 
               description = "Used when IPN callback is not available (dev environment)")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyMomoPayment(
            @RequestBody Map<String, String> params
    ) {
        Long userId = currentUserId();
        
        String momoOrderId = params.get("orderId");
        String resultCode = params.get("resultCode");
        String transId = params.get("transId");
        
        if (momoOrderId == null || resultCode == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Missing orderId or resultCode");
        }
        
        // Extract orderCode from MoMo orderId (format: orderCode_timestamp)
        String orderCode = extractOrderCodeFromMomoOrderId(momoOrderId);
        log.info("Verifying MoMo payment for order: {} (momoOrderId: {}), resultCode: {}", orderCode, momoOrderId, resultCode);
        
        // Find order and verify ownership
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderCode));
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to verify this payment");
        }
        
        // Only process if resultCode is success (0)
        if (!"0".equals(resultCode)) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Payment was not successful. ResultCode: " + resultCode);
        }
        
        // Process the payment (similar to callback but without signature validation for dev)
        paymentService.processMomoRedirectVerification(orderCode, transId, params);
        
        // Return updated payment info
        PaymentEntity payment = paymentService.getPaymentByOrderCode(orderCode);
        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                orderCode,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getCreatedAt()
        );
        
        return ResponseHelper.ok(response, "Payment verified successfully");
    }
    
    /**
     * Extract orderCode from MoMo orderId (format: orderCode_timestamp)
     */
    private String extractOrderCodeFromMomoOrderId(String momoOrderId) {
        if (momoOrderId == null || momoOrderId.isEmpty()) return momoOrderId;
        int lastUnderscore = momoOrderId.lastIndexOf('_');
        if (lastUnderscore == -1) return momoOrderId;
        String suffix = momoOrderId.substring(lastUnderscore + 1);
        if (suffix.matches("\\d{10,}")) {
            return momoOrderId.substring(0, lastUnderscore);
        }
        return momoOrderId;
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Get payment by order code", description = "Retrieve payment information for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderCode(@PathVariable String orderCode) {
        Long userId = currentUserId();

        // Find order and verify ownership
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderCode));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to view this payment");
        }

        // Get payment
        PaymentEntity payment = paymentService.getPaymentByOrderCode(orderCode);

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                orderCode,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getCreatedAt()
        );

        return ResponseHelper.ok(response);
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }
}
