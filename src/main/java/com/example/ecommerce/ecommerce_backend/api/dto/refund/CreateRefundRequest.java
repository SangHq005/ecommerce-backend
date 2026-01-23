package com.example.ecommerce.ecommerce_backend.api.dto.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRefundRequest(
        // Either orderId OR orderCode should be provided
        Long orderId,
        
        // Alternative: Use orderCode if orderId is not available
        String orderCode,

        @NotBlank(message = "Reason is required")
        String reason,

        String description,

        @NotNull(message = "Refund amount is required")
        Long refundAmount
) {
}

