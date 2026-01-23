package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.LocalDate;
import java.util.List;

public record RevenueChartResponse(
        String period, // "daily", "weekly", "monthly"
        LocalDate startDate,
        LocalDate endDate,
        List<RevenueDataPoint> data
) {
    public record RevenueDataPoint(
            String label, // Date label
            Long revenue,
            Long orderCount
    ) {}
}
