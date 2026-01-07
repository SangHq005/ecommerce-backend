package com.example.ecommerce.ecommerce_backend.domain.promotion;

public enum CouponStatus {
    ACTIVE,      // Coupon is active and can be used
    INACTIVE,    // Coupon is temporarily disabled
    EXPIRED,     // Coupon has passed its end date
    DEPLETED     // Coupon usage limit reached
}
