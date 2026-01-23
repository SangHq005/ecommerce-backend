package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Seller Profile Entity - Manages seller verification separately from Shop.
 * 
 * Flow: User upgrades to SELLER → Creates SellerProfile (PENDING) → Admin verifies → ACTIVE
 * Only ACTIVE sellers can create shops.
 */
@Entity
@Table(name = "seller_profile")
public class SellerProfileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    // === Seller Status ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SellerStatus status = SellerStatus.PENDING_VERIFICATION;
    
    // === Seller Type ===
    @Enumerated(EnumType.STRING)
    @Column(name = "seller_type", nullable = false, length = 20)
    private SellerType sellerType = SellerType.INDIVIDUAL;
    
    // === Identity Information ===
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Column(name = "id_type", length = 30)
    private String idType; // CCCD, PASSPORT, BUSINESS_LICENSE
    
    @Column(name = "id_number", length = 50)
    private String idNumber;
    
    @Column(name = "id_image_front", length = 512)
    private String idImageFront;
    
    @Column(name = "id_image_back", length = 512)
    private String idImageBack;
    
    @Column(name = "tax_code", length = 50)
    private String taxCode;
    
    // === Contact Information ===
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    // === Address ===
    @Column(length = 100)
    private String city;
    
    @Column(length = 255)
    private String address;
    
    // === Timestamps ===
    @Column(name = "submitted_at")
    private Instant submittedAt;
    
    @Column(name = "verified_at")
    private Instant verifiedAt;
    
    @Column(name = "rejected_at")
    private Instant rejectedAt;
    
    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;
    
    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user ID who verified
    
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Version
    private Long version;
    
    // === Enums ===
    
    public enum SellerStatus {
        PENDING_VERIFICATION,  // Submitted, waiting for admin review
        ACTIVE,                // Verified and can create shops
        SUSPENDED,             // Temporarily suspended
        REJECTED               // Verification rejected
    }
    
    public enum SellerType {
        INDIVIDUAL,  // Cá nhân
        BUSINESS     // Doanh nghiệp
    }
    
    // === Lifecycle Callbacks ===
    
    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (status == null) {
            status = SellerStatus.PENDING_VERIFICATION;
        }
    }
    
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
    
    // === Getters and Setters ===
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public SellerStatus getStatus() { return status; }
    public void setStatus(SellerStatus status) { this.status = status; }
    
    public SellerType getSellerType() { return sellerType; }
    public void setSellerType(SellerType sellerType) { this.sellerType = sellerType; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }
    
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    
    public String getIdImageFront() { return idImageFront; }
    public void setIdImageFront(String idImageFront) { this.idImageFront = idImageFront; }
    
    public String getIdImageBack() { return idImageBack; }
    public void setIdImageBack(String idImageBack) { this.idImageBack = idImageBack; }
    
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
    
    public Instant getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }
    
    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }
    
    public Long getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(Long verifiedBy) { this.verifiedBy = verifiedBy; }
    
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    
    // === Helper Methods ===
    
    public boolean isActive() {
        return status == SellerStatus.ACTIVE;
    }
    
    public boolean canCreateShop() {
        return status == SellerStatus.ACTIVE;
    }
    
    public boolean isPending() {
        return status == SellerStatus.PENDING_VERIFICATION;
    }
}
