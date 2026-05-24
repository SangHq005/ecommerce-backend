package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailsResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.catalog.CatalogFacade;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.api.dto.shop.SuspendRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Catalog", description = "Admin product management")
public class AdminCatalogController {

    private final CatalogFacade catalog;

    public AdminCatalogController(CatalogFacade catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @Operation(summary = "List products", description = "List products with pagination, search and status filter")
    public ResponseEntity<ApiResponse<List<ProductEntity>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProductEntity> products = catalog.adminListProducts(status, search, pageable);
        return ResponseHelper.page(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product detail", description = "Get product details by ID (admin can view any status)")
    public ResponseEntity<ApiResponse<ProductDetailsResponse>> getProductDetail(@PathVariable Long id) {
        ProductDetailsResponse detail = catalog.adminGetProductDetail(id);
        return ResponseHelper.ok(detail);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve product", description = "Approve a pending product")
    public ResponseEntity<ApiResponse<ProductEntity>> approve(@PathVariable Long id) {
        ProductEntity product = catalog.adminApprove(id);
        return ResponseHelper.ok(product, "Product approved");
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject product", description = "Reject a product with reason")
    public ResponseEntity<ApiResponse<ProductEntity>> reject(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequest req
    ) {
        ProductEntity product = catalog.adminReject(id, req.reason());
        return ResponseHelper.ok(product, "Product rejected");
    }

    @PostMapping("/{id}/hide")
    @Operation(summary = "Hide product", description = "Hide/Ban a product with reason")
    public ResponseEntity<ApiResponse<ProductEntity>> hide(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequest req
    ) {
        ProductEntity product = catalog.adminHide(id, req.reason());
        return ResponseHelper.ok(product, "Product hidden");
    }
}
