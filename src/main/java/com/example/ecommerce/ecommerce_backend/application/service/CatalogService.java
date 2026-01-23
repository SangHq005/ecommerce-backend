package com.example.ecommerce.ecommerce_backend.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailsResponse;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeGroupEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionGroupEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OptionValueEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductAttributeValueEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.BrandJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CategoryJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OptionGroupJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OptionValueJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductAttributeValueJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductImageJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.shared.util.CatalogSlugUtil;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final CategoryJpaRepository categoryRepo;
    private final BrandJpaRepository brandRepo;
    private final ProductJpaRepository productRepo;
    private final ProductImageJpaRepository imageRepo;
    private final OptionGroupJpaRepository ogRepo;
    private final OptionValueJpaRepository ovRepo;
    private final SkuJpaRepository skuRepo;
    private final SellerShopJpaRepository shopRepo; // from Module 2
    private final ProductAttributeValueJpaRepository attrValueRepo;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;
    private final NotificationService notificationService;

    public CatalogService(
            CategoryJpaRepository categoryRepo,
            BrandJpaRepository brandRepo,
            ProductJpaRepository productRepo,
            ProductImageJpaRepository imageRepo,
            OptionGroupJpaRepository ogRepo,
            OptionValueJpaRepository ovRepo,
            SkuJpaRepository skuRepo,
            SellerShopJpaRepository shopRepo,
            ProductAttributeValueJpaRepository attrValueRepo,
            StringRedisTemplate redis,
            EventLogMongoRepository eventRepo,
            NotificationService notificationService
    ) {
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.productRepo = productRepo;
        this.imageRepo = imageRepo;
        this.ogRepo = ogRepo;
        this.ovRepo = ovRepo;
        this.skuRepo = skuRepo;
        this.shopRepo = shopRepo;
        this.attrValueRepo = attrValueRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
        this.notificationService = notificationService;
    }

    // PUBLIC 
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryEntity> listCategories() { 
        return categoryRepo.findByActiveTrueOrderBySortOrderAscIdAsc(); 
    }
    
    @Cacheable(value = "brands", key = "'all'")
    public List<BrandEntity> listBrands() { 
        return brandRepo.findByActiveTrueOrderByIdAsc(); 
    }

    public Page<ProductEntity> listActiveProducts(Long categoryId, Long brandId, Pageable pageable) {
        if (categoryId != null && brandId != null) {
            return productRepo.findByStatusAndCategoryIdAndBrandId("ACTIVE", categoryId, brandId, pageable);
        }
        return productRepo.findByStatus("ACTIVE", pageable);
    }

    public List<ProductEntity> getTopDeals(int limit) {
        return productRepo.findTopDeals(Pageable.ofSize(limit));
    }

    public ProductEntity getActiveProduct(Long id) {
        ProductEntity p = productRepo.findById(id)
                .orElseThrow(() -> new com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException(id));
        if (!"ACTIVE".equals(p.getStatus())) throw new IllegalArgumentException("Product not active");
        return p;
    }

    public ProductDetailsResponse getProductDetail(Long id) {
        ProductEntity p = getActiveProduct(id);
        return buildProductDetailsResponse(p);
    }
    
    public ProductDetailsResponse getProductDetailByIdOrSlug(String idOrSlug) {
        ProductEntity p;
        try {
            // Try to parse as ID first
            Long productId = Long.parseLong(idOrSlug);
            p = getActiveProduct(productId);
        } catch (NumberFormatException e) {
            // It's a slug, find by slug
            p = productRepo.findBySlug(idOrSlug)
                    .orElseThrow(() -> new com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException(idOrSlug));
            if (!"ACTIVE".equals(p.getStatus())) {
                throw new IllegalArgumentException("Product not active");
            }
        }
        return buildProductDetailsResponse(p);
    }
    
    private ProductDetailsResponse buildProductDetailsResponse(ProductEntity p) {
        Long id = p.getId();
        List<ProductImageEntity> images = imageRepo.findByProductIdOrderBySortOrderAsc(id);
        List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(id);
        
        List<OptionGroupEntity> groups = ogRepo.findByProductIdOrderBySortOrderAscIdAsc(id);
        
        // OPTIMIZATION: Fetch all option values in one query instead of N queries
        List<Long> groupIds = groups.stream()
                .map(OptionGroupEntity::getId)
                .collect(Collectors.toList());
        
        List<OptionValueEntity> allOptionValues = groupIds.isEmpty() 
                ? List.of() 
                : ovRepo.findByOptionGroupIdsOrderByGroupIdSortOrderAscIdAsc(groupIds);
        
        // Group option values by optionGroupId for efficient lookup
        java.util.Map<Long, List<OptionValueEntity>> valuesByGroup = allOptionValues.stream()
                .collect(Collectors.groupingBy(OptionValueEntity::getOptionGroupId));
        
        List<ProductDetailsResponse.OptionGroupDetails> options = groups.stream()
                .map(g -> new ProductDetailsResponse.OptionGroupDetails(
                        g, 
                        valuesByGroup.getOrDefault(g.getId(), List.of())
                ))
                .toList();
            
        SellerShopEntity shop = shopRepo.findById(p.getShopId()).orElse(null);
        
        // Fetch and group product attributes
        List<ProductAttributeValueEntity> attributeValues = attrValueRepo.findByProductIdWithAttributeAndGroup(id);
        List<ProductDetailsResponse.AttributeGroupDetails> attributes = groupAttributesByGroup(attributeValues);
        
        return new ProductDetailsResponse(p, images, skus, options, shop, attributes);
    }

    public ProductDetailsResponse sellerGetProductDetail(Long sellerUserId, Long productId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        List<ProductImageEntity> images = imageRepo.findByProductIdOrderBySortOrderAsc(productId);
        List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(productId);
        
        List<OptionGroupEntity> groups = ogRepo.findByProductIdOrderBySortOrderAscIdAsc(productId);
        
        // OPTIMIZATION: Fetch all option values in one query instead of N queries
        List<Long> groupIds = groups.stream()
                .map(OptionGroupEntity::getId)
                .collect(Collectors.toList());
        
        List<OptionValueEntity> allOptionValues = groupIds.isEmpty() 
                ? List.of() 
                : ovRepo.findByOptionGroupIdsOrderByGroupIdSortOrderAscIdAsc(groupIds);
        
        // Group option values by optionGroupId for efficient lookup
        java.util.Map<Long, List<OptionValueEntity>> valuesByGroup = allOptionValues.stream()
                .collect(Collectors.groupingBy(OptionValueEntity::getOptionGroupId));
        
        List<ProductDetailsResponse.OptionGroupDetails> options = groups.stream()
                .map(g -> new ProductDetailsResponse.OptionGroupDetails(
                        g, 
                        valuesByGroup.getOrDefault(g.getId(), List.of())
                ))
                .toList();
            
        SellerShopEntity shop = shopRepo.findById(p.getShopId()).orElse(null);
        
        // Fetch and group product attributes
        List<ProductAttributeValueEntity> attributeValues = attrValueRepo.findByProductIdWithAttributeAndGroup(productId);
        List<ProductDetailsResponse.AttributeGroupDetails> attributes = groupAttributesByGroup(attributeValues);
        
        return new ProductDetailsResponse(p, images, skus, options, shop, attributes);
    }

    // SELLER 
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
                // policy: khi ACTIVE chỉ cho đổi mô tả và main image
                p.setDescription(desc);
                p.setMainImageUrl(mainImageUrl);
                if (price != null) p.setPrice(price); // Allow price update even when active
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

        imageRepo.delete(img);
        invalidate(p.getId(), p.getShopId());
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
        if (!"DRAFT".equals(p.getStatus()) && !"PENDING_REVIEW".equals(p.getStatus()) && !"ACTIVE".equals(p.getStatus()))
            throw new IllegalArgumentException("SKUs editable only in DRAFT/PENDING_REVIEW/ACTIVE");

        for (SkuSpec s : skus) {
            if (s.optionSignature() == null) {
                // safeguard
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

    public record SkuSpec(String skuCode, String optionSignature, long price, Long compareAtPrice, int stockOnHand, boolean active, String imageUrl) {}

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
        
        // Notify all admins about new product submission
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
            // Log error but don't fail the submission
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

    public List<ProductEntity> sellerListProducts(Long sellerUserId) {
        return productRepo.findBySellerUserIdOrderByIdDesc(sellerUserId);
    }

    // ADMIN
    @Transactional(readOnly = true)
    public List<ProductEntity> adminListByStatus(String status) {
        return productRepo.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> adminListProducts(String status, String search, Pageable pageable) {
        if (status != null && !status.isBlank() && search != null && !search.isBlank()) {
            // Both status and search
            return productRepo.findByStatusAndNameContainingIgnoreCase(status, search.trim(), pageable);
        } else if (status != null && !status.isBlank()) {
            // Only status
            return productRepo.findByStatus(status, pageable);
        } else if (search != null && !search.isBlank()) {
            // Only search
            return productRepo.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            // No filters - return all
            return productRepo.findAll(pageable);
        }
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
        // Allow rejecting if not already active or explicitly needed
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
    
    /**
     * Group product attribute values by their attribute groups.
     */
    private List<ProductDetailsResponse.AttributeGroupDetails> groupAttributesByGroup(
            List<ProductAttributeValueEntity> attributeValues) {
        if (attributeValues == null || attributeValues.isEmpty()) {
            return List.of();
        }
        
        // Group by attribute group
        Map<AttributeGroupEntity, List<ProductAttributeValueEntity>> grouped = attributeValues.stream()
                .collect(Collectors.groupingBy(
                    pav -> pav.getAttribute().getAttributeGroup(),
                    Collectors.toList()
                ));
        
        // Convert to AttributeGroupDetails, maintaining sort order
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((ag1, ag2) -> 
                    Integer.compare(ag1.getSortOrder(), ag2.getSortOrder())))
                .map(entry -> {
                    AttributeGroupEntity group = entry.getKey();
                    List<ProductDetailsResponse.AttributeValueDetails> attributeDetails = entry.getValue().stream()
                            .sorted((pav1, pav2) -> Integer.compare(
                                pav1.getAttribute().getSortOrder(),
                                pav2.getAttribute().getSortOrder()))
                            .map(pav -> new ProductDetailsResponse.AttributeValueDetails(
                                pav.getAttribute(),
                                pav
                            ))
                            .collect(Collectors.toList());
                    return new ProductDetailsResponse.AttributeGroupDetails(group, attributeDetails);
                })
                .collect(Collectors.toList());
    }
}
