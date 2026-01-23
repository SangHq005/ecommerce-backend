package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        String type, // "ORDER", "REFUND", "PAYOUT"
        LocalDateTime date,
        String description,
        Long amount,
        String currency,
        String status,
        String referenceId // orderId, refundId, or payoutId
) {}
