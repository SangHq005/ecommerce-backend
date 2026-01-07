package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name="idx_refresh_user", columnList="user_id,revoked_at,expires_at"),
                @Index(name="idx_refresh_family", columnList="user_id,family_id,revoked_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_refresh_jti", columnNames = {"jti"})
        }
)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="jti", nullable=false, length=64)
    private String jti;

    @Column(name="family_id", nullable=false, length=64)
    private String familyId;

    @Column(name="issued_at", nullable=false)
    private LocalDateTime issuedAt;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="revoked_at")
    private LocalDateTime revokedAt;

    @Column(name="replaced_by_jti", length=64)
    private String replacedByJti;

    @Column(name="ip", length=64)
    private String ip;

    @Column(name="user_agent", length=255)
    private String userAgent;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getJti() { return jti; }
    public String getFamilyId() { return familyId; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public String getReplacedByJti() { return replacedByJti; }
    public String getIp() { return ip; }
    public String getUserAgent() { return userAgent; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setJti(String jti) { this.jti = jti; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public void setReplacedByJti(String replacedByJti) { this.replacedByJti = replacedByJti; }
    public void setIp(String ip) { this.ip = ip; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
