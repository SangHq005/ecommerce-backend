package com.example.ecommerce.ecommerce_backend.api.dto.admin;

import java.util.List;

public record SalesAnalyticsResponse(
        Long totalRevenue,
        Long revenueThisMonth,
        Long revenueLastMonth,
        Double growthRate,
        Long averageOrderValue,
        long totalOrders,
        long ordersThisMonth,
        List<MonthlySales> monthlySalesChart,
        List<CategorySales> salesByCategory,
        List<TopSellingProduct> topSellingProducts
) {
    public record MonthlySales(
            String month,
            Long revenue,
            long orderCount
    ) {}

    public record CategorySales(
            Long categoryId,
            String categoryName,
            Long revenue,
            long orderCount
    ) {}

    public record TopSellingProduct(
            Long productId,
            String productName,
            long quantity,
            Long revenue
    ) {}
}
