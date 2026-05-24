package com.example.ecommerce.ecommerce_backend.domain.event;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import java.time.Instant;

public record OrderStatusChangedEvent(
    Long orderId,
    String orderCode,
    Long userId, // Buyer ID
    OrderStatus fromStatus,
    OrderStatus toStatus,
    Long actorId,
    String actorRole,
    Instant occurredAt
) {
    public static OrderStatusChangedEvent of(
            Long orderId, String orderCode, Long userId,
            OrderStatus from, OrderStatus to,
            Long actorId, String actorRole) {
        return new OrderStatusChangedEvent(orderId, orderCode, userId, from, to, actorId, actorRole, Instant.now());
    }
}
