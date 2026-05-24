package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.ai.AiAssistantRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.ai.AiAssistantResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.recommendation.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI Assistant Controller for product queries and comparisons.
 */
@RestController
@RequestMapping("/api/v1/ai-assistant")
@Tag(name = "AI Assistant", description = "AI-powered product information and comparison assistant")
public class AiAssistantController {
    
    private final AiAssistantService aiAssistantService;
    
    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }
    
    @PostMapping("/query")
    @Operation(
        summary = "Query AI Assistant",
        description = "Ask questions about products, get detailed information, or compare products. " +
                     "Supports natural language queries in Vietnamese and English."
    )
    public ResponseEntity<ApiResponse<AiAssistantResponse>> query(
            @RequestBody AiAssistantRequest request
    ) {
        try {
            AiAssistantResponse response = aiAssistantService.processQuery(request);
            return ResponseHelper.ok(response);
        } catch (Exception e) {
            return ResponseHelper.error(
                com.example.ecommerce.ecommerce_backend.api.response.ErrorCode.INTERNAL_ERROR,
                "Failed to process query: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/product/{productId}/details")
    @Operation(
        summary = "Get detailed product information",
        description = "Get comprehensive product information formatted for AI assistant. " +
                     "Includes specifications, variants, images, and pricing."
    )
    public ResponseEntity<ApiResponse<AiAssistantResponse>> getProductDetails(
            @Parameter(description = "Product ID or slug") @PathVariable String productId,
            @Parameter(description = "Response format") @RequestParam(defaultValue = "MARKDOWN") 
                    AiAssistantRequest.ResponseFormat format
    ) {
        try {
            AiAssistantRequest request = new AiAssistantRequest();
            request.setQuery("thông tin chi tiết sản phẩm " + productId);
            request.setProductIds(List.of(Long.parseLong(productId)));
            request.setFormat(format);
            
            AiAssistantResponse response = aiAssistantService.processQuery(request);
            return ResponseHelper.ok(response);
        } catch (NumberFormatException e) {
            // Try as slug
            AiAssistantRequest request = new AiAssistantRequest();
            request.setQuery("thông tin chi tiết sản phẩm " + productId);
            request.setFormat(format);
            
            AiAssistantResponse response = aiAssistantService.processQuery(request);
            return ResponseHelper.ok(response);
        } catch (Exception e) {
            return ResponseHelper.error(
                com.example.ecommerce.ecommerce_backend.api.response.ErrorCode.INTERNAL_ERROR,
                "Failed to get product details: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/compare")
    @Operation(
        summary = "Compare products",
        description = "Compare multiple products side-by-side. " +
                     "Returns differences, similarities, and recommendations."
    )
    public ResponseEntity<ApiResponse<AiAssistantResponse>> compareProducts(
            @Parameter(description = "List of product IDs to compare") 
            @RequestBody List<Long> productIds,
            @Parameter(description = "Response format") 
            @RequestParam(defaultValue = "MARKDOWN") AiAssistantRequest.ResponseFormat format
    ) {
        try {
            if (productIds == null || productIds.size() < 2) {
                return ResponseHelper.error(
                    com.example.ecommerce.ecommerce_backend.api.response.ErrorCode.BAD_REQUEST,
                    "At least 2 product IDs are required for comparison"
                );
            }
            
            AiAssistantRequest request = new AiAssistantRequest();
            request.setQuery("so sánh sản phẩm");
            request.setProductIds(productIds);
            request.setFormat(format);
            
            AiAssistantResponse response = aiAssistantService.processQuery(request);
            return ResponseHelper.ok(response);
        } catch (Exception e) {
            return ResponseHelper.error(
                com.example.ecommerce.ecommerce_backend.api.response.ErrorCode.INTERNAL_ERROR,
                "Failed to compare products: " + e.getMessage()
            );
        }
    }
}
