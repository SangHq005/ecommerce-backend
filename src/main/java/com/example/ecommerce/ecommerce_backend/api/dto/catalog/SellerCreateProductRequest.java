package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SellerCreateProductRequest(
        @NotNull Long categoryId,
        Long brandId,
        @NotBlank String name,
        String description,
        String mainImageUrl,
        @NotNull Long price,
        Long originalPrice,
        Integer stockQuantity
) {}
