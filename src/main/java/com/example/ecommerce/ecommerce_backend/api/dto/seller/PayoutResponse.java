package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.LocalDateTime;

public record PayoutResponse(
        Long id,
        Long amount,
        String currency,
        String status, // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
        LocalDateTime requestedAt,
        LocalDateTime processedAt,
        String bankAccountNumber,
        String note
) {}
