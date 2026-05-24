package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.CompareProductResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.catalog.ProductCompareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Product Comparison feature.
 * 
 * Endpoint: GET /api/v1/compare?productIds=101,205
 */
@RestController
@RequestMapping("/api/v1/compare")
@Tag(name = "Product Compare", description = "Compare products side by side")
public class ProductCompareController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductCompareController.class);
    
    private final ProductCompareService compareService;
    
    public ProductCompareController(ProductCompareService compareService) {
        this.compareService = compareService;
    }
    
    /**
     * Compare multiple products by their IDs.
     * 
     * Products must:
     * - Be in the same category
     * - Be active/visible
     * - Have 2-4 unique product IDs
     * 
     * @param productIds Comma-separated list of product IDs (e.g., "101,205" or "101,205,306")
     * @return CompareProductResponse with product summaries and grouped specifications
     */
    @GetMapping
    @Operation(
            summary = "Compare products",
            description = "Compare 2-4 products from the same category. Returns product info and grouped specifications with difference highlighting."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Comparison successful",
                    content = @Content(schema = @Schema(implementation = CompareProductResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - min 2 products, max 4 products, same category required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "One or more products not found"
            )
    })
    public ResponseEntity<ApiResponse<CompareProductResponse>> compareProducts(
            @Parameter(
                    description = "Comma-separated product IDs (2-4 products, same category)",
                    example = "1,2"
            )
            @RequestParam("productIds") List<Long> productIds
    ) {
        log.info("Compare request for products: {}", productIds);
        
        try {
            CompareProductResponse response = compareService.compareProducts(productIds);
            log.info("Compare successful: {} products, {} spec groups", 
                    response.products().size(), response.specGroups().size());
            return ResponseHelper.ok(response, "So sánh sản phẩm thành công");
        } catch (BusinessException e) {
            log.warn("Compare failed: {} - {}", e.getErrorCode(), e.getMessage());
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    /**
     * Check if products can be compared.
     * Lightweight endpoint to validate before navigating to compare page.
     * 
     * @param productIds List of product IDs to validate
     * @return Validation result
     */
    @GetMapping("/validate")
    @Operation(
            summary = "Validate products for comparison",
            description = "Check if products can be compared (same category, active, etc.) without fetching full specs."
    )
    public ResponseEntity<ApiResponse<CompareValidationResponse>> validateComparison(
            @RequestParam("productIds") List<Long> productIds
    ) {
        log.debug("Validate compare request for products: {}", productIds);
        
        try {
            // This will throw if validation fails
            compareService.compareProducts(productIds);
            return ResponseHelper.ok(new CompareValidationResponse(true, null, productIds.size()));
        } catch (BusinessException e) {
            return ResponseHelper.ok(new CompareValidationResponse(false, e.getMessage(), productIds.size()));
        }
    }
    
    /**
     * Response for validation endpoint.
     */
    public record CompareValidationResponse(
            boolean canCompare,
            String errorMessage,
            int productCount
    ) {}
}
