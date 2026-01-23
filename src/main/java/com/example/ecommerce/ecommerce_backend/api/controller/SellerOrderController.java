package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderDetailResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.UpdateOrderStatusRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.SellerOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/seller/orders")
@Tag(name = "Seller Orders", description = "Seller order management")
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    public SellerOrderController(SellerOrderService sellerOrderService) {
        this.sellerOrderService = sellerOrderService;
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

    @GetMapping
    @Operation(summary = "List orders", description = "Get all orders for seller's shop")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getOrders(
            @RequestParam Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<OrderSummaryResponse> orders = sellerOrderService.getShopOrders(shopId, currentUserId(), pageable);
        return ResponseHelper.page(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Orders by status", description = "Get orders filtered by status")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getOrdersByStatus(
            @RequestParam Long shopId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderSummaryResponse> orders = sellerOrderService.getShopOrdersByStatus(shopId, currentUserId(), status, pageable);
        return ResponseHelper.page(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Order details", description = "Get order details")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @RequestParam Long shopId,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse order = sellerOrderService.getOrderDetail(shopId, currentUserId(), orderId);
        return ResponseHelper.ok(order);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update status", description = "Update order status")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @RequestParam Long shopId,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderDetailResponse order = sellerOrderService.updateOrderStatus(
                shopId,
                currentUserId(),
                orderId,
                request.status(),
                request.note(),
                request.trackingNumber()
        );
        return ResponseHelper.ok(order, "Order status updated");
    }

    @GetMapping("/stats")
    @Operation(summary = "Order statistics", description = "Get order statistics for shop")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> getOrderStats(@RequestParam Long shopId) {
        OrderStatsResponse stats = sellerOrderService.getOrderStats(shopId, currentUserId());
        return ResponseHelper.ok(stats);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @RequestParam Long shopId,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason
    ) {
        sellerOrderService.cancelOrder(shopId, currentUserId(), orderId, reason);
        return ResponseHelper.ok(null, "Order cancelled successfully");
    }
    
    // === NEW: Shipping Management Endpoints ===
    
    @PutMapping("/{orderId}/shipping")
    @Operation(summary = "Set shipping info", description = "Set shipping provider and tracking information")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> setShippingInfo(
            @RequestParam Long shopId,
            @PathVariable Long orderId,
            @Valid @RequestBody SetShippingInfoRequest request
    ) {
        OrderDetailResponse order = sellerOrderService.setShippingInfo(
                shopId,
                currentUserId(),
                orderId,
                request.shippingProvider(),
                request.trackingNumber(),
                request.trackingUrl(),
                request.estimatedDeliveryDate()
        );
        return ResponseHelper.ok(order, "Shipping info updated");
    }
    
    @PostMapping("/{orderId}/ship")
    @Operation(summary = "Mark shipped", description = "Mark order as shipped with tracking details")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> markShipped(
            @RequestParam Long shopId,
            @PathVariable Long orderId,
            @Valid @RequestBody MarkShippedRequest request
    ) {
        OrderDetailResponse order = sellerOrderService.markShipped(
                shopId,
                currentUserId(),
                orderId,
                request.shippingProvider(),
                request.trackingNumber(),
                request.trackingUrl(),
                request.estimatedDeliveryDate()
        );
        return ResponseHelper.ok(order, "Order marked as shipped");
    }
    
    @PostMapping("/{orderId}/delivery-failed")
    @Operation(summary = "Mark delivery failed", description = "Mark order delivery as failed")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> markDeliveryFailed(
            @RequestParam Long shopId,
            @PathVariable Long orderId,
            @RequestParam String reason
    ) {
        OrderDetailResponse order = sellerOrderService.markDeliveryFailed(
                shopId,
                currentUserId(),
                orderId,
                reason
        );
        return ResponseHelper.ok(order, "Delivery marked as failed");
    }
    
    @PostMapping("/{orderId}/retry-delivery")
    @Operation(summary = "Retry delivery", description = "Retry delivery after failure")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> retryDelivery(
            @RequestParam Long shopId,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse order = sellerOrderService.retryDelivery(
                shopId,
                currentUserId(),
                orderId
        );
        return ResponseHelper.ok(order, "Delivery retry initiated");
    }
    
    @GetMapping("/pending-confirmation")
    @Operation(summary = "Pending confirmation", description = "Get orders pending buyer confirmation")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getPendingConfirmation(
            @RequestParam Long shopId
    ) {
        List<OrderSummaryResponse> orders = sellerOrderService.getPendingConfirmationOrders(shopId, currentUserId());
        return ResponseHelper.ok(orders);
    }
    
    // === NEW: Batch Operations ===
    
    @PostMapping("/batch-ship")
    @Operation(summary = "Batch ship orders", description = "Ship multiple orders at once")
    public ResponseEntity<ApiResponse<List<OrderDetailResponse>>> batchShipOrders(
            @RequestParam Long shopId,
            @RequestBody BatchOrderRequest request
    ) {
        List<OrderDetailResponse> results = sellerOrderService.batchShipOrders(shopId, currentUserId(), request.orderIds());
        return ResponseHelper.ok(results, "Batch shipping processed");
    }

    @PostMapping("/batch-print-labels")
    @Operation(summary = "Print batch labels", description = "Generate HTML for shipping labels")
    public ResponseEntity<String> printBatchLabels(
            @RequestParam Long shopId,
            @RequestBody BatchOrderRequest request
    ) {
        String html = sellerOrderService.generateBatchShippingLabels(shopId, currentUserId(), request.orderIds());
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
    
    // === Request DTOs as inner records ===
    
    public record BatchOrderRequest(List<Long> orderIds) {}
    
    public record SetShippingInfoRequest(
            String shippingProvider,
            String trackingNumber,
            String trackingUrl,
            java.time.LocalDateTime estimatedDeliveryDate
    ) {}
    
    public record MarkShippedRequest(
            String shippingProvider,
            String trackingNumber,
            String trackingUrl,
            java.time.LocalDateTime estimatedDeliveryDate
    ) {}
}
