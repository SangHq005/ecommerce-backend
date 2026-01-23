package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.admin.DashboardStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.SalesAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.UserAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.AdminDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Admin dashboard analytics")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Dashboard stats", description = "Get comprehensive dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return ResponseHelper.ok(stats);
    }

    @GetMapping("/analytics/users")
    @Operation(summary = "User analytics", description = "Get user registration and activity analytics")
    public ResponseEntity<ApiResponse<UserAnalyticsResponse>> getUserAnalytics() {
        UserAnalyticsResponse analytics = dashboardService.getUserAnalytics();
        return ResponseHelper.ok(analytics);
    }

    @GetMapping("/analytics/sales")
    @Operation(summary = "Sales analytics", description = "Get sales and revenue analytics")
    public ResponseEntity<ApiResponse<SalesAnalyticsResponse>> getSalesAnalytics() {
        SalesAnalyticsResponse analytics = dashboardService.getSalesAnalytics();
        return ResponseHelper.ok(analytics);
    }
}
