package com.example.ecommerce.ecommerce_backend.domain.payment;

public enum PaymentStatus {
    PENDING,        // Payment initiated, waiting for user action
    PROCESSING,     // Payment is being processed by gateway
    COMPLETED,      // Payment successful
    FAILED,         // Payment failed
    REFUNDED,       // Payment refunded
    CANCELLED       // Payment cancelled
}
