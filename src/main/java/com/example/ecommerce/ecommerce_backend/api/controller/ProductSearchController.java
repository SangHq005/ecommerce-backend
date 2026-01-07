package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.search.PageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit;
import com.example.ecommerce.ecommerce_backend.application.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService searchService;

    /**
     * Advanced product search with filters
     *
     * @param q Search query (supports full-text search)
     * @param categoryId Filter by category
     * @param brandId Filter by brand
     * @param shopId Filter by shop
     * @param minPrice Minimum price filter
     * @param maxPrice Maximum price filter
     * @param inStock Filter only in-stock products
     * @param sort Sort order: RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST (default: RELEVANCE if q provided, else NEWEST)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated search results
     */
    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductSearchHit>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "RELEVANCE") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ProductSearchHit> results = searchService.search(
                q, categoryId, brandId, shopId,
                minPrice, maxPrice, inStock,
                sort, page, size
        );
        return ResponseEntity.ok(results);
    }

    /**
     * Get search suggestions for autocomplete
     *
     * @param q Partial search query
     * @param limit Maximum number of suggestions (default: 10)
     * @return List of product name suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<java.util.List<String>> getSearchSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        java.util.List<String> suggestions = searchService.getSearchSuggestions(q, limit);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get popular/trending search terms
     *
     * @param limit Number of terms to return (default: 10)
     * @return List of popular search queries with counts
     */
    @GetMapping("/popular")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit
    ) {
        java.util.List<java.util.Map<String, Object>> popular = searchService.getPopularSearches(limit);
        return ResponseEntity.ok(popular);
    }
}
