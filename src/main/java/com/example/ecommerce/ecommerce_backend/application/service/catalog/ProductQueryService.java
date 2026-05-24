package com.example.ecommerce.ecommerce_backend.application.service.catalog;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.config.CacheConfig;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailsResponse;
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

@Service
public class ProductQueryService {

    private final CategoryJpaRepository categoryRepo;
    private final BrandJpaRepository brandRepo;
    private final ProductJpaRepository productRepo;
    private final ProductImageJpaRepository imageRepo;
    private final OptionGroupJpaRepository ogRepo;
    private final OptionValueJpaRepository ovRepo;
    private final SkuJpaRepository skuRepo;
    private final SellerShopJpaRepository shopRepo;
    private final ProductAttributeValueJpaRepository attrValueRepo;
    private final ProductQueryService self;

    public ProductQueryService(
            CategoryJpaRepository categoryRepo,
            BrandJpaRepository brandRepo,
            ProductJpaRepository productRepo,
            ProductImageJpaRepository imageRepo,
            OptionGroupJpaRepository ogRepo,
            OptionValueJpaRepository ovRepo,
            SkuJpaRepository skuRepo,
            SellerShopJpaRepository shopRepo,
            ProductAttributeValueJpaRepository attrValueRepo,
            @Lazy ProductQueryService self
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
        this.self = self;
    }

    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryEntity> listCategories() { 
        return categoryRepo.findByActiveTrueOrderBySortOrderAscIdAsc(); 
    }
    
    @Cacheable(value = "brands", key = "'all'")
    public List<BrandEntity> listBrands() { 
        return brandRepo.findByActiveTrueOrderByIdAsc(); 
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> listActiveProducts(Long categoryId, Long brandId, Pageable pageable) {
        if (categoryId != null && brandId != null) {
            return productRepo.findByStatusAndCategoryIdAndBrandId("ACTIVE", categoryId, brandId, pageable);
        }
        return productRepo.findByStatus("ACTIVE", pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> getTopDeals(int limit) {
        return productRepo.findTopDeals(Pageable.ofSize(limit));
    }

    @Transactional(readOnly = true)
    public ProductEntity getActiveProduct(Long id) {
        ProductEntity p = productRepo.findById(id)
                .orElseThrow(() -> new com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException(id));
        if (!"ACTIVE".equals(p.getStatus())) throw new IllegalArgumentException("Product not active");
        return p;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCT_DETAILS, key = "#id")
    public ProductDetailsResponse getProductDetail(Long id) {
        ProductEntity p = getActiveProduct(id);
        return buildProductDetailsResponse(p);
    }
    
    @Transactional(readOnly = true)
    public ProductDetailsResponse adminGetProductDetail(Long id) {
        ProductEntity p = productRepo.findById(id)
                .orElseThrow(() -> new com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException(id));
        return buildProductDetailsResponse(p);
    }
    
    @Transactional(readOnly = true)
    public ProductDetailsResponse getProductDetailByIdOrSlug(String idOrSlug) {
        Long productId = resolveActiveProductId(idOrSlug);
        return self.getProductDetail(productId);
    }

    private Long resolveActiveProductId(String idOrSlug) {
        try {
            return getActiveProduct(Long.parseLong(idOrSlug)).getId();
        } catch (NumberFormatException e) {
            ProductEntity p = productRepo.findBySlug(idOrSlug)
                    .orElseThrow(() -> new com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException(idOrSlug));
            if (!"ACTIVE".equals(p.getStatus())) {
                throw new IllegalArgumentException("Product not active");
            }
            return p.getId();
        }
    }
    
    private ProductDetailsResponse buildProductDetailsResponse(ProductEntity p) {
        Long id = p.getId();
        List<ProductImageEntity> images = imageRepo.findByProductIdOrderBySortOrderAsc(id);
        List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(id);
        
        List<OptionGroupEntity> groups = ogRepo.findByProductIdOrderBySortOrderAscIdAsc(id);
        
        List<Long> groupIds = groups.stream()
                .map(OptionGroupEntity::getId)
                .collect(Collectors.toList());
        
        List<OptionValueEntity> allOptionValues = groupIds.isEmpty() 
                ? List.of() 
                : ovRepo.findByOptionGroupIdsOrderByGroupIdSortOrderAscIdAsc(groupIds);
        
        Map<Long, List<OptionValueEntity>> valuesByGroup = allOptionValues.stream()
                .collect(Collectors.groupingBy(OptionValueEntity::getOptionGroupId));
        
        List<ProductDetailsResponse.OptionGroupDetails> options = groups.stream()
                .map(g -> new ProductDetailsResponse.OptionGroupDetails(
                        g, 
                        valuesByGroup.getOrDefault(g.getId(), List.of())
                ))
                .toList();
            
        SellerShopEntity shop = shopRepo.findById(p.getShopId()).orElse(null);
        
        List<ProductAttributeValueEntity> attributeValues = attrValueRepo.findByProductIdWithAttributeAndGroup(id);
        List<ProductDetailsResponse.AttributeGroupDetails> attributes = groupAttributesByGroup(attributeValues);
        
        return new ProductDetailsResponse(p, images, skus, options, shop, attributes);
    }

    @Transactional(readOnly = true)
    public ProductDetailsResponse sellerGetProductDetail(Long sellerUserId, Long productId) {
        ProductEntity p = productRepo.findByIdAndSellerUserId(productId, sellerUserId).orElseThrow();
        List<ProductImageEntity> images = imageRepo.findByProductIdOrderBySortOrderAsc(productId);
        List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(productId);
        
        List<OptionGroupEntity> groups = ogRepo.findByProductIdOrderBySortOrderAscIdAsc(productId);
        
        List<Long> groupIds = groups.stream()
                .map(OptionGroupEntity::getId)
                .collect(Collectors.toList());
        
        List<OptionValueEntity> allOptionValues = groupIds.isEmpty() 
                ? List.of() 
                : ovRepo.findByOptionGroupIdsOrderByGroupIdSortOrderAscIdAsc(groupIds);
        
        Map<Long, List<OptionValueEntity>> valuesByGroup = allOptionValues.stream()
                .collect(Collectors.groupingBy(OptionValueEntity::getOptionGroupId));
        
        List<ProductDetailsResponse.OptionGroupDetails> options = groups.stream()
                .map(g -> new ProductDetailsResponse.OptionGroupDetails(
                        g, 
                        valuesByGroup.getOrDefault(g.getId(), List.of())
                ))
                .toList();
            
        SellerShopEntity shop = shopRepo.findById(p.getShopId()).orElse(null);
        
        List<ProductAttributeValueEntity> attributeValues = attrValueRepo.findByProductIdWithAttributeAndGroup(productId);
        List<ProductDetailsResponse.AttributeGroupDetails> attributes = groupAttributesByGroup(attributeValues);
        
        return new ProductDetailsResponse(p, images, skus, options, shop, attributes);
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> sellerListProducts(Long sellerUserId, String status, Pageable pageable) {
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            return productRepo.findBySellerUserIdAndStatusOrderByIdDesc(sellerUserId, status, pageable);
        }
        return productRepo.findBySellerUserIdOrderByIdDesc(sellerUserId, pageable);
    }

    /** @deprecated Use {@link #sellerListProducts(Long, String, Pageable)} */
    @Deprecated
    @Transactional(readOnly = true)
    public List<ProductEntity> sellerListProducts(Long sellerUserId) {
        return productRepo.findBySellerUserIdOrderByIdDesc(sellerUserId);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> adminListByStatus(String status) {
        return productRepo.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> adminListProducts(String status, String search, Pageable pageable) {
        if (status != null && !status.isBlank() && search != null && !search.isBlank()) {
            return productRepo.findByStatusAndNameContainingIgnoreCase(status, search.trim(), pageable);
        } else if (status != null && !status.isBlank()) {
            return productRepo.findByStatus(status, pageable);
        } else if (search != null && !search.isBlank()) {
            return productRepo.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            return productRepo.findAll(pageable);
        }
    }

    private List<ProductDetailsResponse.AttributeGroupDetails> groupAttributesByGroup(
            List<ProductAttributeValueEntity> attributeValues) {
        if (attributeValues == null || attributeValues.isEmpty()) {
            return List.of();
        }
        
        Map<AttributeGroupEntity, List<ProductAttributeValueEntity>> grouped = attributeValues.stream()
                .collect(Collectors.groupingBy(
                    pav -> pav.getAttribute().getAttributeGroup(),
                    Collectors.toList()
                ));
        
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
