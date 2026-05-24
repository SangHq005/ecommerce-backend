package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
}
