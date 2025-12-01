package com.example.ecommerce.ecommerce_backend.api.dto.order;

import jakarta.validation.constraints.*;
import java.util.List;

public record CheckoutRequest(
        @NotEmpty List<Item> items
) {
    public record Item(
            @NotNull Long productId,
            @NotNull Long skuId,
            @NotNull @Min(1) Integer quantity
    ) {}
}
