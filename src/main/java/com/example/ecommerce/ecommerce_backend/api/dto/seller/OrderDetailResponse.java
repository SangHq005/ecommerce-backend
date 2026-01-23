package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;

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
        List<OrderItemDetail> items,
        // === NEW: Shipping Fields ===
        ShippingInfo shippingInfo
) {
    // Constructor for backward compatibility (without shipping info)
    public OrderDetailResponse(
            Long id, String orderCode, Long userId, String userEmail, Long shopId,
            String status, Long totalAmount, String currency,
            LocalDateTime createdAt, LocalDateTime updatedAt, List<OrderItemDetail> items
    ) {
        this(id, orderCode, userId, userEmail, shopId, status, totalAmount, currency,
             createdAt, updatedAt, items, null);
    }
    
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
                items.stream().map(OrderItemDetail::from).toList(),
                ShippingInfo.from(order)
        );
    }
    
    public static OrderDetailResponse fromWithShipping(OrderEntity order, String userEmail, List<OrderItemDetail> items) {
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
                items,
                ShippingInfo.from(order)
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
    
    // === NEW: Shipping Info Record ===
    public record ShippingInfo(
            String shippingProvider,
            String trackingNumber,
            String trackingUrl,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime estimatedDeliveryDate,
            Integer deliveryAttempts,
            String deliveryFailedReason,
            Boolean buyerConfirmed,
            LocalDateTime buyerConfirmedAt,
            LocalDateTime autoCompleteAt
    ) {
        public static ShippingInfo from(OrderEntity order) {
            return new ShippingInfo(
                    order.getShippingProvider(),
                    order.getTrackingNumber(),
                    order.getShippingTrackingUrl(),
                    order.getShippedAt(),
                    order.getDeliveredAt(),
                    order.getEstimatedDeliveryDate(),
                    order.getDeliveryAttempts(),
                    order.getDeliveryFailedReason(),
                    order.getBuyerConfirmed(),
                    order.getBuyerConfirmedAt(),
                    order.getAutoCompleteAt()
            );
        }
    }
}
