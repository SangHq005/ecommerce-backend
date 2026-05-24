package com.example.ecommerce.ecommerce_backend.application.service.catalog;

import com.example.ecommerce.ecommerce_backend.api.dto.catalog.ProductSKUDetailDTO;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class SKUService {
    
    private final SkuJpaRepository skuRepository;
    
    public SKUService(SkuJpaRepository skuRepository) {
        this.skuRepository = skuRepository;
    }
    
    /**
     * Find a SKU by product ID and option signature.
     * 
     * @param productId The product ID
     * @param optionSignature The option signature string (e.g., "Màu sắc:Đỏ|Kích cỡ:42")
     * @return Optional containing ProductSKUDetailDTO if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<ProductSKUDetailDTO> findSKUBySignature(Long productId, String optionSignature) {
        if (productId == null || optionSignature == null || optionSignature.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Calculate the hash of the option signature
        String signatureHash = calculateSignatureHash(optionSignature);
        
        // Query by product_id and option_signature_hash
        Optional<SkuEntity> skuEntity = skuRepository.findByProductIdAndOptionSignatureHash(productId, signatureHash);
        
        // Map to DTO if found
        return skuEntity.map(this::mapToDTO);
    }
    
    /**
     * Calculate SHA-256 hash of the option signature.
     * This matches the hash stored in the database.
     */
    private String calculateSignatureHash(String optionSignature) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(optionSignature.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Map SkuEntity to ProductSKUDetailDTO with calculated available stock.
     */
    private ProductSKUDetailDTO mapToDTO(SkuEntity sku) {
        return new ProductSKUDetailDTO(
                sku.getId(),
                sku.getSkuCode(),
                sku.getPrice(),
                sku.getCompareAtPrice(),
                sku.getStockOnHand(),
                sku.getReservedStock(),
                sku.getOptionSignature(),
                sku.isActive(),
                sku.getImageUrl()
        );
    }
}
