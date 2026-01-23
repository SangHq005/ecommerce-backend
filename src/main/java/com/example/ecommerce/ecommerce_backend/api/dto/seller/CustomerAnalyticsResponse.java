package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.util.List;

public record CustomerAnalyticsResponse(
        Long totalCustomers,
        Long newCustomers,
        Double customerGrowth,
        Double averageCustomerValue,
        List<CustomerSegment> segments
) {
    public record CustomerSegment(
            String segment, // "new", "returning", "vip"
            Long count,
            Long revenue
    ) {}
}
