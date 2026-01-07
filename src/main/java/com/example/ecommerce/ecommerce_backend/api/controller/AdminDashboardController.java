package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.admin.DashboardStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.SalesAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.UserAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.application.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    /**
     * Get comprehensive dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user analytics
     */
    @GetMapping("/analytics/users")
    public ResponseEntity<UserAnalyticsResponse> getUserAnalytics() {
        UserAnalyticsResponse analytics = dashboardService.getUserAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get sales analytics
     */
    @GetMapping("/analytics/sales")
    public ResponseEntity<SalesAnalyticsResponse> getSalesAnalytics() {
        SalesAnalyticsResponse analytics = dashboardService.getSalesAnalytics();
        return ResponseEntity.ok(analytics);
    }
}
