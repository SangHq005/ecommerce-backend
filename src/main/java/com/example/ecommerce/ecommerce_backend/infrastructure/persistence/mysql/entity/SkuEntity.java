package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
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

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getOptionSignature() { return optionSignature; }
    public void setOptionSignature(String optionSignature) { this.optionSignature = optionSignature; }
    public String getOptionSignatureHash() { return optionSignatureHash; }
    public void setOptionSignatureHash(String optionSignatureHash) { this.optionSignatureHash = optionSignatureHash; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public Long getCompareAtPrice() { return compareAtPrice; }
    public void setCompareAtPrice(Long compareAtPrice) { this.compareAtPrice = compareAtPrice; }
    public int getStockOnHand() { return stockOnHand; }
    public void setStockOnHand(int stockOnHand) { this.stockOnHand = stockOnHand; }
    public int getReservedStock() { return reservedStock; }
    public void setReservedStock(int reservedStock) { this.reservedStock = reservedStock; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
