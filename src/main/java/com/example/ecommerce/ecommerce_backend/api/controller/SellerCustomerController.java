package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerDetailResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerCustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/seller/customers")
@Tag(name = "Seller Customers", description = "Seller customer management")
@PreAuthorize("hasRole('SELLER')")
public class SellerCustomerController {

    private final SellerCustomerService customerService;

    public SellerCustomerController(SellerCustomerService customerService) {
        this.customerService = customerService;
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
    @Operation(summary = "List customers", description = "Get all customers with pagination")
    public ResponseEntity<ApiResponse<java.util.List<CustomerSummaryResponse>>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "totalSpent") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CustomerSummaryResponse> customers = customerService.getCustomers(currentUserId(), pageable);
        return ResponseHelper.page(customers);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer detail", description = "Get detailed information about a customer")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getCustomerDetail(
            @PathVariable Long customerId
    ) {
        CustomerDetailResponse detail = customerService.getCustomerDetail(currentUserId(), customerId);
        return ResponseHelper.ok(detail);
    }

    @GetMapping("/{customerId}/orders")
    @Operation(summary = "Get customer orders", description = "Get all orders for a specific customer")
    public ResponseEntity<ApiResponse<java.util.List<OrderSummaryResponse>>> getCustomerOrders(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderSummaryResponse> orders = customerService.getCustomerOrders(currentUserId(), customerId, pageable);
        return ResponseHelper.page(orders);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get customer statistics", description = "Get overall customer statistics")
    public ResponseEntity<ApiResponse<CustomerStatsResponse>> getCustomerStats() {
        CustomerStatsResponse stats = customerService.getCustomerStats(currentUserId());
        return ResponseHelper.ok(stats);
    }
}
