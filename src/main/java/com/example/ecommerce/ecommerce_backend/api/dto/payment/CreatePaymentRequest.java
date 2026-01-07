package com.example.ecommerce.ecommerce_backend.api.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record CreatePaymentRequest(
        @NotBlank(message = "Order code is required")
        String orderCode
) {
}
