package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Orders", description = "Admin order management")
public class AdminOrderController {

    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final UserJpaRepository userRepo;
    private final SellerShopJpaRepository shopRepo;

    public AdminOrderController(
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository orderItemRepo,
            UserJpaRepository userRepo,
            SellerShopJpaRepository shopRepo
    ) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.userRepo = userRepo;
        this.shopRepo = shopRepo;
    }

    @GetMapping
    @Operation(summary = "List all orders", description = "Get paginated list of all orders for admin")
    public ResponseEntity<ApiResponse<List<AdminOrderResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderEntity> orders;
        if (status != null && !status.isBlank() && !"ALL".equals(status)) {
            orders = orderRepo.findByStatus(status, pageable);
        } else {
            orders = orderRepo.findAll(pageable);
        }
        
        // Map to response DTOs
        Page<AdminOrderResponse> responsePage = orders.map(this::mapToAdminOrderResponse);
        
        return ResponseHelper.page(responsePage);
    }

    private AdminOrderResponse mapToAdminOrderResponse(OrderEntity order) {
        // Get user info
        UserEntity user = userRepo.findById(order.getUserId()).orElse(null);
        String customerName = user != null ? (user.getFullName() != null ? user.getFullName() : user.getEmail()) : "N/A";
        String customerEmail = user != null ? user.getEmail() : "N/A";
        
        // Get shop info
        SellerShopEntity shop = shopRepo.findById(order.getShopId()).orElse(null);
        String shopName = shop != null ? shop.getShopName() : "Shop #" + order.getShopId();
        
        // Get item count
        List<OrderItemEntity> items = orderItemRepo.findByOrderId(order.getId());
        int itemCount = items.stream().mapToInt(OrderItemEntity::getQuantity).sum();
        
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderCode(),
                customerName,
                customerEmail,
                shopName,
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD",
                itemCount,
                order.getCreatedAt(),
                "" // shippingAddress - can be enhanced later
        );
    }

    public record AdminOrderResponse(
            Long id,
            String orderCode,
            String customerName,
            String customerEmail,
            String shopName,
            Long totalAmount,
            String status,
            String paymentMethod,
            Integer itemCount,
            java.time.LocalDateTime createdAt,
            String shippingAddress
    ) {}
}
