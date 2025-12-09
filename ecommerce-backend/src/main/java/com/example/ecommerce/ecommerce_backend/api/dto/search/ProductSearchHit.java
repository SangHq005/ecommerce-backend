package com.example.ecommerce.ecommerce_backend.api.dto.search;

import java.time.Instant;

public record ProductSearchHit(
        Long productId,
        String name,
        Long shopId,
        Long categoryId,
        Long brandId,
        long minPrice,
        long maxPrice,
        String thumbnailUrl,
        Instant createdAt,
        Double score
) {}
