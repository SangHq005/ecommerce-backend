package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.search.PageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.catalog.ProductSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Product search and suggestions")
public class ProductSearchController {

    private final ProductSearchService searchService;

    public ProductSearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/products")
    @Operation(summary = "Search products", description = "Advanced product search with filters")
    public ResponseEntity<ApiResponse<PageResponse<ProductSearchHit>>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String locations,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(defaultValue = "RELEVANCE") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ProductSearchHit> results = searchService.search(
                q, categoryId, brandId, shopId,
                minPrice, maxPrice, inStock,
                locations, minRating,
                sort, page, size
        );
        return ResponseHelper.ok(results);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Search suggestions", description = "Get autocomplete suggestions for search")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<String> suggestions = searchService.getSearchSuggestions(q, limit);
        return ResponseHelper.ok(suggestions);
    }

    @GetMapping("/popular")
    @Operation(summary = "Popular searches", description = "Get trending search terms")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Map<String, Object>> popular = searchService.getPopularSearches(limit);
        return ResponseHelper.ok(popular);
    }
}
