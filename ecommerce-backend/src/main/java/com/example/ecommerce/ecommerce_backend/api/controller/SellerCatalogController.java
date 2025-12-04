package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.*;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.*;
import com.example.ecommerce.ecommerce_backend.application.service.CatalogService;
import com.example.ecommerce.ecommerce_backend.application.service.LocalFileStorageService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller/products")
public class SellerCatalogController {

    private final CatalogService catalog;
    private final LocalFileStorageService storage;

    public SellerCatalogController(CatalogService catalog, LocalFileStorageService storage) {
        this.catalog = catalog;
        this.storage = storage;
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ProductEntity create(Authentication auth, @Valid @RequestBody SellerCreateProductRequest req) {
        Long sellerId = Long.valueOf(auth.getName());
        return catalog.sellerCreateDraft(sellerId, req.categoryId(), req.brandId(), req.name(), req.description(), req.mainImageUrl());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ProductEntity update(Authentication auth, @PathVariable Long id, @RequestBody SellerUpdateProductRequest req) {
        Long sellerId = Long.valueOf(auth.getName());
        return catalog.sellerUpdate(sellerId, id, req.categoryId(), req.brandId(), req.name(), req.description(), req.mainImageUrl());
    }

    @PostMapping(value="/{id}/images", consumes="multipart/form-data")
    @PreAuthorize("hasRole('SELLER')")
    public UpsertImageResponse uploadImage(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam int sortOrder,
            @RequestPart("file") MultipartFile file
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        String url = storage.save("product-images", file);
        ProductImageEntity saved = catalog.sellerUpsertImage(sellerId, id, sortOrder, url);
        return new UpsertImageResponse(saved.getId(), saved.getProductId(), saved.getSortOrder(), saved.getImageUrl());
    }

    @PutMapping("/{id}/options")
    @PreAuthorize("hasRole('SELLER')")
    public void setOptions(Authentication auth, @PathVariable Long id, @Valid @RequestBody List<OptionGroupRequest> groups) {
        Long sellerId = Long.valueOf(auth.getName());
        List<CatalogService.OptionGroupSpec> specs = groups.stream()
                .map(g -> new CatalogService.OptionGroupSpec(g.name(), g.sortOrder(), g.values()))
                .toList();
        catalog.sellerSetOptions(sellerId, id, specs);
    }

    @PutMapping("/{id}/skus")
    @PreAuthorize("hasRole('SELLER')")
    public List<SkuEntity> upsertSkus(Authentication auth, @PathVariable Long id, @Valid @RequestBody List<SkuRequest> skus) {
        Long sellerId = Long.valueOf(auth.getName());
        List<CatalogService.SkuSpec> specs = skus.stream()
                .map(s -> new CatalogService.SkuSpec(s.skuCode(), s.optionSignature(), s.price(), s.compareAtPrice(), s.stockOnHand(), s.active()))
                .toList();
        return catalog.sellerUpsertSkus(sellerId, id, specs);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('SELLER')")
    public ProductEntity submit(Authentication auth, @PathVariable Long id) {
        Long sellerId = Long.valueOf(auth.getName());
        return catalog.sellerSubmit(sellerId, id);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    public ProductEntity deactivate(Authentication auth, @PathVariable Long id) {
        Long sellerId = Long.valueOf(auth.getName());
        return catalog.sellerDeactivate(sellerId, id);
    }
}
