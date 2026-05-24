package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.catalog.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "Product detail endpoints with variant support")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping("/{productId}/details")
    @Operation(
        summary = "Get product details with variants",
        description = "Get comprehensive product information including variants, SKUs, and option groups. " +
                     "Supports lookup by numeric ID or slug."
    )
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetails(
            @PathVariable String productId
    ) {
        try {
            ProductDetailResponse response = productService.getProductDetails(productId);
            return ResponseHelper.ok(response);
        } catch (ProductNotFoundException e) {
            return ResponseHelper.error(
                com.example.ecommerce.ecommerce_backend.api.response.ErrorCode.PRODUCT_NOT_FOUND,
                e.getMessage()
            );
        }
    }
}
