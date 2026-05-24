package com.example.ecommerce.ecommerce_backend.api.dto.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record DashboardStatsResponse(
        // User Statistics
        long totalUsers,
        long newUsersToday,
        long newUsersThisMonth,
        long activeUsers,

        // Order Statistics
        long totalOrders,
        long ordersToday,
        long ordersThisMonth,
        long pendingOrders,
        long processingOrders,
        long completedOrders,

        // Revenue Statistics
        Long totalRevenue,
        Long revenueToday,
        Long revenueThisMonth,
        Long averageOrderValue,

        // Product Statistics
        long totalProducts,
        long activeProducts,
        long lowStockProducts,
        long outOfStockProducts,

        // Shop Statistics
        long totalShops,
        long activeShops,
        long pendingShopApprovals,

        // Product Approval Statistics
        long pendingProductApprovals,

        // Recent Activities
        List<RecentActivity> recentActivities,

        // Revenue Chart Data (last 30 days)
        List<DailyRevenue> revenueChart,

        // Top Products
        List<TopProduct> topProducts,

        // Top Categories
        List<TopCategory> topCategories,

        LocalDateTime generatedAt
) {
    public record RecentActivity(
            String type,        // ORDER, USER, PRODUCT, REVIEW
            String description,
            LocalDateTime timestamp
    ) {}

    public record DailyRevenue(
            String date,
            Long revenue,
            int orderCount
    ) {}

    public record TopProduct(
            Long productId,
            String productName,
            long soldCount,
            Long revenue
    ) {}

    public record TopCategory(
            Long categoryId,
            String categoryName,
            long productCount,
            long orderCount
    ) {}
}
