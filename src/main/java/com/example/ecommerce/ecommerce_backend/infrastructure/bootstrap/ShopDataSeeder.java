package com.example.ecommerce.ecommerce_backend.infrastructure.bootstrap;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerProfileJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/**
 * Seed shop data for specific user
 * Usage: Run application with this component enabled
 */
@Component
public class ShopDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ShopDataSeeder.class);

    private final UserJpaRepository userRepo;
    private final SellerProfileJpaRepository profileRepo;
    private final SellerShopJpaRepository shopRepo;

    public ShopDataSeeder(UserJpaRepository userRepo, 
                         SellerProfileJpaRepository profileRepo,
                         SellerShopJpaRepository shopRepo) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.shopRepo = shopRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = "quocsang05.uit@gmail.com";
        seedShopForUser(email);
    }

    private void seedShopForUser(String email) {
        try {
            // Find user by email
            Optional<UserEntity> userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User with email {} not found. Skipping shop seed.", email);
                return;
            }

            UserEntity user = userOpt.get();
            Long userId = user.getId();
            log.info("Found user: {} (ID: {})", email, userId);

            // Check if seller profile exists and is ACTIVE
            Optional<SellerProfileEntity> profileOpt = profileRepo.findByUserId(userId);
            if (profileOpt.isEmpty()) {
                log.warn("Seller profile not found for user {}. Creating ACTIVE profile...", email);
                // Create ACTIVE seller profile
                SellerProfileEntity profile = new SellerProfileEntity();
                profile.setUserId(userId);
                profile.setStatus(SellerProfileEntity.SellerStatus.ACTIVE);
                profile.setSellerType(SellerProfileEntity.SellerType.INDIVIDUAL);
                profile.setFullName(user.getFullName() != null ? user.getFullName() : "Seller " + userId);
                profile.setContactEmail(user.getEmail());
                profile.setContactPhone(user.getPhoneNumber());
                profile.setCity("Hồ Chí Minh");
                profile.setAddress("123 Đường ABC, Quận 1");
                profile.setIdType("CCCD");
                profile.setIdNumber("123456789012");
                profile.setSubmittedAt(Instant.now());
                profile.setVerifiedAt(Instant.now());
                profile = profileRepo.save(profile);
                log.info("Created ACTIVE seller profile for user {}", email);
            } else {
                SellerProfileEntity profile = profileOpt.get();
                if (profile.getStatus() != SellerProfileEntity.SellerStatus.ACTIVE) {
                    log.warn("Seller profile exists but is not ACTIVE (status: {}). Setting to ACTIVE...", profile.getStatus());
                    profile.setStatus(SellerProfileEntity.SellerStatus.ACTIVE);
                    profile.setVerifiedAt(Instant.now());
                    profileRepo.save(profile);
                    log.info("Updated seller profile to ACTIVE for user {}", email);
                }
            }

            // Check if shop already exists
            Optional<SellerShopEntity> shopOpt = shopRepo.findBySellerUserId(userId);
            if (shopOpt.isPresent()) {
                SellerShopEntity existingShop = shopOpt.get();
                log.info("Shop already exists for user {}: {} (ID: {}, Status: {})", 
                    email, existingShop.getShopName(), existingShop.getId(), existingShop.getStatus());
                
                // Update shop to ACTIVE if not already
                if (!"ACTIVE".equals(existingShop.getStatus())) {
                    existingShop.setStatus("ACTIVE");
                    existingShop.setVerifiedAt(Instant.now());
                    shopRepo.save(existingShop);
                    log.info("Updated shop status to ACTIVE for user {}", email);
                }
                return;
            }

            // Create new shop
            String shopName = "Shop của " + (user.getFullName() != null ? user.getFullName() : "Seller");
            String shopSlug = generateSlug(shopName);
            
            // Ensure slug is unique
            int counter = 1;
            String originalSlug = shopSlug;
            while (shopRepo.findByShopSlug(shopSlug).isPresent()) {
                shopSlug = originalSlug + "-" + counter;
                counter++;
            }

            SellerShopEntity shop = new SellerShopEntity();
            shop.setSellerUserId(userId);
            shop.setShopName(shopName);
            shop.setShopSlug(shopSlug);
            shop.setDescription("Đây là shop mẫu được seed tự động. Chào mừng bạn đến với shop của chúng tôi!");
            shop.setStatus("ACTIVE");
            shop.setCity("Hồ Chí Minh");
            shop.setAddress("123 Đường ABC, Quận 1, TP.HCM");
            shop.setContactName(user.getFullName() != null ? user.getFullName() : "Seller");
            shop.setContactPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : "0901234567");
            shop.setContactEmail(user.getEmail());
            shop.setIdentityCode("123456789012");
            shop.setBankName("Vietcombank");
            shop.setBankAccountNumber("1234567890");
            shop.setBankAccountName(user.getFullName() != null ? user.getFullName().toUpperCase() : "SELLER");
            shop.setVerifiedAt(Instant.now());

            shop = shopRepo.save(shop);
            log.info("Successfully created shop '{}' (slug: {}) for user {} (ID: {})", 
                shop.getShopName(), shop.getShopSlug(), email, userId);

        } catch (Exception e) {
            log.error("Error seeding shop data for user {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Generate URL-friendly slug from Vietnamese text
     */
    private String generateSlug(String text) {
        if (text == null || text.isBlank()) {
            return "shop-" + System.currentTimeMillis();
        }
        
        // Normalize Vietnamese characters
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        // Remove diacritics
        String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Convert to lowercase
        String lowercased = withoutDiacritics.toLowerCase(Locale.ROOT);
        // Replace spaces and special chars with hyphens
        String slug = lowercased.replaceAll("[^a-z0-9]+", "-");
        // Remove leading/trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");
        // Limit length
        if (slug.length() > 100) {
            slug = slug.substring(0, 100);
        }
        
        return slug.isEmpty() ? "shop-" + System.currentTimeMillis() : slug;
    }
}
