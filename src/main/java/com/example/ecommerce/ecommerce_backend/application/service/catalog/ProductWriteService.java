package com.example.ecommerce.ecommerce_backend.application.service.catalog;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.config.CacheConfig;
import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;
import com.example.ecommerce.ecommerce_backend.application.service.storage.ImageUploadService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionGroupEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionValueEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OptionGroupJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OptionValueJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductImageJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.shared.util.CatalogSlugUtil;

@Service
public class ProductWriteService {

    private static final Logger log = LoggerFactory.getLogger(ProductWriteService.class);

    private final ProductJpaRepository productRepo;
    private final ProductImageJpaRepository imageRepo;
    private final OptionGroupJpaRepository ogRepo;
    private final OptionValueJpaRepository ovRepo;
    private final SkuJpaRepository skuRepo;
    private final SellerShopJpaRepository shopRepo;
    private final StringRedisTemplate redis;
    private final CacheManager cacheManager;
    private final EventLogMongoRepository eventRepo;
    private final NotificationService notificationService;
    private final ImageUploadService imageUploadService;

    public ProductWriteService(
            ProductJpaRepository productRepo,
            ProductImageJpaRepository imageRepo,
            OptionGroupJpaRepository ogRepo,
            OptionValueJpaRepository ovRepo,
            SkuJpaRepository skuRepo,
            SellerShopJpaRepository shopRepo,
            StringRedisTemplate redis,
            CacheManager cacheManager,
            EventLogMongoRepository eventRepo,
            NotificationService notificationService,
            ImageUploadService imageUploadService
    ) {
        this.productRepo = productRepo;
        this.imageRepo = imageRepo;
        this.ogRepo = ogRepo;
        this.ovRepo = ovRepo;
        this.skuRepo = skuRepo;
        this.shopRepo = shopRepo;
        this.redis = redis;
        this.cacheManager = cacheManager;
        this.eventRepo = eventRepo;
        this.notificationService = notificationService;
        this.imageUploadService = imageUploadService;
    }

    @Transactional
    public ProductEntity sellerCreateDraft(Long sellerUserId, Long categoryId, Long brandId, String name, String desc, String mainImageUrl, Long price, Long originalPrice, Integer stockQuantity) {
        Long shopId = shopRepo.findBySellerUserId(sellerUserId).orElseThrow().getId();

        ProductEntity p = new ProductEntity();
        p.setSellerUserId(sellerUserId);
        p.setShopId(shopId);
        p.setCategoryId(categoryId);
        p.setBrandId(brandId);
        p.setName(name);
        p.setSlug(CatalogSlugUtil.slugify(name));
        p.setDescription(desc);
        p.setStatus("DRAFT");
        p.setMainImageUrl(mainImageUrl);
        p.setPrice(price != null ? price : 0L);
        p.setOriginalPrice(originalPrice);
        p.setStockQuantity(stockQuantity != null ? stockQuantity : 0);

        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), shopId);

        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_CREATED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "sellerUserId", sellerUserId, "shopId", shopId)));

        return saved;
    }

    @Transactional
    public ProductEntity sellerUpdate(Long sellerUserId, Long productId, Long categoryId, Long brandId, String name, String desc, String mainImageUrl, Long price, Long originalPrice, Integer stockQuantity) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();

        switch (p.getStatus()) {
            case "DRAFT", "PENDING_REVIEW" -> {
                if (name != null && !name.isBlank()) {
                    p.setName(name);
                    p.setSlug(CatalogSlugUtil.slugify(name));
                }
                if (categoryId != null) p.setCategoryId(categoryId);
                if (brandId != null) p.setBrandId(brandId);
                p.setDescription(desc);
                p.setMainImageUrl(mainImageUrl);
                if (price != null) p.setPrice(price);
                p.setOriginalPrice(originalPrice);
                if (stockQuantity != null) p.setStockQuantity(stockQuantity);
            }
            case "ACTIVE" -> {
                p.setDescription(desc);
                p.setMainImageUrl(mainImageUrl);
                if (price != null) p.setPrice(price);
                p.setOriginalPrice(originalPrice);
                if (stockQuantity != null) p.setStockQuantity(stockQuantity);
            }
            default -> throw new IllegalArgumentException("Not editable in status=" + p.getStatus());
        }

        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());
        return saved;
    }

    @Transactional
    public ProductImageEntity sellerUpsertImage(Long sellerUserId, Long productId, int sortOrder, String imageUrl) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        ProductImageEntity img = imageRepo.findByProductIdAndSortOrder(productId, sortOrder).orElseGet(ProductImageEntity::new);
        img.setProductId(productId);
        img.setSortOrder(sortOrder);
        img.setImageUrl(imageUrl);
        ProductImageEntity saved = imageRepo.save(img);

        invalidate(p.getId(), p.getShopId());
        return saved;
    }

    @Transactional
    public void sellerDeleteImage(Long sellerUserId, Long productId, Long imageId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        ProductImageEntity img = imageRepo.findById(imageId).orElseThrow();
        
        if (!img.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to product");
        }

        imageUploadService.deleteImage(img.getImageUrl());
        imageRepo.delete(img);
        invalidate(p.getId(), p.getShopId());
    }

    @Transactional
    public void sellerSetOptions(Long sellerUserId, Long productId, List<CatalogFacade.OptionGroupSpec> groups) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus()) && !"PENDING_REVIEW".equals(p.getStatus()))
            throw new IllegalArgumentException("Options editable only in DRAFT/PENDING_REVIEW");

        List<OptionGroupEntity> existing = ogRepo.findByProductIdOrderBySortOrderAscIdAsc(productId);
        for (OptionGroupEntity g : existing) {
            ovRepo.deleteByOptionGroupId(g.getId());
        }
        ogRepo.deleteByProductId(productId);

        for (CatalogFacade.OptionGroupSpec gs : groups) {
            OptionGroupEntity g = new OptionGroupEntity();
            g.setProductId(productId);
            g.setName(gs.name());
            g.setSortOrder(gs.sortOrder());
            g = ogRepo.save(g);

            int idx = 0;
            for (String v : gs.values()) {
                OptionValueEntity ov = new OptionValueEntity();
                ov.setOptionGroupId(g.getId());
                ov.setValue(v);
                ov.setSortOrder(idx++);
                ovRepo.save(ov);
            }
        }

        invalidate(p.getId(), p.getShopId());
    }

    @Transactional
    public List<SkuEntity> sellerUpsertSkus(Long sellerUserId, Long productId, List<CatalogFacade.SkuSpec> skus) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus()) && !"PENDING_REVIEW".equals(p.getStatus()) && !"ACTIVE".equals(p.getStatus()))
            throw new IllegalArgumentException("SKUs editable only in DRAFT/PENDING_REVIEW/ACTIVE");

        for (CatalogFacade.SkuSpec s : skus) {
            if (s.optionSignature() == null) {
                throw new IllegalArgumentException("optionSignature cannot be null");
            }
            String sigHash = sha256Hex(s.optionSignature());
            
            SkuEntity sku = skuRepo.findByProductIdAndSkuCode(productId, s.skuCode())
                    .orElseGet(() -> skuRepo.findByProductIdAndOptionSignatureHash(productId, sigHash).orElseGet(SkuEntity::new));

            sku.setProductId(productId);
            sku.setSkuCode(s.skuCode());
            sku.setOptionSignature(s.optionSignature());
            sku.setOptionSignatureHash(sigHash);
            sku.setPrice(s.price());
            sku.setCompareAtPrice(s.compareAtPrice());
            sku.setStockOnHand(s.stockOnHand());
            sku.setActive(s.active());
            sku.setImageUrl(s.imageUrl());
            skuRepo.save(sku);

            eventRepo.save(new EventLogDocument("CATALOG_SKU_UPSERTED", "product_" + productId, Instant.now(), null,
                    Map.of("productId", productId, "skuCode", s.skuCode(), "sig", s.optionSignature())));
        }

        invalidate(p.getId(), p.getShopId());
        return skuRepo.findByProductIdOrderByIdAsc(productId);
    }

    @Transactional
    public ProductEntity sellerSubmit(Long sellerUserId, Long productId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new IllegalArgumentException("Only DRAFT can submit");

        if (p.getMainImageUrl() == null || p.getMainImageUrl().isBlank())
            throw new IllegalArgumentException("mainImageUrl required");
        if (skuRepo.findByProductIdOrderByIdAsc(productId).isEmpty())
            throw new IllegalArgumentException("At least 1 SKU required");

        p.setStatus("PENDING_REVIEW");
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());

        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_SUBMITTED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "status", "PENDING_REVIEW")));
        
        try {
            log.info("Notifying admins about product submission: productId={}, name={}, shopId={}", 
                    saved.getId(), saved.getName(), saved.getShopId());
            notificationService.notifyAdminsNewProduct(
                    saved.getId(),
                    saved.getName(),
                    saved.getShopId()
            );
            log.info("Successfully initiated notification for product submission: productId={}", saved.getId());
        } catch (Exception e) {
            log.error("Failed to notify admins about product submission {}: {}", saved.getId(), e.getMessage(), e);
        }
        
        return saved;
    }

    @Transactional
    public ProductEntity sellerDeactivate(Long sellerUserId, Long productId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"ACTIVE".equals(p.getStatus())) throw new IllegalArgumentException("Only ACTIVE can deactivate");
        p.setStatus("INACTIVE");
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());
        return saved;
    }

    @Transactional
    public ProductEntity sellerHide(Long sellerUserId, Long productId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"ACTIVE".equals(p.getStatus())) throw new IllegalArgumentException("Only ACTIVE products can be hidden");
        p.setStatus("HIDDEN");
        p.setHiddenAt(Instant.now());
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());
        
        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_STATUS_CHANGED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "status", "HIDDEN", "action", "SELLER_HIDE")));
        return saved;
    }

    @Transactional
    public ProductEntity sellerShow(Long sellerUserId, Long productId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"HIDDEN".equals(p.getStatus())) throw new IllegalArgumentException("Only HIDDEN products can be shown");
        p.setStatus("ACTIVE");
        p.setHiddenAt(null);
        if (p.getPublishedAt() == null) {
            p.setPublishedAt(Instant.now());
        }
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());
        
        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_STATUS_CHANGED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "status", "ACTIVE", "action", "SELLER_SHOW")));
        return saved;
    }

    @Transactional
    public ProductEntity adminApprove(Long productId) {
        ProductEntity p = productRepo.findById(productId).orElseThrow();
        if (!"PENDING_REVIEW".equals(p.getStatus())) throw new IllegalArgumentException("Only PENDING_REVIEW can approve");
        p.setStatus("ACTIVE");
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());

        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_STATUS_CHANGED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "status", "ACTIVE")));
        return saved;
    }

    @Transactional
    public ProductEntity adminReject(Long productId, String reason) {
        ProductEntity p = productRepo.findById(productId).orElseThrow();
        if ("ACTIVE".equals(p.getStatus())) throw new IllegalArgumentException("Cannot reject already ACTIVE product. Hide instead.");

        p.setStatus("REJECTED");
        p.setActionReason(reason);
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());

        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_STATUS_CHANGED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "status", "REJECTED", "reason", reason)));
        return saved;
    }

    @Transactional
    public ProductEntity adminHide(Long productId, String reason) {
        ProductEntity p = productRepo.findById(productId).orElseThrow();
        p.setStatus("HIDDEN");
        p.setActionReason(reason);
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());

        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_STATUS_CHANGED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "status", "HIDDEN", "reason", reason)));
        return saved;
    }

    private void invalidate(Long productId, Long shopId) {
        var cache = cacheManager.getCache(CacheConfig.CACHE_PRODUCT_DETAILS);
        if (cache != null) {
            cache.evict(productId);
        }
        redis.opsForValue().increment("cache:catalog:ver");
        redis.delete("cache:catalog:product:" + productId);
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Hash error");
        }
    }
}
