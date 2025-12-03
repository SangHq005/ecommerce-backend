package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.application.service.CatalogService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
public class PublicCatalogController {

    private final CatalogService catalog;

    public PublicCatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<CategoryEntity> categories() {
        return catalog.listCategories();
    }

    @GetMapping("/brands")
    public List<BrandEntity> brands() {
        return catalog.listBrands();
    }

    @GetMapping("/products")
    public Page<ProductEntity> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return catalog.listActiveProducts(categoryId, brandId, pageable);
    }

    @GetMapping("/products/{id}")
    public ProductEntity detail(@PathVariable Long id) {
        return catalog.getActiveProduct(id);
    }
}
