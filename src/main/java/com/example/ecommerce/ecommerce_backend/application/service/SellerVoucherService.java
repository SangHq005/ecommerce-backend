package com.example.ecommerce.ecommerce_backend.application.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherValidationResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.ValidateSellerVoucherRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherEntity.DiscountType;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherEntity.VoucherStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherUsageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerVoucherJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerVoucherUsageJpaRepository;

@Service
public class SellerVoucherService {
    private static final Logger log = LoggerFactory.getLogger(SellerVoucherService.class);

    private final SellerVoucherJpaRepository voucherRepository;
    private final SellerVoucherUsageJpaRepository usageRepository;
    private final SellerShopJpaRepository shopRepository;

    public SellerVoucherService(
            SellerVoucherJpaRepository voucherRepository,
            SellerVoucherUsageJpaRepository usageRepository,
            SellerShopJpaRepository shopRepository
    ) {
        this.voucherRepository = voucherRepository;
        this.usageRepository = usageRepository;
        this.shopRepository = shopRepository;
    }

    // ============ SELLER OPERATIONS ============

    @Transactional
    public SellerVoucherResponse createVoucher(Long sellerId, SellerVoucherRequest request) {
        SellerShopEntity shop = getShopBySeller(sellerId);
        
        // Check code uniqueness
        if (voucherRepository.existsByShopIdAndCode(shop.getId(), request.code())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Voucher code already exists");
        }
        
        // Validate dates
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "End date must be after start date");
        }
        
        // Create entity
        SellerVoucherEntity voucher = new SellerVoucherEntity();
        voucher.setShopId(shop.getId());
        voucher.setCode(request.code().toUpperCase());
        voucher.setName(request.name());
        voucher.setDescription(request.description());
        voucher.setDiscountType(DiscountType.valueOf(request.discountType()));
        voucher.setDiscountValue(request.discountValue());
        voucher.setMaxDiscountAmount(request.maxDiscountAmount());
        voucher.setMinOrderAmount(request.minOrderAmount());
        voucher.setStartDate(request.startDate());
        voucher.setEndDate(request.endDate());
        voucher.setUsageLimit(request.usageLimit());
        voucher.setUsageLimitPerUser(request.usageLimitPerUser());
        voucher.setApplicableProductIds(request.applicableProductIds());
        voucher.setApplicableCategoryIds(request.applicableCategoryIds());
        voucher.setStatus(VoucherStatus.DRAFT);
        
        voucher = voucherRepository.save(voucher);
        log.info("Created voucher {} for shop {}", voucher.getCode(), shop.getId());
        
        return SellerVoucherResponse.from(voucher);
    }

    @Transactional
    public SellerVoucherResponse updateVoucher(Long sellerId, Long voucherId, SellerVoucherRequest request) {
        SellerShopEntity shop = getShopBySeller(sellerId);
        SellerVoucherEntity voucher = getVoucherByIdAndShop(voucherId, shop.getId());
        
        // Can only update DRAFT or PAUSED vouchers
        if (voucher.getStatus() != VoucherStatus.DRAFT && voucher.getStatus() != VoucherStatus.PAUSED) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                "Can only update vouchers in DRAFT or PAUSED status");
        }
        
        // Check code uniqueness if changed
        if (!voucher.getCode().equals(request.code().toUpperCase())) {
            if (voucherRepository.existsByShopIdAndCode(shop.getId(), request.code())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Voucher code already exists");
            }
            voucher.setCode(request.code().toUpperCase());
        }
        
        voucher.setName(request.name());
        voucher.setDescription(request.description());
        voucher.setDiscountType(DiscountType.valueOf(request.discountType()));
        voucher.setDiscountValue(request.discountValue());
        voucher.setMaxDiscountAmount(request.maxDiscountAmount());
        voucher.setMinOrderAmount(request.minOrderAmount());
        voucher.setStartDate(request.startDate());
        voucher.setEndDate(request.endDate());
        voucher.setUsageLimit(request.usageLimit());
        voucher.setUsageLimitPerUser(request.usageLimitPerUser());
        voucher.setApplicableProductIds(request.applicableProductIds());
        voucher.setApplicableCategoryIds(request.applicableCategoryIds());
        
        voucher = voucherRepository.save(voucher);
        log.info("Updated voucher {}", voucher.getId());
        
        return SellerVoucherResponse.from(voucher);
    }

    @Transactional
    public SellerVoucherResponse activateVoucher(Long sellerId, Long voucherId) {
        SellerShopEntity shop = getShopBySeller(sellerId);
        SellerVoucherEntity voucher = getVoucherByIdAndShop(voucherId, shop.getId());
        
        if (voucher.getStatus() != VoucherStatus.DRAFT && voucher.getStatus() != VoucherStatus.PAUSED) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                "Can only activate vouchers in DRAFT or PAUSED status");
        }
        
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher = voucherRepository.save(voucher);
        log.info("Activated voucher {}", voucher.getId());
        
        return SellerVoucherResponse.from(voucher);
    }

    @Transactional
    public SellerVoucherResponse pauseVoucher(Long sellerId, Long voucherId) {
        SellerShopEntity shop = getShopBySeller(sellerId);
        SellerVoucherEntity voucher = getVoucherByIdAndShop(voucherId, shop.getId());
        
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Can only pause active vouchers");
        }
        
        voucher.setStatus(VoucherStatus.PAUSED);
        voucher = voucherRepository.save(voucher);
        log.info("Paused voucher {}", voucher.getId());
        
        return SellerVoucherResponse.from(voucher);
    }

    @Transactional
    public void deleteVoucher(Long sellerId, Long voucherId) {
        SellerShopEntity shop = getShopBySeller(sellerId);
        SellerVoucherEntity voucher = getVoucherByIdAndShop(voucherId, shop.getId());
        
        // Soft delete by changing status
        voucher.setStatus(VoucherStatus.DELETED);
        voucherRepository.save(voucher);
        log.info("Soft deleted voucher {}", voucher.getId());
    }

    @Transactional(readOnly = true)
    public Page<SellerVoucherResponse> getVouchers(Long sellerId, String status, Pageable pageable) {
        SellerShopEntity shop = getShopBySeller(sellerId);
        
        Page<SellerVoucherEntity> vouchers;
        if (status != null && !status.isBlank()) {
            VoucherStatus voucherStatus = VoucherStatus.valueOf(status.toUpperCase());
            vouchers = voucherRepository.findByShopIdAndStatus(shop.getId(), voucherStatus, pageable);
        } else {
            vouchers = voucherRepository.findByShopId(shop.getId(), pageable);
        }
        
        return vouchers.map(SellerVoucherResponse::from);
    }

    @Transactional(readOnly = true)
    public SellerVoucherResponse getVoucherDetail(Long sellerId, Long voucherId) {
        SellerShopEntity shop = getShopBySeller(sellerId);
        SellerVoucherEntity voucher = getVoucherByIdAndShop(voucherId, shop.getId());
        return SellerVoucherResponse.from(voucher);
    }

    // ============ BUYER OPERATIONS ============

    @Transactional(readOnly = true)
    public List<SellerVoucherResponse> getActiveVouchersForShop(Long shopId) {
        List<SellerVoucherEntity> vouchers = voucherRepository.findActiveVouchersByShop(shopId, Instant.now());
        return vouchers.stream().map(SellerVoucherResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SellerVoucherValidationResponse validateVoucher(Long userId, ValidateSellerVoucherRequest request) {
        // Find voucher
        var voucherOpt = voucherRepository.findValidVoucherByCodeAndShop(
            request.code().toUpperCase(), request.shopId(), Instant.now());
        
        if (voucherOpt.isEmpty()) {
            return SellerVoucherValidationResponse.invalid(
                request.code(), "Voucher not found or expired", "NOT_FOUND");
        }
        
        SellerVoucherEntity voucher = voucherOpt.get();
        
        // Check minimum order amount
        if (voucher.getMinOrderAmount() != null && request.orderTotal() < voucher.getMinOrderAmount()) {
            return SellerVoucherValidationResponse.invalid(
                request.code(),
                String.format("Minimum order amount is %,d VND", voucher.getMinOrderAmount()),
                "MIN_ORDER_NOT_MET"
            );
        }
        
        // Check per-user limit
        if (voucher.getUsageLimitPerUser() != null) {
            long userUsageCount = usageRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
            if (userUsageCount >= voucher.getUsageLimitPerUser()) {
                return SellerVoucherValidationResponse.invalid(
                    request.code(), "You have reached the usage limit for this voucher", "USER_LIMIT_REACHED");
            }
        }
        
        // Check product applicability
        if (voucher.getApplicableProductIds() != null && !voucher.getApplicableProductIds().isEmpty()) {
            if (request.productIds() == null || request.productIds().isEmpty()) {
                return SellerVoucherValidationResponse.invalid(
                    request.code(), "Voucher is not applicable to selected products", "NOT_APPLICABLE");
            }
            boolean hasApplicable = request.productIds().stream()
                .anyMatch(pid -> voucher.getApplicableProductIds().contains(pid));
            if (!hasApplicable) {
                return SellerVoucherValidationResponse.invalid(
                    request.code(), "Voucher is not applicable to selected products", "NOT_APPLICABLE");
            }
        }
        
        // Calculate discount
        long discountAmount = voucher.calculateDiscount(request.orderTotal());
        
        return SellerVoucherValidationResponse.valid(
            voucher.getCode(), voucher.getName(), discountAmount, request.orderTotal());
    }

    @Transactional
    public void applyVoucher(Long voucherId, Long userId, Long orderId, Long discountAmount) {
        SellerVoucherEntity voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Voucher not found"));
        
        // Increment usage count
        voucher.incrementUsageCount();
        voucherRepository.save(voucher);
        
        // Record usage
        SellerVoucherUsageEntity usage = new SellerVoucherUsageEntity();
        usage.setVoucherId(voucherId);
        usage.setUserId(userId);
        usage.setOrderId(orderId);
        usage.setDiscountAmount(discountAmount);
        usageRepository.save(usage);
        
        log.info("Applied voucher {} to order {}, discount: {}", voucher.getCode(), orderId, discountAmount);
    }

    // ============ HELPER METHODS ============

    private SellerShopEntity getShopBySeller(Long sellerId) {
        return shopRepository.findBySellerUserId(sellerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Shop not found for seller"));
    }

    private SellerVoucherEntity getVoucherByIdAndShop(Long voucherId, Long shopId) {
        SellerVoucherEntity voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Voucher not found"));
        
        if (!voucher.getShopId().equals(shopId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "Voucher does not belong to your shop");
        }
        
        return voucher;
    }
}
