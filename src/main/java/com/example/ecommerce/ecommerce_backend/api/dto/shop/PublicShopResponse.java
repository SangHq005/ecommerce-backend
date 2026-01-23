package com.example.ecommerce.ecommerce_backend.api.dto.shop;

/**
 * Public shop information visible to all users
 */
public record PublicShopResponse(
    Long id,
    String shopName,
    String shopSlug,
    String description,
    String logoUrl,
    String bannerUrl,
    String city,
    boolean verified,
    long productCount
) {}
