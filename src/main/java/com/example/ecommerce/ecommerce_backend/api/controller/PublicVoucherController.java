package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherValidationResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.ValidateSellerVoucherRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerVoucherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/vouchers")
@Tag(name = "Vouchers", description = "Public voucher endpoints for buyers")
public class PublicVoucherController {

    private final SellerVoucherService voucherService;

    public PublicVoucherController(SellerVoucherService voucherService) {
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

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "Get shop vouchers", description = "Get all active vouchers for a specific shop")
    public ResponseEntity<ApiResponse<List<SellerVoucherResponse>>> getShopVouchers(
            @PathVariable Long shopId
    ) {
        List<SellerVoucherResponse> vouchers = voucherService.getActiveVouchersForShop(shopId);
        return ResponseHelper.ok(vouchers);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate voucher", description = "Check if a voucher is valid for the order")
    public ResponseEntity<ApiResponse<SellerVoucherValidationResponse>> validateVoucher(
            @Valid @RequestBody ValidateSellerVoucherRequest request
    ) {
        SellerVoucherValidationResponse result = voucherService.validateVoucher(currentUserId(), request);
        return ResponseHelper.ok(result);
    }
}
