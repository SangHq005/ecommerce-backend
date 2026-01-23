package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerDetailResponse(
        Long userId,
        String email,
        String name,
        Long totalOrders,
        Long totalSpent,
        LocalDateTime firstOrderAt,
        LocalDateTime lastOrderAt,
        Double averageOrderValue,
        Long customerLifetimeValue,
        List<OrderSummaryResponse> recentOrders
) {}
