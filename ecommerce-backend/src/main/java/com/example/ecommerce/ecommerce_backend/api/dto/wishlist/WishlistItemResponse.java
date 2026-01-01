package com.example.ecommerce.ecommerce_backend.api.dto.wishlist;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.WishlistItemEntity;

import java.time.Instant;

public record WishlistItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSlug,
        String mainImageUrl,
        Long minPrice,
        String status,
        Instant addedAt,
        String note
) {
    public static WishlistItemResponse from(WishlistItemEntity item, ProductEntity product, Long minPrice) {
        return new WishlistItemResponse(
                item.getId(),
                item.getProductId(),
                product.getName(),
                product.getSlug(),
                product.getMainImageUrl(),
                minPrice,
                product.getStatus(),
                item.getAddedAt(),
                item.getNote()
        );
    }
}
