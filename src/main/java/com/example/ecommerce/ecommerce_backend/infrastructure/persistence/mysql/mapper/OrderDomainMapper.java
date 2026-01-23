package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.mapper;

import com.example.ecommerce.ecommerce_backend.domain.order.Order;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderItem;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDomainMapper {

    public Order toDomain(OrderEntity entity, List<OrderItemEntity> itemEntities) {
        List<OrderItem> items = itemEntities.stream()
                .map(i -> new OrderItem(i.getProductId(), i.getSkuId(), i.getQuantity(), i.getUnitPrice()))
                .collect(Collectors.toList());

        return Order.builder()
                .orderCode(entity.getOrderCode())
                .userId(entity.getUserId())
                .shopId(entity.getShopId())
                .status(OrderStatus.valueOf(entity.getStatus()))
                .currency(entity.getCurrency())
                .shippingFee(entity.getShippingFee())
                .discountAmount(entity.getDiscountAmount())
                .items(items)
                .build();
    }

    public void updateEntity(Order domain, OrderEntity entity) {
        entity.setStatus(domain.getStatus().name());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setDiscountAmount(domain.getDiscountAmount());
        // Shipping fee and other fields are usually immutable after checkout
    }

    public List<OrderItemEntity> toItemEntities(Order domain, Long orderId) {
        return domain.getItems().stream()
                .map(item -> {
                    OrderItemEntity entity = new OrderItemEntity();
                    entity.setOrderId(orderId);
                    entity.setProductId(item.getProductId());
                    entity.setSkuId(item.getSkuId());
                    entity.setQuantity(item.getQuantity());
                    entity.setUnitPrice(item.getUnitPrice());
                    entity.setTotalPrice(item.getTotalPrice());
                    return entity;
                })
                .collect(Collectors.toList());
    }
}
