package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="inventory_log")
public class InventoryLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="shop_id", nullable=false)
    private Long shopId;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(name="sku_id", nullable=false)
    private Long skuId;

    @Column(name="change_amount", nullable=false)
    private int changeAmount;

    @Column(name="previous_stock", nullable=false)
    private int previousStock;

    @Column(name="new_stock", nullable=false)
    private int newStock;

    @Column(nullable=false, length=50)
    private String reason; // ORDER, RESTOCK, CORRECTION, CANCEL, RETURN

    @Column(name="reference_id", length=191)
    private String referenceId; // Order ID

    @Column(name="actor_id")
    private Long actorId; // Null if system, User ID if manual

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();
}
