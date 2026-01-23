package com.example.ecommerce.ecommerce_backend.api.dto.voucher;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating seller vouchers.
 */
public record SellerVoucherRequest(
    @NotBlank(message = "Voucher code is required")
    @Size(min = 3, max = 50, message = "Code must be 3-50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code can only contain uppercase letters, numbers, underscore and hyphen")
    String code,
    
    @NotBlank(message = "Voucher name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name,
    
    String description,
    
    @NotNull(message = "Discount type is required")
    String discountType, // PERCENTAGE or FIXED_AMOUNT
    
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    Long discountValue,
    
    Long maxDiscountAmount, // For percentage discounts
    
    Long minOrderAmount, // Minimum order value
    
    @NotNull(message = "Start date is required")
    Instant startDate,
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    Instant endDate,
    
    @Min(value = 1, message = "Usage limit must be at least 1")
    Integer usageLimit,
    
    @Min(value = 1, message = "Per-user limit must be at least 1")
    Integer usageLimitPerUser,
    
    List<Long> applicableProductIds,
    
    List<Long> applicableCategoryIds
) {}
