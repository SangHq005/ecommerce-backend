package com.example.ecommerce.ecommerce_backend.api.dto.search;

import java.util.List;

public record ProductSearchRequest(
        String keyword,
        Long categoryId,
        List<Long> categoryIds,
        Long minPrice,
        Long maxPrice,
        String brand,
        List<String> brands,
        Double minRating,
        Boolean inStock,
        String sortBy, // price_asc, price_desc, rating, newest, popular
        Integer page,
        Integer size
) {
    public ProductSearchRequest {
        page = page != null ? page : 0;
        size = size != null ? size : 20;
        sortBy = sortBy != null ? sortBy : "newest";
    }
}
