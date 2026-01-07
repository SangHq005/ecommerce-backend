package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "seller_shop")
public class SellerShopEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_user_id", nullable = false, unique = true)
    private Long sellerUserId;

    @Column(name = "shop_name", nullable = false, length = 191)
    private String shopName;

    @Column(name = "shop_slug", nullable = false, unique = true, length = 191)
    private String shopSlug;

    @Lob
    private String description;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(name = "banner_url", length = 512)
    private String bannerUrl;

    @Column(nullable = false, length = 32)
    private String status; // DRAFT/PENDING_REVIEW/ACTIVE/SUSPENDED

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "suspended_reason", length = 255)
    private String suspendedReason;

    @Version
    private long version;

    public Long getId() { return id; }
    public Long getSellerUserId() { return sellerUserId; }
    public void setSellerUserId(Long sellerUserId) { this.sellerUserId = sellerUserId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getShopSlug() { return shopSlug; }
    public void setShopSlug(String shopSlug) { this.shopSlug = shopSlug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getSuspendedReason() { return suspendedReason; }
    public void setSuspendedReason(String suspendedReason) { this.suspendedReason = suspendedReason; }
}
