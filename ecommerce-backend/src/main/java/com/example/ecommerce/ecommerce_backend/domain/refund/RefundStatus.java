package com.example.ecommerce.ecommerce_backend.domain.refund;

public enum RefundStatus {
    PENDING,        // Refund request submitted
    UNDER_REVIEW,   // Being reviewed by shop/admin
    APPROVED,       // Refund approved
    REJECTED,       // Refund rejected
    PROCESSING,     // Refund being processed
    COMPLETED,      // Refund completed
    CANCELLED       // Refund request cancelled
}
