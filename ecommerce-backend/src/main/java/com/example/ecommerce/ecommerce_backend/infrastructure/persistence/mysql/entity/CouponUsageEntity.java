package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "coupon_usage",
        indexes = {
                @Index(name = "idx_coupon_usage_coupon", columnList = "coupon_id"),
                @Index(name = "idx_coupon_usage_user", columnList = "user_id"),
                @Index(name = "idx_coupon_usage_order", columnList = "order_id")
        })
public class CouponUsageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "discount_amount", nullable = false)
    private Long discountAmount;

    @Column(name = "used_at", nullable = false, updatable = false)
    private Instant usedAt;

    @PrePersist
    void prePersist() {
        usedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Long discountAmount) { this.discountAmount = discountAmount; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
}
