package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.OrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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

    @GetMapping("/orders/{orderCode}")
    public OrderResponse get(@PathVariable String orderCode) {
        return orderService.get(currentUserId(), orderCode);
    }

    @PostMapping("/orders/{orderCode}/cancel")
    public void cancel(@PathVariable String orderCode) {
        orderService.cancel(currentUserId(), orderCode);
    }
}
