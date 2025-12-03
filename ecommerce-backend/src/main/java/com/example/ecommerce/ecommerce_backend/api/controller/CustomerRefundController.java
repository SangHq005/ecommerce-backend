package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.MessageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.CreateRefundRequest;
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
@RequestMapping("/api/v1/customer/refunds")
@RequiredArgsConstructor
public class CustomerRefundController {

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
     * Create a new refund request
     */
    @PostMapping
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody CreateRefundRequest request) {
        Long userId = currentUserId();
        RefundResponse refund = refundService.createRefund(userId, request);
        return ResponseEntity.ok(refund);
    }

    /**
     * Get all refunds for current user
     */
    @GetMapping
    public ResponseEntity<Page<RefundResponse>> getUserRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Long userId = currentUserId();
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RefundResponse> refunds = refundService.getUserRefunds(userId, pageable);
        return ResponseEntity.ok(refunds);
    }

    /**
     * Get refund detail
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getRefundDetail(@PathVariable Long refundId) {
        RefundResponse refund = refundService.getRefundDetail(refundId);
        return ResponseEntity.ok(refund);
    }

    /**
     * Cancel refund request
     */
    @PostMapping("/{refundId}/cancel")
    public ResponseEntity<MessageResponse> cancelRefund(@PathVariable Long refundId) {
        Long userId = currentUserId();
        refundService.cancelRefund(userId, refundId);
        return ResponseEntity.ok(new MessageResponse("Refund request cancelled successfully"));
    }
}
