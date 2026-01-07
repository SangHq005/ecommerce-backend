package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_sessions",
        indexes = {
                @Index(name = "idx_session_user", columnList = "user_id,status,expires_at"),
                @Index(name = "idx_session_root", columnList = "session_root_jti,status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_jti", columnNames = {"refresh_jti"})
        }
)
public class RefreshSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_root_jti", nullable = false, length = 64)
    private String sessionRootJti;

    @Column(name = "refresh_jti", nullable = false, length = 64)
    private String refreshJti;

    @Column(nullable = false, length = 32)
    private String status; // ACTIVE, ROTATED, REVOKED

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(length = 64)
    private String ip;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "rotated_at")
    private Instant rotatedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionRootJti() {
        return sessionRootJti;
    }

    public void setSessionRootJti(String sessionRootJti) {
        this.sessionRootJti = sessionRootJti;
    }

    public String getRefreshJti() {
        return refreshJti;
    }

    public void setRefreshJti(String refreshJti) {
        this.refreshJti = refreshJti;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getRotatedAt() {
        return rotatedAt;
    }

    public void setRotatedAt(Instant rotatedAt) {
        this.rotatedAt = rotatedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
