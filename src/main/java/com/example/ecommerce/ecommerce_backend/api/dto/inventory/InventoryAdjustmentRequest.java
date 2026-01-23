package com.example.ecommerce.ecommerce_backend.api.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryAdjustmentRequest(
    @NotNull Long skuId,
    @NotNull Integer delta,
    @NotNull @Size(max=50) String reason
) {}
