package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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

    // Getters
    public Long getId() { return id; }
    public String getOrderToken() { return orderToken; }
    public Long getSkuId() { return skuId; }
    public Integer getQty() { return qty; }
    public String getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setOrderToken(String orderToken) { this.orderToken = orderToken; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public void setQty(Integer qty) { this.qty = qty; }
    public void setStatus(String status) { this.status = status; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
