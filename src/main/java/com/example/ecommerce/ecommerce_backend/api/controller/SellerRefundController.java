package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.refund.ProcessRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.RefundResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.RefundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/seller/refunds")
@Tag(name = "Seller Refunds", description = "Seller refund management")
public class SellerRefundController {

    private final RefundService refundService;

    public SellerRefundController(RefundService refundService) {
        this.refundService = refundService;
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

    @GetMapping
    @Operation(summary = "List refunds", description = "Get all refunds for seller's shop")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getShopRefunds(
            @RequestParam Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RefundResponse> refunds = refundService.getShopRefunds(shopId, currentUserId(), pageable);
        return ResponseHelper.page(refunds);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Refunds by status", description = "Get refunds filtered by status")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundsByStatus(
            @RequestParam Long shopId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RefundResponse> refunds = refundService.getShopRefundsByStatus(shopId, currentUserId(), status, pageable);
        return ResponseHelper.page(refunds);
    }

    @GetMapping("/{refundId}")
    @Operation(summary = "Refund detail", description = "Get refund details")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundDetail(
            @RequestParam Long shopId,
            @PathVariable Long refundId
    ) {
        RefundResponse refund = refundService.getSellerRefundDetail(shopId, currentUserId(), refundId);
        return ResponseHelper.ok(refund);
    }

    @PutMapping("/{refundId}/process")
    @Operation(summary = "Process refund", description = "Approve or reject a refund request")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @RequestParam Long shopId,
            @PathVariable Long refundId,
            @Valid @RequestBody ProcessRefundRequest request
    ) {
        RefundResponse refund = refundService.processRefund(shopId, currentUserId(), refundId, request);
        return ResponseHelper.ok(refund, "Refund processed successfully");
    }
}
