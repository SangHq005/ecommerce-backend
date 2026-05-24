package com.example.ecommerce.ecommerce_backend.application.service.discount;

public record DiscountResult(long discountAmount, String description) {
    public static DiscountResult zero() {
        return new DiscountResult(0L, "");
    }
}
