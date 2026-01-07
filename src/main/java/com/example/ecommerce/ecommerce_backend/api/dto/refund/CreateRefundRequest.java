package com.example.ecommerce.ecommerce_backend.api.dto.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRefundRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotBlank(message = "Reason is required")
        String reason,

        String description,

        @NotNull(message = "Refund amount is required")
        Long refundAmount
) {
}
