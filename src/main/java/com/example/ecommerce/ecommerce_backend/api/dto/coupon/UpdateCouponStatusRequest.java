package com.example.ecommerce.ecommerce_backend.api.dto.coupon;

import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCouponStatusRequest(
        @NotNull(message = "Status is required")
        CouponStatus status
) {}
