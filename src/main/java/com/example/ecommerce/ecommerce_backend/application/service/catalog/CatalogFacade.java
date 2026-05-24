package com.example.ecommerce.ecommerce_backend.application.service.catalog;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductDetailsResponse;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;

@Service
public class CatalogFacade {

    public record OptionGroupSpec(String name, int sortOrder, List<String> values) {}
    public record SkuSpec(String skuCode, String optionSignature, long price, Long compareAtPrice, int stockOnHand, boolean active, String imageUrl) {}

    private final ProductQueryService queryService;
    private final ProductWriteService writeService;

    public CatalogFacade(ProductQueryService queryService, ProductWriteService writeService) {
        this.queryService = queryService;
        this.writeService = writeService;
    }

    // --- Query Operations ---
    
    public List<CategoryEntity> listCategories() { 
        return queryService.listCategories(); 
    }
    
    public List<BrandEntity> listBrands() { 
        return queryService.listBrands(); 
    }

    public Page<ProductEntity> listActiveProducts(Long categoryId, Long brandId, Pageable pageable) {
        return queryService.listActiveProducts(categoryId, brandId, pageable);
    }

    public List<ProductEntity> getTopDeals(int limit) {
        return queryService.getTopDeals(limit);
    }

    public ProductEntity getActiveProduct(Long id) {
        return queryService.getActiveProduct(id);
    }

    public ProductDetailsResponse getProductDetail(Long id) {
        return queryService.getProductDetail(id);
    }
    
    public ProductDetailsResponse adminGetProductDetail(Long id) {
        return queryService.adminGetProductDetail(id);
    }
    
    public ProductDetailsResponse getProductDetailByIdOrSlug(String idOrSlug) {
        return queryService.getProductDetailByIdOrSlug(idOrSlug);
    }
    
    public ProductDetailsResponse sellerGetProductDetail(Long sellerUserId, Long productId) {
        return queryService.sellerGetProductDetail(sellerUserId, productId);
    }

    public Page<ProductEntity> sellerListProducts(Long sellerUserId, String status, Pageable pageable) {
        return queryService.sellerListProducts(sellerUserId, status, pageable);
    }

    public List<ProductEntity> sellerListProducts(Long sellerUserId) {
        return queryService.sellerListProducts(sellerUserId);
    }

    public List<ProductEntity> adminListByStatus(String status) {
        return queryService.adminListByStatus(status);
    }

    public Page<ProductEntity> adminListProducts(String status, String search, Pageable pageable) {
        return queryService.adminListProducts(status, search, pageable);
    }

    // --- Write Operations ---

    public ProductEntity sellerCreateDraft(Long sellerUserId, Long categoryId, Long brandId, String name, String desc, String mainImageUrl, Long price, Long originalPrice, Integer stockQuantity) {
        return writeService.sellerCreateDraft(sellerUserId, categoryId, brandId, name, desc, mainImageUrl, price, originalPrice, stockQuantity);
    }

    public ProductEntity sellerUpdate(Long sellerUserId, Long productId, Long categoryId, Long brandId, String name, String desc, String mainImageUrl, Long price, Long originalPrice, Integer stockQuantity) {
        return writeService.sellerUpdate(sellerUserId, productId, categoryId, brandId, name, desc, mainImageUrl, price, originalPrice, stockQuantity);
    }

    public ProductImageEntity sellerUpsertImage(Long sellerUserId, Long productId, int sortOrder, String imageUrl) {
        return writeService.sellerUpsertImage(sellerUserId, productId, sortOrder, imageUrl);
    }

    public void sellerDeleteImage(Long sellerUserId, Long productId, Long imageId) {
        writeService.sellerDeleteImage(sellerUserId, productId, imageId);
    }

    public void sellerSetOptions(Long sellerUserId, Long productId, List<OptionGroupSpec> groups) {
        writeService.sellerSetOptions(sellerUserId, productId, groups);
    }

    public List<SkuEntity> sellerUpsertSkus(Long sellerUserId, Long productId, List<SkuSpec> skus) {
        return writeService.sellerUpsertSkus(sellerUserId, productId, skus);
    }

    public ProductEntity sellerSubmit(Long sellerUserId, Long productId) {
        return writeService.sellerSubmit(sellerUserId, productId);
    }

    public ProductEntity sellerDeactivate(Long sellerUserId, Long productId) {
        return writeService.sellerDeactivate(sellerUserId, productId);
    }

    public ProductEntity sellerHide(Long sellerUserId, Long productId) {
        return writeService.sellerHide(sellerUserId, productId);
    }

    public ProductEntity sellerShow(Long sellerUserId, Long productId) {
        return writeService.sellerShow(sellerUserId, productId);
    }

    public ProductEntity adminApprove(Long productId) {
        return writeService.adminApprove(productId);
    }

    public ProductEntity adminReject(Long productId, String reason) {
        return writeService.adminReject(productId, reason);
    }

    public ProductEntity adminHide(Long productId, String reason) {
        return writeService.adminHide(productId, reason);
    }
}
