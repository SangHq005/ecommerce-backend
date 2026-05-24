package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "stock_reservation",
        uniqueConstraints = @UniqueConstraint(name="uk_reserve_order_sku", columnNames = {"order_token","sku_id"}),
        indexes = {
                @Index(name = "idx_stock_reservation_expires", columnList = "status,expires_at")
        }
)
public class StockReservationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_token", nullable=false, length=64)
    private String orderToken;

    @Column(name="sku_id", nullable=false)
    private Long skuId;

    @Column(name="qty", nullable=false)
    private Integer qty;

    @Column(name="status", nullable=false, length=16)
    private String status; // RESERVED/RELEASED/COMMITTED

    @Column(name="expires_at")
    private LocalDateTime expiresAt;

    @Column(name="created_at", insertable=false, updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private LocalDateTime updatedAt;
}
