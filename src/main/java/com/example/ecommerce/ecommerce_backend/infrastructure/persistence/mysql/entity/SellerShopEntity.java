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

    @Column(length = 100)
    private String city;

    @Column(length = 255)
    private String address;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "suspended_reason", length = 255)
    private String suspendedReason;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "identity_code", length = 50)
    private String identityCode;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_name", length = 100)
    private String bankAccountName;

    @Version
    private long version;
}
