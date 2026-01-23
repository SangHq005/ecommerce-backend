package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.LocalDate;
import java.util.List;

public record RevenueReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        Long totalRevenue,
        Long totalOrders,
        Long totalRefunds,
        Double averageOrderValue,
        List<DailyRevenue> dailyRevenue
) {
    public record DailyRevenue(
            LocalDate date,
            Long revenue,
            Long orderCount
    ) {}
}
