package com.example.ecommerce.ecommerce_backend.api.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.AnalyticsOverviewResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.RevenueChartResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.TopProductsResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.SellerAnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/seller/analytics")
@Tag(name = "Seller Analytics", description = "Seller analytics and reporting")
@PreAuthorize("hasRole('SELLER')")
public class SellerAnalyticsController {

    private final SellerAnalyticsService analyticsService;

    public SellerAnalyticsController(SellerAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
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

    @GetMapping("/overview")
    @Operation(summary = "Get analytics overview", description = "Get comprehensive analytics overview with revenue, orders, products, and customers metrics")
    public ResponseEntity<ApiResponse<AnalyticsOverviewResponse>> getOverview() {
        AnalyticsOverviewResponse overview = analyticsService.getOverview(currentUserId());
        return ResponseHelper.ok(overview);
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue chart data", description = "Get revenue data for chart visualization")
    public ResponseEntity<ApiResponse<RevenueChartResponse>> getRevenueChart(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        RevenueChartResponse chart = analyticsService.getRevenueChart(currentUserId(), period, startDate, endDate);
        return ResponseHelper.ok(chart);
    }

    @GetMapping("/products")
    @Operation(summary = "Get top products", description = "Get top selling products by revenue or quantity")
    public ResponseEntity<ApiResponse<TopProductsResponse>> getTopProducts(
            @RequestParam(defaultValue = "revenue") String sortBy,
            @RequestParam(defaultValue = "10") int limit
    ) {
        TopProductsResponse topProducts = analyticsService.getTopProducts(currentUserId(), sortBy, limit);
        return ResponseHelper.ok(topProducts);
    }

    @GetMapping("/orders")
    @Operation(summary = "Get order analytics", description = "Get order statistics and trends")
    public ResponseEntity<ApiResponse<OrderAnalyticsResponse>> getOrderAnalytics() {
        OrderAnalyticsResponse analytics = analyticsService.getOrderAnalytics(currentUserId());
        return ResponseHelper.ok(analytics);
    }

    @GetMapping("/customers")
    @Operation(summary = "Get customer analytics", description = "Get customer statistics and segments")
    public ResponseEntity<ApiResponse<CustomerAnalyticsResponse>> getCustomerAnalytics() {
        CustomerAnalyticsResponse analytics = analyticsService.getCustomerAnalytics(currentUserId());
        return ResponseHelper.ok(analytics);
    }
}
