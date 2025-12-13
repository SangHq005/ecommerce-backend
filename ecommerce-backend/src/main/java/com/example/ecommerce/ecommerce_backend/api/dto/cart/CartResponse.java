package com.example.ecommerce.ecommerce_backend.api.dto.cart;

import java.util.List;

public record CartResponse(
    List<CartGroup> groups,
    long totalCount
) {
    public record CartGroup(Long shopId, String shopName, List<CartItem> items) {}
    
    public record CartItem(
        Long itemId,
        Long productId,
        String productName,
        Long skuId,
        String skuName, // or variant name
        String imageUrl,
        Long price,
        Integer quantity,
        Integer maxStock
    ) {}
}
