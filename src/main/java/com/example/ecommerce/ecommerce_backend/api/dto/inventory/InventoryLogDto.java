package com.example.ecommerce.ecommerce_backend.api.dto.inventory;

import java.time.Instant;

public record InventoryLogDto(
    Long id,
    Long skuId,
    Long productId,
    int changeAmount,
    int previousStock,
    int newStock,
    String reason,
    String referenceId,
    Long actorId,
    Instant createdAt
) {}
