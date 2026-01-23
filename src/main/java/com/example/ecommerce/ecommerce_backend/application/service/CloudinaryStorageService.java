package com.example.ecommerce.ecommerce_backend.application.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.CloudStorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Cloudinary implementation of CloudStorageService
 * Activated when cloud.storage.provider=cloudinary
 */
@Service
@ConditionalOnProperty(name = "cloud.storage.provider", havingValue = "cloudinary")
public class CloudinaryStorageService implements CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(CloudStorageConfig config) {
        log.info("Initializing Cloudinary Storage Service...");
        
        String cloudName = config.getCloudinaryCloudName();
        String apiKey = config.getCloudinaryApiKey();
        String apiSecret = config.getCloudinaryApiSecret();
        
        if (cloudName == null || cloudName.isBlank() ||
            apiKey == null || apiKey.isBlank() ||
            apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                "Cloudinary configuration is incomplete. Please set cloud.storage.cloudinary-cloud-name, " +
                "cloud.storage.cloudinary-api-key, and cloud.storage.cloudinary-api-secret"
            );
        }
        
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
        
        log.info("Cloudinary initialized successfully for cloud: {}", cloudName);
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            log.info("Uploading file to Cloudinary: {} in folder: {}", file.getOriginalFilename(), folder);
            
            // Generate unique public ID (without extension)
            String originalFilename = file.getOriginalFilename();
            String baseName = originalFilename;
            if (originalFilename != null && originalFilename.contains(".")) {
                baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            }
            
            // Create a clean public ID
            String publicId = "shopmart/" + folder + "/" + baseName + "_" + UUID.randomUUID().toString().substring(0, 8);
            
            // Upload options - keep it simple without transformation
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", true
            ));
            
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully to Cloudinary: {}", secureUrl);
            
            return secureUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage(), e);
            throw ApiException.internalError("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            log.info("Deleting file from Cloudinary: {}", fileUrl);
            
            // Extract public ID from URL
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
            String publicId = extractPublicId(fileUrl);
            
            if (publicId == null || publicId.isBlank()) {
                log.warn("Could not extract public_id from URL: {}", fileUrl);
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            String resultStatus = (String) result.get("result");
            if ("ok".equals(resultStatus)) {
                log.info("File deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("Cloudinary delete returned: {} for {}", resultStatus, publicId);
            }
            
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", e.getMessage(), e);
            throw ApiException.internalError("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        // Cloudinary handles this differently - return signed URL with transformation
        log.info("Generating signed URL for: {}", fileKey);
        
        long expirationTime = System.currentTimeMillis() / 1000 + (expirationMinutes * 60L);
        
        String signedUrl = cloudinary.url()
            .signed(true)
            .type("authenticated")
            .generate(fileKey);
        
        return signedUrl != null ? signedUrl : fileKey;
    }

    /**
     * Extract public_id from Cloudinary URL
     */
    private String extractPublicId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        
        try {
            // URL format: https://res.cloudinary.com/{cloud}/image/upload/v{version}/{folder}/{public_id}.{ext}
            // We need to extract everything after /upload/v{version}/ without the extension
            
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }
            
            String afterUpload = url.substring(uploadIndex + "/upload/".length());
            
            // Skip version if present (v123456789/)
            if (afterUpload.startsWith("v")) {
                int slashIndex = afterUpload.indexOf("/");
                if (slashIndex != -1) {
                    afterUpload = afterUpload.substring(slashIndex + 1);
                }
            }
            
            // Remove file extension
            int lastDotIndex = afterUpload.lastIndexOf(".");
            if (lastDotIndex != -1) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }
            
            return afterUpload;
            
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", url, e);
            return null;
        }
    }
}
