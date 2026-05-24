package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "refund")
public class RefundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Lob
    private String description;

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;

    @Column(nullable = false, length = 8)
    private String currency = "VND";

    @Column(nullable = false, length = 32)
    private String status; // PENDING, UNDER_REVIEW, APPROVED, REJECTED, PROCESSING, COMPLETED, CANCELLED

    @Column(name = "admin_note", length = 1000)
    private String adminNote;
    
    @Column(name = "refund_type", length = 20)
    private String refundType = "REFUND"; 
    
    //  Return tracking info
    @Column(name = "return_tracking_number", length = 100)
    private String returnTrackingNumber;
    
    @Column(name = "return_shipping_provider", length = 50)
    private String returnShippingProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    @Version
    private long version;
}
