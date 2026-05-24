package com.example.ecommerce.ecommerce_backend.api.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.seller.IncomeSummaryResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.PayoutResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.RequestPayoutRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.RevenueReportResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.TransactionResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerIncomeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/seller/income")
@Tag(name = "Seller Income", description = "Seller income and payout management")
@PreAuthorize("hasRole('SELLER')")
public class SellerIncomeController {

    private final SellerIncomeService incomeService;

    public SellerIncomeController(SellerIncomeService incomeService) {
        this.incomeService = incomeService;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid User ID");
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "Get income summary", description = "Get overall income summary with revenue and payout information")
    public ResponseEntity<ApiResponse<IncomeSummaryResponse>> getSummary() {
        IncomeSummaryResponse summary = incomeService.getSummary(currentUserId());
        return ResponseHelper.ok(summary);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transactions", description = "Get transaction history with pagination")
    public ResponseEntity<ApiResponse<java.util.List<TransactionResponse>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<TransactionResponse> transactions = incomeService.getTransactions(currentUserId(), pageable);
        return ResponseHelper.page(transactions);
    }

    @GetMapping("/payouts")
    @Operation(summary = "Get payouts", description = "Get payout history with pagination")
    public ResponseEntity<ApiResponse<java.util.List<PayoutResponse>>> getPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
        Page<PayoutResponse> payouts = incomeService.getPayouts(currentUserId(), pageable);
        return ResponseHelper.page(payouts);
    }

    @PostMapping("/request-payout")
    @Operation(summary = "Request payout", description = "Request a payout")
    public ResponseEntity<ApiResponse<PayoutResponse>> requestPayout(
            @Valid @RequestBody RequestPayoutRequest request
    ) {
        PayoutResponse payout = incomeService.requestPayout(currentUserId(), request.amount(), request.note());
        return ResponseHelper.ok(payout, "Payout requested successfully");
    }

    @GetMapping("/revenue-report")
    @Operation(summary = "Get revenue report", description = "Get detailed revenue report for a date range")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        RevenueReportResponse report = incomeService.getRevenueReport(currentUserId(), startDate, endDate);
        return ResponseHelper.ok(report);
    }
}
