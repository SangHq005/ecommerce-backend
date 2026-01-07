package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.admin.DashboardStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.SalesAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.UserAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserJpaRepository userRepo;
    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final ProductJpaRepository productRepo;
    private final ProductSkuJpaRepository skuRepo;

    /**
     * Get comprehensive dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.info("Generating admin dashboard statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLast30Days = now.minusDays(30);

        Instant instantNow = Instant.now();
        Instant instantStartOfToday = startOfToday.atZone(ZoneId.systemDefault()).toInstant();
        Instant instantStartOfMonth = startOfMonth.atZone(ZoneId.systemDefault()).toInstant();
        Instant instant7DaysAgo = instantNow.minus(7, ChronoUnit.DAYS);
        Instant instant30DaysAgo = instantNow.minus(30, ChronoUnit.DAYS);

        // User Statistics
        long totalUsers = userRepo.count();
        long newUsersToday = userRepo.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(instantStartOfToday))
                .count();
        long newUsersThisMonth = userRepo.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(instantStartOfMonth))
                .count();
        long activeUsers = totalUsers; // Simplified: all users are active

        // Order Statistics
        List<OrderEntity> allOrders = orderRepo.findAll();
        long totalOrders = allOrders.size();
        long ordersToday = allOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfToday))
                .count();
        long ordersThisMonth = allOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
                .count();
        long pendingOrders = allOrders.stream()
                .filter(o -> OrderStatus.PAID.name().equals(o.getStatus()))
                .count();
        long processingOrders = allOrders.stream()
                .filter(o -> OrderStatus.PROCESSING.name().equals(o.getStatus()) ||
                        OrderStatus.READY_TO_SHIP.name().equals(o.getStatus()))
                .count();
        long completedOrders = allOrders.stream()
                .filter(o -> OrderStatus.COMPLETED.name().equals(o.getStatus()) ||
                        OrderStatus.DELIVERED.name().equals(o.getStatus()))
                .count();

        // Revenue Statistics
        List<OrderEntity> completedOrdersList = allOrders.stream()
                .filter(o -> OrderStatus.COMPLETED.name().equals(o.getStatus()) ||
                        OrderStatus.DELIVERED.name().equals(o.getStatus()))
                .toList();

        Long totalRevenue = completedOrdersList.stream()
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        Long revenueToday = completedOrdersList.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfToday))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        Long revenueThisMonth = completedOrdersList.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        Long averageOrderValue = completedOrdersList.isEmpty() ? 0L :
                totalRevenue / completedOrdersList.size();

        // Product Statistics
        List<ProductEntity> allProducts = productRepo.findAll();
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .count();

        // Simplified stock tracking
        long lowStockProducts = 0L;
        long outOfStockProducts = 0L;

        // Shop Statistics (simplified)
        long totalShops = allProducts.stream()
                .map(ProductEntity::getShopId)
                .distinct()
                .count();
        long activeShops = totalShops;

        // Recent Activities
        List<DashboardStatsResponse.RecentActivity> recentActivities = buildRecentActivities(allOrders);

        // Revenue Chart (last 30 days)
        List<DashboardStatsResponse.DailyRevenue> revenueChart = buildRevenueChart(completedOrdersList, startOfLast30Days);

        // Top Products
        List<DashboardStatsResponse.TopProduct> topProducts = buildTopProducts();

        // Top Categories
        List<DashboardStatsResponse.TopCategory> topCategories = buildTopCategories();

        return new DashboardStatsResponse(
                totalUsers, newUsersToday, newUsersThisMonth, activeUsers,
                totalOrders, ordersToday, ordersThisMonth, pendingOrders, processingOrders, completedOrders,
                totalRevenue, revenueToday, revenueThisMonth, averageOrderValue,
                totalProducts, activeProducts, lowStockProducts, outOfStockProducts,
                totalShops, activeShops,
                recentActivities,
                revenueChart,
                topProducts,
                topCategories,
                LocalDateTime.now()
        );
    }

    /**
     * Get user analytics
     */
    @Transactional(readOnly = true)
    public UserAnalyticsResponse getUserAnalytics() {
        log.info("Generating user analytics");

        List<UserEntity> allUsers = userRepo.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = totalUsers;
        long inactiveUsers = 0;

        Instant now = Instant.now();
        Instant last7Days = now.minus(7, ChronoUnit.DAYS);
        Instant last30Days = now.minus(30, ChronoUnit.DAYS);

        long newUsersLast7Days = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(last7Days))
                .count();

        long newUsersLast30Days = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(last30Days))
                .count();

        // User growth chart (last 30 days)
        List<UserAnalyticsResponse.UserGrowth> userGrowthChart = buildUserGrowthChart(allUsers);

        // Users by role (simplified)
        List<UserAnalyticsResponse.UserByRole> usersByRole = List.of(
                new UserAnalyticsResponse.UserByRole("CUSTOMER", totalUsers),
                new UserAnalyticsResponse.UserByRole("SELLER", 0L),
                new UserAnalyticsResponse.UserByRole("ADMIN", 0L)
        );

        return new UserAnalyticsResponse(
                totalUsers,
                activeUsers,
                inactiveUsers,
                newUsersLast7Days,
                newUsersLast30Days,
                userGrowthChart,
                usersByRole
        );
    }

    /**
     * Get sales analytics
     */
    @Transactional(readOnly = true)
    public SalesAnalyticsResponse getSalesAnalytics() {
        log.info("Generating sales analytics");

        List<OrderEntity> allOrders = orderRepo.findAll();
        List<OrderEntity> completedOrders = allOrders.stream()
                .filter(o -> OrderStatus.COMPLETED.name().equals(o.getStatus()) ||
                        OrderStatus.DELIVERED.name().equals(o.getStatus()))
                .toList();

        Long totalRevenue = completedOrders.stream()
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

        Long revenueThisMonth = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfThisMonth))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        Long revenueLastMonth = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfLastMonth) && o.getCreatedAt().isBefore(startOfThisMonth))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();

        Double growthRate = revenueLastMonth > 0 ?
                ((revenueThisMonth - revenueLastMonth) * 100.0) / revenueLastMonth : 0.0;

        Long averageOrderValue = completedOrders.isEmpty() ? 0L :
                totalRevenue / completedOrders.size();

        long totalOrders = allOrders.size();
        long ordersThisMonth = allOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfThisMonth))
                .count();

        // Monthly sales chart (last 12 months)
        List<SalesAnalyticsResponse.MonthlySales> monthlySalesChart = buildMonthlySalesChart(completedOrders);

        // Sales by category
        List<SalesAnalyticsResponse.CategorySales> salesByCategory = buildSalesByCategory();

        // Top selling products
        List<SalesAnalyticsResponse.TopSellingProduct> topSellingProducts = buildTopSellingProductsList();

        return new SalesAnalyticsResponse(
                totalRevenue,
                revenueThisMonth,
                revenueLastMonth,
                growthRate,
                averageOrderValue,
                totalOrders,
                ordersThisMonth,
                monthlySalesChart,
                salesByCategory,
                topSellingProducts
        );
    }

    // Helper Methods

    private List<DashboardStatsResponse.RecentActivity> buildRecentActivities(List<OrderEntity> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(OrderEntity::getCreatedAt).reversed())
                .limit(10)
                .map(order -> new DashboardStatsResponse.RecentActivity(
                        "ORDER",
                        "New order " + order.getOrderCode() + " - " + order.getTotalAmount() + " VND",
                        order.getCreatedAt()
                ))
                .toList();
    }

    private List<DashboardStatsResponse.DailyRevenue> buildRevenueChart(List<OrderEntity> completedOrders, LocalDateTime since) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Long> revenueByDate = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(since))
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate().format(formatter),
                        Collectors.summingLong(OrderEntity::getTotalAmount)
                ));

        Map<String, Long> orderCountByDate = completedOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(since))
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate().format(formatter),
                        Collectors.counting()
                ));

        return revenueByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardStatsResponse.DailyRevenue(
                        entry.getKey(),
                        entry.getValue(),
                        orderCountByDate.getOrDefault(entry.getKey(), 0L).intValue()
                ))
                .toList();
    }

    private List<DashboardStatsResponse.TopProduct> buildTopProducts() {
        // Get all order items and aggregate by product
        List<OrderItemEntity> allItems = orderItemRepo.findAll();

        Map<Long, Long> productQuantities = allItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItemEntity::getProductId,
                        Collectors.summingLong(OrderItemEntity::getQuantity)
                ));

        Map<Long, Long> productRevenue = allItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItemEntity::getProductId,
                        Collectors.summingLong(OrderItemEntity::getTotalPrice)
                ));

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Long productId = entry.getKey();
                    String productName = productRepo.findById(productId)
                            .map(ProductEntity::getName)
                            .orElse("Unknown Product");

                    return new DashboardStatsResponse.TopProduct(
                            productId,
                            productName,
                            entry.getValue(),
                            productRevenue.getOrDefault(productId, 0L)
                    );
                })
                .toList();
    }

    private List<DashboardStatsResponse.TopCategory> buildTopCategories() {
        List<ProductEntity> allProducts = productRepo.findAll();

        Map<Long, Long> categoryProductCount = allProducts.stream()
                .collect(Collectors.groupingBy(
                        ProductEntity::getCategoryId,
                        Collectors.counting()
                ));

        return categoryProductCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new DashboardStatsResponse.TopCategory(
                        entry.getKey(),
                        "Category " + entry.getKey(),
                        entry.getValue(),
                        0L // Order count would require join
                ))
                .toList();
    }

    private List<UserAnalyticsResponse.UserGrowth> buildUserGrowthChart(List<UserEntity> users) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        List<UserAnalyticsResponse.UserGrowth> chart = new ArrayList<>();
        long cumulativeTotal = 0;

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            long newUsers = users.stream()
                    .filter(u -> u.getCreatedAt() != null &&
                            u.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(date))
                    .count();

            cumulativeTotal += newUsers;

            chart.add(new UserAnalyticsResponse.UserGrowth(
                    dateStr,
                    newUsers,
                    cumulativeTotal
            ));
        }

        return chart;
    }

    private List<SalesAnalyticsResponse.MonthlySales> buildMonthlySalesChart(List<OrderEntity> completedOrders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, Long> revenueByMonth = completedOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().format(formatter),
                        Collectors.summingLong(OrderEntity::getTotalAmount)
                ));

        Map<String, Long> orderCountByMonth = completedOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().format(formatter),
                        Collectors.counting()
                ));

        return revenueByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new SalesAnalyticsResponse.MonthlySales(
                        entry.getKey(),
                        entry.getValue(),
                        orderCountByMonth.getOrDefault(entry.getKey(), 0L)
                ))
                .toList();
    }

    private List<SalesAnalyticsResponse.CategorySales> buildSalesByCategory() {
        // Simplified: would require joining orders with products
        return new ArrayList<>();
    }

    private List<SalesAnalyticsResponse.TopSellingProduct> buildTopSellingProductsList() {
        List<OrderItemEntity> allItems = orderItemRepo.findAll();

        Map<Long, Long> productQuantities = allItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItemEntity::getProductId,
                        Collectors.summingLong(OrderItemEntity::getQuantity)
                ));

        Map<Long, Long> productRevenue = allItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItemEntity::getProductId,
                        Collectors.summingLong(OrderItemEntity::getTotalPrice)
                ));

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(20)
                .map(entry -> {
                    Long productId = entry.getKey();
                    String productName = productRepo.findById(productId)
                            .map(ProductEntity::getName)
                            .orElse("Unknown Product");

                    return new SalesAnalyticsResponse.TopSellingProduct(
                            productId,
                            productName,
                            entry.getValue(),
                            productRevenue.getOrDefault(productId, 0L)
                    );
                })
                .toList();
    }
}
