package com.example.ecommerce.ecommerce_backend.api.dto.search;

import java.util.List;

public record SearchFacetsResponse(
        List<CategoryFacet> categories,
        List<BrandFacet> brands,
        PriceRange priceRange,
        List<RatingFacet> ratings
) {
    public record CategoryFacet(
            Long categoryId,
            String categoryName,
            long count
    ) {}

    public record BrandFacet(
            String brand,
            long count
    ) {}

    public record PriceRange(
            Long minPrice,
            Long maxPrice,
            Long averagePrice
    ) {}

    public record RatingFacet(
            int rating,
            long count
    ) {}
}
