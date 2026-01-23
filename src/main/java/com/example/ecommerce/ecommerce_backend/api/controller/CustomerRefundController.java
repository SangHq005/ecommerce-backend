package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.refund.CreateRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.RefundResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.RefundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customer/refunds")
@Tag(name = "Customer Refunds", description = "Customer refund requests")
public class CustomerRefundController {

    private final RefundService refundService;

    public CustomerRefundController(RefundService refundService) {
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

    @PostMapping
    @Operation(summary = "Create refund", description = "Create a new refund request")
    public ResponseEntity<ApiResponse<RefundResponse>> createRefund(
            @Valid @RequestBody CreateRefundRequest request
    ) {
        Long userId = currentUserId();
        RefundResponse refund = refundService.createRefund(userId, request);
        return ResponseHelper.created(refund, "Refund request submitted");
    }

    @GetMapping
    @Operation(summary = "List refunds", description = "Get all refunds for current user")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getUserRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Long userId = currentUserId();
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RefundResponse> refunds = refundService.getUserRefunds(userId, pageable);
        return ResponseHelper.page(refunds);
    }

    @GetMapping("/{refundId}")
    @Operation(summary = "Refund detail", description = "Get refund details")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundDetail(@PathVariable Long refundId) {
        RefundResponse refund = refundService.getUserRefundDetail(currentUserId(), refundId);
        return ResponseHelper.ok(refund);
    }

    @PostMapping("/{refundId}/cancel")
    @Operation(summary = "Cancel refund", description = "Cancel a refund request")
    public ResponseEntity<ApiResponse<Void>> cancelRefund(@PathVariable Long refundId) {
        Long userId = currentUserId();
        refundService.cancelRefund(userId, refundId);
        return ResponseHelper.ok(null, "Refund request cancelled successfully");
    }
}
