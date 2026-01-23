package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.cart.AddToCartRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.cart.CartResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid User ID in token");
        }
    }

    @GetMapping
    @Operation(summary = "Get cart", description = "Get current user's shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart() {
        CartResponse cart = cartService.getMyCart(currentUserId());
        return ResponseHelper.ok(cart);
    }

    @PostMapping
    @Operation(summary = "Add to cart", description = "Add item to shopping cart")
    public ResponseEntity<ApiResponse<Void>> addToCart(@Valid @RequestBody AddToCartRequest req) {
        cartService.addToCart(currentUserId(), req);
        return ResponseHelper.ok(null, "Item added to cart");
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add item to shopping cart with enhanced validation and stock reservation")
    public ResponseEntity<ApiResponse<Long>> addItemToCart(@Valid @RequestBody AddToCartRequest req) {
        Long cartItemId = cartService.addItem(currentUserId(), req);
        return ResponseHelper.ok(cartItemId, "Item added to cart successfully");
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update quantity", description = "Update cart item quantity")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity
    ) {
        cartService.updateQuantity(currentUserId(), itemId, quantity);
        return ResponseHelper.ok(null, "Cart updated");
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item", description = "Remove item from cart")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable Long itemId) {
        cartService.deleteItem(currentUserId(), itemId);
        return ResponseHelper.ok(null, "Item removed from cart");
    }
}
