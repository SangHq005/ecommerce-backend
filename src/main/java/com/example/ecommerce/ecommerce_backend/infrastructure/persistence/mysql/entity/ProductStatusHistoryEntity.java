package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

//Product Status History - tracks all status changes for auditing
@Entity
@Getter
@Setter
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
    private Long changedBy; 
    
    @Column(name = "changed_by_type", length = 20)
    private String changedByType;  
    
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
}
