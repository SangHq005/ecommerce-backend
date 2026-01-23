package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.OptionGroupRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.SellerCreateProductRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.SellerUpdateProductRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.SkuRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.UpsertImageResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.CatalogService;
import com.example.ecommerce.ecommerce_backend.application.service.LocalFileStorageService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/seller/products")
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Catalog", description = "Seller product management")
public class SellerCatalogController {

    private final CatalogService catalog;
    private final LocalFileStorageService storage;

    public SellerCatalogController(CatalogService catalog, LocalFileStorageService storage) {
        this.catalog = catalog;
        this.storage = storage;
    }

    @GetMapping
    @Operation(summary = "List products", description = "Get all seller's products")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> list(Authentication auth) {
        Long sellerId = Long.valueOf(auth.getName());
        List<ProductEntity> products = catalog.sellerListProducts(sellerId);
        return ResponseHelper.ok(products);
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product draft")
    public ResponseEntity<ApiResponse<ProductEntity>> create(
            Authentication auth,
            @Valid @RequestBody SellerCreateProductRequest req
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        ProductEntity product = catalog.sellerCreateDraft(
                sellerId, req.categoryId(), req.brandId(),
                req.name(), req.description(), req.mainImageUrl(),
                req.price(), req.originalPrice(), req.stockQuantity()
        );
        return ResponseHelper.created(product, "Product created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update product details")
    public ResponseEntity<ApiResponse<ProductEntity>> update(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody SellerUpdateProductRequest req
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        ProductEntity product = catalog.sellerUpdate(
                sellerId, id, req.categoryId(), req.brandId(),
                req.name(), req.description(), req.mainImageUrl(),
                req.price(), req.originalPrice(), req.stockQuantity()
        );
        return ResponseHelper.ok(product, "Product updated");
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    @Operation(summary = "Upload image", description = "Upload product image")
    public ResponseEntity<ApiResponse<UpsertImageResponse>> uploadImage(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam int sortOrder,
            @RequestPart("file") MultipartFile file
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        String url = storage.save("product-images", file);
        ProductImageEntity saved = catalog.sellerUpsertImage(sellerId, id, sortOrder, url);
        UpsertImageResponse response = new UpsertImageResponse(
                saved.getId(), saved.getProductId(), saved.getSortOrder(), saved.getImageUrl()
        );
        return ResponseHelper.created(response, "Image uploaded");
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Delete image", description = "Delete product image")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            Authentication auth,
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        catalog.sellerDeleteImage(sellerId, id, imageId);
        return ResponseHelper.ok(null, "Image deleted");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details", description = "Get full product details including images and SKUs")
    public ResponseEntity<ApiResponse<com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailsResponse>> getDetail(
            Authentication auth,
            @PathVariable Long id
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        var result = catalog.sellerGetProductDetail(sellerId, id);
        return ResponseHelper.ok(result);
    }

    @PutMapping("/{id}/options")
    @Operation(summary = "Set options", description = "Set product variant options")
    public ResponseEntity<ApiResponse<Void>> setOptions(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody List<OptionGroupRequest> groups
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        List<CatalogService.OptionGroupSpec> specs = groups.stream()
                .map(g -> new CatalogService.OptionGroupSpec(g.name(), g.sortOrder(), g.values()))
                .toList();
        catalog.sellerSetOptions(sellerId, id, specs);
        return ResponseHelper.ok(null, "Options updated");
    }

    @PutMapping("/{id}/skus")
    @Operation(summary = "Update SKUs", description = "Update product SKUs")
    public ResponseEntity<ApiResponse<List<SkuEntity>>> upsertSkus(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody List<SkuRequest> skus
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        List<CatalogService.SkuSpec> specs = skus.stream()
                .map(s -> new CatalogService.SkuSpec(
                        s.skuCode(), s.optionSignature(), s.price(),
                        s.compareAtPrice(), s.stockOnHand(), s.active(), s.imageUrl()
                ))
                .toList();
        List<SkuEntity> result = catalog.sellerUpsertSkus(sellerId, id, specs);
        return ResponseHelper.ok(result, "SKUs updated");
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit product", description = "Submit product for approval")
    public ResponseEntity<ApiResponse<ProductEntity>> submit(Authentication auth, @PathVariable Long id) {
        Long sellerId = Long.valueOf(auth.getName());
        ProductEntity product = catalog.sellerSubmit(sellerId, id);
        return ResponseHelper.ok(product, "Product submitted for approval");
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate product", description = "Deactivate a product")
    public ResponseEntity<ApiResponse<ProductEntity>> deactivate(Authentication auth, @PathVariable Long id) {
        Long sellerId = Long.valueOf(auth.getName());
        ProductEntity product = catalog.sellerDeactivate(sellerId, id);
        return ResponseHelper.ok(product, "Product deactivated");
    }

    @PostMapping("/{id}/hide")
    @Operation(summary = "Hide product", description = "Hide a product from customers")
    public ResponseEntity<ApiResponse<ProductEntity>> hide(Authentication auth, @PathVariable Long id) {
        Long sellerId = Long.valueOf(auth.getName());
        ProductEntity product = catalog.sellerHide(sellerId, id);
        return ResponseHelper.ok(product, "Product hidden");
    }

    @PostMapping("/{id}/show")
    @Operation(summary = "Show product", description = "Show a hidden product to customers")
    public ResponseEntity<ApiResponse<ProductEntity>> show(Authentication auth, @PathVariable Long id) {
        Long sellerId = Long.valueOf(auth.getName());
        ProductEntity product = catalog.sellerShow(sellerId, id);
        return ResponseHelper.ok(product, "Product shown");
    }
}
