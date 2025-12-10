package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud.storage.provider", havingValue = "gcs")
public class GcsConfiguration {

    private final CloudStorageConfig config;

    @Bean
    public Storage storage() throws IOException {
        log.info("Initializing Google Cloud Storage client");

        StorageOptions.Builder builder = StorageOptions.newBuilder();

        // Set project ID if available
        if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
            builder.setHost(config.getEndpoint());
        }

        // Load credentials from file or use application default credentials
        if (config.getAccessKey() != null && !config.getAccessKey().isEmpty()) {
            // Use service account key file
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(config.getAccessKey())
            );
            builder.setCredentials(credentials);
        } else {
            // Use Application Default Credentials (ADC)
            log.info("Using Application Default Credentials for GCS");
        }

        Storage storage = builder.build().getService();
        log.info("Google Cloud Storage client initialized successfully");

        return storage;
    }
}
