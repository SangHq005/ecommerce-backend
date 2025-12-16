package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="order_item")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id", nullable=false)
    private Long orderId;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(name="sku_id", nullable=false)
    private Long skuId;

    private Integer quantity;
    private Long unitPrice;
    private Long totalPrice;

    // getters/setters
}
