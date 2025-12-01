package com.example.ecommerce.ecommerce_backend.api.dto.coupon;

public record CouponValidationResponse(
        boolean valid,
        String message,
        Long discountAmount,
        String couponCode,
        String couponName
) {
    public static CouponValidationResponse invalid(String message) {
        return new CouponValidationResponse(false, message, null, null, null);
    }

    public static CouponValidationResponse valid(Long discountAmount, String code, String name) {
        return new CouponValidationResponse(true, "Coupon applied successfully", discountAmount, code, name);
    }
}
