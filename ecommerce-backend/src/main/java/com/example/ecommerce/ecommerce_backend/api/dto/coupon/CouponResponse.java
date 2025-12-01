package com.example.ecommerce.ecommerce_backend.api.dto.coupon;

import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponType;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;

import java.time.Instant;
import java.util.List;

public record CouponResponse(
        Long id,
        String code,
        String name,
        String description,
        CouponType type,
        CouponStatus status,
        Long discountValue,
        Long maxDiscountAmount,
        Long minOrderAmount,
        Instant startDate,
        Instant endDate,
        Integer usageLimit,
        Integer usageCount,
        Integer usageLimitPerUser,
        Boolean autoApply,
        List<Long> applicableProductIds,
        List<Long> applicableCategoryIds,
        Instant createdAt
) {
    public static CouponResponse from(CouponEntity entity) {
        return new CouponResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getStatus(),
                entity.getDiscountValue(),
                entity.getMaxDiscountAmount(),
                entity.getMinOrderAmount(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getUsageLimit(),
                entity.getUsageCount(),
                entity.getUsageLimitPerUser(),
                entity.getAutoApply(),
                entity.getApplicableProductIds(),
                entity.getApplicableCategoryIds(),
                entity.getCreatedAt()
        );
    }
}
