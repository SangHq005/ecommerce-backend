package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.shop.ShopResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.shop.ShopUpsertRequest;
import com.example.ecommerce.ecommerce_backend.application.service.LocalFileStorageService;
import com.example.ecommerce.ecommerce_backend.application.service.ShopService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/seller/shop")
public class SellerShopController {

    private final ShopService shopService;
    private final LocalFileStorageService storage;

    public SellerShopController(ShopService shopService, LocalFileStorageService storage) {
        this.shopService = shopService;
        this.storage = storage;
    }

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ShopResponse> get(Authentication auth) {
        Long sellerId = Long.valueOf(auth.getName());
        SellerShopEntity s = shopService.getBySeller(sellerId).orElse(null);
        return ResponseEntity.ok(s == null ? null : toResp(s));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ShopResponse create(Authentication auth, @Valid @RequestBody ShopUpsertRequest req) {
        Long sellerId = Long.valueOf(auth.getName());
        SellerShopEntity s = shopService.createDraft(sellerId, req.shopName(), req.description());
        return toResp(s);
    }

    @PutMapping
    @PreAuthorize("hasRole('SELLER')")
    public ShopResponse update(Authentication auth, @Valid @RequestBody ShopUpsertRequest req) {
        Long sellerId = Long.valueOf(auth.getName());
        SellerShopEntity s = shopService.updateShop(sellerId, req.shopName(), req.description());
        return toResp(s);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('SELLER')")
    public ShopResponse submit(Authentication auth) {
        Long sellerId = Long.valueOf(auth.getName());
        return toResp(shopService.submitForReview(sellerId));
    }

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SELLER')")
    public ShopResponse uploadLogo(Authentication auth, @RequestPart("file") MultipartFile file) {
        Long sellerId = Long.valueOf(auth.getName());
        String url = storage.save("shop-logos", file);
        return toResp(shopService.setLogo(sellerId, url));
    }

    @PostMapping(value = "/banner", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SELLER')")
    public ShopResponse uploadBanner(Authentication auth, @RequestPart("file") MultipartFile file) {
        Long sellerId = Long.valueOf(auth.getName());
        String url = storage.save("shop-banners", file);
        return toResp(shopService.setBanner(sellerId, url));
    }

    private ShopResponse toResp(SellerShopEntity s) {
        return new ShopResponse(
                s.getId(), s.getSellerUserId(), s.getShopName(), s.getShopSlug(),
                s.getDescription(), s.getLogoUrl(), s.getBannerUrl(), s.getStatus()
        );
    }
}
