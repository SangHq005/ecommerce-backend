package com.example.ecommerce.ecommerce_backend.application.service.seller;

import java.time.LocalDateTime;
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

    private static final List<String> COMPLETED_STATUSES = List.of(
            OrderStatus.COMPLETED.name(),
            OrderStatus.DELIVERED.name()
    );

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

        Page<Object[]> rows = orderRepo.aggregateCustomersByShop(shopId, COMPLETED_STATUSES, pageable);
        if (rows.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> userIds = rows.getContent().stream()
                .map(row -> (Long) row[0])
                .toList();
        Map<Long, UserEntity> userMap = userRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<CustomerSummaryResponse> content = rows.getContent().stream()
                .map(row -> toCustomerSummary(row, userMap))
                .toList();

        return new PageImpl<>(content, pageable, rows.getTotalElements());
    }

    private CustomerSummaryResponse toCustomerSummary(Object[] row, Map<Long, UserEntity> userMap) {
        Long userId = (Long) row[0];
        long totalOrders = ((Number) row[1]).longValue();
        long totalSpent = ((Number) row[2]).longValue();
        LocalDateTime lastOrderAt = (LocalDateTime) row[3];

        UserEntity user = userMap.get(userId);
        String email = user != null ? user.getEmail() : "Unknown";
        String name = user != null ? (user.getFullName() != null ? user.getFullName() : email) : "Unknown";
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
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long sellerUserId, Long customerId) {
        Long shopId = getShopId(sellerUserId);

        List<Object[]> statsRows = orderRepo.aggregateCustomerByShopAndUserId(
                shopId, customerId, COMPLETED_STATUSES);
        if (statsRows.isEmpty()) {
            throw ApiException.notFound("Customer not found");
        }

        Object[] stats = statsRows.get(0);
        long totalOrders = ((Number) stats[1]).longValue();
        long totalSpent = ((Number) stats[2]).longValue();
        LocalDateTime firstOrderAt = (LocalDateTime) stats[3];
        LocalDateTime lastOrderAt = (LocalDateTime) stats[4];

        UserEntity user = userRepo.findById(customerId).orElse(null);
        String email = user != null ? user.getEmail() : "Unknown";
        String name = user != null ? (user.getFullName() != null ? user.getFullName() : email) : "Unknown";
        double averageOrderValue = totalOrders > 0 ? (totalSpent * 1.0 / totalOrders) : 0.0;

        Page<OrderEntity> recentOrderPage = orderRepo.findByShopIdAndUserId(
                shopId,
                customerId,
                org.springframework.data.domain.PageRequest.of(0, 10,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
        );
        List<OrderSummaryResponse> recentOrders = mapOrderSummaries(recentOrderPage.getContent());

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
        Page<OrderEntity> orders = orderRepo.findByShopIdAndUserId(shopId, customerId, pageable);
        List<OrderSummaryResponse> content = mapOrderSummaries(orders.getContent());
        return new PageImpl<>(content, pageable, orders.getTotalElements());
    }

    private List<OrderSummaryResponse> mapOrderSummaries(List<OrderEntity> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(OrderEntity::getId).toList();
        Map<Long, Long> itemCountByOrder = orderItemRepo.countItemsByOrderIds(orderIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return orders.stream()
                .map(order -> OrderSummaryResponse.from(
                        order,
                        itemCountByOrder.getOrDefault(order.getId(), 0L).intValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerStatsResponse getCustomerStats(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);

        long totalCustomers = orderRepo.countDistinctCustomersByShopId(shopId);

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newCustomers = orderRepo.countDistinctCustomersByShopIdAndCreatedAtAfter(shopId, startOfMonth);
        long returningCustomers = orderRepo.countReturningCustomersByShopId(shopId);

        Long totalCustomerRevenue = orderRepo.sumTotalAmountByShopIdAndStatusIn(shopId, COMPLETED_STATUSES);
        double averageCustomerValue = totalCustomers > 0
                ? (totalCustomerRevenue * 1.0 / totalCustomers)
                : 0.0;

        return new CustomerStatsResponse(
                totalCustomers,
                newCustomers,
                returningCustomers,
                averageCustomerValue,
                totalCustomerRevenue
        );
    }
}
