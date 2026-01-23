package com.example.ecommerce.ecommerce_backend.api.dto.voucher;

import java.time.Instant;
import java.util.List;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherEntity;

/**
 * Response DTO for seller vouchers.
 */
public record SellerVoucherResponse(
    Long id,
    Long shopId,
    String code,
    String name,
    String description,
    String discountType,
    Long discountValue,
    Long maxDiscountAmount,
    Long minOrderAmount,
    Instant startDate,
    Instant endDate,
    Integer usageLimit,
    Integer usageCount,
    Integer usageLimitPerUser,
    String status,
    List<Long> applicableProductIds,
    List<Long> applicableCategoryIds,
    Instant createdAt,
    Instant updatedAt,
    // Computed fields
    boolean isValid,
    Integer remainingUsage
) {
    public static SellerVoucherResponse from(SellerVoucherEntity entity) {
        Integer remaining = entity.getUsageLimit() != null 
            ? entity.getUsageLimit() - entity.getUsageCount() 
            : null;
            
        return new SellerVoucherResponse(
            entity.getId(),
            entity.getShopId(),
            entity.getCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getDiscountType().name(),
            entity.getDiscountValue(),
            entity.getMaxDiscountAmount(),
            entity.getMinOrderAmount(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getUsageLimit(),
            entity.getUsageCount(),
            entity.getUsageLimitPerUser(),
            entity.getStatus().name(),
            entity.getApplicableProductIds(),
            entity.getApplicableCategoryIds(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.isValid(),
            remaining
        );
    }
}
