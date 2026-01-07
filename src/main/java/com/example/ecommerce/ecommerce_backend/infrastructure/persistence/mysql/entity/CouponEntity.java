package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CouponType getType() { return type; }
    public void setType(CouponType type) { this.type = type; }
    public CouponStatus getStatus() { return status; }
    public void setStatus(CouponStatus status) { this.status = status; }
    public Long getDiscountValue() { return discountValue; }
    public void setDiscountValue(Long discountValue) { this.discountValue = discountValue; }
    public Long getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Long maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    public Long getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Long minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    public Integer getUsageLimitPerUser() { return usageLimitPerUser; }
    public void setUsageLimitPerUser(Integer usageLimitPerUser) { this.usageLimitPerUser = usageLimitPerUser; }
    public Boolean getAutoApply() { return autoApply; }
    public void setAutoApply(Boolean autoApply) { this.autoApply = autoApply; }
    public List<Long> getApplicableProductIds() { return applicableProductIds; }
    public void setApplicableProductIds(List<Long> applicableProductIds) { this.applicableProductIds = applicableProductIds; }
    public List<Long> getApplicableCategoryIds() { return applicableCategoryIds; }
    public void setApplicableCategoryIds(List<Long> applicableCategoryIds) { this.applicableCategoryIds = applicableCategoryIds; }
    public List<Long> getApplicableUserIds() { return applicableUserIds; }
    public void setApplicableUserIds(List<Long> applicableUserIds) { this.applicableUserIds = applicableUserIds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public void incrementUsageCount() {
        this.usageCount++;
    }
}
