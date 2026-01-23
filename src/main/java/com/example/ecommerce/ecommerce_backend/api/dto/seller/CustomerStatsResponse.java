package com.example.ecommerce.ecommerce_backend.api.dto.seller;

public record CustomerStatsResponse(
        Long totalCustomers,
        Long newCustomers,
        Long returningCustomers,
        Double averageCustomerValue,
        Long totalCustomerRevenue
) {}
