package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

public record SellerUpdateProductRequest(
        Long categoryId,
        Long brandId,
        String name,
        String description,
        String mainImageUrl,
        Long price,
        Long originalPrice,
        Integer stockQuantity
) {}
