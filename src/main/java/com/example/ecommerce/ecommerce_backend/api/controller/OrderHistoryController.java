package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderStatusHistoryResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.order.OrderStatusHistoryService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order History", description = "Order status history endpoints")
public class OrderHistoryController {

    private final OrderStatusHistoryService historyService;
    private final OrderJpaRepository orderRepository;

    public OrderHistoryController(
            OrderStatusHistoryService historyService,
            OrderJpaRepository orderRepository
    ) {
        this.historyService = historyService;
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
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid User ID");
        }
    }

    @GetMapping("/orders/{orderCode}/history")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get order history", description = "Get status change history for an order")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getOrderHistory(
            @PathVariable String orderCode
    ) {
        // Find order and verify ownership
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found"));
        
        if (!order.getUserId().equals(currentUserId())) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "Access denied");
        }
        
        List<OrderStatusHistoryResponse> history = historyService.getOrderHistory(order.getId());
        return ResponseHelper.ok(history);
    }

    @GetMapping("/seller/orders/{orderId}/history")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get seller order history", description = "Get status change history for a seller's order")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getSellerOrderHistory(
            @PathVariable Long orderId,
            @RequestParam Long shopId
    ) {
        // Verify order belongs to shop
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found"));
        
        if (!order.getShopId().equals(shopId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "Order does not belong to your shop");
        }
        
        List<OrderStatusHistoryResponse> history = historyService.getOrderHistory(orderId);
        return ResponseHelper.ok(history);
    }
}
