package com.example.ecommerce.ecommerce_backend.api.dto.voucher;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for validating a seller voucher.
 */
public record ValidateSellerVoucherRequest(
    @NotBlank(message = "Voucher code is required")
    String code,
    
    @NotNull(message = "Shop ID is required")
    Long shopId,
    
    @NotNull(message = "Order total is required")
    @Positive(message = "Order total must be positive")
    Long orderTotal,
    
    List<Long> productIds,
    
    List<Long> categoryIds
) {}
