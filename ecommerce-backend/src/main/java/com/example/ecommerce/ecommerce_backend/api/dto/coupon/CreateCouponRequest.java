package com.example.ecommerce.ecommerce_backend.api.dto.coupon;

import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponType;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public record CreateCouponRequest(
        @NotBlank(message = "Coupon code is required")
        @Size(max = 50, message = "Coupon code must not exceed 50 characters")
        String code,

        @NotBlank(message = "Coupon name is required")
        @Size(max = 255, message = "Coupon name must not exceed 255 characters")
        String name,

        String description,

        @NotNull(message = "Coupon type is required")
        CouponType type,

        @NotNull(message = "Discount value is required")
        @Min(value = 1, message = "Discount value must be at least 1")
        Long discountValue,

        Long maxDiscountAmount,

        Long minOrderAmount,

        @NotNull(message = "Start date is required")
        Instant startDate,

        @NotNull(message = "End date is required")
        Instant endDate,

        Integer usageLimit,

        Integer usageLimitPerUser,

        Boolean autoApply,

        List<Long> applicableProductIds,

        List<Long> applicableCategoryIds,

        List<Long> applicableUserIds
) {
}
