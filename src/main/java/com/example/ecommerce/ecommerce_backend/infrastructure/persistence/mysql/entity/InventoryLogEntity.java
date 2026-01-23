package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
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
    private String referenceId; // e.g. Order ID

    @Column(name="actor_id")
    private Long actorId; // Null if system, User ID if manual

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public int getChangeAmount() { return changeAmount; }
    public void setChangeAmount(int changeAmount) { this.changeAmount = changeAmount; }
    public int getPreviousStock() { return previousStock; }
    public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }
    public int getNewStock() { return newStock; }
    public void setNewStock(int newStock) { this.newStock = newStock; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
