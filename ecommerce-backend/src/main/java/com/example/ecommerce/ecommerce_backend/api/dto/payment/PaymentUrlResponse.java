package com.example.ecommerce.ecommerce_backend.api.dto.payment;

public record PaymentUrlResponse(
        String paymentUrl,
        String orderCode,
        Long amount,
        String currency
) {
}
