package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "product",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_shop_slug", columnNames = {"shop_id","slug"}))
public class ProductEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "seller_user_id", nullable = false)
    private Long sellerUserId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String slug;

    @Lob
    private String description;

    @Column(nullable = false, length = 32)
    private String status; // DRAFT/PENDING_APPROVAL/ACTIVE/REJECTED/HIDDEN

    @Column(name = "main_image_url", length = 512)
    private String mainImageUrl;

    @Column(nullable = false)
    private Long price;

    @Column(name = "original_price")
    private Long originalPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(length = 100)
    private String sku;

    @Column(nullable = false, length = 8)
    private String currency = "VND";

    @Column(name = "average_rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal averageRating = BigDecimal.valueOf(0.0);

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "sold_count", nullable = false)
    private Integer soldCount = 0;

    @Column(name = "action_reason", length = 512)
    private String actionReason;
    
    // Product Listing
    
    @Column(name = "quality_score")
    private Integer qualityScore;  // 0-100, calculated based on completeness
    
    @Column(name = "published_at")
    private Instant publishedAt;   // When product was first made ACTIVE
    
    @Column(name = "hidden_at")
    private Instant hiddenAt;      // When seller hid the product
    
    @Column(name = "rejected_at")
    private Instant rejectedAt;    // When admin rejected
    
    @Column(name = "rejected_by")
    private Long rejectedBy;       // Admin who rejected
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;  // Featured by shop owner
    
    @Column(name = "weight_grams")
    private Integer weightGrams;   // For shipping calculation
    
    @Column(name = "shipping_fee_type", length = 20)
    private String shippingFeeType = "STANDARD";  // STANDARD, FREE, CONDITIONAL_FREE

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private long version;
}
