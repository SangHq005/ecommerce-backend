package com.example.ecommerce.ecommerce_backend.api.dto.payment;

import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentMethod;
import com.example.ecommerce.ecommerce_backend.domain.payment.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String orderCode,
        Long amount,
        String currency,
        PaymentMethod method,
        PaymentStatus status,
        String transactionId,
        LocalDateTime createdAt
) {
}
