package com.example.ecommerce.ecommerce_backend.application.service.seller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.config.CacheConfig;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.AnalyticsOverviewResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.RevenueChartResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.TopProductsResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;

@Service
public class SellerAnalyticsService {

    private static final List<String> COMPLETED_STATUSES = List.of(
            OrderStatus.COMPLETED.name(),
            OrderStatus.DELIVERED.name()
    );

    private final SellerShopJpaRepository shopRepo;
    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final ProductJpaRepository productRepo;

    public SellerAnalyticsService(
            SellerShopJpaRepository shopRepo,
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository orderItemRepo,
            ProductJpaRepository productRepo
    ) {
        this.shopRepo = shopRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productRepo = productRepo;
    }

    private Long getShopId(Long sellerUserId) {
        SellerShopEntity shop = shopRepo.findBySellerUserId(sellerUserId)
                .orElseThrow(() -> ApiException.notFound("Shop not found"));
        return shop.getId();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_SELLER_ANALYTICS, key = "'overview:' + #sellerUserId")
    public AnalyticsOverviewResponse getOverview(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfWeek = startOfDay.minusDays(now.getDayOfWeek().getValue() - 1L);
        LocalDateTime startOfMonth = startOfDay.withDayOfMonth(1);
        LocalDateTime lastMonthStart = startOfMonth.minusMonths(1);
        LocalDateTime lastMonthEnd = startOfMonth.minusSeconds(1);

        Long totalRevenue = orderRepo.sumTotalAmountByShopIdAndStatusIn(shopId, COMPLETED_STATUSES);
        Long todayRevenue = orderRepo.sumTotalAmountByShopIdAndStatusInAndCreatedAtAfter(
                shopId, COMPLETED_STATUSES, startOfDay);
        Long thisWeekRevenue = orderRepo.sumTotalAmountByShopIdAndStatusInAndCreatedAtAfter(
                shopId, COMPLETED_STATUSES, startOfWeek);
        Long thisMonthRevenue = orderRepo.sumTotalAmountByShopIdAndStatusInAndCreatedAtAfter(
                shopId, COMPLETED_STATUSES, startOfMonth);
        Long lastMonthRevenue = orderRepo.sumTotalAmountByShopIdAndStatusInAndCreatedAtBetween(
                shopId, COMPLETED_STATUSES, lastMonthStart, lastMonthEnd);

        Double revenueGrowth = lastMonthRevenue != null && lastMonthRevenue > 0
                ? ((thisMonthRevenue - lastMonthRevenue) * 100.0 / lastMonthRevenue)
                : 0.0;

        long totalOrders = orderRepo.countByShopId(shopId);
        long todayOrders = orderRepo.countByShopIdAndCreatedAtAfter(shopId, startOfDay);
        long thisWeekOrders = orderRepo.countByShopIdAndCreatedAtAfter(shopId, startOfWeek);
        long thisMonthOrders = orderRepo.countByShopIdAndCreatedAtAfter(shopId, startOfMonth);
        long lastMonthOrders = orderRepo.countByShopIdAndCreatedAtBetween(shopId, lastMonthStart, lastMonthEnd);

        Double orderGrowth = lastMonthOrders > 0
                ? ((thisMonthOrders - lastMonthOrders) * 100.0 / lastMonthOrders)
                : 0.0;

        Double averageOrderValue = thisMonthOrders > 0
                ? (thisMonthRevenue * 1.0 / thisMonthOrders)
                : 0.0;

        long totalProducts = productRepo.countByShopIdAndSellerUserId(shopId, sellerUserId);
        long activeProducts = productRepo.countByShopIdAndStatus(shopId, "ACTIVE");

        List<AnalyticsOverviewResponse.ProductMetrics.TopProduct> topProducts =
                loadTopProductsForOverview(shopId, 5);

        long totalCustomers = orderRepo.countDistinctCustomersByShopId(shopId);
        long newCustomers = orderRepo.countDistinctCustomersByShopIdAndCreatedAtAfter(shopId, startOfMonth);

        Double averageCustomerValue = totalCustomers > 0
                ? (totalRevenue * 1.0 / totalCustomers)
                : 0.0;

        return new AnalyticsOverviewResponse(
                new AnalyticsOverviewResponse.RevenueMetrics(
                        totalRevenue,
                        todayRevenue,
                        thisWeekRevenue,
                        thisMonthRevenue,
                        revenueGrowth
                ),
                new AnalyticsOverviewResponse.OrderMetrics(
                        totalOrders,
                        todayOrders,
                        thisWeekOrders,
                        thisMonthOrders,
                        orderGrowth,
                        averageOrderValue,
                        0.0
                ),
                new AnalyticsOverviewResponse.ProductMetrics(
                        totalProducts,
                        activeProducts,
                        topProducts
                ),
                new AnalyticsOverviewResponse.CustomerMetrics(
                        totalCustomers,
                        newCustomers,
                        0.0,
                        averageCustomerValue
                )
        );
    }

    private List<AnalyticsOverviewResponse.ProductMetrics.TopProduct> loadTopProductsForOverview(
            Long shopId,
            int limit
    ) {
        List<Object[]> rows = orderItemRepo.aggregateProductSalesByShopOrderByRevenue(
                shopId,
                COMPLETED_STATUSES,
                PageRequest.of(0, limit)
        );

        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = rows.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();
        Map<Long, ProductEntity> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return rows.stream()
                .map(row -> {
                    Long productId = ((Number) row[0]).longValue();
                    Long quantity = ((Number) row[1]).longValue();
                    Long revenue = ((Number) row[2]).longValue();
                    ProductEntity product = productMap.get(productId);
                    return new AnalyticsOverviewResponse.ProductMetrics.TopProduct(
                            productId,
                            product != null ? product.getName() : "Unknown",
                            revenue,
                            quantity
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RevenueChartResponse getRevenueChart(Long sellerUserId, String period, LocalDate startDate, LocalDate endDate) {
        Long shopId = getShopId(sellerUserId);

        List<RevenueChartResponse.RevenueDataPoint> dataPoints = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime start = current.atStartOfDay();
            LocalDateTime end = current.plusDays(1).atStartOfDay().minusSeconds(1);

            List<OrderEntity> orders = orderRepo.findByShopIdAndDateRange(shopId, start, end);
            Long revenue = orders.stream()
                    .filter(o -> COMPLETED_STATUSES.contains(o.getStatus()))
                    .mapToLong(OrderEntity::getTotalAmount)
                    .sum();
            Long orderCount = orders.stream()
                    .filter(o -> COMPLETED_STATUSES.contains(o.getStatus()))
                    .count();

            String label = switch (period) {
                case "daily" -> current.toString();
                case "weekly" -> "Week " + current.getDayOfYear() / 7;
                case "monthly" -> current.getYear() + "-" + String.format("%02d", current.getMonthValue());
                default -> current.toString();
            };

            dataPoints.add(new RevenueChartResponse.RevenueDataPoint(label, revenue, orderCount));

            current = switch (period) {
                case "daily" -> current.plusDays(1);
                case "weekly" -> current.plusWeeks(1);
                case "monthly" -> current.plusMonths(1);
                default -> current.plusDays(1);
            };
        }

        return new RevenueChartResponse(period, startDate, endDate, dataPoints);
    }

    @Transactional(readOnly = true)
    public TopProductsResponse getTopProducts(Long sellerUserId, String sortBy, int limit) {
        Long shopId = getShopId(sellerUserId);

        List<Object[]> rows = "revenue".equals(sortBy)
                ? orderItemRepo.aggregateProductSalesByShopOrderByRevenue(
                        shopId, COMPLETED_STATUSES, PageRequest.of(0, limit))
                : orderItemRepo.aggregateProductSalesByShopOrderByQuantity(
                        shopId, COMPLETED_STATUSES, PageRequest.of(0, limit));

        if (rows.isEmpty()) {
            return new TopProductsResponse(sortBy, List.of());
        }

        List<Long> productIds = rows.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();
        Map<Long, ProductEntity> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        List<TopProductsResponse.TopProduct> topProducts = rows.stream()
                .map(row -> {
                    Long productId = ((Number) row[0]).longValue();
                    Long quantity = ((Number) row[1]).longValue();
                    Long revenue = ((Number) row[2]).longValue();
                    ProductEntity product = productMap.get(productId);
                    Double avgPrice = quantity > 0 ? (revenue * 1.0 / quantity) : 0.0;
                    return new TopProductsResponse.TopProduct(
                            productId,
                            product != null ? product.getName() : "Unknown",
                            product != null ? product.getMainImageUrl() : null,
                            revenue,
                            quantity,
                            avgPrice
                    );
                })
                .collect(Collectors.toList());

        return new TopProductsResponse(sortBy, topProducts);
    }

    @Transactional(readOnly = true)
    public OrderAnalyticsResponse getOrderAnalytics(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);

        long totalOrders = orderRepo.countByShopId(shopId);

        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepo.countByShopIdAndStatus(shopId, status.name()));
        }

        List<OrderAnalyticsResponse.OrderTrend> trends = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

            List<OrderEntity> dayOrders = orderRepo.findByShopIdAndDateRange(shopId, dayStart, dayEnd);
            long orderCount = dayOrders.size();
            long revenue = dayOrders.stream()
                    .filter(o -> COMPLETED_STATUSES.contains(o.getStatus()))
                    .mapToLong(OrderEntity::getTotalAmount)
                    .sum();

            trends.add(new OrderAnalyticsResponse.OrderTrend(
                    "day",
                    dayStart.toLocalDate().toString(),
                    orderCount,
                    revenue
            ));
        }

        return new OrderAnalyticsResponse(totalOrders, ordersByStatus, trends);
    }

    @Transactional(readOnly = true)
    public CustomerAnalyticsResponse getCustomerAnalytics(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);

        List<OrderEntity> allOrders = orderRepo.findByShopId(shopId, null).getContent();
        long totalCustomers = allOrders.stream()
                .map(OrderEntity::getUserId)
                .distinct()
                .count();

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newCustomers = allOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
                .map(OrderEntity::getUserId)
                .distinct()
                .count();

        Long totalRevenue = allOrders.stream()
                .filter(o -> COMPLETED_STATUSES.contains(o.getStatus()))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        Double averageCustomerValue = totalCustomers > 0
                ? (totalRevenue * 1.0 / totalCustomers)
                : 0.0;

        Map<Long, Long> customerRevenue = new HashMap<>();
        Map<Long, Long> customerOrderCount = new HashMap<>();

        for (OrderEntity order : allOrders) {
            if (COMPLETED_STATUSES.contains(order.getStatus())) {
                customerRevenue.merge(order.getUserId(), order.getTotalAmount(), Long::sum);
                customerOrderCount.merge(order.getUserId(), 1L, Long::sum);
            }
        }

        long newCustomerCount = customerOrderCount.entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .count();
        long returningCustomerCount = customerOrderCount.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .count();

        Long newCustomerRevenue = customerRevenue.entrySet().stream()
                .filter(e -> customerOrderCount.get(e.getKey()) == 1)
                .mapToLong(Map.Entry::getValue)
                .sum();
        Long returningCustomerRevenue = customerRevenue.entrySet().stream()
                .filter(e -> customerOrderCount.get(e.getKey()) > 1)
                .mapToLong(Map.Entry::getValue)
                .sum();

        List<CustomerAnalyticsResponse.CustomerSegment> segments = List.of(
                new CustomerAnalyticsResponse.CustomerSegment("new", newCustomerCount, newCustomerRevenue),
                new CustomerAnalyticsResponse.CustomerSegment("returning", returningCustomerCount, returningCustomerRevenue),
                new CustomerAnalyticsResponse.CustomerSegment("vip", 0L, 0L)
        );

        return new CustomerAnalyticsResponse(
                totalCustomers,
                newCustomers,
                0.0,
                averageCustomerValue,
                segments
        );
    }
}
