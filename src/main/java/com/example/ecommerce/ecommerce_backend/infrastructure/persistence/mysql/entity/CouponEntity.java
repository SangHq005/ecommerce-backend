package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "coupon",
        uniqueConstraints = @UniqueConstraint(name = "uk_coupon_code", columnNames = {"code"}))
public class CouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status;

    // Discount value (percentage or fixed amount in VND)
    @Column(name = "discount_value", nullable = false)
    private Long discountValue;

    // Maximum discount amount for percentage coupons (in VND)
    @Column(name = "max_discount_amount")
    private Long maxDiscountAmount;

    // Minimum order amount required to use coupon (in VND)
    @Column(name = "min_order_amount")
    private Long minOrderAmount;

    // Start date of coupon validity
    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    // End date of coupon validity
    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    // Maximum number of times this coupon can be used in total
    @Column(name = "usage_limit")
    private Integer usageLimit;

    // Current usage count
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    // Maximum number of times a single user can use this coupon
    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    // If true, coupon is applied automatically when conditions are met
    @Column(name = "auto_apply", nullable = false)
    private Boolean autoApply = false;

    // Specific product IDs this coupon applies to (null = all products)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_product_ids", columnDefinition = "json")
    private List<Long> applicableProductIds;

    // Specific category IDs this coupon applies to (null = all categories)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_category_ids", columnDefinition = "json")
    private List<Long> applicableCategoryIds;

    // Specific user IDs who can use this coupon (null = all users)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_user_ids", columnDefinition = "json")
    private List<Long> applicableUserIds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }
}
