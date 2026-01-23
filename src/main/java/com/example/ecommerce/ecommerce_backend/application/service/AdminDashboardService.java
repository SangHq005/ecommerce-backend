package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.admin.DashboardStatsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.SalesAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.UserAnalyticsResponse;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RoleEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CategoryJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RoleJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class AdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardService.class);

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final UserJpaRepository userRepo;
    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final ProductJpaRepository productRepo;
    private final SkuJpaRepository skuRepo;
    private final CategoryJpaRepository categoryRepo;
    private final SellerShopJpaRepository shopRepo;
    private final RoleJpaRepository roleRepo;

    public AdminDashboardService(
            UserJpaRepository userRepo,
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository orderItemRepo,
            ProductJpaRepository productRepo,
            SkuJpaRepository skuRepo,
            CategoryJpaRepository categoryRepo,
            SellerShopJpaRepository shopRepo,
            RoleJpaRepository roleRepo
    ) {
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productRepo = productRepo;
        this.skuRepo = skuRepo;
        this.categoryRepo = categoryRepo;
        this.shopRepo = shopRepo;
        this.roleRepo = roleRepo;
    }

    /**
     * Get comprehensive dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.info("Generating admin dashboard statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfLast30Days = now.minusDays(30);

        Instant instantStartOfToday = startOfToday.atZone(ZoneId.systemDefault()).toInstant();
        Instant instantStartOfMonth = startOfMonth.atZone(ZoneId.systemDefault()).toInstant();

        List<String> completedStatuses = List.of(
                OrderStatus.COMPLETED.name(),
                OrderStatus.DELIVERED.name()
        );
        List<String> processingStatuses = List.of(
                OrderStatus.PROCESSING.name(),
                OrderStatus.READY_TO_SHIP.name()
        );

        // User Statistics
        long totalUsers = userRepo.count();
        long newUsersToday = userRepo.countByCreatedAtAfter(instantStartOfToday);
        long newUsersThisMonth = userRepo.countByCreatedAtAfter(instantStartOfMonth);
        long activeUsers = userRepo.countByStatus("ACTIVE");

        // Order Statistics
        long totalOrders = orderRepo.count();
        long ordersToday = orderRepo.countByCreatedAtAfter(startOfToday);
        long ordersThisMonth = orderRepo.countByCreatedAtAfter(startOfMonth);
        long pendingOrders = orderRepo.countByStatus(OrderStatus.PAID.name());
        long processingOrders = orderRepo.countByStatusIn(processingStatuses);
        long completedOrders = orderRepo.countByStatusIn(completedStatuses);

        // Revenue Statistics
        Long totalRevenue = orderRepo.sumTotalAmountByStatusIn(completedStatuses);
        Long revenueToday = orderRepo.sumTotalAmountByStatusInAndCreatedAtAfter(completedStatuses, startOfToday);
        Long revenueThisMonth = orderRepo.sumTotalAmountByStatusInAndCreatedAtAfter(completedStatuses, startOfMonth);
        Long averageOrderValue = completedOrders == 0 ? 0L : totalRevenue / completedOrders;

        // Product Statistics
        List<ProductEntity> allProducts = productRepo.findAll();
        long totalProducts = allProducts.size();
        long activeProducts = productRepo.countByStatus("ACTIVE");

        Map<Long, Long> availableStock = loadAvailableStockByProduct();
        long lowStockProducts = 0L;
        long outOfStockProducts = 0L;
        Map<Long, Long> categoryProductCount = new HashMap<>();

        for (ProductEntity product : allProducts) {
            Integer stockQuantity = product.getStockQuantity();
            long stock = availableStock.getOrDefault(
                    product.getId(),
                    stockQuantity == null ? 0L : stockQuantity.longValue()
            );
            if (stock <= 0) {
                outOfStockProducts++;
            } else if (stock <= LOW_STOCK_THRESHOLD) {
                lowStockProducts++;
            }
            categoryProductCount.merge(product.getCategoryId(), 1L, Long::sum);
        }

        // Shop Statistics
        long totalShops = shopRepo.count();
        long activeShops = shopRepo.countByStatus("ACTIVE");

        // Recent Activities
        List<DashboardStatsResponse.RecentActivity> recentActivities =
                buildRecentActivities(orderRepo.findTop10ByOrderByCreatedAtDesc());

        // Revenue Chart (last 30 days)
        List<OrderEntity> completedRecentOrders =
                orderRepo.findByStatusInAndCreatedAtAfter(completedStatuses, startOfLast30Days);
        List<DashboardStatsResponse.DailyRevenue> revenueChart =
                buildRevenueChart(completedRecentOrders, startOfLast30Days);

        // Top Products
        List<DashboardStatsResponse.TopProduct> topProducts = buildTopProducts(completedStatuses);

        // Top Categories
        List<DashboardStatsResponse.TopCategory> topCategories =
                buildTopCategories(completedStatuses, categoryProductCount);

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
        long totalUsers = userRepo.count();
        long activeUsers = userRepo.countByStatus("ACTIVE");
        long inactiveUsers = userRepo.countByStatus("DISABLED");

        Instant now = Instant.now();
        Instant last7Days = now.minus(7, ChronoUnit.DAYS);
        Instant last30Days = now.minus(30, ChronoUnit.DAYS);

        long newUsersLast7Days = userRepo.countByCreatedAtAfter(last7Days);
        long newUsersLast30Days = userRepo.countByCreatedAtAfter(last30Days);

        // User growth chart (last 30 days)
        List<UserAnalyticsResponse.UserGrowth> userGrowthChart = buildUserGrowthChart(allUsers);

        Map<String, Long> roleCounts = new HashMap<>();
        for (Object[] row : userRepo.countUsersByRole()) {
            String code = (String) row[0];
            long count = ((Number) row[1]).longValue();
            roleCounts.put(code, count);
        }

        List<RoleEntity> roles = roleRepo.findAll();
        List<UserAnalyticsResponse.UserByRole> usersByRole;
        if (roles.isEmpty()) {
            usersByRole = roleCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new UserAnalyticsResponse.UserByRole(entry.getKey(), entry.getValue()))
                    .toList();
        } else {
            usersByRole = roles.stream()
                    .sorted(Comparator.comparing(RoleEntity::getCode))
                    .map(role -> new UserAnalyticsResponse.UserByRole(
                            role.getCode(),
                            roleCounts.getOrDefault(role.getCode(), 0L)
                    ))
                    .toList();
        }

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

        List<String> completedStatuses = List.of(
                OrderStatus.COMPLETED.name(),
                OrderStatus.DELIVERED.name()
        );

        Long totalRevenue = orderRepo.sumTotalAmountByStatusIn(completedStatuses);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);

        Long revenueThisMonth = orderRepo.sumTotalAmountByStatusInAndCreatedAtAfter(completedStatuses, startOfThisMonth);
        Long revenueLastMonth = orderRepo.sumTotalAmountByStatusInAndCreatedAtBetween(
                completedStatuses, startOfLastMonth, startOfThisMonth
        );

        Double growthRate = revenueLastMonth > 0 ?
                ((revenueThisMonth - revenueLastMonth) * 100.0) / revenueLastMonth : 0.0;

        long completedOrdersCount = orderRepo.countByStatusIn(completedStatuses);
        Long averageOrderValue = completedOrdersCount == 0 ? 0L :
                totalRevenue / completedOrdersCount;

        long totalOrders = orderRepo.count();
        long ordersThisMonth = orderRepo.countByCreatedAtAfter(startOfThisMonth);

        // Monthly sales chart (last 12 months)
        LocalDateTime startOf12Months = startOfThisMonth.minusMonths(11);
        List<OrderEntity> recentCompletedOrders =
                orderRepo.findByStatusInAndCreatedAtAfter(completedStatuses, startOf12Months);
        List<SalesAnalyticsResponse.MonthlySales> monthlySalesChart =
                buildMonthlySalesChart(recentCompletedOrders, startOf12Months.toLocalDate());

        // Sales by category
        List<SalesAnalyticsResponse.CategorySales> salesByCategory = buildSalesByCategory(completedStatuses);

        // Top selling products
        List<SalesAnalyticsResponse.TopSellingProduct> topSellingProducts =
                buildTopSellingProductsList(completedStatuses);

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

    private Map<Long, Long> loadAvailableStockByProduct() {
        Map<Long, Long> stockByProduct = new HashMap<>();
        for (Object[] row : skuRepo.sumAvailableStockByProductId()) {
            Long productId = (Long) row[0];
            long total = row[1] == null ? 0L : ((Number) row[1]).longValue();
            stockByProduct.put(productId, total);
        }
        return stockByProduct;
    }

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

    private List<DashboardStatsResponse.TopProduct> buildTopProducts(List<String> statuses) {
        List<Object[]> rows = orderItemRepo.aggregateProductSalesByStatus(statuses);
        List<Object[]> topRows = rows.stream().limit(10).toList();
        List<Long> productIds = topRows.stream()
                .map(row -> (Long) row[0])
                .distinct()
                .toList();

        Map<Long, String> productNames = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, ProductEntity::getName));

        return topRows.stream()
                .map(row -> {
                    Long productId = (Long) row[0];
                    long quantity = ((Number) row[1]).longValue();
                    Long revenue = row[2] == null ? 0L : ((Number) row[2]).longValue();
                    String productName = productNames.getOrDefault(productId, "Unknown Product");
                    return new DashboardStatsResponse.TopProduct(
                            productId,
                            productName,
                            quantity,
                            revenue
                    );
                })
                .toList();
    }

    private List<DashboardStatsResponse.TopCategory> buildTopCategories(
            List<String> statuses,
            Map<Long, Long> categoryProductCount
    ) {
        List<Object[]> rows = orderItemRepo.aggregateCategorySalesByStatus(statuses);
        List<Object[]> topRows = rows.stream().limit(10).toList();
        List<Long> categoryIds = topRows.stream()
                .map(row -> (Long) row[0])
                .distinct()
                .toList();

        Map<Long, String> categoryNames = categoryRepo.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));

        return topRows.stream()
                .map(row -> {
                    Long categoryId = (Long) row[0];
                    long orderCount = ((Number) row[1]).longValue();
                    long productCount = categoryProductCount.getOrDefault(categoryId, 0L);
                    String categoryName = categoryNames.getOrDefault(categoryId, "Category " + categoryId);
                    return new DashboardStatsResponse.TopCategory(
                            categoryId,
                            categoryName,
                            productCount,
                            orderCount
                    );
                })
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

    private List<SalesAnalyticsResponse.MonthlySales> buildMonthlySalesChart(
            List<OrderEntity> completedOrders,
            LocalDate startMonth
    ) {
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

        List<SalesAnalyticsResponse.MonthlySales> chart = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDate month = startMonth.plusMonths(i);
            String key = month.format(formatter);
            chart.add(new SalesAnalyticsResponse.MonthlySales(
                    key,
                    revenueByMonth.getOrDefault(key, 0L),
                    orderCountByMonth.getOrDefault(key, 0L)
            ));
        }
        return chart;
    }

    private List<SalesAnalyticsResponse.CategorySales> buildSalesByCategory(List<String> statuses) {
        List<Object[]> rows = orderItemRepo.aggregateCategorySalesByStatus(statuses);
        List<Long> categoryIds = rows.stream()
                .map(row -> (Long) row[0])
                .distinct()
                .toList();

        Map<Long, String> categoryNames = categoryRepo.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));

        return rows.stream()
                .map(row -> {
                    Long categoryId = (Long) row[0];
                    long orderCount = ((Number) row[1]).longValue();
                    Long revenue = row[2] == null ? 0L : ((Number) row[2]).longValue();
                    String categoryName = categoryNames.getOrDefault(categoryId, "Category " + categoryId);
                    return new SalesAnalyticsResponse.CategorySales(
                            categoryId,
                            categoryName,
                            revenue,
                            orderCount
                    );
                })
                .toList();
    }

    private List<SalesAnalyticsResponse.TopSellingProduct> buildTopSellingProductsList(List<String> statuses) {
        List<Object[]> rows = orderItemRepo.aggregateProductSalesByStatus(statuses);
        List<Object[]> topRows = rows.stream().limit(20).toList();
        List<Long> productIds = topRows.stream()
                .map(row -> (Long) row[0])
                .distinct()
                .toList();

        Map<Long, String> productNames = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, ProductEntity::getName));

        return topRows.stream()
                .map(row -> {
                    Long productId = (Long) row[0];
                    long quantity = ((Number) row[1]).longValue();
                    Long revenue = row[2] == null ? 0L : ((Number) row[2]).longValue();
                    String productName = productNames.getOrDefault(productId, "Unknown Product");
                    return new SalesAnalyticsResponse.TopSellingProduct(
                            productId,
                            productName,
                            quantity,
                            revenue
                    );
                })
                .toList();
    }
}
