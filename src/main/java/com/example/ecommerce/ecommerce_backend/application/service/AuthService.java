package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.PasswordResetTokenEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshSessionEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RoleEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PasswordResetTokenJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefreshSessionJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RoleJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.TrustedDeviceEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserProfileEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.TrustedDeviceJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserProfileJpaRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserJpaRepository userRepo;
    private final RoleJpaRepository roleRepo;
    private final RefreshSessionJpaRepository refreshRepo;
    private final PasswordHasher hasher;
    private final JwtService jwt;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventLogRepo;
    private final EmailService emailService;
    private final PasswordResetTokenJpaRepository passwordResetTokenRepo;
    private final TrustedDeviceJpaRepository trustedDeviceRepo;
    private final UserProfileJpaRepository userProfileRepo;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserJpaRepository userRepo,
            RoleJpaRepository roleRepo,
            RefreshSessionJpaRepository refreshRepo,
            PasswordHasher hasher,
            JwtService jwt,
            StringRedisTemplate redis,
            EventLogMongoRepository eventLogRepo,
            EmailService emailService,
            PasswordResetTokenJpaRepository passwordResetTokenRepo,
            TrustedDeviceJpaRepository trustedDeviceRepo,
            UserProfileJpaRepository userProfileRepo
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
        this.trustedDeviceRepo = trustedDeviceRepo;
        this.userProfileRepo = userProfileRepo;
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
        
        // Log to terminal for development
        System.out.println("\n=================================================");
        System.out.println(">>> PASSWORD RESET TOKEN for " + email + ": " + resetToken);
        System.out.println(">>> RESET LINK: http://localhost:3000/reset-password?token=" + resetToken);
        System.out.println("=================================================\n");

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

    @Transactional
    public void sendOtp(String phoneNumber) {
        // 1. Generate OTP
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        
        // 2. Save to Redis (5 mins expiry)
        // Key: auth:otp:{phoneNumber}
        redis.opsForValue().set("auth:otp:" + phoneNumber, otp, Duration.ofMinutes(5));
        
        // 3. Log (Simulation)
        log.info("\n\n#################################################\n>>> TERMINAL OTP: {} for {}\n#################################################\n", otp, phoneNumber);
        System.out.println("\n=================================================");
        System.out.println(">>> OTP for " + phoneNumber + ": " + otp);
        System.out.println("=================================================\n");
        try {
            eventLogRepo.save(new EventLogDocument("AUTH_OTP_SENT", "phone_" + phoneNumber, Instant.now(), null, Map.of("phone", phoneNumber)));
        } catch (Exception e) {}
    }

    @Transactional
    public JwtService.TokenPair verifyOtpAndLogin(String phoneNumber, String otp, String deviceId, String ua, String ip) {
        // 1. Validate OTP
        String key = "auth:otp:" + phoneNumber;
        String storedOtp = redis.opsForValue().get(key);
        
        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        // 2. Clear OTP
        redis.delete(key);
        
        // 3. Find or Create User
        UserEntity u = userRepo.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                     RoleEntity clientRole = roleRepo.findByCode("CLIENT")
                        .orElseThrow(() -> new IllegalStateException("CLIENT role missing"));

                     UserEntity newUser = new UserEntity();
                     newUser.setPhoneNumber(phoneNumber);
                     newUser.setEmail(null); 
                     
                     // Generate a random name or use phone
                     newUser.setFullName("User " + phoneNumber);
                     newUser.setStatus("ACTIVE");
                     newUser.getRoles().add(clientRole);
                     return userRepo.save(newUser);
                });
        
        if (!"ACTIVE".equals(u.getStatus())) throw new IllegalArgumentException("User disabled");
        
        if (!"ACTIVE".equals(u.getStatus())) throw new IllegalArgumentException("User disabled");

        // Save Trusted Device if deviceId provided
        if (deviceId != null && !deviceId.isBlank()) {
            System.out.println("Saving trusted device: " + deviceId + " for " + phoneNumber);
            Optional<TrustedDeviceEntity> existing = trustedDeviceRepo.findByDeviceIdAndUser_PhoneNumber(deviceId, phoneNumber);
            TrustedDeviceEntity device = existing.orElse(new TrustedDeviceEntity());
            if (device.getId() == null) {
                device.setUser(u);
                device.setDeviceId(deviceId);
            }
            device.setExpiresAt(Instant.now().plus(Duration.ofDays(30)));
            device.setTrustedAt(Instant.now());
            device.setLastUsedAt(Instant.now());
            trustedDeviceRepo.save(device);
            
            try {
                eventLogRepo.save(new EventLogDocument("AUTH_DEVICE_TRUSTED", "device_" + deviceId, Instant.now(), null, Map.of("phone", phoneNumber)));
            } catch (Exception e) {}
        }
        
        // 4. Issue Tokens
        return issueNewPairForUser(u, ua, ip);
    }

    @Transactional
    public JwtService.TokenPair trustedDeviceLogin(String phoneNumber, String deviceId, String ua, String ip) {
        System.out.println("Attempting Trusted Login. Phone: " + phoneNumber + ", DeviceId: " + deviceId);
        TrustedDeviceEntity device = trustedDeviceRepo.findByDeviceIdAndUser_PhoneNumber(deviceId, phoneNumber)
                .orElseThrow(() -> {
                    System.out.println("Trusted Login Failed: Device not found or phone mismatch");
                    return new IllegalArgumentException("Device not trusted or user not found");
                });

        if (device.getExpiresAt().isBefore(Instant.now())) {
            System.out.println("Trusted Login Failed: Device expired");
            trustedDeviceRepo.delete(device);
            throw new IllegalArgumentException("Device trust expired");
        }
        
        System.out.println("Trusted Login Success for user: " + device.getUser().getId());

        UserEntity u = device.getUser();
        if (!"ACTIVE".equals(u.getStatus())) throw new IllegalArgumentException("User disabled");

        device.setLastUsedAt(Instant.now());
        trustedDeviceRepo.save(device);

        try {
            eventLogRepo.save(new EventLogDocument("AUTH_TRUSTED_LOGIN", "user_" + u.getId(), Instant.now(), null, Map.of("phone", phoneNumber, "deviceId", deviceId)));
        } catch (Exception e) {}

        return issueNewPairForUser(u, ua, ip);
    }
    public UserEntity getUserProfile(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Optional<UserProfileEntity> getUserProfileDetails(Long userId) {
        return userProfileRepo.findById(userId);
    }

    @Transactional
    public UserEntity updateProfile(Long userId, String fullName, String phoneNumber, String gender, java.time.LocalDate dateOfBirth, String avatarUrl) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"ACTIVE".equals(user.getStatus())) {
             throw new IllegalArgumentException("User disabled");
        }
        
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            // Check if phone number is already taken by another user
             userRepo.findByPhoneNumber(phoneNumber)
                .ifPresent(u -> {
                    if (!u.getId().equals(userId)) {
                        throw new IllegalArgumentException("Phone number already in use");
                    }
                });
            user.setPhoneNumber(phoneNumber);
        }
        
        // Sync with UserProfile
        UserProfileEntity profile = userProfileRepo.findById(userId)
                .orElseGet(() -> {
                    UserProfileEntity p = new UserProfileEntity();
                    p.setUserId(userId);
                    return p;
                });
        
        boolean profileChanged = false;
        
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            profile.setPhone(phoneNumber);
            profileChanged = true;
        }

        if (gender != null && !gender.isBlank()) {
            try {
                profile.setGender(com.example.ecommerce.ecommerce_backend.domain.model.Gender.valueOf(gender.toUpperCase()));
                profileChanged = true;
            } catch (IllegalArgumentException e) {
                // Ignore invalid gender
            }
        }

        if (dateOfBirth != null) {
            profile.setDateOfBirth(dateOfBirth);
            profileChanged = true;
        }

        if (avatarUrl != null && !avatarUrl.isBlank()) {
            profile.setAvatarUrl(avatarUrl);
            profileChanged = true;
        }

        if (profileChanged) {
            userProfileRepo.save(profile);
        }
        
        return userRepo.save(user);
    }

    @Transactional
    public JwtService.TokenPair registerSeller(Long userId, String ua, String ip) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("User disabled");
        }

        boolean alreadySeller = user.getRoles().stream()
                .anyMatch(r -> "SELLER".equals(r.getCode()));

        if (!alreadySeller) {
            RoleEntity sellerRole = roleRepo.findByCode("SELLER")
                    .orElseThrow(() -> new IllegalStateException("SELLER role missing"));
            user.getRoles().add(sellerRole);
            user = userRepo.save(user);
            
            // Log to terminal for development
            System.out.println("\n#################################################");
            System.out.println(">>> USER UPGRADED TO SELLER: " + user.getEmail() + " (ID: " + user.getId() + ")");
            System.out.println("#################################################\n");

            try {
                eventLogRepo.save(new EventLogDocument("AUTH_REGISTER_SELLER", "user_" + userId, Instant.now(), null, Map.of("userId", userId)));
            } catch (Exception e) {}
        }

        return issueNewPairForUser(user, ua, ip);
    }
}
