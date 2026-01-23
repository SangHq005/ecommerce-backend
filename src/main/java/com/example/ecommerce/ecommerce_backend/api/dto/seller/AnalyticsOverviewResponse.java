package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.util.List;

public record AnalyticsOverviewResponse(
        RevenueMetrics revenue,
        OrderMetrics orders,
        ProductMetrics products,
        CustomerMetrics customers
) {
    public record RevenueMetrics(
            Long totalRevenue,
            Long todayRevenue,
            Long thisWeekRevenue,
            Long thisMonthRevenue,
            Double revenueGrowth
    ) {}
    
    public record OrderMetrics(
            Long totalOrders,
            Long todayOrders,
            Long thisWeekOrders,
            Long thisMonthOrders,
            Double orderGrowth,
            Double averageOrderValue,
            Double conversionRate
    ) {}
    
    public record ProductMetrics(
            Long totalProducts,
            Long activeProducts,
            List<TopProduct> topProducts
    ) {
        public record TopProduct(
                Long productId,
                String productName,
                Long revenue,
                Long quantitySold
        ) {}
    }
    
    public record CustomerMetrics(
            Long totalCustomers,
            Long newCustomers,
            Double customerGrowth,
            Double averageCustomerValue
    ) {}
}
