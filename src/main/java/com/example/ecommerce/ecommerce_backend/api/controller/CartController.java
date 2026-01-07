package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.cart.AddToCartRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.cart.CartResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
             throw ApiException.unauthorized("Invalid User ID in token");
        }
    }

    @GetMapping
    public CartResponse getMyCart() {
        return cartService.getMyCart(currentUserId());
    }

    @PostMapping
    public void addToCart(@Valid @RequestBody AddToCartRequest req) {
        cartService.addToCart(currentUserId(), req);
    }

    @PutMapping("/items/{itemId}")
    public void updateQuantity(@PathVariable Long itemId, @RequestParam Integer quantity) {
        cartService.updateQuantity(currentUserId(), itemId, quantity);
    }

    @DeleteMapping("/items/{itemId}")
    public void removeItem(@PathVariable Long itemId) {
        cartService.deleteItem(currentUserId(), itemId);
    }
}
