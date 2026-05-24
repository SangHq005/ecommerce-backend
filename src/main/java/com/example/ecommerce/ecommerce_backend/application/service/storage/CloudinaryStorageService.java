package com.example.ecommerce.ecommerce_backend.application.service.storage;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cloudinary implementation of CloudStorageService.
 * Upload path convention: {prefix}/{folder}/{uuid}
 */
@Service
@ConditionalOnProperty(name = "cloud.storage.provider", havingValue = "cloudinary")
public class CloudinaryStorageService implements CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;
    private final CloudStorageConfig config;

    public CloudinaryStorageService(CloudStorageConfig config) {
        log.info("Initializing Cloudinary Storage Service...");

        String cloudName = config.getCloudinaryCloudName();
        String apiKey = config.getCloudinaryApiKey();
        String apiSecret = config.getCloudinaryApiSecret();

        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Cloudinary configuration is incomplete. Please set cloud.storage.cloudinary-cloud-name, "
                            + "cloud.storage.cloudinary-api-key, and cloud.storage.cloudinary-api-secret"
            );
        }

        this.config = config;
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
            return uploadBytes(file.getBytes(), folder);
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage(), e);
            throw ApiException.internalError("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public String uploadLocalFile(Path localFile, String folder) {
        try {
            if (!Files.exists(localFile)) {
                throw ApiException.notFound("Local file not found: " + localFile);
            }
            byte[] bytes = Files.readAllBytes(localFile);
            String secureUrl = uploadBytes(bytes, folder);
            log.info("Local file uploaded to Cloudinary: {} -> {}", localFile, secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload local file to Cloudinary: {}", localFile, e);
            throw ApiException.internalError("Failed to upload local file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            log.info("Deleting file from Cloudinary: {}", fileUrl);

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
        log.info("Generating signed URL for: {}", fileKey);
        String signedUrl = cloudinary.url()
                .signed(true)
                .type("authenticated")
                .generate(fileKey);
        return signedUrl != null ? signedUrl : fileKey;
    }

    private String uploadBytes(byte[] bytes, String folder) throws IOException {
        String publicId = buildPublicId(folder);

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", true,
                "tags", buildTags(folder)
        ));

        String secureUrl = (String) uploadResult.get("secure_url");
        log.info("File uploaded successfully to Cloudinary: {}", secureUrl);
        return secureUrl;
    }

    private String buildPublicId(String folder) {
        String prefix = config.getPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "shopmart";
        }
        return prefix + "/" + folder + "/" + UUID.randomUUID();
    }

    private List<String> buildTags(String folder) {
        String prefix = config.getPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "shopmart";
        }
        return List.of(prefix, folder.replace("/", "-"));
    }

    private String extractPublicId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            String afterUpload = url.substring(uploadIndex + "/upload/".length());

            if (afterUpload.startsWith("v")) {
                int slashIndex = afterUpload.indexOf("/");
                if (slashIndex != -1) {
                    afterUpload = afterUpload.substring(slashIndex + 1);
                }
            }

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
