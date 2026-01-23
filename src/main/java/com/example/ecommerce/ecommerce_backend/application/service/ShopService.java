package com.example.ecommerce.ecommerce_backend.application.service;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerProfileJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;

import jakarta.transaction.Transactional;

@Service
public class ShopService {

    private static final Logger log = LoggerFactory.getLogger(ShopService.class);

    private final SellerShopJpaRepository shopRepo;
    private final SellerProfileJpaRepository profileRepo;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;
    private final ShopStatusHistoryService historyService;
    private final NotificationService notificationService;

    public ShopService(SellerShopJpaRepository shopRepo, SellerProfileJpaRepository profileRepo,
                       StringRedisTemplate redis, EventLogMongoRepository eventRepo, 
                       ShopStatusHistoryService historyService, NotificationService notificationService) {
        this.shopRepo = shopRepo;
        this.profileRepo = profileRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
        this.historyService = historyService;
        this.notificationService = notificationService;
    }
    
    /**
     * Check if seller can create shop (must have ACTIVE profile)
     */
    private void validateSellerCanCreateShop(Long sellerUserId) {
        Optional<SellerProfileEntity> profile = profileRepo.findByUserId(sellerUserId);
        
        if (profile.isEmpty()) {
            throw ApiException.forbidden("Không tìm thấy hồ sơ người bán. Vui lòng hoàn tất xác thực người bán trước.");
        }
        
        if (!profile.get().isActive()) {
            String status = profile.get().getStatus().name();
            String message = switch (status) {
                case "PENDING_VERIFICATION" -> "Hồ sơ người bán của bạn đang chờ xác thực. Vui lòng đợi admin duyệt.";
                case "REJECTED" -> "Hồ sơ người bán của bạn đã bị từ chối. Vui lòng cập nhật và gửi lại.";
                case "SUSPENDED" -> "Tài khoản người bán của bạn đã bị tạm khóa. Vui lòng liên hệ hỗ trợ.";
                default -> "Hồ sơ người bán của bạn chưa được kích hoạt. Trạng thái hiện tại: " + status;
            };
            throw ApiException.forbidden(message);
        }
    }
    
    /**
     * Validate bank information format
     */
    private void validateBankInfo(String bankName, String bankAccountNumber, String bankAccountName) {
        if (bankName == null || bankName.isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập tên ngân hàng");
        }
        if (bankAccountNumber == null || bankAccountNumber.isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập số tài khoản ngân hàng");
        }
        if (bankAccountName == null || bankAccountName.isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập tên chủ tài khoản");
        }
        
        // Validate bank account number format (8-20 digits)
        String accountNumber = bankAccountNumber.trim().replaceAll("\\s+", "");
        if (!accountNumber.matches("^\\d{8,20}$")) {
            throw ApiException.badRequest("Số tài khoản ngân hàng phải là 8-20 chữ số");
        }
        
        // Validate bank account name (uppercase, no special chars except spaces)
        String accountName = bankAccountName.trim().toUpperCase();
        if (!accountName.matches("^[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ\\s]+$")) {
            throw ApiException.badRequest("Tên chủ tài khoản chỉ được chứa chữ cái và khoảng trắng");
        }
        if (accountName.length() < 3 || accountName.length() > 100) {
            throw ApiException.badRequest("Tên chủ tài khoản phải từ 3-100 ký tự");
        }
    }

    public Optional<SellerShopEntity> getBySeller(Long sellerUserId) {
        return shopRepo.findBySellerUserId(sellerUserId);
    }

    @Transactional
    public SellerShopEntity createDraft(Long sellerUserId, String shopName, String description, String city, String address,
                                      String contactName, String contactPhone, String contactEmail,
                                      String identityCode, String taxCode,
                                      String bankName, String bankAccountNumber, String bankAccountName) {
        // RULE: Seller must have ACTIVE profile before creating shop
        validateSellerCanCreateShop(sellerUserId);
        
        // Idempotent by UNIQUE seller_user_id: if exists, return existing
        return shopRepo.findBySellerUserId(sellerUserId).orElseGet(() -> {
            SellerShopEntity s = new SellerShopEntity();
            s.setSellerUserId(sellerUserId);
            s.setShopName(shopName);
            s.setShopSlug(uniqueSlug(shopName));
            s.setDescription(description);
            s.setCity(city);
            s.setAddress(address);
            s.setContactName(contactName);
            s.setContactPhone(contactPhone);
            s.setContactEmail(contactEmail);
            s.setIdentityCode(identityCode);
            s.setTaxCode(taxCode);
            s.setBankName(bankName);
            s.setBankAccountNumber(bankAccountNumber);
            s.setBankAccountName(bankAccountName);
            s.setStatus("DRAFT");
            SellerShopEntity saved = shopRepo.save(s);

            // Log to terminal for development
            System.out.println("\n*************************************************");
            System.out.println(">>> NEW SHOP CREATED (DRAFT): " + saved.getShopName() + " (ID: " + saved.getId() + ")");
            System.out.println(">>> SELLER USER ID: " + sellerUserId);
            System.out.println("*************************************************\n");

            invalidate(sellerUserId, saved.getId());
            eventRepo.save(new EventLogDocument("SHOP_CREATED", "shop_" + saved.getId(), Instant.now(), null,
                    Map.of("shopId", saved.getId(), "sellerUserId", sellerUserId, "status", "DRAFT")));
            
            // Record status history
            historyService.recordSellerChange(saved.getId(), null, "DRAFT", sellerUserId, "Shop created");
            
            return saved;
        });
    }

    @Transactional
    public SellerShopEntity updateShop(Long sellerUserId, String shopName, String description, String city, String address,
                                      String contactName, String contactPhone, String contactEmail,
                                      String identityCode, String taxCode,
                                      String bankName, String bankAccountNumber, String bankAccountName) {
        SellerShopEntity s = shopRepo.findBySellerUserId(sellerUserId).orElseThrow();

        // Validate phone format if provided
        if (contactPhone != null && !contactPhone.isBlank()) {
            String phone = contactPhone.trim();
            if (!phone.matches("^(0|\\+84)[3-9]\\d{8}$")) {
                throw ApiException.badRequest("Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam (10 chữ số)");
            }
        }
        
        // Validate email format if provided
        if (contactEmail != null && !contactEmail.isBlank()) {
            String email = contactEmail.trim().toLowerCase();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw ApiException.badRequest("Email không hợp lệ");
            }
        }
        
        // Validate bank info format if provided
        if (bankName != null && !bankName.isBlank() 
            || bankAccountNumber != null && !bankAccountNumber.isBlank()
            || bankAccountName != null && !bankAccountName.isBlank()) {
            // If any bank field is provided, all must be provided and valid
            if (bankName == null || bankName.isBlank() 
                || bankAccountNumber == null || bankAccountNumber.isBlank()
                || bankAccountName == null || bankAccountName.isBlank()) {
                throw ApiException.badRequest("Vui lòng nhập đầy đủ thông tin ngân hàng");
            }
            validateBankInfo(bankName, bankAccountNumber, bankAccountName);
        }

        // State rules
        switch (s.getStatus()) {
            case "DRAFT", "PENDING_REVIEW" -> {
                s.setShopName(shopName);
                s.setDescription(description);
                s.setCity(city);
                s.setAddress(address);
                s.setContactName(contactName);
                s.setContactPhone(contactPhone);
                s.setContactEmail(contactEmail);
                s.setIdentityCode(identityCode);
                s.setTaxCode(taxCode);
                s.setBankName(bankName);
                s.setBankAccountNumber(bankAccountNumber);
                s.setBankAccountName(bankAccountName);
            }
            case "ACTIVE" -> {
                // In ACTIVE, allow only description/address/bank updates
                s.setDescription(description);
                s.setCity(city);
                s.setAddress(address);
                s.setBankName(bankName);
                s.setBankAccountNumber(bankAccountNumber);
                s.setBankAccountName(bankAccountName);
            }
            default -> throw new IllegalArgumentException("Shop not editable in status=" + s.getStatus());
        }

        SellerShopEntity saved = shopRepo.save(s);
        invalidate(sellerUserId, saved.getId());
        return saved;
    }

    @Transactional
    public SellerShopEntity submitForReview(Long sellerUserId) {
        SellerShopEntity s = shopRepo.findBySellerUserId(sellerUserId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy shop"));
        
        if (!"DRAFT".equals(s.getStatus())) {
            throw ApiException.badRequest("Chỉ có thể gửi shop ở trạng thái DRAFT");
        }
        
        // Validate required fields before submission
        if (s.getShopName() == null || s.getShopName().isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập tên shop");
        }
        if (s.getContactPhone() == null || s.getContactPhone().isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập số điện thoại liên hệ");
        }
        // Validate phone format (Vietnamese)
        String phone = s.getContactPhone().trim();
        if (!phone.matches("^(0|\\+84)[3-9]\\d{8}$")) {
            throw ApiException.badRequest("Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam (10 chữ số)");
        }
        if (s.getContactEmail() == null || s.getContactEmail().isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập email liên hệ");
        }
        // Validate email format
        String email = s.getContactEmail().trim().toLowerCase();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw ApiException.badRequest("Email không hợp lệ");
        }
        if (s.getCity() == null || s.getCity().isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập tỉnh/thành phố");
        }
        if (s.getAddress() == null || s.getAddress().isBlank()) {
            throw ApiException.badRequest("Vui lòng nhập địa chỉ");
        }
        
        // Validate bank information (required before submission)
        validateBankInfo(s.getBankName(), s.getBankAccountNumber(), s.getBankAccountName());
        
        String previousStatus = s.getStatus();
        s.setStatus("PENDING_REVIEW");
        SellerShopEntity saved = shopRepo.save(s);

        // Log to terminal for development
        System.out.println("\n#################################################");
        System.out.println(">>> SHOP SUBMITTED FOR REVIEW: " + saved.getShopName() + " (ID: " + saved.getId() + ")");
        System.out.println(">>> STATUS: PENDING_REVIEW");
        System.out.println("#################################################\n");

        invalidate(sellerUserId, saved.getId());
        eventRepo.save(new EventLogDocument("SHOP_SUBMITTED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "sellerUserId", sellerUserId, "status", "PENDING_REVIEW")));
        
        // Record status history
        historyService.recordSellerChange(saved.getId(), previousStatus, "PENDING_REVIEW", sellerUserId, "Shop submitted for review");
        
        // Notify all admins about new shop submission
        try {
            notificationService.notifyAdminsNewShop(
                    saved.getId(),
                    saved.getShopName(),
                    sellerUserId
            );
        } catch (Exception e) {
            log.warn("Failed to notify admins about shop submission {}: {}", saved.getId(), e.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public SellerShopEntity setLogo(Long sellerUserId, String logoUrl) {
        SellerShopEntity s = shopRepo.findBySellerUserId(sellerUserId).orElseThrow();
        s.setLogoUrl(logoUrl);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(sellerUserId, saved.getId());
        return saved;
    }

    @Transactional
    public SellerShopEntity setBanner(Long sellerUserId, String bannerUrl) {
        SellerShopEntity s = shopRepo.findBySellerUserId(sellerUserId).orElseThrow();
        s.setBannerUrl(bannerUrl);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(sellerUserId, saved.getId());
        return saved;
    }

    private void invalidate(Long sellerUserId, Long shopId) {
        redis.delete("cache:shop_by_seller:" + sellerUserId);
        redis.delete("cache:shop:" + shopId);
    }

    private String uniqueSlug(String name) {
        String base = slugify(name);
        String slug = base;
        int i = 1;
        while (shopRepo.findByShopSlug(slug).isPresent()) {
            i++;
            slug = base + "-" + i;
        }
        return slug;
    }

    private String slugify(String input) {
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9\\-]", "")
                .toLowerCase(Locale.ROOT);
        return slug.isBlank() ? "shop" : slug;
    }
}
