package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.LocalDateTime;

public record CustomerSummaryResponse(
        Long userId,
        String email,
        String name,
        Long totalOrders,
        Long totalSpent,
        LocalDateTime lastOrderAt,
        Double averageOrderValue,
        Long customerLifetimeValue
) {}
