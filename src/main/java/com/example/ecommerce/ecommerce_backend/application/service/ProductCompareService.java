package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.CompareProductResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.CompareProductResponse.*;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for product comparison feature.
 */
@Service
public class ProductCompareService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductCompareService.class);
    private static final int MIN_PRODUCTS = 2;
    private static final int MAX_PRODUCTS = 4;
    
    private final ProductJpaRepository productRepo;
    private final ProductAttributeValueJpaRepository attrValueRepo;
    private final AttributeGroupJpaRepository attrGroupRepo;
    private final AttributeJpaRepository attrRepo;
    private final CategoryJpaRepository categoryRepo;
    private final SellerShopJpaRepository shopRepo;
    
    public ProductCompareService(
            ProductJpaRepository productRepo,
            ProductAttributeValueJpaRepository attrValueRepo,
            AttributeGroupJpaRepository attrGroupRepo,
            AttributeJpaRepository attrRepo,
            CategoryJpaRepository categoryRepo,
            SellerShopJpaRepository shopRepo
    ) {
        this.productRepo = productRepo;
        this.attrValueRepo = attrValueRepo;
        this.attrGroupRepo = attrGroupRepo;
        this.attrRepo = attrRepo;
        this.categoryRepo = categoryRepo;
        this.shopRepo = shopRepo;
    }
    
    /**
     * Compare products by their IDs.
     * 
     * @param productIds List of product IDs to compare (2-4 products)
     * @return CompareProductResponse with products and grouped specs
     * @throws BusinessException if validation fails
     */
    @Transactional(readOnly = true)
    public CompareProductResponse compareProducts(List<Long> productIds) {
        // 1. Validate input
        validateInput(productIds);
        
        // 2. Remove duplicates and preserve order
        List<Long> uniqueIds = productIds.stream().distinct().toList();
        if (uniqueIds.size() < MIN_PRODUCTS) {
            throw new BusinessException(ErrorCode.COMPARE_DUPLICATE_PRODUCTS, 
                "Vui lòng chọn ít nhất 2 sản phẩm khác nhau để so sánh");
        }
        
        // 3. Fetch products
        List<ProductEntity> products = productRepo.findAllById(uniqueIds);
        
        // Validate all products found
        if (products.size() != uniqueIds.size()) {
            Set<Long> foundIds = products.stream().map(ProductEntity::getId).collect(Collectors.toSet());
            List<Long> notFoundIds = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new BusinessException(ErrorCode.COMPARE_PRODUCT_NOT_FOUND, 
                "Không tìm thấy sản phẩm với ID: " + notFoundIds);
        }
        
        // 4. Validate same category
        validateSameCategory(products);
        
        // 5. Validate products are active/visible
        validateProductStatus(products);
        
        // 6. Preserve order as requested
        Map<Long, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));
        products = uniqueIds.stream().map(productMap::get).toList();
        
        // 7. Fetch category info
        Long categoryId = products.get(0).getCategoryId();
        String categoryName = categoryRepo.findById(categoryId)
                .map(CategoryEntity::getName)
                .orElse("Unknown");
        
        // 8. Build product summaries
        List<ProductSummary> productSummaries = buildProductSummaries(products);
        
        // 9. Fetch and group specs
        List<SpecGroup> specGroups = buildSpecGroups(uniqueIds);
        
        return new CompareProductResponse(productSummaries, specGroups, categoryId, categoryName);
    }
    
    private void validateInput(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.COMPARE_INVALID_INPUT, 
                "Vui lòng cung cấp danh sách sản phẩm để so sánh");
        }
        
        if (productIds.size() < MIN_PRODUCTS) {
            throw new BusinessException(ErrorCode.COMPARE_MIN_PRODUCTS, 
                String.format("Cần ít nhất %d sản phẩm để so sánh", MIN_PRODUCTS));
        }
        
        if (productIds.size() > MAX_PRODUCTS) {
            throw new BusinessException(ErrorCode.COMPARE_MAX_PRODUCTS, 
                String.format("Chỉ có thể so sánh tối đa %d sản phẩm", MAX_PRODUCTS));
        }
    }
    
    private void validateSameCategory(List<ProductEntity> products) {
        Set<Long> categories = products.stream()
                .map(ProductEntity::getCategoryId)
                .collect(Collectors.toSet());
        
        if (categories.size() > 1) {
            throw new BusinessException(ErrorCode.COMPARE_DIFFERENT_CATEGORY, 
                "Chỉ có thể so sánh các sản phẩm trong cùng danh mục");
        }
    }
    
    private void validateProductStatus(List<ProductEntity> products) {
        List<String> invalidProducts = products.stream()
                .filter(p -> !"ACTIVE".equals(p.getStatus()))
                .map(p -> p.getName() + " (" + p.getStatus() + ")")
                .toList();
        
        if (!invalidProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.COMPARE_PRODUCT_UNAVAILABLE, 
                "Sản phẩm không khả dụng: " + String.join(", ", invalidProducts));
        }
    }
    
    private List<ProductSummary> buildProductSummaries(List<ProductEntity> products) {
        // Batch fetch shop info
        Set<Long> shopIds = products.stream().map(ProductEntity::getShopId).collect(Collectors.toSet());
        Map<Long, SellerShopEntity> shopMap = shopRepo.findAllById(shopIds).stream()
                .collect(Collectors.toMap(SellerShopEntity::getId, Function.identity()));
        
        return products.stream().map(p -> {
            SellerShopEntity shop = shopMap.get(p.getShopId());
            return new ProductSummary(
                    p.getId(),
                    p.getName(),
                    p.getSlug(),
                    p.getMainImageUrl(),
                    p.getPrice(),
                    p.getOriginalPrice(),
                    p.getCurrency(),
                    p.getAverageRating(),
                    p.getReviewCount(),
                    p.getSoldCount(),
                    p.getShopId(),
                    shop != null ? shop.getShopName() : null,
                    p.getStatus()
            );
        }).toList();
    }
    
    private List<SpecGroup> buildSpecGroups(List<Long> productIds) {
        // Fetch all attribute values with eager loading
        List<ProductAttributeValueEntity> allValues = 
                attrValueRepo.findByProductIdsWithAttributeAndGroup(productIds);
        
        if (allValues.isEmpty()) {
            return List.of();
        }
        
        // Group by attribute group
        Map<Long, List<ProductAttributeValueEntity>> valuesByGroup = allValues.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getAttribute().getAttributeGroupId(),
                        LinkedHashMap::new, // Preserve order
                        Collectors.toList()
                ));
        
        // Build spec groups
        List<SpecGroup> specGroups = new ArrayList<>();
        
        for (Map.Entry<Long, List<ProductAttributeValueEntity>> entry : valuesByGroup.entrySet()) {
            List<ProductAttributeValueEntity> groupValues = entry.getValue();
            
            // Get group info from first value (they all belong to same group)
            AttributeGroupEntity group = groupValues.get(0).getAttribute().getAttributeGroup();
            
            // Group values by attribute
            Map<Long, List<ProductAttributeValueEntity>> valuesByAttr = groupValues.stream()
                    .collect(Collectors.groupingBy(
                            ProductAttributeValueEntity::getAttributeId,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));
            
            // Build spec rows
            List<SpecRow> rows = new ArrayList<>();
            for (Map.Entry<Long, List<ProductAttributeValueEntity>> attrEntry : valuesByAttr.entrySet()) {
                List<ProductAttributeValueEntity> attrValues = attrEntry.getValue();
                AttributeEntity attr = attrValues.get(0).getAttribute();
                
                // Build values list (one per product, in order)
                Map<Long, ProductAttributeValueEntity> valueMap = attrValues.stream()
                        .collect(Collectors.toMap(
                                ProductAttributeValueEntity::getProductId,
                                Function.identity()
                        ));
                
                List<SpecValue> specValues = productIds.stream().map(productId -> {
                    ProductAttributeValueEntity val = valueMap.get(productId);
                    if (val == null) {
                        return new SpecValue(productId, "—", null, null, null);
                    }
                    return new SpecValue(
                            productId,
                            val.getDisplayValue(),
                            val.getValueText(),
                            val.getValueNumber(),
                            val.getValueBoolean()
                    );
                }).toList();
                
                // Check if values are different
                boolean isDifferent = checkDifferent(specValues, attr.getDataType());
                
                rows.add(new SpecRow(
                        attr.getId(),
                        attr.getName(),
                        attr.getSlug(),
                        attr.getUnit(),
                        attr.getDataType().name(),
                        attr.getSortOrder(),
                        specValues,
                        isDifferent
                ));
            }
            
            // Sort rows by sortOrder
            rows.sort(Comparator.comparing(SpecRow::sortOrder));
            
            specGroups.add(new SpecGroup(
                    group.getId(),
                    group.getName(),
                    group.getSlug(),
                    group.getSortOrder(),
                    rows
            ));
        }
        
        // Sort groups by sortOrder
        specGroups.sort(Comparator.comparing(SpecGroup::sortOrder));
        
        return specGroups;
    }
    
    /**
     * Check if spec values are different across products.
     * Uses normalized comparison based on data type.
     */
    private boolean checkDifferent(List<SpecValue> values, AttributeEntity.DataType dataType) {
        // Filter out missing values
        List<SpecValue> presentValues = values.stream()
                .filter(v -> v.displayValue() != null && !"—".equals(v.displayValue()))
                .toList();
        
        if (presentValues.size() < 2) {
            // If only 0-1 product has value, consider it different (to highlight)
            return presentValues.size() == 1;
        }
        
        // Compare based on data type
        return switch (dataType) {
            case NUMBER -> {
                Set<BigDecimal> uniqueNumbers = presentValues.stream()
                        .map(SpecValue::valueNumber)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                yield uniqueNumbers.size() > 1;
            }
            case BOOLEAN -> {
                Set<Boolean> uniqueBooleans = presentValues.stream()
                        .map(SpecValue::valueBoolean)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                yield uniqueBooleans.size() > 1;
            }
            case TEXT, ENUM -> {
                // Normalize text for comparison (trim, lowercase)
                Set<String> uniqueTexts = presentValues.stream()
                        .map(v -> normalizeText(v.valueText() != null ? v.valueText() : v.displayValue()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                yield uniqueTexts.size() > 1;
            }
        };
    }
    
    /**
     * Normalize text for comparison.
     */
    private String normalizeText(String text) {
        if (text == null) return null;
        return text.trim().toLowerCase()
                .replaceAll("\\s+", " ")  // Normalize whitespace
                .replaceAll("[,.]", "");  // Remove punctuation
    }
}
