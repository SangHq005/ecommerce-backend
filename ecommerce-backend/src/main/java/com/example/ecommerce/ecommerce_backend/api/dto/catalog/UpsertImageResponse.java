package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

public record UpsertImageResponse(Long id, Long productId, int sortOrder, String imageUrl) {}
