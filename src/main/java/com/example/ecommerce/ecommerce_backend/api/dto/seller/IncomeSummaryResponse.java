package com.example.ecommerce.ecommerce_backend.api.dto.seller;

public record IncomeSummaryResponse(
        Long totalRevenue,
        Long pendingPayouts,
        Long completedPayouts,
        Long thisMonthRevenue,
        Long lastMonthRevenue,
        Double revenueGrowth
) {}
