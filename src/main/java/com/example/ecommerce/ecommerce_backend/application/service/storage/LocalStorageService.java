package com.example.ecommerce.ecommerce_backend.application.service.storage;

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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "cloud.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final CloudStorageConfig config;

    public LocalStorageService(CloudStorageConfig config) {
        this.config = config;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("File is empty");
        }

        if (file.getSize() > config.getMaxFileSize()) {
            throw ApiException.badRequest("File size exceeds maximum allowed size");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(config.getUploadDir(), folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL
            String fileUrl = "/files/" + folder + "/" + fileName;
            log.info("File uploaded successfully: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw ApiException.internalError("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public String uploadLocalFile(Path localFile, String folder) {
        if (!Files.exists(localFile)) {
            throw ApiException.notFound("Local file not found: " + localFile);
        }

        try {
            Path uploadPath = Paths.get(config.getUploadDir(), folder);
            Files.createDirectories(uploadPath);

            String fileName = localFile.getFileName().toString();
            Path target = uploadPath.resolve(fileName);
            Files.copy(localFile, target, StandardCopyOption.REPLACE_EXISTING);

            return "/files/" + folder + "/" + fileName;
        } catch (IOException e) {
            log.error("Failed to upload local file: {}", localFile, e);
            throw ApiException.internalError("Failed to upload local file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract file path from URL
            String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path path = Paths.get(filePath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted successfully: {}", fileUrl);
            } else {
                log.warn("File not found for deletion: {}", fileUrl);
            }

        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw ApiException.internalError("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        // For local storage, just return the file path
        return fileKey;
    }
}
