package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.payment.CreatePaymentRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.payment.PaymentResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.payment.PaymentUrlResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment gateway endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final OrderJpaRepository orderRepository;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Invalid User ID in token");
        }
    }

    @PostMapping("/vnpay/create")
    @Operation(summary = "Create VNPay payment URL", description = "Generate payment URL for VNPay gateway")
    public ResponseEntity<PaymentUrlResponse> createVNPayPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = currentUserId();

        // Find order
        OrderEntity order = orderRepository.findByOrderCode(request.orderCode())
                .orElseThrow(() -> ApiException.notFound("Order not found: " + request.orderCode()));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw ApiException.forbidden("You don't have permission to pay for this order");
        }

        // Verify order status
        if (!OrderStatus.PAYMENT_PENDING.name().equals(order.getStatus())) {
            throw ApiException.badRequest("Order is not in PAYMENT_PENDING status. Current status: " + order.getStatus());
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

        return ResponseEntity.ok(response);
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
                return new RedirectView("http://localhost:3000/payment/failed?reason=invalid_signature");
            }

            // Process payment
            paymentService.processVNPayCallback(params);

            String responseCode = params.get("vnp_ResponseCode");
            String orderCode = params.get("vnp_TxnRef");

            if ("00".equals(responseCode)) {
                // Payment success
                return new RedirectView("http://localhost:3000/payment/success?orderCode=" + orderCode);
            } else {
                // Payment failed
                return new RedirectView("http://localhost:3000/payment/failed?orderCode=" + orderCode + "&code=" + responseCode);
            }
        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            return new RedirectView("http://localhost:3000/payment/error");
        }
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Get payment by order code", description = "Retrieve payment information for an order")
    public ResponseEntity<PaymentResponse> getPaymentByOrderCode(@PathVariable String orderCode) {
        Long userId = currentUserId();

        // Find order and verify ownership
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> ApiException.notFound("Order not found: " + orderCode));

        if (!order.getUserId().equals(userId)) {
            throw ApiException.forbidden("You don't have permission to view this payment");
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

        return ResponseEntity.ok(response);
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
