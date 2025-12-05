package com.example.ecommerce.ecommerce_backend.api.exception;

/**
 * Exception thrown when a coupon is invalid or cannot be applied
 */
public class InvalidCouponException extends RuntimeException {

    public InvalidCouponException(String message) {
        super(message);
    }

    public InvalidCouponException(String couponCode, String reason) {
        super(String.format("Coupon '%s' is invalid: %s", couponCode, reason));
    }
}
