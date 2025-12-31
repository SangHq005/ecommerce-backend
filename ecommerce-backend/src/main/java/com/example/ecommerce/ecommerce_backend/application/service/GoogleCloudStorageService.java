package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.CloudStorageConfig;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud.storage.provider", havingValue = "gcs")
public class GoogleCloudStorageService implements CloudStorageService {

    private final CloudStorageConfig config;
    private final Storage storage;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("File is empty");
        }

        if (file.getSize() > config.getMaxFileSize()) {
            throw ApiException.badRequest("File size exceeds maximum allowed size");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String blobName = folder + "/" + fileName;

            // Create blob info
            BlobId blobId = BlobId.of(config.getBucketName(), blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // Upload file
            storage.create(blobInfo, file.getBytes());

            // Return public URL
            String fileUrl = String.format("https://storage.googleapis.com/%s/%s",
                    config.getBucketName(), blobName);

            log.info("File uploaded to GCS successfully: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to GCS", e);
            throw ApiException.internalError("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract blob name from URL
            String blobName = extractBlobNameFromUrl(fileUrl);

            if (blobName == null) {
                log.warn("Invalid file URL: {}", fileUrl);
                return;
            }

            BlobId blobId = BlobId.of(config.getBucketName(), blobName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("File deleted from GCS successfully: {}", fileUrl);
            } else {
                log.warn("File not found in GCS: {}", fileUrl);
            }

        } catch (Exception e) {
            log.error("Failed to delete file from GCS: {}", fileUrl, e);
            throw ApiException.internalError("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        try {
            BlobId blobId = BlobId.of(config.getBucketName(), fileKey);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            URL signedUrl = storage.signUrl(
                    blobInfo,
                    expirationMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature()
            );

            return signedUrl.toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL", e);
            throw ApiException.internalError("Failed to generate presigned URL: " + e.getMessage());
        }
    }

    /**
     * Extract blob name from GCS URL
     */
    private String extractBlobNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        // Handle format: https://storage.googleapis.com/bucket-name/path/to/file
        if (fileUrl.contains("storage.googleapis.com")) {
            String[] parts = fileUrl.split(config.getBucketName() + "/", 2);
            if (parts.length == 2) {
                return parts[1];
            }
        }

        return null;
    }
}
