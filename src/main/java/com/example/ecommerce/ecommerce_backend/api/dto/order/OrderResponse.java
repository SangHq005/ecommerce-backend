package com.example.ecommerce.ecommerce_backend.api.dto.order;

import java.util.List;

public record OrderResponse(
        String orderCode,
        String status,
        Long totalAmount,
        String currency,
        java.time.LocalDateTime createdAt,
        String paymentMethod,
        Long shippingFee,
        Long discountAmount,
        String note,
        Long addressId,
        String receiverName,
        String receiverPhone,
        String shippingAddress,
        List<Item> items
) {
    public record Item(
            Long productId, String productName, String thumbnailUrl,
            Long skuId, Integer quantity,
            Long unitPrice, Long totalPrice
    ) {}
}
