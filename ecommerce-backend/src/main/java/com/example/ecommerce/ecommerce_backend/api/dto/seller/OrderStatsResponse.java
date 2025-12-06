package com.example.ecommerce.ecommerce_backend.api.dto.seller;

public record OrderStatsResponse(
        long totalOrders,
        long pendingOrders,
        long processingOrders,
        long shippedOrders,
        long completedOrders,
        long cancelledOrders,
        long refundRequested,
        Long totalRevenue,
        Long todayRevenue
) {
}
