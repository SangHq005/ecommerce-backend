package com.example.ecommerce.ecommerce_backend.domain.order;

public enum OrderStatus {
    SUBMITTED,          // Order submitted, awaiting payment
    PAYMENT_PENDING,    // Payment initiated but not confirmed
    PAID,               // Payment confirmed, awaiting seller processing
    PROCESSING,         // Seller is processing the order
    READY_TO_SHIP,      // Order packed and ready for shipment
    SHIPPED,            // Order shipped to customer
    DELIVERED,          // Order delivered successfully
    COMPLETED,          // Order completed (after delivery confirmation)
    CANCELLED,          // Order cancelled
    REFUND_REQUESTED,   // Customer requested refund
    REFUNDED,           // Order refunded
    FULFILLED           // Legacy status
}
