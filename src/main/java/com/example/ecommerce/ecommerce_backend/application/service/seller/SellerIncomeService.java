package com.example.ecommerce.ecommerce_backend.application.service.seller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.IncomeSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.PayoutResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.RevenueReportResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.TransactionResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefundEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefundJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;

@Service
public class SellerIncomeService {

    private final SellerShopJpaRepository shopRepo;
    private final OrderJpaRepository orderRepo;
    private final RefundJpaRepository refundRepo;

    public SellerIncomeService(
            SellerShopJpaRepository shopRepo,
            OrderJpaRepository orderRepo,
            RefundJpaRepository refundRepo
    ) {
        this.shopRepo = shopRepo;
        this.orderRepo = orderRepo;
        this.refundRepo = refundRepo;
    }

    private Long getShopId(Long sellerUserId) {
        SellerShopEntity shop = shopRepo.findBySellerUserId(sellerUserId)
                .orElseThrow(() -> ApiException.notFound("Shop not found"));
        return shop.getId();
    }

    @Transactional(readOnly = true)
    public IncomeSummaryResponse getSummary(Long sellerUserId) {
        Long shopId = getShopId(sellerUserId);
        
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        List<OrderEntity> allOrders = orderRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        
        // Calculate total revenue from completed orders
        Long totalRevenue = allOrders.stream()
                .filter(o -> completedStatuses.contains(o.getStatus()))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        // This month revenue
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long thisMonthRevenue = allOrders.stream()
                .filter(o -> completedStatuses.contains(o.getStatus()) && o.getCreatedAt().isAfter(startOfMonth))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        // Last month revenue
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);
        Long lastMonthRevenue = allOrders.stream()
                .filter(o -> completedStatuses.contains(o.getStatus())
                        && o.getCreatedAt().isAfter(startOfLastMonth)
                        && o.getCreatedAt().isBefore(endOfLastMonth))
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        Double revenueGrowth = lastMonthRevenue != null && lastMonthRevenue > 0
                ? ((thisMonthRevenue - lastMonthRevenue) * 100.0 / lastMonthRevenue)
                : 0.0;
        
        // Payouts (placeholder - would need payout table)
        Long pendingPayouts = 0L;
        Long completedPayouts = 0L;
        
        return new IncomeSummaryResponse(
                totalRevenue,
                pendingPayouts,
                completedPayouts,
                thisMonthRevenue,
                lastMonthRevenue,
                revenueGrowth
        );
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long sellerUserId, Pageable pageable) {
        Long shopId = getShopId(sellerUserId);
        
        List<TransactionResponse> transactions = new ArrayList<>();
        
        // Get orders as transactions
        List<OrderEntity> orders = orderRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        
        for (OrderEntity order : orders) {
            if (completedStatuses.contains(order.getStatus())) {
                transactions.add(new TransactionResponse(
                        "ORDER_" + order.getId(),
                        "ORDER",
                        order.getCreatedAt(),
                        "Order #" + order.getOrderCode(),
                        order.getTotalAmount(),
                        order.getCurrency(),
                        order.getStatus(),
                        String.valueOf(order.getId())
                ));
            }
        }
        
        // Get refunds as transactions
        List<RefundEntity> refunds = refundRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        
        for (RefundEntity refund : refunds) {
            if ("APPROVED".equals(refund.getStatus()) || "COMPLETED".equals(refund.getStatus())) {
                // Get order code from order
                OrderEntity order = orderRepo.findById(refund.getOrderId()).orElse(null);
                String orderCode = order != null ? order.getOrderCode() : "Unknown";
                
                LocalDateTime refundDate = LocalDateTime.ofInstant(refund.getCreatedAt(), ZoneId.systemDefault());
                transactions.add(new TransactionResponse(
                        "REFUND_" + refund.getId(),
                        "REFUND",
                        refundDate,
                        "Refund for Order #" + orderCode,
                        -refund.getRefundAmount(), // Negative for refunds
                        refund.getCurrency(),
                        refund.getStatus(),
                        String.valueOf(refund.getId())
                ));
            }
        }
        
        // Sort by date descending
        transactions.sort(Comparator.comparing(TransactionResponse::date).reversed());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), transactions.size());
        List<TransactionResponse> pageContent = transactions.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, transactions.size());
    }

    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayouts(Long sellerUserId, Pageable pageable) {
        // Placeholder - would need payout table
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    @Transactional
    public PayoutResponse requestPayout(Long sellerUserId, Long amount, String note) {
        // Placeholder - would need payout table and business logic
        throw ApiException.badRequest("Payout functionality not yet implemented");
    }

    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(Long sellerUserId, LocalDate startDate, LocalDate endDate) {
        Long shopId = getShopId(sellerUserId);
        
        List<String> completedStatuses = List.of(OrderStatus.COMPLETED.name(), OrderStatus.DELIVERED.name());
        List<OrderEntity> orders = orderRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .filter(o -> completedStatuses.contains(o.getStatus()))
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
        
        Long totalRevenue = orders.stream()
                .mapToLong(OrderEntity::getTotalAmount)
                .sum();
        
        long totalOrders = orders.size();
        
        // Get refunds in period
        List<RefundEntity> refunds = refundRepo.findByShopId(shopId, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .filter(r -> {
                    LocalDate refundDate = LocalDate.ofInstant(r.getCreatedAt(), ZoneId.systemDefault());
                    return !refundDate.isBefore(startDate) && !refundDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
        
        Long totalRefunds = refunds.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus()))
                .mapToLong(RefundEntity::getRefundAmount)
                .sum();
        
        Double averageOrderValue = totalOrders > 0 ? (totalRevenue * 1.0 / totalOrders) : 0.0;
        
        // Daily revenue breakdown
        List<RevenueReportResponse.DailyRevenue> dailyRevenue = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDate date = current;
            Long dayRevenue = orders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().equals(date))
                    .mapToLong(OrderEntity::getTotalAmount)
                    .sum();
            long dayOrderCount = orders.stream()
                    .filter(o -> o.getCreatedAt().toLocalDate().equals(date))
                    .count();
            
            dailyRevenue.add(new RevenueReportResponse.DailyRevenue(date, dayRevenue, dayOrderCount));
            current = current.plusDays(1);
        }
        
        return new RevenueReportResponse(
                startDate,
                endDate,
                totalRevenue,
                totalOrders,
                totalRefunds,
                averageOrderValue,
                dailyRevenue
        );
    }
}
