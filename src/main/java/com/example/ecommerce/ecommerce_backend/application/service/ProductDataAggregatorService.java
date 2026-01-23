package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.ai.AiAssistantResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductSKUDetailDTO;
import com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to aggregate all product data for AI assistant.
 * Combines product details, attributes, variants, images, etc.
 */
@Service
public class ProductDataAggregatorService {
    
    private final ProductJpaRepository productRepository;
    private final ProductImageJpaRepository imageRepository;
    private final OptionGroupJpaRepository optionGroupRepository;
    private final OptionValueJpaRepository optionValueRepository;
    private final SkuJpaRepository skuRepository;
    private final SellerShopJpaRepository shopRepository;
    private final ProductAttributeValueJpaRepository attrValueRepository;
    private final BrandJpaRepository brandRepository;
    private final CategoryJpaRepository categoryRepository;
    private final ProductService productService;
    private final CatalogService catalogService;
    
    public ProductDataAggregatorService(
            ProductJpaRepository productRepository,
            ProductImageJpaRepository imageRepository,
            OptionGroupJpaRepository optionGroupRepository,
            OptionValueJpaRepository optionValueRepository,
            SkuJpaRepository skuRepository,
            SellerShopJpaRepository shopRepository,
            ProductAttributeValueJpaRepository attrValueRepository,
            BrandJpaRepository brandRepository,
            CategoryJpaRepository categoryRepository,
            ProductService productService,
            CatalogService catalogService) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.optionGroupRepository = optionGroupRepository;
        this.optionValueRepository = optionValueRepository;
        this.skuRepository = skuRepository;
        this.shopRepository = shopRepository;
        this.attrValueRepository = attrValueRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.catalogService = catalogService;
    }
    
    /**
     * Get comprehensive product data for AI assistant.
     */
    @Transactional(readOnly = true)
    public AiAssistantResponse.ProductData getComprehensiveProductData(String productIdOrSlug) {
        // Get basic product details
        ProductDetailResponse detailResponse = productService.getProductDetails(productIdOrSlug);
        ProductEntity product = getProductEntity(productIdOrSlug);
        
        // Get attributes
        ProductDetailsResponse detailsResponse = catalogService.getProductDetail(product.getId());
        
        // Build comprehensive product data
        AiAssistantResponse.ProductData productData = new AiAssistantResponse.ProductData();
        productData.setId(product.getId());
        productData.setName(product.getName());
        productData.setSlug(product.getSlug());
        productData.setPrice(product.getPrice());
        productData.setOriginalPrice(product.getOriginalPrice());
        productData.setRating(product.getAverageRating());
        productData.setReviewCount(product.getReviewCount());
        
        // Images
        List<String> imageUrls = detailResponse.getImages().stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());
        productData.setImages(imageUrls);
        
        // Specifications (attributes)
        Map<String, Object> specifications = new LinkedHashMap<>();
        if (detailsResponse.attributes() != null) {
            for (ProductDetailsResponse.AttributeGroupDetails groupDetail : detailsResponse.attributes()) {
                String groupName = groupDetail.group().getName();
                Map<String, String> groupSpecs = new LinkedHashMap<>();
                
                for (ProductDetailsResponse.AttributeValueDetails attrDetail : groupDetail.attributes()) {
                    String attrName = attrDetail.attribute().getName();
                    String displayValue = attrDetail.value().getDisplayValue();
                    groupSpecs.put(attrName, displayValue);
                }
                
                specifications.put(groupName, groupSpecs);
            }
        }
        productData.setSpecifications(specifications);
        
        // Variants (SKUs with options)
        List<Map<String, Object>> variants = new ArrayList<>();
        for (ProductSKUDetailDTO sku : detailResponse.getSkus()) {
            Map<String, Object> variant = new HashMap<>();
            variant.put("skuId", sku.getId());
            variant.put("skuCode", sku.getSkuCode());
            variant.put("price", sku.getPrice());
            variant.put("stock", sku.getStockOnHand());
            variant.put("optionSignature", sku.getOptionSignature());
            variant.put("imageUrl", sku.getImageUrl());
            variants.add(variant);
        }
        productData.setVariants(variants);
        
        // Additional info
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("description", product.getDescription());
        additionalInfo.put("brand", product.getBrandId() != null ? 
                brandRepository.findById(product.getBrandId()).map(BrandEntity::getName).orElse(null) : null);
        additionalInfo.put("category", product.getCategoryId() != null ?
                categoryRepository.findById(product.getCategoryId()).map(CategoryEntity::getName).orElse(null) : null);
        additionalInfo.put("seller", detailResponse.getProduct().getSeller().getName());
        additionalInfo.put("soldCount", product.getSoldCount());
        additionalInfo.put("weight", product.getWeightGrams());
        
        specifications.put("_additional", additionalInfo);
        
        return productData;
    }
    
    /**
     * Get comprehensive data for multiple products (for comparison).
     */
    @Transactional(readOnly = true)
    public List<AiAssistantResponse.ProductData> getMultipleProductData(List<String> productIdsOrSlugs) {
        return productIdsOrSlugs.stream()
                .map(this::getComprehensiveProductData)
                .collect(Collectors.toList());
    }
    
    private ProductEntity getProductEntity(String productIdOrSlug) {
        try {
            Long productId = Long.parseLong(productIdOrSlug);
            return productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
        } catch (NumberFormatException e) {
            return productRepository.findBySlug(productIdOrSlug)
                    .orElseThrow(() -> new ProductNotFoundException(productIdOrSlug));
        }
    }
}
