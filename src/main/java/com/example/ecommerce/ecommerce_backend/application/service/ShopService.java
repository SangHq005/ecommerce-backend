package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class ShopService {

    private final SellerShopJpaRepository shopRepo;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;

    public ShopService(SellerShopJpaRepository shopRepo, StringRedisTemplate redis, EventLogMongoRepository eventRepo) {
        this.shopRepo = shopRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
    }

    public Optional<SellerShopEntity> getBySeller(Long sellerUserId) {
        return shopRepo.findBySellerUserId(sellerUserId);
    }

    @Transactional
    public SellerShopEntity createDraft(Long sellerUserId, String shopName, String description) {
        // Idempotent by UNIQUE seller_user_id: if exists, return existing
        return shopRepo.findBySellerUserId(sellerUserId).orElseGet(() -> {
            SellerShopEntity s = new SellerShopEntity();
            s.setSellerUserId(sellerUserId);
            s.setShopName(shopName);
            s.setShopSlug(uniqueSlug(shopName));
            s.setDescription(description);
            s.setStatus("DRAFT");
            SellerShopEntity saved = shopRepo.save(s);

            invalidate(sellerUserId, saved.getId());
            eventRepo.save(new EventLogDocument("SHOP_CREATED", "shop_" + saved.getId(), Instant.now(), null,
                    Map.of("shopId", saved.getId(), "sellerUserId", sellerUserId, "status", "DRAFT")));
            return saved;
        });
    }

    @Transactional
    public SellerShopEntity updateShop(Long sellerUserId, String shopName, String description) {
        SellerShopEntity s = shopRepo.findBySellerUserId(sellerUserId).orElseThrow();

        // State rules
        switch (s.getStatus()) {
            case "DRAFT", "PENDING_REVIEW" -> {
                s.setShopName(shopName);
                s.setDescription(description);
            }
            case "ACTIVE" -> {
                // In ACTIVE, allow only description changes (policy choice)
                s.setDescription(description);
            }
            default -> throw new IllegalArgumentException("Shop not editable in status=" + s.getStatus());
        }

        SellerShopEntity saved = shopRepo.save(s);
        invalidate(sellerUserId, saved.getId());
        return saved;
    }

    @Transactional
    public SellerShopEntity submitForReview(Long sellerUserId) {
        SellerShopEntity s = shopRepo.findBySellerUserId(sellerUserId).orElseThrow();
        if (!"DRAFT".equals(s.getStatus())) throw new IllegalArgumentException("Only DRAFT can submit");
        s.setStatus("PENDING_REVIEW");
        SellerShopEntity saved = shopRepo.save(s);

        invalidate(sellerUserId, saved.getId());
        eventRepo.save(new EventLogDocument("SHOP_SUBMITTED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "sellerUserId", sellerUserId, "status", "PENDING_REVIEW")));
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
