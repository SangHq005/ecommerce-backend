package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.wishlist.AddToWishlistRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.wishlist.WishlistItemResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.user.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wishlist")
@Tag(name = "Wishlist", description = "User wishlist management")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid User ID");
        }
    }

    @PostMapping
    @Operation(summary = "Add to wishlist", description = "Add product to wishlist")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @Valid @RequestBody AddToWishlistRequest request
    ) {
        WishlistItemResponse response = wishlistService.addToWishlist(
                currentUserId(),
                request.productId(),
                request.note()
        );
        return ResponseHelper.created(response, "Added to wishlist");
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove from wishlist", description = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(currentUserId(), productId);
        return ResponseHelper.ok(null, "Removed from wishlist");
    }

    @GetMapping
    @Operation(summary = "Get wishlist", description = "Get all wishlist items")
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist() {
        List<WishlistItemResponse> wishlist = wishlistService.getUserWishlist(currentUserId());
        return ResponseHelper.ok(wishlist);
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check wishlist", description = "Check if product is in wishlist")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkInWishlist(@PathVariable Long productId) {
        boolean isInWishlist = wishlistService.isInWishlist(currentUserId(), productId);
        return ResponseHelper.ok(Map.of("inWishlist", isInWishlist));
    }

    @GetMapping("/count")
    @Operation(summary = "Wishlist count", description = "Get wishlist item count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getWishlistCount() {
        long count = wishlistService.getWishlistCount(currentUserId());
        return ResponseHelper.ok(Map.of("count", count));
    }

    @DeleteMapping
    @Operation(summary = "Clear wishlist", description = "Remove all items from wishlist")
    public ResponseEntity<ApiResponse<Void>> clearWishlist() {
        wishlistService.clearWishlist(currentUserId());
        return ResponseHelper.ok(null, "Wishlist cleared");
    }
}
