package com.example.ecommerce.ecommerce_backend.api.dto.admin;

import java.util.List;

public record UserAnalyticsResponse(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long newUsersLast7Days,
        long newUsersLast30Days,
        List<UserGrowth> userGrowthChart,
        List<UserByRole> usersByRole
) {
    public record UserGrowth(
            String date,
            long newUsers,
            long totalUsers
    ) {}

    public record UserByRole(
            String role,
            long count
    ) {}
}
