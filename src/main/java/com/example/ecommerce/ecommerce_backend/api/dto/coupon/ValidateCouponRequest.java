package com.example.ecommerce.ecommerce_backend.api.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ValidateCouponRequest(
        @NotBlank(message = "Coupon code is required")
        String couponCode,

        @NotNull(message = "Order total is required")
        Long orderTotal,

        List<Long> productIds,
        List<Long> categoryIds
) {
}
