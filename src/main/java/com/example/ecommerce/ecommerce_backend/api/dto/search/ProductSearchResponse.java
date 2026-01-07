package com.example.ecommerce.ecommerce_backend.api.dto.search;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record ProductSearchResponse(
        Long id,
        String name,
        String description,
        Long price,
        String currency,
        Integer stockQuantity,
        String sku,
        Long categoryId,
        String categoryName,
        Long shopId,
        String shopName,
        String imageUrl,
        String status,
        Double averageRating,
        Integer reviewCount,
        LocalDateTime createdAt
) {
    public static ProductSearchResponse from(ProductEntity product, String categoryName, String shopName) {
        LocalDateTime createdAtLocal = product.getCreatedAt() != null
                ? LocalDateTime.ofInstant(product.getCreatedAt(), ZoneId.systemDefault())
                : null;

        Double rating = product.getAverageRating() != null
                ? product.getAverageRating().doubleValue()
                : 0.0;

        return new ProductSearchResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStockQuantity(),
                product.getSku(),
                product.getCategoryId(),
                categoryName,
                product.getShopId(),
                shopName,
                product.getMainImageUrl(),
                product.getStatus(),
                rating,
                product.getReviewCount(),
                createdAtLocal
        );
    }
}
