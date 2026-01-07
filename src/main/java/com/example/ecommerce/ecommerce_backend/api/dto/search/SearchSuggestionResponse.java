package com.example.ecommerce.ecommerce_backend.api.dto.search;

import java.util.List;

public record SearchSuggestionResponse(
        List<String> suggestions,
        List<PopularSearch> popularSearches,
        List<TrendingProduct> trendingProducts
) {
    public record PopularSearch(
            String keyword,
            long searchCount
    ) {}

    public record TrendingProduct(
            Long productId,
            String productName,
            Long price,
            String imageUrl
    ) {}
}
