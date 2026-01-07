package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.MessageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderDetailResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.UpdateOrderStatusRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.SellerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Invalid User ID");
        }
    }

    /**
     * Get all orders for seller's shop
     */
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getOrders(
            @RequestParam Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderSummaryResponse> orders = sellerOrderService.getShopOrders(shopId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderSummaryResponse>> getOrdersByStatus(
            @RequestParam Long shopId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderSummaryResponse> orders = sellerOrderService.getShopOrdersByStatus(shopId, status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @RequestParam Long shopId,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse order = sellerOrderService.getOrderDetail(shopId, orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDetailResponse> updateOrderStatus(
            @RequestParam Long shopId,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderDetailResponse order = sellerOrderService.updateOrderStatus(
                shopId,
                orderId,
                request.status(),
                request.note()
        );
        return ResponseEntity.ok(order);
    }

    /**
     * Get order statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<OrderStatsResponse> getOrderStats(@RequestParam Long shopId) {
        OrderStatsResponse stats = sellerOrderService.getOrderStats(shopId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<MessageResponse> cancelOrder(
            @RequestParam Long shopId,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason
    ) {
        sellerOrderService.cancelOrder(shopId, orderId, reason);
        return ResponseEntity.ok(new MessageResponse("Order cancelled successfully"));
    }
}
