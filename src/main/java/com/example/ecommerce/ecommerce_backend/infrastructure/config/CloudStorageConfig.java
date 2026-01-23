package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloud.storage")
public class CloudStorageConfig {

    private String provider = "cloudinary"; // local, cloudinary
    private String uploadDir = "uploads";
    private long maxFileSize = 10485760; // 10MB default
    
    // Cloudinary config
    private String cloudinaryCloudName;
    private String cloudinaryApiKey;
    private String cloudinaryApiSecret;

    // Getters and Setters
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }

    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }

    // Cloudinary getters/setters
    public String getCloudinaryCloudName() { return cloudinaryCloudName; }
    public void setCloudinaryCloudName(String cloudinaryCloudName) { this.cloudinaryCloudName = cloudinaryCloudName; }

    public String getCloudinaryApiKey() { return cloudinaryApiKey; }
    public void setCloudinaryApiKey(String cloudinaryApiKey) { this.cloudinaryApiKey = cloudinaryApiKey; }

    public String getCloudinaryApiSecret() { return cloudinaryApiSecret; }
    public void setCloudinaryApiSecret(String cloudinaryApiSecret) { this.cloudinaryApiSecret = cloudinaryApiSecret; }
}
