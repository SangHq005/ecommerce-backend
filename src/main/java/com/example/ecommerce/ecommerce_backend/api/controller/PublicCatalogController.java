package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.shop.PublicShopResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.catalog.CatalogFacade;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/catalog/public")
@Tag(name = "Public Catalog", description = "Public product catalog endpoints")
public class PublicCatalogController {

    private final CatalogFacade catalog;
    private final SellerShopJpaRepository shopRepository;
    private final ProductJpaRepository productRepository;

    public PublicCatalogController(CatalogFacade catalog, SellerShopJpaRepository shopRepository, ProductJpaRepository productRepository) {
        this.catalog = catalog;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/categories")
    @Operation(summary = "List categories", description = "Get all active categories")
    public ResponseEntity<ApiResponse<List<CategoryEntity>>> categories() {
        List<CategoryEntity> categories = catalog.listCategories();
        return ResponseHelper.ok(categories);
    }

    @GetMapping("/brands")
    @Operation(summary = "List brands", description = "Get all active brands")
    public ResponseEntity<ApiResponse<List<BrandEntity>>> brands() {
        List<BrandEntity> brands = catalog.listBrands();
        return ResponseHelper.ok(brands);
    }

    @GetMapping("/products")
    @Operation(summary = "List products", description = "Get paginated list of active products")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProductEntity> products = catalog.listActiveProducts(categoryId, brandId, pageable);
        return ResponseHelper.page(products);
    }

    @GetMapping("/products/top-deals")
    @Operation(summary = "Top Deals", description = "Get top products with highest discount percentage")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> topDeals(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<ProductEntity> deals = catalog.getTopDeals(limit);
        return ResponseHelper.ok(deals);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product", description = "Get product details by ID or slug (including SKUs, images, shop)")
    public ResponseEntity<ApiResponse<ProductDetailsResponse>> detail(@PathVariable String id) {
        try {
            ProductDetailsResponse detail = catalog.getProductDetailByIdOrSlug(id);
            return ResponseHelper.ok(detail);
        } catch (com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException e) {
            return ResponseHelper.error(
                com.example.ecommerce.ecommerce_backend.api.response.ErrorCode.PRODUCT_NOT_FOUND,
                e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            return ResponseHelper.error(
                com.example.ecommerce.ecommerce_backend.api.response.ErrorCode.BAD_REQUEST,
                e.getMessage()
            );
        }
    }
    
    @GetMapping("/shops/{shopId}")
    @Operation(summary = "Get shop info", description = "Get public shop information by ID")
    public ResponseEntity<ApiResponse<PublicShopResponse>> getShop(@PathVariable Long shopId) {
        SellerShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));
        
        // Only return active shops
        if (!"ACTIVE".equals(shop.getStatus())) {
            throw new IllegalArgumentException("Shop not available");
        }
        
        // Count products
        long productCount = productRepository.countByShopIdAndStatus(shopId, "ACTIVE");
        
        PublicShopResponse response = new PublicShopResponse(
                shop.getId(),
                shop.getShopName(),
                shop.getShopSlug(),
                shop.getDescription(),
                shop.getLogoUrl(),
                shop.getBannerUrl(),
                shop.getCity(),
                shop.getVerifiedAt() != null,
                productCount
        );
        
        return ResponseHelper.ok(response);
    }
}
