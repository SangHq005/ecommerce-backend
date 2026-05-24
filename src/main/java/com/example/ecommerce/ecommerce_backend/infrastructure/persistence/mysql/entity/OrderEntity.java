package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name="address_id")
    private Long addressId;

    @Column(name="payment_method", length=32)
    private String paymentMethod;

    @Column(columnDefinition="TEXT")
    private String note;

    @Column(name="coupon_code", length=64)
    private String couponCode;

    @Column(name="discount_amount", nullable=false)
    private Long discountAmount = 0L;

    @Column(name="shipping_fee", nullable=false)
    private Long shippingFee = 0L;

    @Column(name="tracking_number", length=64)
    private String trackingNumber;
    
    // Shipping Provider Fields
    @Column(name="shipping_provider", length=50)
    private String shippingProvider;  // GHN, GHTK, VNPost, JT Express, etc.
    
    @Column(name="shipping_tracking_url", length=512)
    private String shippingTrackingUrl;
    
    //  Timestamp Fields 
    @Column(name="shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name="delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name="completed_at")
    private LocalDateTime completedAt;
    
    @Column(name="estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    //  Delivery Tracking 
    @Column(name="delivery_attempts", nullable=false)
    private Integer deliveryAttempts = 0;
    
    @Column(name="delivery_failed_reason", length=255)
    private String deliveryFailedReason;
    
    //  Buyer Confirmation 
    @Column(name="buyer_confirmed", nullable=false)
    private Boolean buyerConfirmed = false;
    
    @Column(name="buyer_confirmed_at")
    private LocalDateTime buyerConfirmedAt;
    
    // Auto-complete Scheduling 
    @Column(name="auto_complete_at")
    private LocalDateTime autoCompleteAt;  // Set to delivered_at + 7 days

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
}
