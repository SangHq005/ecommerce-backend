package com.example.ecommerce.ecommerce_backend.api.dto.refund;

import jakarta.validation.constraints.NotBlank;

public record ProcessRefundRequest(
        @NotBlank(message = "Status is required")
        String status, // APPROVED, REJECTED

        String adminNote
) {
}
