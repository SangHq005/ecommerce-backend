package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.ecommerce_backend.api.dto.shop.ShopResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.shop.ShopUpsertRequest;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.storage.ImageUploadService;
import com.example.ecommerce.ecommerce_backend.application.service.seller.ShopService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/seller/shop")
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Shop", description = "Seller shop management")
public class SellerShopController {

    private final ShopService shopService;
    private final ImageUploadService uploadService;

    public SellerShopController(ShopService shopService, ImageUploadService uploadService) {
        this.shopService = shopService;
        this.uploadService = uploadService;
    }

    @GetMapping
    @Operation(summary = "Get shop", description = "Get seller's shop details")
    public ResponseEntity<ApiResponse<ShopResponse>> get(Authentication auth) {
        Long sellerId = Long.valueOf(auth.getName());
        SellerShopEntity s = shopService.getBySeller(sellerId).orElse(null);
        return ResponseHelper.ok(s == null ? null : toResp(s));
    }

    @PostMapping
    @Operation(summary = "Create shop", description = "Create a new shop")
    public ResponseEntity<ApiResponse<ShopResponse>> create(
            Authentication auth,
            @Valid @RequestBody ShopUpsertRequest req
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        SellerShopEntity s = shopService.createDraft(
                sellerId, req.getShopName(), req.getDescription(), req.getCity(), req.getAddress(),
                req.getContactName(), req.getContactPhone(), req.getContactEmail(),
                req.getIdentityCode(), req.getTaxCode(),
                req.getBankName(), req.getBankAccountNumber(), req.getBankAccountName()
        );
        return ResponseHelper.created(toResp(s), "Shop created");
    }

    @PutMapping
    @Operation(summary = "Update shop", description = "Update shop details")
    public ResponseEntity<ApiResponse<ShopResponse>> update(
            Authentication auth,
            @Valid @RequestBody ShopUpsertRequest req
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        SellerShopEntity s = shopService.updateShop(
                sellerId, req.getShopName(), req.getDescription(), req.getCity(), req.getAddress(),
                req.getContactName(), req.getContactPhone(), req.getContactEmail(),
                req.getIdentityCode(), req.getTaxCode(),
                req.getBankName(), req.getBankAccountNumber(), req.getBankAccountName()
        );
        return ResponseHelper.ok(toResp(s), "Shop updated");
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit shop", description = "Submit shop for review")
    public ResponseEntity<ApiResponse<ShopResponse>> submit(Authentication auth) {
        Long sellerId = Long.valueOf(auth.getName());
        SellerShopEntity s = shopService.submitForReview(sellerId);
        return ResponseHelper.ok(toResp(s), "Shop submitted for review");
    }

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    @Operation(summary = "Upload logo", description = "Upload shop logo")
    public ResponseEntity<ApiResponse<ShopResponse>> uploadLogo(
            Authentication auth,
            @RequestPart("file") MultipartFile file
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        String url = uploadService.uploadShopLogo(file).fileUrl();
        SellerShopEntity s = shopService.setLogo(sellerId, url);
        return ResponseHelper.ok(toResp(s), "Logo uploaded");
    }

    @PostMapping(value = "/banner", consumes = "multipart/form-data")
    @Operation(summary = "Upload banner", description = "Upload shop banner")
    public ResponseEntity<ApiResponse<ShopResponse>> uploadBanner(
            Authentication auth,
            @RequestPart("file") MultipartFile file
    ) {
        Long sellerId = Long.valueOf(auth.getName());
        String url = uploadService.uploadShopBanner(file).fileUrl();
        SellerShopEntity s = shopService.setBanner(sellerId, url);
        return ResponseHelper.ok(toResp(s), "Banner uploaded");
    }

    private ShopResponse toResp(SellerShopEntity s) {
        return new ShopResponse(
                s.getId(), s.getSellerUserId(), s.getShopName(), s.getShopSlug(),
                s.getDescription(), s.getLogoUrl(), s.getBannerUrl(), s.getStatus(),
                s.getCity(), s.getAddress(),
                s.getContactName(), s.getContactPhone(), s.getContactEmail(),
                s.getIdentityCode(), s.getTaxCode(),
                s.getBankName(), s.getBankAccountNumber(), s.getBankAccountName()
        );
    }
}
