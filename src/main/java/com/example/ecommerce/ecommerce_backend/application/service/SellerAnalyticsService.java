package com.example.ecommerce.ecommerce_backend.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.AnalyticsOverviewResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.CustomerAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.OrderAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.RevenueChartResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.TopProductsResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;

@Service
public class SellerAnalyticsService {

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
    public AnalyticsOverviewResponse getOverview(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfWeek = startOfDay.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDateTime startOfMonth = startOfDay.withDayOfMonth(1);
        
        LocalDateTime lastMonthStart = startOfMonth.minusMonths(1);
        LocalDateTime lastMonthEnd = startOfMonth.minusSeconds(1);
        
        // Revenue metrics - calculate from orders
        org.springframework.data.domain.Pageable unpaged = org.springframework.data.domain.Pageable.unpaged();
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        List<OrderEntity> allShopOrdersForOverview = orderRepo.findByShopId(shopId, unpaged).getContent();
        List<OrderEntity> completedOrders = allShopOrdersForOverview.stream()
                .filter(o -> completedStatuses.contains(o.getStatus()))
                .collect(Collectors.toList());
        
        Long totalRevenue = completedOrders.stream()
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        Long todayRevenue = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfDay))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        Long thisWeekRevenue = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfWeek))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        Long thisMonthRevenue = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        // Calculate revenue growth (this month vs last month)
        Long lastMonthRevenue = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(lastMonthStart) && o.getCreatedAt().isBefore(lastMonthEnd))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        Double revenueGrowth = lastMonthRevenue != null && lastMonthRevenue > 0
                ? ((thisMonthRevenue - lastMonthRevenue) * 100.0 / lastMonthRevenue)
                : 0.0;
        
        // Order metrics
        long totalOrders = allShopOrdersForOverview.size();
        long todayOrders = (int) allShopOrdersForOverview.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfDay))
                .count();
        long thisWeekOrders = (int) allShopOrdersForOverview.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfWeek))
                .count();
        long thisMonthOrders = (int) allShopOrdersForOverview.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
                .count();
        
        long lastMonthOrders = (int) allShopOrdersForOverview.stream()
                .filter(o -> o.getCreatedAt().isAfter(lastMonthStart) && o.getCreatedAt().isBefore(lastMonthEnd))
                .count();
        Double orderGrowth = lastMonthOrders > 0
                ? ((thisMonthOrders - lastMonthOrders) * 100.0 / lastMonthOrders)
                : 0.0;
        
        Double averageOrderValue = thisMonthOrders > 0
                ? (thisMonthRevenue * 1.0 / thisMonthOrders)
                : 0.0;
        
        // Conversion rate (simplified - would need view data for real calculation)
        Double conversionRate = 0.0; // Placeholder
        
        // Product metrics
        List<ProductEntity> allProducts = productRepo.findByShopIdAndSellerUserId(shopId, sellerUserId, unpaged).getContent();
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .count();
        
        // Top products by revenue
        List<OrderItemEntity> orderItems = orderItemRepo.findAll();
        Map<Long, Long> productRevenue = new HashMap<>();
        Map<Long, Long> productQuantity = new HashMap<>();
        
        for (OrderItemEntity item : orderItems) {
            OrderEntity order = orderRepo.findById(item.getOrderId()).orElse(null);
            if (order != null && order.getShopId().equals(shopId) 
                    && completedStatuses.contains(order.getStatus())) {
                productRevenue.merge(item.getProductId(), item.getTotalPrice(), Long::sum);
                productQuantity.merge(item.getProductId(), (long) item.getQuantity(), Long::sum);
            }
        }
        
        List<AnalyticsOverviewResponse.ProductMetrics.TopProduct> topProducts = productRevenue.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    ProductEntity product = productRepo.findById(entry.getKey()).orElse(null);
                    return new AnalyticsOverviewResponse.ProductMetrics.TopProduct(
                            entry.getKey(),
                            product != null ? product.getName() : "Unknown",
                            entry.getValue(),
                            productQuantity.getOrDefault(entry.getKey(), 0L)
                    );
                })
                .collect(Collectors.toList());
        
        // Customer metrics
        List<OrderEntity> allShopOrders = orderRepo.findByShopId(shopId, unpaged).getContent();
        long totalCustomers = allShopOrders.stream()
                .map(OrderEntity::getUserId)
                .distinct()
                .count();
        
        long newCustomers = allShopOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
                .map(OrderEntity::getUserId)
                .distinct()
                .count();
        
        Double customerGrowth = 0.0; // Placeholder
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
                        conversionRate
                ),
                new AnalyticsOverviewResponse.ProductMetrics(
                        totalProducts,
                        activeProducts,
                        topProducts
                ),
                new AnalyticsOverviewResponse.CustomerMetrics(
                        totalCustomers,
                        newCustomers,
                        customerGrowth,
                        averageCustomerValue
                )
        );
    }

    @Transactional(readOnly = true)
    public RevenueChartResponse getRevenueChart(Long sellerUserId, String period, LocalDate startDate, LocalDate endDate) {
        Long shopId = getShopId(sellerUserId);
        
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        List<RevenueChartResponse.RevenueDataPoint> dataPoints = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime start = current.atStartOfDay();
            LocalDateTime end = current.plusDays(1).atStartOfDay().minusSeconds(1);
            
            List<OrderEntity> orders = orderRepo.findByShopIdAndDateRange(shopId, start, end);
            Long revenue = orders.stream()
                    .filter(o -> completedStatuses.contains(o.getStatus()))
                    .mapToLong(OrderEntity::getTotalAmount)
                    .sum();
            Long orderCount = orders.stream()
                    .filter(o -> completedStatuses.contains(o.getStatus()))
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
        
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        List<OrderItemEntity> orderItems = orderItemRepo.findAll();
        
        Map<Long, Long> productRevenue = new HashMap<>();
        Map<Long, Long> productQuantity = new HashMap<>();
        
        for (OrderItemEntity item : orderItems) {
            OrderEntity order = orderRepo.findById(item.getOrderId()).orElse(null);
            if (order != null && order.getShopId().equals(shopId) 
                    && completedStatuses.contains(order.getStatus())) {
                productRevenue.merge(item.getProductId(), item.getTotalPrice(), Long::sum);
                productQuantity.merge(item.getProductId(), (long) item.getQuantity(), Long::sum);
            }
        }
        
        List<TopProductsResponse.TopProduct> topProducts;
        if ("revenue".equals(sortBy)) {
            topProducts = productRevenue.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        ProductEntity product = productRepo.findById(entry.getKey()).orElse(null);
                        Long quantity = productQuantity.getOrDefault(entry.getKey(), 0L);
                        Double avgPrice = quantity > 0 ? (entry.getValue() * 1.0 / quantity) : 0.0;
                        return new TopProductsResponse.TopProduct(
                                entry.getKey(),
                                product != null ? product.getName() : "Unknown",
                                product != null ? product.getMainImageUrl() : null,
                                entry.getValue(),
                                quantity,
                                avgPrice
                        );
                    })
                    .collect(Collectors.toList());
        } else {
            topProducts = productQuantity.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        ProductEntity product = productRepo.findById(entry.getKey()).orElse(null);
                        Long revenue = productRevenue.getOrDefault(entry.getKey(), 0L);
                        Double avgPrice = entry.getValue() > 0 ? (revenue * 1.0 / entry.getValue()) : 0.0;
                        return new TopProductsResponse.TopProduct(
                                entry.getKey(),
                                product != null ? product.getName() : "Unknown",
                                product != null ? product.getMainImageUrl() : null,
                                revenue,
                                entry.getValue(),
                                avgPrice
                        );
                    })
                    .collect(Collectors.toList());
        }
        
        return new TopProductsResponse(sortBy, topProducts);
    }

    @Transactional(readOnly = true)
    public OrderAnalyticsResponse getOrderAnalytics(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);
        
        long totalOrders = orderRepo.countByShopIdAndStatus(shopId, null);
        
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepo.countByShopIdAndStatus(shopId, status.name()));
        }
        
        // Order trends (last 7 days)
        List<OrderAnalyticsResponse.OrderTrend> trends = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);
            
            List<OrderEntity> dayOrders = orderRepo.findByShopIdAndDateRange(shopId, dayStart, dayEnd);
            long orderCount = dayOrders.size();
            long revenue = dayOrders.stream()
                    .filter(o -> List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name())
                            .contains(o.getStatus()))
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
        
        Double customerGrowth = 0.0; // Placeholder
        
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        Long totalRevenue = allOrders.stream()
                .filter(o -> completedStatuses.contains(o.getStatus()))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        Double averageCustomerValue = totalCustomers > 0
                ? (totalRevenue * 1.0 / totalCustomers)
                : 0.0;
        
        // Customer segments
        Map<Long, Long> customerRevenue = new HashMap<>();
        Map<Long, Long> customerOrderCount = new HashMap<>();
        
        for (OrderEntity order : allOrders) {
            if (completedStatuses.contains(order.getStatus())) {
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
                new CustomerAnalyticsResponse.CustomerSegment("vip", 0L, 0L) // Placeholder
        );
        
        return new CustomerAnalyticsResponse(
                totalCustomers,
                newCustomers,
                customerGrowth,
                averageCustomerValue,
                segments
        );
    }
}
