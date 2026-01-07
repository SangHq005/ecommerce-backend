package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        String orderCode,
        Long userId,
        String status,
        Long totalAmount,
        String currency,
        LocalDateTime createdAt,
        int itemCount
) {
    public static OrderSummaryResponse from(OrderEntity order, int itemCount) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderCode(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                itemCount
        );
    }
}
