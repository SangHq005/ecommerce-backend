package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Entity
@Getter
@Setter
@Table(name = "seller_voucher",
        uniqueConstraints = @UniqueConstraint(name = "uk_seller_voucher_code", columnNames = {"shop_id", "code"}))
public class SellerVoucherEntity implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private Long discountValue;

    @Column(name = "max_discount_amount")
    private Long maxDiscountAmount;

    @Column(name = "min_order_amount")
    private Long minOrderAmount;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherStatus status = VoucherStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_product_ids", columnDefinition = "json")
    private List<Long> applicableProductIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_category_ids", columnDefinition = "json")
    private List<Long> applicableCategoryIds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    // Enums
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }

    public enum VoucherStatus {
        DRAFT,
        ACTIVE,
        PAUSED,
        EXPIRED,
        DELETED
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // Business methods
    public void incrementUsageCount() {
        this.usageCount++;
    }

    public boolean isValid() {
        Instant now = Instant.now();
        return status == VoucherStatus.ACTIVE
                && now.isAfter(startDate)
                && now.isBefore(endDate)
                && (usageLimit == null || usageCount < usageLimit);
    }

    public long calculateDiscount(long orderAmount) {
        if (minOrderAmount != null && orderAmount < minOrderAmount) {
            return 0;
        }

        long discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = (orderAmount * discountValue) / 100;
            if (maxDiscountAmount != null && discount > maxDiscountAmount) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = discountValue;
        }

        return Math.min(discount, orderAmount);
    }

    // Prototype Pattern implementation
    public SellerVoucherEntity() {}

    public SellerVoucherEntity(SellerVoucherEntity target) {
        if (target != null) {
            this.shopId = target.shopId;
            this.name = target.name;
            this.description = target.description;
            this.discountType = target.discountType;
            this.discountValue = target.discountValue;
            this.maxDiscountAmount = target.maxDiscountAmount;
            this.minOrderAmount = target.minOrderAmount;
            this.startDate = target.startDate;
            this.endDate = target.endDate;
            this.usageLimit = target.usageLimit;
            this.usageLimitPerUser = target.usageLimitPerUser;
            this.status = VoucherStatus.DRAFT; // Reset status for clone
            
            // Deep copy collections
            if (target.applicableProductIds != null) {
                this.applicableProductIds = new ArrayList<>(target.applicableProductIds);
            }
            if (target.applicableCategoryIds != null) {
                this.applicableCategoryIds = new ArrayList<>(target.applicableCategoryIds);
            }
        }
    }

    @Override
    public SellerVoucherEntity clone() {
        return new SellerVoucherEntity(this);
    }
}
