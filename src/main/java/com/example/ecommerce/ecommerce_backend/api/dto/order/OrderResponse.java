package com.example.ecommerce.ecommerce_backend.api.dto.order;

import java.util.List;

public record OrderResponse(
        String orderCode,
        String status,
        Long totalAmount,
        String currency,
        List<Item> items
) {
    public record Item(
            Long productId, Long skuId, Integer quantity,
            Long unitPrice, Long totalPrice
    ) {}
}
