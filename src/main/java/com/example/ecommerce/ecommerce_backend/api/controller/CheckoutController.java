package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.order.CheckoutRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.order.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Checkout", description = "Order checkout endpoints")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
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

    @PostMapping("/checkout")
    @Operation(summary = "Checkout", description = "Create orders from cart items")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> checkout(
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
            @Valid @RequestBody CheckoutRequest req
    ) {
        List<OrderResponse> orders = orderService.checkout(currentUserId(), idemKey, req);
        return ResponseHelper.created(orders, "Checkout completed successfully");
    }
}
