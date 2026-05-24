package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="product_sku",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_sku_code", columnNames={"product_id","sku_code"}),
                @UniqueConstraint(name="uk_sku_sig", columnNames={"product_id","option_signature_hash"})
        }
)
public class SkuEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(name="sku_code", nullable=false, length=64)
    private String skuCode;

    @Column(name="option_signature", nullable=false, length=255)
    private String optionSignature;

    @Column(name="option_signature_hash", nullable=false, length=64)
    private String optionSignatureHash;

    @Column(nullable=false)
    private long price;

    @Column(name="compare_at_price")
    private Long compareAtPrice;

    @Column(name="stock_on_hand", nullable=false)
    private int stockOnHand;

    @Column(name="is_active", nullable=false)
    private boolean active = true;

    @Column(name="reserved_stock", nullable=false)
    private int reservedStock = 0;

    @Column(name="image_url", length=512)
    private String imageUrl;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt = Instant.now();

    @Version
    private long version;
}
