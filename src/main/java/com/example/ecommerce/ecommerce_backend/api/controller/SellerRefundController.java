package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.refund.ProcessRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.RefundResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/refunds")
@RequiredArgsConstructor
public class SellerRefundController {

    private final RefundService refundService;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw ApiException.unauthorized("User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw ApiException.unauthorized("Invalid User ID");
        }
    }

    /**
     * Get all refunds for seller's shop
     */
    @GetMapping
    public ResponseEntity<Page<RefundResponse>> getShopRefunds(
            @RequestParam Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RefundResponse> refunds = refundService.getShopRefunds(shopId, pageable);
        return ResponseEntity.ok(refunds);
    }

    /**
     * Get refunds by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<RefundResponse>> getRefundsByStatus(
            @RequestParam Long shopId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RefundResponse> refunds = refundService.getShopRefundsByStatus(shopId, status, pageable);
        return ResponseEntity.ok(refunds);
    }

    /**
     * Get refund detail
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getRefundDetail(
            @RequestParam Long shopId,
            @PathVariable Long refundId
    ) {
        RefundResponse refund = refundService.getRefundDetail(refundId);
        return ResponseEntity.ok(refund);
    }

    /**
     * Process refund (approve or reject)
     */
    @PutMapping("/{refundId}/process")
    public ResponseEntity<RefundResponse> processRefund(
            @RequestParam Long shopId,
            @PathVariable Long refundId,
            @Valid @RequestBody ProcessRefundRequest request
    ) {
        RefundResponse refund = refundService.processRefund(shopId, refundId, request);
        return ResponseEntity.ok(refund);
    }
}
