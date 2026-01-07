package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshSessionEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RoleEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefreshSessionJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RoleJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final UserJpaRepository userRepo;
    private final RoleJpaRepository roleRepo;
    private final RefreshSessionJpaRepository refreshRepo;
    private final PasswordHasher hasher;
    private final JwtService jwt;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventLogRepo;
    private final EmailService emailService;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PasswordResetTokenJpaRepository passwordResetTokenRepo;

    public AuthService(
            UserJpaRepository userRepo,
            RoleJpaRepository roleRepo,
            RefreshSessionJpaRepository refreshRepo,
            PasswordHasher hasher,
            JwtService jwt,
            StringRedisTemplate redis,
            EventLogMongoRepository eventLogRepo,
            EmailService emailService,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PasswordResetTokenJpaRepository passwordResetTokenRepo
    ) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.refreshRepo = refreshRepo;
        this.hasher = hasher;
        this.jwt = jwt;
        this.redis = redis;
        this.eventLogRepo = eventLogRepo;
        this.emailService = emailService;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
    }

    @Transactional
    public JwtService.TokenPair registerClient(String email, String password, String fullName, String ua, String ip) {
        userRepo.findByEmail(email).ifPresent(u -> { throw new IllegalArgumentException("Email already exists"); });

        RoleEntity clientRole = roleRepo.findByCode("CLIENT")
                .orElseThrow(() -> new IllegalStateException("CLIENT role missing"));

        UserEntity u = new UserEntity();
        u.setEmail(email.toLowerCase());
        u.setPasswordHash(hasher.hash(password));
        u.setFullName(fullName);
        u.setStatus("ACTIVE");
        u.getRoles().add(clientRole);
        u = userRepo.save(u);

        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(u);

        return issueNewPairForUser(u, ua, ip);
    }

    @Transactional
    public JwtService.TokenPair login(String email, String password, String ua, String ip) {
        UserEntity u = userRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!"ACTIVE".equals(u.getStatus())) throw new IllegalArgumentException("User disabled");
        if (u.getPasswordHash() == null || !hasher.matches(password, u.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");

        JwtService.TokenPair pair = issueNewPairForUser(u, ua, ip);
        try {
            eventLogRepo.save(new EventLogDocument(
                    "AUTH_LOGIN_SUCCESS",
                    "user_" + u.getId(),
                    Instant.now(),
                    null,
                    Map.of("userId", u.getId(), "email", u.getEmail())
            ));
        } catch (Exception e) {
            // log warn thôi, không làm login fail
        }
        return pair;
    }

    @Transactional
    public JwtService.TokenPair oauth2LoginOrLink(String googleSub, String email, String fullName, String ua, String ip) {
        UserEntity u = userRepo.findByGoogleSub(googleSub)
                .orElseGet(() -> userRepo.findByEmail(email.toLowerCase()).orElse(null));

        RoleEntity clientRole = roleRepo.findByCode("CLIENT")
                .orElseThrow(() -> new IllegalStateException("CLIENT role missing"));

        if (u == null) {
            u = new UserEntity();
            u.setEmail(email.toLowerCase());
            u.setFullName(fullName != null ? fullName : email);
            u.setStatus("ACTIVE");
            u.setGoogleSub(googleSub);
            u.getRoles().add(clientRole);
            u = userRepo.save(u);
        } else {
            if (!"ACTIVE".equals(u.getStatus())) throw new IllegalArgumentException("User disabled");
            if (u.getGoogleSub() == null) {
                u.setGoogleSub(googleSub);
                u = userRepo.save(u);
            }
        }
        try {
            eventLogRepo.save(new EventLogDocument("AUTH_OAUTH2_LINKED", "user_" + u.getId(), Instant.now(), null,
                    Map.of("userId", u.getId(), "email", u.getEmail(), "googleSub", googleSub)));

        } catch (Exception e) {

        }

        return issueNewPairForUser(u, ua, ip);
    }

    @Transactional
    public JwtService.TokenPair refreshRotate(String refreshToken, String ua, String ip) {
        Claims c = jwt.parse(refreshToken);
        if (!"refresh".equals(String.valueOf(c.get("typ")))) throw new IllegalArgumentException("Not a refresh token");

        String refreshJti = c.getId();
        String root = String.valueOf(c.get("root"));
        Long userId = Long.valueOf(c.getSubject());
        Instant exp = c.getExpiration().toInstant();

        // Anti-reuse: SETNX auth:rt:used:{jti}
        String usedKey = "auth:rt:used:" + refreshJti;
        long ttl = Math.max(1, Duration.between(Instant.now(), exp).getSeconds());
        Boolean firstUse = redis.opsForValue().setIfAbsent(usedKey, "1", Duration.ofSeconds(ttl));
        if (Boolean.FALSE.equals(firstUse)) {
            // replay detected => revoke all sessions under root
            revokeRoot(root);
            throw new IllegalArgumentException("Refresh token reuse detected. Session revoked.");
        }

        RefreshSessionEntity sess = refreshRepo.findByRefreshJti(refreshJti)
                .orElseThrow(() -> new IllegalArgumentException("Refresh session not found"));
        if (!sess.getUserId().equals(userId)) throw new IllegalArgumentException("Refresh invalid");
        if (!"ACTIVE".equals(sess.getStatus())) throw new IllegalArgumentException("Refresh not active");
        if (sess.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Refresh expired");

        UserEntity u = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User missing"));
        if (!"ACTIVE".equals(u.getStatus())) throw new IllegalArgumentException("User disabled");

        // rotate: mark old ROTATED + create new ACTIVE
        sess.setStatus("ROTATED");
        sess.setRotatedAt(Instant.now());
        refreshRepo.save(sess);

        JwtService.TokenPair pair = issueRotatedPair(u, root, ua, ip);

        try {
            eventLogRepo.save(new EventLogDocument("AUTH_TOKEN_REFRESHED", "user_" + u.getId(), Instant.now(), null,
                    Map.of("userId", u.getId(), "root", root)));
        } catch (Exception e) {

        }
        return pair;
    }

    @Transactional
    public void logout(Long userId, String accessTokenOrNull, String refreshTokenOrNull) {
        if (accessTokenOrNull != null && !accessTokenOrNull.isBlank()) {
            Claims a = jwt.parse(accessTokenOrNull);
            String jti = a.getId();
            Instant exp = a.getExpiration().toInstant();
            long ttl = Math.max(1, Duration.between(Instant.now(), exp).getSeconds());
            redis.opsForValue().set("auth:blacklist:" + jti, "1", Duration.ofSeconds(ttl));
        }

        if (refreshTokenOrNull != null && !refreshTokenOrNull.isBlank()) {
            Claims r = jwt.parse(refreshTokenOrNull);

            if (!"refresh".equals(String.valueOf(r.get("typ")))) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            String refreshJti = r.getId();
            refreshRepo.findByRefreshJti(refreshJti).ifPresent(sess -> {
                if (sess.getUserId().equals(userId) && "ACTIVE".equals(sess.getStatus())) {
                    sess.setStatus("REVOKED");
                    sess.setRevokedAt(Instant.now());
                    refreshRepo.save(sess);
                }
            });
        }

        try {
            eventLogRepo.save(new EventLogDocument(
                    "AUTH_LOGOUT",
                    "user_" + userId,
                    Instant.now(),
                    null,
                    Map.of("userId", userId)
            ));
        } catch (Exception e) {
            // log warn thôi, không làm logout fail
        }

    }

    private void revokeRoot(String root) {
        List<RefreshSessionEntity> sessions = refreshRepo.findBySessionRootJti(root);
        for (RefreshSessionEntity s : sessions) {
            if (!"REVOKED".equals(s.getStatus())) {
                s.setStatus("REVOKED");
                s.setRevokedAt(Instant.now());
                refreshRepo.save(s);
            }
        }
    }

    private JwtService.TokenPair issueNewPairForUser(UserEntity u, String ua, String ip) {
        String root = jwt.newJti();
        return issueRotatedPair(u, root, ua, ip);
    }

    private JwtService.TokenPair issueRotatedPair(UserEntity u, String root, String ua, String ip) {
        List<String> roles = u.getRoles().stream().map(RoleEntity::getCode).toList();

        String accessJti = jwt.newJti();
        String access = jwt.issueAccessToken(u.getId(), u.getEmail(), roles, accessJti);

        String refreshJti = jwt.newJti();
        String refresh = jwt.issueRefreshToken(u.getId(), root, refreshJti);

        RefreshSessionEntity sess = new RefreshSessionEntity();
        sess.setUserId(u.getId());
        sess.setSessionRootJti(root);
        sess.setRefreshJti(refreshJti);
        sess.setStatus("ACTIVE");
        sess.setUserAgent(ua);
        sess.setIp(ip);
        sess.setExpiresAt(Instant.now().plusSeconds(jwt.getRefreshTtlSeconds()));
        refreshRepo.save(sess);

        return new JwtService.TokenPair(access, refresh, jwt.getAccessTtlSeconds());
    }

    /**
     * Initiate password reset - send email with reset token
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        UserEntity user = userRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("User account is not active");
        }

        // Generate unique reset token (UUID)
        String resetToken = java.util.UUID.randomUUID().toString();

        // Create token entity (expires in 1 hour)
        var tokenEntity = new com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PasswordResetTokenEntity();
        tokenEntity.setUserId(user.getId());
        tokenEntity.setToken(resetToken);
        tokenEntity.setExpiresAt(java.time.LocalDateTime.now().plusHours(1));
        tokenEntity.setUsed(false);
        passwordResetTokenRepo.save(tokenEntity);

        // Send password reset email
        emailService.sendPasswordResetEmail(user, resetToken);

        // Log event
        try {
            eventLogRepo.save(new EventLogDocument(
                    "PASSWORD_RESET_INITIATED",
                    "user_" + user.getId(),
                    Instant.now(),
                    null,
                    Map.of("email", email, "userId", user.getId())
            ));
        } catch (Exception e) {
            // Log warning but don't fail
        }
    }

    /**
     * Reset password using token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find valid token
        var tokenEntity = passwordResetTokenRepo.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        // Check expiry
        if (tokenEntity.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        // Find user
        UserEntity user = userRepo.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Update password
        user.setPasswordHash(hasher.hash(newPassword));
        userRepo.save(user);

        // Mark token as used
        tokenEntity.setUsed(true);
        passwordResetTokenRepo.save(tokenEntity);

        // Revoke all refresh sessions (force re-login)
        List<RefreshSessionEntity> sessions = refreshRepo.findByUserId(user.getId());
        for (RefreshSessionEntity s : sessions) {
            if (!"REVOKED".equals(s.getStatus())) {
                s.setStatus("REVOKED");
                s.setRevokedAt(Instant.now());
                refreshRepo.save(s);
            }
        }

        // Log event
        try {
            eventLogRepo.save(new EventLogDocument(
                    "PASSWORD_RESET_COMPLETED",
                    "user_" + user.getId(),
                    Instant.now(),
                    null,
                    Map.of("email", user.getEmail(), "userId", user.getId())
            ));
        } catch (Exception e) {
            // Log warning but don't fail
        }
    }
}
