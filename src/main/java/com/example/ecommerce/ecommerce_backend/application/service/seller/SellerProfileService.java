package com.example.ecommerce.ecommerce_backend.application.service.seller;

import java.time.Duration;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import java.time.Instant;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import java.util.Map;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import java.util.Optional;
import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import org.slf4j.Logger;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import org.slf4j.LoggerFactory;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import org.springframework.data.domain.Page;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import org.springframework.data.domain.Pageable;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import org.springframework.stereotype.Service;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import org.springframework.transaction.annotation.Transactional;
import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.SellerProfileRequest;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.SellerProfileResponse;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity.SellerStatus;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity.SellerType;import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerProfileJpaRepository;

import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;
/**
 * Service for managing Seller Profile verification flow.
 * 
 * Flow:
 * 1. User upgrades to SELLER role (via AuthService)
 * 2. User creates SellerProfile with identity info → status: PENDING_VERIFICATION
 * 3. Admin reviews and approves/rejects
 * 4. Only ACTIVE sellers can create shops
 */
@Service
public class SellerProfileService {

    private static final Logger log = LoggerFactory.getLogger(SellerProfileService.class);

    private final SellerProfileJpaRepository profileRepo;
    private final NotificationService notificationService;
    private final EventLogMongoRepository eventRepo;
    private final ShopService shopService;

    public SellerProfileService(
            SellerProfileJpaRepository profileRepo,
            NotificationService notificationService,
            EventLogMongoRepository eventRepo,
            ShopService shopService
    ) {
        this.profileRepo = profileRepo;
        this.notificationService = notificationService;
        this.eventRepo = eventRepo;
        this.shopService = shopService;
    }

    // ==================== SELLER METHODS ====================

    /**
     * Get current user's seller profile
     */
    @Transactional(readOnly = true)
    public Optional<SellerProfileResponse> getMyProfile(Long userId) {
        return profileRepo.findByUserId(userId)
                .map(SellerProfileResponse::from);
    }

    /**
     * Check if user has an active seller profile
     */
    @Transactional(readOnly = true)
    public boolean isSellerActive(Long userId) {
        return profileRepo.findByUserId(userId)
                .map(SellerProfileEntity::isActive)
                .orElse(false);
    }

    /**
     * Check if user can create a shop
     */
    @Transactional(readOnly = true)
    public boolean canCreateShop(Long userId) {
        return profileRepo.findByUserId(userId)
                .map(SellerProfileEntity::canCreateShop)
                .orElse(false);
    }

    /**
     * Create or update seller profile and submit for verification
     */
    @Transactional
    public SellerProfileResponse createOrUpdateProfile(Long userId, SellerProfileRequest request) {
        log.info("Creating/updating seller profile for user {}", userId);

        SellerProfileEntity profile = profileRepo.findByUserId(userId)
                .orElse(new SellerProfileEntity());

        // Can only update if PENDING or REJECTED (allow resubmission)
        if (profile.getId() != null && profile.getStatus() == SellerStatus.ACTIVE) {
            throw ApiException.conflict("Không thể chỉnh sửa hồ sơ người bán đã được xác thực");
        }
        
        // Idempotency check: if profile was just submitted (within 1 minute), return existing
        if (profile.getId() != null 
                && profile.getStatus() == SellerStatus.PENDING_VERIFICATION 
                && profile.getSubmittedAt() != null) {
            Duration timeSinceSubmission = Duration.between(profile.getSubmittedAt(), Instant.now());
            if (timeSinceSubmission.toMinutes() < 1) {
                log.info("Profile {} was recently submitted ({} seconds ago), returning existing", 
                        profile.getId(), timeSinceSubmission.getSeconds());
                return SellerProfileResponse.from(profile);
            }
        }

        // Validate seller type and related fields
        SellerType sellerType = SellerType.valueOf(request.sellerType());
        
        // Validate tax code for BUSINESS type
        if (sellerType == SellerType.BUSINESS) {
            if (request.taxCode() == null || request.taxCode().isBlank()) {
                throw ApiException.badRequest("Mã số thuế là bắt buộc đối với doanh nghiệp");
            }
            // Validate tax code format (10 digits for Vietnam)
            String taxCode = request.taxCode().trim();
            if (!taxCode.matches("^\\d{10}$")) {
                throw ApiException.badRequest("Mã số thuế phải là 10 chữ số");
            }
        }
        
        // Validate ID number format based on type
        String idNumber = request.idNumber().trim();
        if (sellerType == SellerType.INDIVIDUAL && "CCCD".equals(request.idType())) {
            // CCCD must be 12 digits
            if (!idNumber.matches("^\\d{12}$")) {
                throw ApiException.badRequest("Số CCCD phải là 12 chữ số");
            }
        } else if (sellerType == SellerType.INDIVIDUAL && "PASSPORT".equals(request.idType())) {
            // Passport format: alphanumeric, 8-9 characters
            if (!idNumber.matches("^[A-Z0-9]{8,9}$")) {
                throw ApiException.badRequest("Số hộ chiếu không hợp lệ");
            }
        } else if (sellerType == SellerType.BUSINESS && "BUSINESS_LICENSE".equals(request.idType())) {
            // Business license: alphanumeric
            if (idNumber.length() < 5 || idNumber.length() > 50) {
                throw ApiException.badRequest("Số giấy phép kinh doanh không hợp lệ");
            }
        }
        
        // Validate phone number format (Vietnamese)
        String phone = request.contactPhone().trim();
        if (!phone.matches("^(0|\\+84)[3-9]\\d{8}$")) {
            throw ApiException.badRequest("Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam (10 chữ số)");
        }
        
        // Validate email format
        String email = request.contactEmail().trim().toLowerCase();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw ApiException.badRequest("Email không hợp lệ");
        }

        // Set basic info
        profile.setUserId(userId);
        profile.setFullName(request.fullName().trim());
        profile.setSellerType(sellerType);
        profile.setIdType(request.idType());
        profile.setIdNumber(idNumber);
        profile.setIdImageFront(request.idImageFront() != null && !request.idImageFront().isBlank() ? request.idImageFront().trim() : null);
        profile.setIdImageBack(request.idImageBack() != null && !request.idImageBack().isBlank() ? request.idImageBack().trim() : null);
        profile.setTaxCode(request.taxCode() != null ? request.taxCode().trim() : null);
        profile.setContactPhone(phone);
        profile.setContactEmail(email);
        profile.setCity(request.city().trim());
        profile.setAddress(request.address().trim());

        // Set status to pending verification
        profile.setStatus(SellerStatus.PENDING_VERIFICATION);
        profile.setSubmittedAt(Instant.now());
        profile.setRejectedReason(null);
        profile.setRejectedAt(null);

        SellerProfileEntity saved = profileRepo.save(profile);

        // Log event
        try {
            eventRepo.save(new EventLogDocument(
                    "SELLER_PROFILE_SUBMITTED",
                    "user_" + userId,
                    Instant.now(),
                    null,
                    Map.of("profileId", saved.getId(), "sellerType", request.sellerType())
            ));
        } catch (Exception e) {
            log.warn("Failed to log event: {}", e.getMessage());
        }

        // Notify all admins about new seller profile submission
        try {
            notificationService.notifyAdminsNewSellerProfile(
                    saved.getId(),
                    saved.getFullName(),
                    saved.getSellerType().name()
            );
        } catch (Exception e) {
            log.warn("Failed to notify admins about new seller profile: {}", e.getMessage());
        }

        log.info("Seller profile {} submitted for verification", saved.getId());
        return SellerProfileResponse.from(saved);
    }

    // ==================== ADMIN METHODS ====================

    /**
     * Get all profiles by status (Admin)
     */
    @Transactional(readOnly = true)
    public Page<SellerProfileResponse> getProfilesByStatus(SellerStatus status, Pageable pageable) {
        return profileRepo.findByStatus(status, pageable)
                .map(SellerProfileResponse::from);
    }

    /**
     * Get pending verification count (Admin dashboard)
     */
    @Transactional(readOnly = true)
    public long getPendingCount() {
        return profileRepo.countByStatus(SellerStatus.PENDING_VERIFICATION);
    }

    /**
     * Get profile by ID (Admin)
     */
    @Transactional(readOnly = true)
    public SellerProfileResponse getProfileById(Long profileId) {
        SellerProfileEntity profile = profileRepo.findById(profileId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy hồ sơ người bán"));
        return SellerProfileResponse.from(profile);
    }

    /**
     * Approve seller profile (Admin)
     */
    @Transactional
    public SellerProfileResponse approve(Long profileId, Long adminId) {
        log.info("Admin {} approving seller profile {}", adminId, profileId);

        SellerProfileEntity profile = profileRepo.findById(profileId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy hồ sơ người bán"));

        if (profile.getStatus() != SellerStatus.PENDING_VERIFICATION) {
            throw ApiException.badRequest("Chỉ có thể duyệt hồ sơ đang chờ xác thực");
        }

        profile.setStatus(SellerStatus.ACTIVE);
        profile.setVerifiedAt(Instant.now());
        profile.setVerifiedBy(adminId);
        profile.setRejectedReason(null);
        profile.setRejectedAt(null);

        SellerProfileEntity saved = profileRepo.save(profile);

        // AUTO-CREATE SHOP DRAFT for approved seller
        boolean shopCreated = false;
        try {
            log.info("Auto-creating shop draft for seller user {}", saved.getUserId());
            
            // Generate default shop name from seller's full name
            String defaultShopName = profile.getFullName() + "'s Shop";
            
            shopService.createDraft(
                saved.getUserId(),
                defaultShopName,
                "", // empty description
                profile.getCity(),
                profile.getAddress(),
                profile.getFullName(), // contact name
                profile.getContactPhone(),
                profile.getContactEmail(),
                profile.getSellerType() == SellerType.INDIVIDUAL ? profile.getIdNumber() : "", // identityCode
                profile.getSellerType() == SellerType.BUSINESS ? profile.getTaxCode() : "", // taxCode
                "", // bankName - seller will fill later
                "", // bankAccountNumber - seller will fill later
                ""  // bankAccountName - seller will fill later
            );
            
            shopCreated = true;
            log.info("Shop draft created successfully for seller user {}", saved.getUserId());
        } catch (Exception e) {
            log.error("Failed to auto-create shop for seller user {}: {}", saved.getUserId(), e.getMessage(), e);
            // Log to admin notification queue for manual intervention
            try {
                eventRepo.save(new EventLogDocument(
                    "SHOP_AUTO_CREATE_FAILED",
                    "user_" + saved.getUserId(),
                    Instant.now(),
                    null,
                    Map.of("userId", saved.getUserId(), "profileId", profileId, "error", e.getMessage())
                ));
            } catch (Exception logEx) {
                log.warn("Failed to log shop creation failure: {}", logEx.getMessage());
            }
        }

        // Notify seller
        String notificationMessage = shopCreated 
            ? "Chúc mừng! Hồ sơ người bán của bạn đã được xác thực. Shop của bạn đã được tạo tự động. Vui lòng hoàn thiện thông tin shop và bắt đầu bán hàng!"
            : "Chúc mừng! Hồ sơ người bán của bạn đã được xác thực. Vui lòng tạo shop để bắt đầu bán hàng.";
        
        notificationService.createNotification(
                saved.getUserId(),
                "SELLER_VERIFIED",
                "Xác thực người bán thành công",
                notificationMessage,
                "SELLER_PROFILE",
                saved.getId()
        );

        // Log event
        try {
            eventRepo.save(new EventLogDocument(
                    "SELLER_PROFILE_APPROVED",
                    "profile_" + profileId,
                    Instant.now(),
                    null,
                    Map.of("profileId", profileId, "adminId", adminId, "userId", saved.getUserId())
            ));
        } catch (Exception e) {
            log.warn("Failed to log event: {}", e.getMessage());
        }

        log.info("Seller profile {} approved by admin {}", profileId, adminId);
        return SellerProfileResponse.from(saved);
    }

    /**
     * Reject seller profile (Admin)
     */
    @Transactional
    public SellerProfileResponse reject(Long profileId, Long adminId, String reason) {
        log.info("Admin {} rejecting seller profile {} with reason: {}", adminId, profileId, reason);

        SellerProfileEntity profile = profileRepo.findById(profileId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy hồ sơ người bán"));

        if (profile.getStatus() != SellerStatus.PENDING_VERIFICATION) {
            throw ApiException.badRequest("Chỉ có thể từ chối hồ sơ đang chờ xác thực");
        }

        if (reason == null || reason.isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập lý do từ chối");
        }

        profile.setStatus(SellerStatus.REJECTED);
        profile.setRejectedAt(Instant.now());
        profile.setRejectedReason(reason);
        profile.setVerifiedBy(adminId);

        SellerProfileEntity saved = profileRepo.save(profile);

        // Notify seller
        notificationService.createNotification(
                saved.getUserId(),
                "SELLER_REJECTED",
                "Xác thực người bán không thành công",
                String.format("Hồ sơ người bán của bạn chưa được duyệt. Lý do: %s. Vui lòng cập nhật thông tin và gửi lại.", reason),
                "SELLER_PROFILE",
                saved.getId()
        );

        // Log event
        try {
            eventRepo.save(new EventLogDocument(
                    "SELLER_PROFILE_REJECTED",
                    "profile_" + profileId,
                    Instant.now(),
                    null,
                    Map.of("profileId", profileId, "adminId", adminId, "reason", reason, "userId", saved.getUserId())
            ));
        } catch (Exception e) {
            log.warn("Failed to log event: {}", e.getMessage());
        }

        log.info("Seller profile {} rejected by admin {}", profileId, adminId);
        return SellerProfileResponse.from(saved);
    }

    /**
     * Suspend seller profile (Admin)
     */
    @Transactional
    public SellerProfileResponse suspend(Long profileId, Long adminId, String reason) {
        log.info("Admin {} suspending seller profile {}", adminId, profileId);

        SellerProfileEntity profile = profileRepo.findById(profileId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy hồ sơ người bán"));

        if (profile.getStatus() != SellerStatus.ACTIVE) {
            throw ApiException.badRequest("Chỉ có thể tạm khóa hồ sơ đang hoạt động");
        }

        profile.setStatus(SellerStatus.SUSPENDED);
        profile.setRejectedReason(reason);
        profile.setRejectedAt(Instant.now());

        SellerProfileEntity saved = profileRepo.save(profile);

        // Notify seller
        notificationService.createNotification(
                saved.getUserId(),
                "SELLER_SUSPENDED",
                "Tài khoản người bán bị tạm khóa",
                String.format("Tài khoản người bán của bạn đã bị tạm khóa. Lý do: %s. Vui lòng liên hệ hỗ trợ.", reason),
                "SELLER_PROFILE",
                saved.getId()
        );

        log.info("Seller profile {} suspended by admin {}", profileId, adminId);
        return SellerProfileResponse.from(saved);
    }

    /**
     * Reactivate suspended seller profile (Admin)
     */
    @Transactional
    public SellerProfileResponse reactivate(Long profileId, Long adminId) {
        log.info("Admin {} reactivating seller profile {}", adminId, profileId);

        SellerProfileEntity profile = profileRepo.findById(profileId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy hồ sơ người bán"));

        if (profile.getStatus() != SellerStatus.SUSPENDED) {
            throw ApiException.badRequest("Chỉ có thể kích hoạt lại hồ sơ đang bị tạm khóa");
        }

        profile.setStatus(SellerStatus.ACTIVE);
        profile.setRejectedReason(null);
        profile.setRejectedAt(null);

        SellerProfileEntity saved = profileRepo.save(profile);

        // Notify seller
        notificationService.createNotification(
                saved.getUserId(),
                "SELLER_REACTIVATED",
                "Tài khoản người bán đã được kích hoạt lại",
                "Chúc mừng! Tài khoản người bán của bạn đã được kích hoạt lại. Bạn có thể tiếp tục kinh doanh.",
                "SELLER_PROFILE",
                saved.getId()
        );

        log.info("Seller profile {} reactivated by admin {}", profileId, adminId);
        return SellerProfileResponse.from(saved);
    }
}
