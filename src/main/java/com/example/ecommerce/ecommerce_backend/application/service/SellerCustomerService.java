package com.example.ecommerce.ecommerce_backend.application.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerDetailResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;

@Service
public class SellerCustomerService {

    private final SellerShopJpaRepository shopRepo;
    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final UserJpaRepository userRepo;

    public SellerCustomerService(
            SellerShopJpaRepository shopRepo,
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository orderItemRepo,
            UserJpaRepository userRepo
    ) {
        this.shopRepo = shopRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.userRepo = userRepo;
    }

    private Long getShopId(Long sellerUserId) {
        SellerShopEntity shop = shopRepo.findBySellerUserId(sellerUserId)
                .orElseThrow(() -> ApiException.notFound("Shop not found"));
        return shop.getId();
    }

    @Transactional(readOnly = true)
    public Page<CustomerSummaryResponse> getCustomers(Long sellerUserId, Pageable pageable) {
        Long shopId = getShopId(sellerUserId);
        
        // Get all orders for this shop
        List<OrderEntity> allOrders = orderRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        
        // Group orders by customer
        Map<Long, List<OrderEntity>> ordersByCustomer = allOrders.stream()
                .collect(Collectors.groupingBy(OrderEntity::getUserId));
        
        // Calculate customer summaries
        List<CustomerSummaryResponse> customers = ordersByCustomer.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<OrderEntity> customerOrders = entry.getValue();
                    
                    UserEntity user = userRepo.findById(userId).orElse(null);
                    String email = user != null ? user.getEmail() : "Unknown";
                    String name = user != null ? (user.getFullName() != null ? user.getFullName() : email) : "Unknown";
                    
                    long totalOrders = customerOrders.size();
                    
                    List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
                    long totalSpent = customerOrders.stream()
                            .filter(o -> completedStatuses.contains(o.getStatus()))
                            .mapToLong(OrderEntity::getTotalAmount)
                            .sum();
                    
                    LocalDateTime lastOrderAt = customerOrders.stream()
                            .map(OrderEntity::getCreatedAt)
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                    
                    double averageOrderValue = totalOrders > 0 ? (totalSpent * 1.0 / totalOrders) : 0.0;
                    
                    return new CustomerSummaryResponse(
                            userId,
                            email,
                            name,
                            totalOrders,
                            totalSpent,
                            lastOrderAt,
                            averageOrderValue,
                            totalSpent
                    );
                })
                .sorted(Comparator.comparing(CustomerSummaryResponse::totalSpent).reversed())
                .collect(Collectors.toList());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), customers.size());
        List<CustomerSummaryResponse> pageContent = customers.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, customers.size());
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long sellerUserId, Long customerId) {
        Long shopId = getShopId(sellerUserId);
        
        // Get all orders for this customer and shop
        List<OrderEntity> customerOrders = orderRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .filter(o -> o.getUserId().equals(customerId))
                .collect(Collectors.toList());
        
        if (customerOrders.isEmpty()) {
            throw ApiException.notFound("Customer not found");
        }
        
        UserEntity user = userRepo.findById(customerId).orElse(null);
        String email = user != null ? user.getEmail() : "Unknown";
        String name = user != null ? (user.getFullName() != null ? user.getFullName() : email) : "Unknown";
        
        long totalOrders = customerOrders.size();
        
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        long totalSpent = customerOrders.stream()
                .filter(o -> completedStatuses.contains(o.getStatus()))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        LocalDateTime firstOrderAt = customerOrders.stream()
                .map(OrderEntity::getCreatedAt)
                .min(Comparator.naturalOrder())
                .orElse(null);
        
        LocalDateTime lastOrderAt = customerOrders.stream()
                .map(OrderEntity::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
        
        double averageOrderValue = totalOrders > 0 ? (totalSpent * 1.0 / totalOrders) : 0.0;
        
        // Get recent orders (last 10)
        List<OrderSummaryResponse> recentOrders = customerOrders.stream()
                .sorted(Comparator.comparing(OrderEntity::getCreatedAt).reversed())
                .limit(10)
                .map(order -> {
                    int itemCount = orderItemRepo.findByOrderId(order.getId()).size();
                    return OrderSummaryResponse.from(order, itemCount);
                })
                .collect(Collectors.toList());
        
        return new CustomerDetailResponse(
                customerId,
                email,
                name,
                totalOrders,
                totalSpent,
                firstOrderAt,
                lastOrderAt,
                averageOrderValue,
                totalSpent,
                recentOrders
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getCustomerOrders(Long sellerUserId, Long customerId, Pageable pageable) {
        Long shopId = getShopId(sellerUserId);
        
        // Get orders for this customer and shop
        List<OrderEntity> customerOrders = orderRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .filter(o -> o.getUserId().equals(customerId))
                .sorted(Comparator.comparing(OrderEntity::getCreatedAt).reversed())
                .collect(Collectors.toList());
        
        List<OrderSummaryResponse> orderSummaries = customerOrders.stream()
                .map(order -> {
                    int itemCount = orderItemRepo.findByOrderId(order.getId()).size();
                    return OrderSummaryResponse.from(order, itemCount);
                })
                .collect(Collectors.toList());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), orderSummaries.size());
        List<OrderSummaryResponse> pageContent = orderSummaries.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, orderSummaries.size());
    }

    @Transactional(readOnly = true)
    public CustomerStatsResponse getCustomerStats(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);
        
        // Get all orders for this shop
        List<OrderEntity> allOrders = orderRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        
        // Group orders by customer
        Map<Long, List<OrderEntity>> ordersByCustomer = allOrders.stream()
                .collect(Collectors.groupingBy(OrderEntity::getUserId));
        
        long totalCustomers = ordersByCustomer.size();
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newCustomers = ordersByCustomer.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime firstOrder = entry.getValue().stream()
                            .map(OrderEntity::getCreatedAt)
                            .min(Comparator.naturalOrder())
                            .orElse(null);
                    return firstOrder != null && firstOrder.isAfter(startOfMonth);
                })
                .count();
        
        long returningCustomers = ordersByCustomer.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .count();
        
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        long totalCustomerRevenue = allOrders.stream()
                .filter(o -> completedStatuses.contains(o.getStatus()))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        double averageCustomerValue = totalCustomers > 0 ? (totalCustomerRevenue * 1.0 / totalCustomers) : 0.0;
        
        return new CustomerStatsResponse(
                totalCustomers,
                newCustomers,
                returningCustomers,
                averageCustomerValue,
                totalCustomerRevenue
        );
    }
}
