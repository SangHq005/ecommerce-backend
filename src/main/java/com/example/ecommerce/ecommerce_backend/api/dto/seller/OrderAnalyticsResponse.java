package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.util.List;
import java.util.Map;

public record OrderAnalyticsResponse(
        Long totalOrders,
        Map<String, Long> ordersByStatus,
        List<OrderTrend> trends
) {
    public record OrderTrend(
            String period, // "day", "week", "month"
            String label,
            Long orderCount,
            Long revenue
    ) {}
}
