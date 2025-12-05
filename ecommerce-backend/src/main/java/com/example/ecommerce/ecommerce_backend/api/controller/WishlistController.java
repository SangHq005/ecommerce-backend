package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.MessageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.wishlist.AddToWishlistRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.wishlist.WishlistItemResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Invalid User ID");
        }
    }

    /**
     * Add product to wishlist
     */
    @PostMapping
    public ResponseEntity<WishlistItemResponse> addToWishlist(@Valid @RequestBody AddToWishlistRequest request) {
        WishlistItemResponse response = wishlistService.addToWishlist(
                currentUserId(),
                request.productId(),
                request.note()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Remove product from wishlist
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<MessageResponse> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(currentUserId(), productId);
        return ResponseEntity.ok(new MessageResponse("Product removed from wishlist"));
    }

    /**
     * Get all wishlist items
     */
    @GetMapping
    public ResponseEntity<List<WishlistItemResponse>> getWishlist() {
        List<WishlistItemResponse> wishlist = wishlistService.getUserWishlist(currentUserId());
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Check if product is in wishlist
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkInWishlist(@PathVariable Long productId) {
        boolean isInWishlist = wishlistService.isInWishlist(currentUserId(), productId);
        return ResponseEntity.ok(Map.of("inWishlist", isInWishlist));
    }

    /**
     * Get wishlist count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWishlistCount() {
        long count = wishlistService.getWishlistCount(currentUserId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Clear entire wishlist
     */
    @DeleteMapping
    public ResponseEntity<MessageResponse> clearWishlist() {
        wishlistService.clearWishlist(currentUserId());
        return ResponseEntity.ok(new MessageResponse("Wishlist cleared"));
    }
}
