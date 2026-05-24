package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.refund.ProcessRefundRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.refund.RefundResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.refund.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/refunds")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Refund", description = "Admin refund/complaint management")
public class AdminRefundController {

    private final RefundService refundService;

    public AdminRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    @Operation(summary = "List refunds", description = "List refunds by status")
    public ResponseEntity<ApiResponse<Page<RefundResponse>>> list(
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RefundResponse> refunds = refundService.adminListRefunds(status, pageable);
        return ResponseHelper.ok(refunds);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get refund", description = "Get refund details")
    public ResponseEntity<ApiResponse<RefundResponse>> get(
            @PathVariable Long id
    ) {
        RefundResponse refund = refundService.adminGetRefundDetail(id);
        return ResponseHelper.ok(refund);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Process refund", description = "Approve or Reject refund")
    public ResponseEntity<ApiResponse<RefundResponse>> process(
            @PathVariable Long id,
            @Valid @RequestBody ProcessRefundRequest request
    ) {
        RefundResponse refund = refundService.adminProcessRefund(id, request);
        return ResponseHelper.ok(refund, "Refund processed");
    }
}
