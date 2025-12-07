package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SkuRequest(
        @NotBlank String skuCode,
        @NotBlank String optionSignature,
        long price,
        Long compareAtPrice,
        int stockOnHand,
        boolean active
) {}
