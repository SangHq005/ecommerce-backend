package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for Product Comparison API.
 * Contains product info and grouped specifications.
 */
public record CompareProductResponse(
        List<ProductSummary> products,
        List<SpecGroup> specGroups,
        Long categoryId,
        String categoryName
) {
    
    /**
     * Basic product info for comparison header.
     */
    public record ProductSummary(
            Long id,
            String name,
            String slug,
            String mainImageUrl,
            Long price,
            Long originalPrice,
            String currency,
            BigDecimal averageRating,
            Integer reviewCount,
            Integer soldCount,
            Long shopId,
            String shopName,
            String status
    ) {}
    
    /**
     * Group of specifications (e.g., "Màn hình", "Hiệu năng").
     */
    public record SpecGroup(
            Long groupId,
            String groupName,
            String groupSlug,
            Integer sortOrder,
            List<SpecRow> rows
    ) {}
    
    /**
     * Single specification row with values for each product.
     */
    public record SpecRow(
            Long attributeId,
            String attributeName,
            String attributeSlug,
            String unit,
            String dataType,
            Integer sortOrder,
            List<SpecValue> values,
            boolean isDifferent
    ) {}
    
    /**
     * Specification value for a specific product.
     */
    public record SpecValue(
            Long productId,
            String displayValue,
            String valueText,
            BigDecimal valueNumber,
            Boolean valueBoolean
    ) {}
}
