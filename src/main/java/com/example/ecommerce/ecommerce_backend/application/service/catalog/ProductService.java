package com.example.ecommerce.ecommerce_backend.application.service.catalog;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.*;
import com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    private final ProductJpaRepository productRepository;
    private final ProductImageJpaRepository imageRepository;
    private final OptionGroupJpaRepository optionGroupRepository;
    private final OptionValueJpaRepository optionValueRepository;
    private final SkuJpaRepository skuRepository;
    private final SellerShopJpaRepository shopRepository;
    
    public ProductService(ProductJpaRepository productRepository,
                         ProductImageJpaRepository imageRepository,
                         OptionGroupJpaRepository optionGroupRepository,
                         OptionValueJpaRepository optionValueRepository,
                         SkuJpaRepository skuRepository,
                         SellerShopJpaRepository shopRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.optionGroupRepository = optionGroupRepository;
        this.optionValueRepository = optionValueRepository;
        this.skuRepository = skuRepository;
        this.shopRepository = shopRepository;
    }
    
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetails(String productIdOrSlug) {
        // Try to parse as ID first, otherwise treat as slug
        ProductEntity product;
        try {
            Long productId = Long.parseLong(productIdOrSlug);
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
        } catch (NumberFormatException e) {
            // It's a slug, find by slug
            product = productRepository.findBySlug(productIdOrSlug)
                    .orElseThrow(() -> new ProductNotFoundException(productIdOrSlug));
        }
        
        // Fetch seller/shop information
        SellerShopEntity shop = shopRepository.findById(product.getShopId())
                .orElse(null);
        
        ProductDetailResponse.SellerInfo sellerInfo = new ProductDetailResponse.SellerInfo(
                shop != null ? shop.getId() : null,
                shop != null ? shop.getShopName() : "Unknown Seller",
                BigDecimal.ZERO // Shop rating not implemented yet
        );
        
        // Build product info
        ProductDetailResponse.ProductInfo productInfo = new ProductDetailResponse.ProductInfo(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getMainImageUrl(),
                product.getAverageRating(),
                product.getReviewCount(),
                sellerInfo
        );
        
        // Fetch images ordered by sort_order
        List<ProductImageDTO> images = imageRepository.findByProductIdOrderBySortOrderAsc(product.getId())
                .stream()
                .map(img -> new ProductImageDTO(
                        img.getId(),
                        img.getProductId(),
                        img.getImageUrl(),
                        img.getSortOrder(),
                        null // altText not in entity yet
                ))
                .collect(Collectors.toList());
        
        // Fetch option groups ordered by sort_order
        List<OptionGroupEntity> optionGroups = optionGroupRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId());
        
        // OPTIMIZATION: Fetch all option values in one query instead of N queries
        List<Long> groupIds = optionGroups.stream()
                .map(OptionGroupEntity::getId)
                .collect(Collectors.toList());
        
        List<OptionValueEntity> allOptionValues = groupIds.isEmpty() 
                ? List.of() 
                : optionValueRepository.findByOptionGroupIdsOrderByGroupIdSortOrderAscIdAsc(groupIds);
        
        // Group option values by optionGroupId for efficient lookup
        java.util.Map<Long, List<OptionValueEntity>> valuesByGroup = allOptionValues.stream()
                .collect(Collectors.groupingBy(OptionValueEntity::getOptionGroupId));
        
        List<ProductOptionGroupDTO> optionGroupDTOs = optionGroups.stream()
                .map(group -> {
                    // Get option values for this group from the pre-fetched map
                    List<OptionValueEntity> values = valuesByGroup.getOrDefault(group.getId(), List.of());
                    List<ProductOptionValueDTO> valueDTOs = values.stream()
                            .map(val -> new ProductOptionValueDTO(val.getId(), val.getValue(), val.getSortOrder()))
                            .collect(Collectors.toList());
                    
                    return new ProductOptionGroupDTO(group.getId(), group.getName(), group.getSortOrder(), valueDTOs);
                })
                .collect(Collectors.toList());
        
        // Fetch all active SKUs with stock information
        List<SkuEntity> skus = skuRepository.findByProductIdOrderByIdAsc(product.getId());
        List<ProductSKUDetailDTO> skuDTOs = skus.stream()
                .filter(SkuEntity::isActive) // Filter to only active SKUs
                .map(sku -> new ProductSKUDetailDTO(
                        sku.getId(),
                        sku.getSkuCode(),
                        sku.getPrice(),
                        sku.getCompareAtPrice(),
                        sku.getStockOnHand(),
                        sku.getReservedStock(),
                        sku.getOptionSignature(),
                        sku.isActive(),
                        sku.getImageUrl()
                ))
                .collect(Collectors.toList());
        
        return new ProductDetailResponse(productInfo, images, optionGroupDTOs, skuDTOs);
    }
}
