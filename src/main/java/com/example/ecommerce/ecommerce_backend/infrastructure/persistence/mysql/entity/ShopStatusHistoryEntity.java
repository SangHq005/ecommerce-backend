package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity for tracking shop status changes.
 * Provides complete audit trail for shop lifecycle.
 */
@Entity
@Table(name = "shop_status_history")
public class ShopStatusHistoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "shop_id", nullable = false)
    private Long shopId;
    
    @Column(name = "from_status", length = 32)
    private String fromStatus;
    
    @Column(name = "to_status", nullable = false, length = 32)
    private String toStatus;
    
    @Column(name = "actor_type", nullable = false, length = 20)
    private String actorType; // SYSTEM, SELLER, ADMIN
    
    @Column(name = "actor_id")
    private Long actorId;
    
    @Column(length = 500)
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    // Constructors
    public ShopStatusHistoryEntity() {}
    
    public ShopStatusHistoryEntity(Long shopId, String fromStatus, String toStatus, 
                                    String actorType, Long actorId, String reason) {
        this.shopId = shopId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorType = actorType;
        this.actorId = actorId;
        this.reason = reason;
        this.createdAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    
    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    
    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    
    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }
    
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
