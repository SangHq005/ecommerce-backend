package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_code", nullable=false, unique=true)
    private String orderCode;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="shop_id", nullable=false)
    private Long shopId;

    @Column(nullable=false)
    private String status;

    @Column(name="total_amount", nullable=false)
    private Long totalAmount;

    @Column(nullable=false)
    private String currency;

    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters/setters omitted for brevity
}
