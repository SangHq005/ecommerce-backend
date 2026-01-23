package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.OrderService;
import com.example.ecommerce.ecommerce_backend.application.service.ScheduledOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Orders", description = "Client order management endpoints")
public class OrderController {

    private final OrderService orderService;
    private final ScheduledOrderService scheduledOrderService;

    public OrderController(OrderService orderService, ScheduledOrderService scheduledOrderService) {
        this.orderService = orderService;
        this.scheduledOrderService = scheduledOrderService;
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

    @GetMapping("/orders")
    @Operation(summary = "List user orders", description = "Get paginated list of orders for the authenticated user")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<OrderResponse> orders = orderService.list(currentUserId(), pageable);
        return ResponseHelper.page(orders);
    }

    @GetMapping("/orders/{orderCode}")
    @Operation(summary = "Get order details", description = "Get details of a specific order by order code")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable String orderCode) {
        OrderResponse order = orderService.get(currentUserId(), orderCode);
        
        // DEBUG LOGGING
        System.out.println("DEBUG: Fetching order " + orderCode);
        System.out.println("DEBUG: Status in DB/Response: " + order.status());
        System.out.println("DEBUG: Payment Method: " + order.paymentMethod());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(ApiResponse.success(order, ResponseHelper.getCorrelationId(), ResponseHelper.getRequestPath()));
    }

    @PostMapping("/orders/{orderCode}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only if not yet shipped)")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable String orderCode) {
        orderService.cancel(currentUserId(), orderCode);
        return ResponseHelper.ok(null, "Order cancelled successfully");
    }
    
    // === NEW: Confirm Receipt Endpoint ===
    
    @PostMapping("/orders/{orderCode}/confirm-received")
    @Operation(summary = "Confirm receipt", description = "Buyer confirms order has been received")
    public ResponseEntity<ApiResponse<Void>> confirmReceived(@PathVariable String orderCode) {
        scheduledOrderService.buyerConfirmReceipt(currentUserId(), orderCode);
        return ResponseHelper.ok(null, "Order receipt confirmed. Thank you for your purchase!");
    }
    
    @PostMapping("/orders/{orderCode}/request-return")
    @Operation(summary = "Request return", description = "Request to return items from a delivered order")
    public ResponseEntity<ApiResponse<Void>> requestReturn(@PathVariable String orderCode) {
        // This will be handled by refund service in Sprint 2
        // For now, just update order status
        orderService.requestReturn(currentUserId(), orderCode);
        return ResponseHelper.ok(null, "Return request submitted. Please wait for seller response.");
    }
}
