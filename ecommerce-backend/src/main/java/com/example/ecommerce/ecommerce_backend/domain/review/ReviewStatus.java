package com.example.ecommerce.ecommerce_backend.domain.review;

public enum ReviewStatus {
    PENDING,      // Waiting for admin approval
    APPROVED,     // Approved and visible
    REJECTED      // Rejected by admin
}
