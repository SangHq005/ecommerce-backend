package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.AdminCatalogManagementService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeGroupEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/admin/catalog")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Catalog Management", description = "Admin catalog management (Categories, Brands, Attributes)")
public class AdminCatalogManagementController {

    private final AdminCatalogManagementService catalogService;

    public AdminCatalogManagementController(AdminCatalogManagementService catalogService) {
        this.catalogService = catalogService;
    }

    // ============ CATEGORIES ============
    
    @GetMapping("/categories")
    @Operation(summary = "List categories", description = "Get all categories with pagination")
    public ResponseEntity<ApiResponse<List<CategoryEntity>>> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder", "id"));
        Page<CategoryEntity> categories = catalogService.listCategories(pageable);
        return ResponseHelper.page(categories);
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category", description = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryEntity>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CategoryEntity category = catalogService.createCategory(
            request.name(),
            request.parentId(),
            request.sortOrder()
        );
        return ResponseHelper.created(category, "Category created successfully");
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryEntity>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        CategoryEntity category = catalogService.updateCategory(
            id,
            request.name(),
            request.parentId(),
            request.sortOrder(),
            request.active()
        );
        return ResponseHelper.ok(category, "Category updated successfully");
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category", description = "Delete a category (soft delete by setting inactive)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        catalogService.deleteCategory(id);
        return ResponseHelper.ok(null, "Category deleted successfully");
    }

    // ============ BRANDS ============
    
    @GetMapping("/brands")
    @Operation(summary = "List brands", description = "Get all brands with pagination")
    public ResponseEntity<ApiResponse<List<BrandEntity>>> listBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<BrandEntity> brands = catalogService.listBrands(pageable);
        return ResponseHelper.page(brands);
    }

    @PostMapping("/brands")
    @Operation(summary = "Create brand", description = "Create a new brand")
    public ResponseEntity<ApiResponse<BrandEntity>> createBrand(
            @Valid @RequestBody CreateBrandRequest request
    ) {
        BrandEntity brand = catalogService.createBrand(
            request.name(),
            request.logoUrl()
        );
        return ResponseHelper.created(brand, "Brand created successfully");
    }

    @PutMapping("/brands/{id}")
    @Operation(summary = "Update brand", description = "Update an existing brand")
    public ResponseEntity<ApiResponse<BrandEntity>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBrandRequest request
    ) {
        BrandEntity brand = catalogService.updateBrand(
            id,
            request.name(),
            request.logoUrl(),
            request.active()
        );
        return ResponseHelper.ok(brand, "Brand updated successfully");
    }

    @DeleteMapping("/brands/{id}")
    @Operation(summary = "Delete brand", description = "Delete a brand (soft delete by setting inactive)")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
        catalogService.deleteBrand(id);
        return ResponseHelper.ok(null, "Brand deleted successfully");
    }

    // ============ ATTRIBUTE GROUPS ============
    
    @GetMapping("/attribute-groups")
    @Operation(summary = "List attribute groups", description = "Get all attribute groups")
    public ResponseEntity<ApiResponse<List<AttributeGroupEntity>>> listAttributeGroups() {
        List<AttributeGroupEntity> groups = catalogService.listAttributeGroups();
        return ResponseHelper.ok(groups);
    }

    @PostMapping("/attribute-groups")
    @Operation(summary = "Create attribute group", description = "Create a new attribute group")
    public ResponseEntity<ApiResponse<AttributeGroupEntity>> createAttributeGroup(
            @Valid @RequestBody CreateAttributeGroupRequest request
    ) {
        AttributeGroupEntity group = catalogService.createAttributeGroup(
            request.name(),
            request.description(),
            request.sortOrder()
        );
        return ResponseHelper.created(group, "Attribute group created successfully");
    }

    @PutMapping("/attribute-groups/{id}")
    @Operation(summary = "Update attribute group", description = "Update an existing attribute group")
    public ResponseEntity<ApiResponse<AttributeGroupEntity>> updateAttributeGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeGroupRequest request
    ) {
        AttributeGroupEntity group = catalogService.updateAttributeGroup(
            id,
            request.name(),
            request.description(),
            request.sortOrder()
        );
        return ResponseHelper.ok(group, "Attribute group updated successfully");
    }

    @DeleteMapping("/attribute-groups/{id}")
    @Operation(summary = "Delete attribute group", description = "Delete an attribute group")
    public ResponseEntity<ApiResponse<Void>> deleteAttributeGroup(@PathVariable Long id) {
        catalogService.deleteAttributeGroup(id);
        return ResponseHelper.ok(null, "Attribute group deleted successfully");
    }

    // ============ ATTRIBUTES ============
    
    @GetMapping("/attributes")
    @Operation(summary = "List attributes", description = "Get all attributes with pagination")
    public ResponseEntity<ApiResponse<List<AttributeEntity>>> listAttributes(
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder", "id"));
        Page<AttributeEntity> attributes = catalogService.listAttributes(groupId, pageable);
        return ResponseHelper.page(attributes);
    }

    @PostMapping("/attributes")
    @Operation(summary = "Create attribute", description = "Create a new attribute")
    public ResponseEntity<ApiResponse<AttributeEntity>> createAttribute(
            @Valid @RequestBody CreateAttributeRequest request
    ) {
        AttributeEntity attribute = catalogService.createAttribute(
            request.attributeGroupId(),
            request.name(),
            request.dataType(),
            request.unit(),
            request.description(),
            request.sortOrder(),
            request.isFilterable(),
            request.isComparable()
        );
        return ResponseHelper.created(attribute, "Attribute created successfully");
    }

    @PutMapping("/attributes/{id}")
    @Operation(summary = "Update attribute", description = "Update an existing attribute")
    public ResponseEntity<ApiResponse<AttributeEntity>> updateAttribute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeRequest request
    ) {
        AttributeEntity attribute = catalogService.updateAttribute(
            id,
            request.name(),
            request.dataType(),
            request.unit(),
            request.description(),
            request.sortOrder(),
            request.isFilterable(),
            request.isComparable(),
            request.isActive()
        );
        return ResponseHelper.ok(attribute, "Attribute updated successfully");
    }

    @DeleteMapping("/attributes/{id}")
    @Operation(summary = "Delete attribute", description = "Delete an attribute")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(@PathVariable Long id) {
        catalogService.deleteAttribute(id);
        return ResponseHelper.ok(null, "Attribute deleted successfully");
    }

    // ============ REQUEST DTOs ============
    
    public record CreateCategoryRequest(
            @NotBlank String name,
            Long parentId,
            Integer sortOrder
    ) {}

    public record UpdateCategoryRequest(
            @NotBlank String name,
            Long parentId,
            Integer sortOrder,
            Boolean active
    ) {}

    public record CreateBrandRequest(
            @NotBlank String name,
            String logoUrl
    ) {}

    public record UpdateBrandRequest(
            @NotBlank String name,
            String logoUrl,
            Boolean active
    ) {}

    public record CreateAttributeGroupRequest(
            @NotBlank String name,
            String description,
            Integer sortOrder
    ) {}

    public record UpdateAttributeGroupRequest(
            @NotBlank String name,
            String description,
            Integer sortOrder
    ) {}

    public record CreateAttributeRequest(
            @NotNull Long attributeGroupId,
            @NotBlank String name,
            AttributeEntity.DataType dataType,
            String unit,
            String description,
            Integer sortOrder,
            Boolean isFilterable,
            Boolean isComparable
    ) {}

    public record UpdateAttributeRequest(
            @NotBlank String name,
            AttributeEntity.DataType dataType,
            String unit,
            String description,
            Integer sortOrder,
            Boolean isFilterable,
            Boolean isComparable,
            Boolean isActive
    ) {}
}
