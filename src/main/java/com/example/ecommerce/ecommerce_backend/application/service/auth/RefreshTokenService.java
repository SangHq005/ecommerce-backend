package com.example.ecommerce.ecommerce_backend.application.service.auth;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshTokenEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefreshTokenJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenJpaRepository repo;
    private final Duration refreshTtl;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenJpaRepository repo,
            @Value("${app.jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds
    ) {
        this.repo = repo;
        this.refreshTtl = Duration.ofSeconds(refreshTtlSeconds);
    }

    /** Tạo refresh token mới. */
    @Transactional
    public String issue(Long userId, String ip, String userAgent) {
        String familyId = randomId64(); // mới đăng nhập: family mới
        return issueInFamily(userId, familyId, ip, userAgent);
    }

    /** Issue token trong cùng family (dùng cho rotate). */
    @Transactional
    public String issueInFamily(Long userId, String familyId, String ip, String userAgent) {
        RefreshTokenEntity t = new RefreshTokenEntity();
        LocalDateTime now = LocalDateTime.now();

        t.setUserId(userId);
        t.setFamilyId(familyId);
        t.setJti(randomId64());
        t.setIssuedAt(now);
        t.setExpiresAt(now.plusSeconds(refreshTtl.toSeconds()));
        t.setIp(ip);
        t.setUserAgent(userAgent);

        repo.save(t);
        return t.getJti();
    }

    /**
     * Rotate:
     * - lock row by old jti
     * - nếu revoked rồi => detect reuse => revoke cả family (security)
     * - nếu hết hạn => invalid
     * - else: create new token in same family, mark old revoked + replaced_by
     */
    @Transactional
    public String rotate(String oldJti, String ip, String userAgent) {
        LocalDateTime now = LocalDateTime.now();

        RefreshTokenEntity old = repo.findByJtiForUpdate(oldJti)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // reuse detection: token bị dùng lại sau khi revoked => revoke family
        if (old.getRevokedAt() != null) {
            repo.revokeFamily(old.getUserId(), old.getFamilyId(), now);
            throw new IllegalStateException("Refresh token reuse detected. Session revoked.");
        }

        if (old.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        String newJti = issueInFamily(old.getUserId(), old.getFamilyId(), ip, userAgent);

        old.setRevokedAt(now);
        old.setReplacedByJti(newJti);
        repo.save(old);

        return newJti;
    }

    /** Logout 1 refresh token: revoke token đó (không revoke cả family). */
    @Transactional
    public void revoke(String jti) {
        repo.findByJtiForUpdate(jti).ifPresent(t -> {
            if (t.getRevokedAt() == null) {
                t.setRevokedAt(LocalDateTime.now());
                repo.save(t);
            }
        });
    }

    /** Logout all devices/session of that family. */
    @Transactional
    public void revokeFamily(Long userId, String familyId) {
        repo.revokeFamily(userId, familyId, LocalDateTime.now());
    }

    private String randomId64() {
        byte[] b = new byte[32]; // 256-bit
        random.nextBytes(b);
        return HexFormat.of().formatHex(b); // 64 hex chars
    }
}
