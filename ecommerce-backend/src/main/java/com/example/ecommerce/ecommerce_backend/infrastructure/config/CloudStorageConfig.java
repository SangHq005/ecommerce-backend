package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloud.storage")
public class CloudStorageConfig {

    private String provider = "local"; // local, s3, cloudinary
    private String bucketName;
    private String region;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String uploadDir = "uploads";
    private long maxFileSize = 10485760; // 10MB default

    // Getters and Setters
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }

    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
}
