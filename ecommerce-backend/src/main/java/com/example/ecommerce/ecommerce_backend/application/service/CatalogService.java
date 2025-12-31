package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.*;
import com.example.ecommerce.ecommerce_backend.shared.util.CatalogSlugUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class CatalogService {

    private final CategoryJpaRepository categoryRepo;
    private final BrandJpaRepository brandRepo;
    private final ProductJpaRepository productRepo;
    private final ProductImageJpaRepository imageRepo;
    private final OptionGroupJpaRepository ogRepo;
    private final OptionValueJpaRepository ovRepo;
    private final SkuJpaRepository skuRepo;
    private final SellerShopJpaRepository shopRepo; // from Module 2
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;

    public CatalogService(
            CategoryJpaRepository categoryRepo,
            BrandJpaRepository brandRepo,
            ProductJpaRepository productRepo,
            ProductImageJpaRepository imageRepo,
            OptionGroupJpaRepository ogRepo,
            OptionValueJpaRepository ovRepo,
            SkuJpaRepository skuRepo,
            SellerShopJpaRepository shopRepo,
            StringRedisTemplate redis,
            EventLogMongoRepository eventRepo
    ) {
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.productRepo = productRepo;
        this.imageRepo = imageRepo;
        this.ogRepo = ogRepo;
        this.ovRepo = ovRepo;
        this.skuRepo = skuRepo;
        this.shopRepo = shopRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
    }

    // ---------- PUBLIC ----------
    public List<CategoryEntity> listCategories() { return categoryRepo.findByActiveTrueOrderBySortOrderAscIdAsc(); }
    public List<BrandEntity> listBrands() { return brandRepo.findByActiveTrueOrderByIdAsc(); }

    public Page<ProductEntity> listActiveProducts(Long categoryId, Long brandId, Pageable pageable) {
        if (categoryId != null && brandId != null) {
            return productRepo.findByStatusAndCategoryIdAndBrandId("ACTIVE", categoryId, brandId, pageable);
        }
        return productRepo.findByStatus("ACTIVE", pageable);
    }

    public ProductEntity getActiveProduct(Long id) {
        ProductEntity p = productRepo.findById(id).orElseThrow();
        if (!"ACTIVE".equals(p.getStatus())) throw new IllegalArgumentException("Product not active");
        return p;
    }

    // ---------- SELLER ----------
    @Transactional
    public ProductEntity sellerCreateDraft(Long sellerUserId, Long categoryId, Long brandId, String name, String desc, String mainImageUrl) {
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

        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), shopId);

        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_CREATED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "sellerUserId", sellerUserId, "shopId", shopId)));

        return saved;
    }

    @Transactional
    public ProductEntity sellerUpdate(Long sellerUserId, Long productId, Long categoryId, Long brandId, String name, String desc, String mainImageUrl) {
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
            }
            case "ACTIVE" -> {
                // policy: khi ACTIVE chỉ cho đổi mô tả và main image
                p.setDescription(desc);
                p.setMainImageUrl(mainImageUrl);
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
    public void sellerSetOptions(Long sellerUserId, Long productId, List<OptionGroupSpec> groups) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus()) && !"PENDING_REVIEW".equals(p.getStatus()))
            throw new IllegalArgumentException("Options editable only in DRAFT/PENDING_REVIEW");

        // replace strategy: delete all then insert (safe for module)
        List<OptionGroupEntity> existing = ogRepo.findByProductIdOrderBySortOrderAscIdAsc(productId);
        for (OptionGroupEntity g : existing) {
            ovRepo.deleteByOptionGroupId(g.getId());
        }
        ogRepo.deleteByProductId(productId);

        for (OptionGroupSpec gs : groups) {
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

    public record OptionGroupSpec(String name, int sortOrder, List<String> values) {}

    @Transactional
    public List<SkuEntity> sellerUpsertSkus(Long sellerUserId, Long productId, List<SkuSpec> skus) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus()) && !"PENDING_REVIEW".equals(p.getStatus()))
            throw new IllegalArgumentException("SKUs editable only in DRAFT/PENDING_REVIEW");

        for (SkuSpec s : skus) {
            String sigHash = sha256Hex(s.optionSignature());
            SkuEntity sku = skuRepo.findByProductIdAndOptionSignatureHash(productId, sigHash).orElseGet(SkuEntity::new);
            sku.setProductId(productId);
            sku.setSkuCode(s.skuCode());
            sku.setOptionSignature(s.optionSignature());
            sku.setOptionSignatureHash(sigHash);
            sku.setPrice(s.price());
            sku.setCompareAtPrice(s.compareAtPrice());
            sku.setStockOnHand(s.stockOnHand());
            sku.setActive(s.active());
            skuRepo.save(sku);

            eventRepo.save(new EventLogDocument("CATALOG_SKU_UPSERTED", "product_" + productId, Instant.now(), null,
                    Map.of("productId", productId, "skuCode", s.skuCode(), "sig", s.optionSignature())));
        }

        invalidate(p.getId(), p.getShopId());
        return skuRepo.findByProductIdOrderByIdAsc(productId);
    }

    public record SkuSpec(String skuCode, String optionSignature, long price, Long compareAtPrice, int stockOnHand, boolean active) {}

    @Transactional
    public ProductEntity sellerSubmit(Long sellerUserId, Long productId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        if (!"DRAFT".equals(p.getStatus())) throw new IllegalArgumentException("Only DRAFT can submit");

        // Minimum completeness checks
        if (p.getMainImageUrl() == null || p.getMainImageUrl().isBlank())
            throw new IllegalArgumentException("mainImageUrl required");
        if (skuRepo.findByProductIdOrderByIdAsc(productId).isEmpty())
            throw new IllegalArgumentException("At least 1 SKU required");

        p.setStatus("PENDING_REVIEW");
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());

        eventRepo.save(new EventLogDocument("CATALOG_PRODUCT_SUBMITTED", "product_" + saved.getId(), Instant.now(), null,
                Map.of("productId", saved.getId(), "status", "PENDING_REVIEW")));
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

    // ---------- ADMIN ----------
    public List<ProductEntity> adminListByStatus(String status) {
        return productRepo.findByStatus(status);
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
    public ProductEntity adminRejectToDraft(Long productId) {
        ProductEntity p = productRepo.findById(productId).orElseThrow();
        if (!"PENDING_REVIEW".equals(p.getStatus())) throw new IllegalArgumentException("Only PENDING_REVIEW can reject");
        p.setStatus("DRAFT");
        ProductEntity saved = productRepo.save(p);
        invalidate(saved.getId(), saved.getShopId());
        return saved;
    }

    private void invalidate(Long productId, Long shopId) {
        redis.delete("cache:catalog:product:" + productId);
        // listing keys vary; simplest is not attempting wildcard delete in Redis here
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
