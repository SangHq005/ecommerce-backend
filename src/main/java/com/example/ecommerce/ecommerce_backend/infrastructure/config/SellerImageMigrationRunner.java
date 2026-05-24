package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerLocalImageMigrationService;
import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerLocalImageMigrationService.MigrationResult;

@Component
@ConditionalOnProperty(name = "app.migration.migrate-local-seller-images", havingValue = "true")
public class SellerImageMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SellerImageMigrationRunner.class);

    private final SellerLocalImageMigrationService migrationService;

    public SellerImageMigrationRunner(SellerLocalImageMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting seller local image migration (app.migration.migrate-local-seller-images=true)");
        MigrationResult result = migrationService.migrateAll();
        log.info("Startup migration complete: migrated={}, skipped={}, failed={}",
                result.migrated(), result.skipped(), result.failed());
    }
}
