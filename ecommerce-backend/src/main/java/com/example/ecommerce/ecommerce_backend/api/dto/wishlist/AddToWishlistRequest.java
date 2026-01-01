package com.example.ecommerce.ecommerce_backend.api.dto.wishlist;

import jakarta.validation.constraints.NotNull;

public record AddToWishlistRequest(
        @NotNull(message = "Product ID is required")
        Long productId,
        String note
) {
}
