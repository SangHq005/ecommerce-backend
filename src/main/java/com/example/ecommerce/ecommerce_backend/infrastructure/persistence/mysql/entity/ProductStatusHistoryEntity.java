package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Product Status History - tracks all status changes for auditing
 */
@Entity
@Table(name = "product_status_history")
public class ProductStatusHistoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "shop_id", nullable = false)
    private Long shopId;
    
    @Column(name = "previous_status", length = 32)
    private String previousStatus;
    
    @Column(name = "new_status", nullable = false, length = 32)
    private String newStatus;
    
    @Column(name = "changed_by")
    private Long changedBy;  // User ID who made the change
    
    @Column(name = "changed_by_type", length = 20)
    private String changedByType;  // SELLER, ADMIN, SYSTEM
    
    @Column(length = 500)
    private String reason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
    
    // === Getters and Setters ===
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    
    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }
    
    public String getChangedByType() { return changedByType; }
    public void setChangedByType(String changedByType) { this.changedByType = changedByType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Instant getCreatedAt() { return createdAt; }
}
