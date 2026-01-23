package com.example.ecommerce.ecommerce_backend.domain.order;

import lombok.Getter;

@Getter
public class OrderItem {
    private final Long productId;
    private final Long skuId;
    private final int quantity;
    private final long unitPrice;

    public OrderItem(Long productId, Long skuId, int quantity, long unitPrice) {
        this.productId = productId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    public long getTotalPrice() {
        return unitPrice * (long) quantity;
    }
}
