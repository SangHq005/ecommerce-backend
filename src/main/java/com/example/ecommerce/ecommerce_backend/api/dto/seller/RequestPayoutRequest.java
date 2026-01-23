package com.example.ecommerce.ecommerce_backend.api.dto.seller;

public record RequestPayoutRequest(
        Long amount,
        String note
) {}
