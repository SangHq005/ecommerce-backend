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
}
