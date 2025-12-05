package com.example.ecommerce.ecommerce_backend.api.dto.shop;

public record ShopResponse(
        Long id,
        Long sellerUserId,
        String shopName,
        String shopSlug,
        String description,
        String logoUrl,
        String bannerUrl,
        String status
) {}
