package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.SellerVoucherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/seller/vouchers")
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Vouchers", description = "Seller voucher management")
public class SellerVoucherController {

    private final SellerVoucherService voucherService;

    public SellerVoucherController(SellerVoucherService voucherService) {
        this.voucherService = voucherService;
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
    @Operation(summary = "Create voucher", description = "Create a new shop voucher")
    public ResponseEntity<ApiResponse<SellerVoucherResponse>> createVoucher(
            @Valid @RequestBody SellerVoucherRequest request
    ) {
        SellerVoucherResponse voucher = voucherService.createVoucher(currentUserId(), request);
        return ResponseHelper.created(voucher, "Voucher created successfully");
    }

    @PutMapping("/{voucherId}")
    @Operation(summary = "Update voucher", description = "Update an existing voucher")
    public ResponseEntity<ApiResponse<SellerVoucherResponse>> updateVoucher(
            @PathVariable Long voucherId,
            @Valid @RequestBody SellerVoucherRequest request
    ) {
        SellerVoucherResponse voucher = voucherService.updateVoucher(currentUserId(), voucherId, request);
        return ResponseHelper.ok(voucher, "Voucher updated successfully");
    }

    @GetMapping
    @Operation(summary = "List vouchers", description = "Get all vouchers for the shop")
    public ResponseEntity<ApiResponse<List<SellerVoucherResponse>>> getVouchers(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<SellerVoucherResponse> vouchers = voucherService.getVouchers(currentUserId(), status, pageable);
        return ResponseHelper.page(vouchers);
    }

    @GetMapping("/{voucherId}")
    @Operation(summary = "Get voucher details", description = "Get details of a specific voucher")
    public ResponseEntity<ApiResponse<SellerVoucherResponse>> getVoucherDetail(
            @PathVariable Long voucherId
    ) {
        SellerVoucherResponse voucher = voucherService.getVoucherDetail(currentUserId(), voucherId);
        return ResponseHelper.ok(voucher);
    }

    @PostMapping("/{voucherId}/activate")
    @Operation(summary = "Activate voucher", description = "Activate a draft or paused voucher")
    public ResponseEntity<ApiResponse<SellerVoucherResponse>> activateVoucher(
            @PathVariable Long voucherId
    ) {
        SellerVoucherResponse voucher = voucherService.activateVoucher(currentUserId(), voucherId);
        return ResponseHelper.ok(voucher, "Voucher activated successfully");
    }

    @PostMapping("/{voucherId}/pause")
    @Operation(summary = "Pause voucher", description = "Pause an active voucher")
    public ResponseEntity<ApiResponse<SellerVoucherResponse>> pauseVoucher(
            @PathVariable Long voucherId
    ) {
        SellerVoucherResponse voucher = voucherService.pauseVoucher(currentUserId(), voucherId);
        return ResponseHelper.ok(voucher, "Voucher paused successfully");
    }

    @DeleteMapping("/{voucherId}")
    @Operation(summary = "Delete voucher", description = "Delete a voucher (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(
            @PathVariable Long voucherId
    ) {
        voucherService.deleteVoucher(currentUserId(), voucherId);
        return ResponseHelper.ok(null, "Voucher deleted successfully");
    }
}
