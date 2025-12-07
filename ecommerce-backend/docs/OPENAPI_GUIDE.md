# OpenAPI / Swagger Documentation Guide

This guide shows how to add comprehensive API documentation using OpenAPI 3.0 annotations.

## Dependencies

Already included in `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Access Swagger UI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Common Annotations

### Controller Level

```java
@Tag(name = "Products", description = "Product management APIs")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    // ...
}
```

### Operation (Method) Level

```java
@Operation(
    summary = "Get product by ID",
    description = "Returns a single product by its ID. Requires authentication.",
    tags = {"Products"}
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Product found successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProductResponse.class)
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Product not found",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiError.class)
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Invalid or missing token"
    )
})
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getProduct(
    @Parameter(description = "Product ID", required = true, example = "1")
    @PathVariable Long id
) {
    // Implementation
}
```

### Request Body

```java
@PostMapping
public ResponseEntity<ProductResponse> createProduct(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Product details to create",
        required = true,
        content = @Content(
            schema = @Schema(implementation = CreateProductRequest.class),
            examples = @ExampleObject(
                name = "Create Product Example",
                value = """
                    {
                        "name": "iPhone 15 Pro",
                        "description": "Latest iPhone model",
                        "categoryId": 1,
                        "brandId": 2,
                        "price": 29990000,
                        "stock": 100
                    }
                    """
            )
        )
    )
    @Valid @RequestBody CreateProductRequest request
) {
    // Implementation
}
```

### Parameters

```java
@GetMapping
public ResponseEntity<PageResponse<ProductResponse>> listProducts(
    @Parameter(description = "Page number (0-indexed)", example = "0")
    @RequestParam(defaultValue = "0") int page,

    @Parameter(description = "Page size", example = "20")
    @RequestParam(defaultValue = "20") int size,

    @Parameter(description = "Category filter", example = "smartphones")
    @RequestParam(required = false) String category,

    @Parameter(description = "Sort by field", schema = @Schema(allowableValues = {"name", "price", "createdAt"}))
    @RequestParam(defaultValue = "createdAt") String sortBy
) {
    // Implementation
}
```

## Complete Controller Example

```java
package com.example.ecommerce.ecommerce_backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Products",
    description = "Product catalog management. " +
                  "Public endpoints for browsing, authenticated endpoints for cart/wishlist, " +
                  "seller endpoints for managing own products, " +
                  "admin endpoints for moderation."
)
@RestController
@RequestMapping("/api/v1/products")
public class ProductControllerExample {

    @Operation(
        summary = "List all products",
        description = "Get paginated list of products with optional filters. " +
                      "Public endpoint - no authentication required.",
        tags = {"Products"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> listProducts(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Items per page", example = "20")
        @RequestParam(defaultValue = "20") int size,

        @Parameter(description = "Filter by category ID")
        @RequestParam(required = false) Long categoryId,

        @Parameter(description = "Filter by brand ID")
        @RequestParam(required = false) Long brandId,

        @Parameter(description = "Minimum price filter")
        @RequestParam(required = false) Long minPrice,

        @Parameter(description = "Maximum price filter")
        @RequestParam(required = false) Long maxPrice,

        @Parameter(description = "Search query in product name/description")
        @RequestParam(required = false) String search,

        @Parameter(
            description = "Sort field",
            schema = @Schema(
                type = "string",
                allowableValues = {"name", "price", "createdAt", "rating"}
            )
        )
        @RequestParam(defaultValue = "createdAt") String sortBy,

        @Parameter(
            description = "Sort direction",
            schema = @Schema(type = "string", allowableValues = {"asc", "desc"})
        )
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Implementation
        return ResponseEntity.ok(new PageResponse<>());
    }

    @Operation(
        summary = "Get product details",
        description = "Get detailed information about a specific product including images, variants, and reviews",
        tags = {"Products"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDetailResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProduct(
        @Parameter(description = "Product ID", required = true, example = "123")
        @PathVariable Long id
    ) {
        // Implementation
        return ResponseEntity.ok(new ProductDetailResponse());
    }

    @Operation(
        summary = "Create new product",
        description = "Create a new product. Only accessible by sellers for their own shop.",
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {"Products", "Seller"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid product data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not a seller")
    })
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Product information",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateProductRequest.class),
                examples = @ExampleObject(
                    name = "iPhone Example",
                    summary = "Example of creating an iPhone product",
                    value = """
                        {
                            "name": "iPhone 15 Pro Max 256GB",
                            "description": "Apple iPhone 15 Pro Max with 256GB storage, A17 Pro chip, titanium design",
                            "categoryId": 1,
                            "brandId": 2,
                            "price": 32990000,
                            "stock": 50,
                            "images": [
                                {"url": "https://example.com/iphone-front.jpg", "sortOrder": 0},
                                {"url": "https://example.com/iphone-back.jpg", "sortOrder": 1}
                            ],
                            "tags": ["smartphone", "apple", "5g", "premium"]
                        }
                        """
                )
            )
        )
        @Valid @RequestBody CreateProductRequest request
    ) {
        // Implementation
        return ResponseEntity.status(201).body(new ProductResponse());
    }

    @Operation(
        summary = "Update product",
        description = "Update existing product. Only accessible by product owner or admin.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update this product")
    })
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @Parameter(description = "Product ID") @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        // Implementation
        return ResponseEntity.ok(new ProductResponse());
    }

    @Operation(
        summary = "Delete product",
        description = "Soft delete a product. Admin only.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // Implementation
        return ResponseEntity.noContent().build();
    }
}
```

## Schema Documentation for DTOs

```java
@Schema(description = "Product response object")
public record ProductResponse(
    @Schema(description = "Product ID", example = "123")
    Long id,

    @Schema(description = "Product name", example = "iPhone 15 Pro")
    String name,

    @Schema(description = "Product description")
    String description,

    @Schema(description = "Price in VND", example = "29990000")
    Long price,

    @Schema(description = "Available stock", example = "100")
    Integer stock,

    @Schema(description = "Product status", allowableValues = {"DRAFT", "ACTIVE", "INACTIVE"})
    String status,

    @Schema(description = "Average rating (0-5)", example = "4.5", minimum = "0", maximum = "5")
    Double rating,

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00Z")
    Instant createdAt
) {}
```

## Global Security Configuration

Add to `OpenApiConfig.java`:

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Backend API")
                        .version("1.0.0")
                        .description("REST API for e-commerce platform")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@ecommerce.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.ecommerce.com").description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication")));
    }
}
```

## Best Practices

1. **Always add @Operation** - Every endpoint should have a summary and description
2. **Document all responses** - Include success and error cases
3. **Use examples** - Provide realistic example values
4. **Tag logically** - Group related endpoints with tags
5. **Document security** - Specify which endpoints need authentication
6. **Keep descriptions current** - Update docs when code changes
7. **Use schema validation** - Define allowable values, min/max, patterns

## Action Items

Add OpenAPI annotations to:
- ✅ AuthController - Login, Register, Refresh endpoints
- ⚠️ ProductController - All CRUD operations
- ⚠️ OrderController - Checkout, Order management
- ⚠️ CartController - Cart operations
- ⚠️ All other controllers

Run `mvn compile` and visit `/swagger-ui.html` to verify documentation.
