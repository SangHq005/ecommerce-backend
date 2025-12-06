package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderCode,
        Long userId,
        String userEmail,
        Long shopId,
        String status,
        Long totalAmount,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemDetail> items
) {
    public static OrderDetailResponse from(OrderEntity order, String userEmail, List<OrderItemEntity> items) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderCode(),
                order.getUserId(),
                userEmail,
                order.getShopId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items.stream().map(OrderItemDetail::from).toList()
        );
    }

    public record OrderItemDetail(
            Long id,
            Long productId,
            String productName,
            Long skuId,
            Integer quantity,
            Long unitPrice,
            Long totalPrice
    ) {
        public static OrderItemDetail from(OrderItemEntity item) {
            return new OrderItemDetail(
                    item.getId(),
                    item.getProductId(),
                    null, // Will be populated by service
                    item.getSkuId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
            );
        }

        public static OrderItemDetail from(OrderItemEntity item, String productName) {
            return new OrderItemDetail(
                    item.getId(),
                    item.getProductId(),
                    productName,
                    item.getSkuId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
            );
        }
    }
}
