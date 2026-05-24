package com.example.ecommerce.ecommerce_backend.application.service.seller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.application.service.storage.CloudStorageService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductImageJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;

@Service
public class SellerLocalImageMigrationService {

    private static final Logger log = LoggerFactory.getLogger(SellerLocalImageMigrationService.class);
    private static final String LOCAL_PREFIX = "/files/";

    private final CloudStorageService cloudStorageService;
    private final ProductImageJpaRepository productImageRepo;
    private final ProductJpaRepository productRepo;
    private final SellerShopJpaRepository shopRepo;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public SellerLocalImageMigrationService(
            CloudStorageService cloudStorageService,
            ProductImageJpaRepository productImageRepo,
            ProductJpaRepository productRepo,
            SellerShopJpaRepository shopRepo
    ) {
        this.cloudStorageService = cloudStorageService;
        this.productImageRepo = productImageRepo;
        this.productRepo = productRepo;
        this.shopRepo = shopRepo;
    }

    public record MigrationResult(int migrated, int skipped, int failed) {}

    @Transactional
    public MigrationResult migrateAll() {
        Map<String, String> urlCache = new HashMap<>();
        int migrated = 0;
        int skipped = 0;
        int failed = 0;

        for (ProductImageEntity image : productImageRepo.findByImageUrlStartingWith(LOCAL_PREFIX)) {
            Outcome outcome = migrateUrl(image.getImageUrl(), "products", urlCache);
            switch (outcome) {
                case MIGRATED -> {
                    image.setImageUrl(urlCache.get(image.getImageUrl()));
                    productImageRepo.save(image);
                    migrated++;
                }
                case SKIPPED -> skipped++;
                case FAILED -> failed++;
            }
        }

        for (ProductEntity product : productRepo.findByMainImageUrlStartingWith(LOCAL_PREFIX)) {
            String oldUrl = product.getMainImageUrl();
            Outcome outcome = migrateUrl(oldUrl, "products", urlCache);
            switch (outcome) {
                case MIGRATED -> {
                    product.setMainImageUrl(urlCache.get(oldUrl));
                    productRepo.save(product);
                    migrated++;
                }
                case SKIPPED -> skipped++;
                case FAILED -> failed++;
            }
        }

        for (SellerShopEntity shop : shopRepo.findByLogoUrlStartingWith(LOCAL_PREFIX)) {
            String oldUrl = shop.getLogoUrl();
            Outcome outcome = migrateUrl(oldUrl, "shops", urlCache);
            switch (outcome) {
                case MIGRATED -> {
                    shop.setLogoUrl(urlCache.get(oldUrl));
                    shopRepo.save(shop);
                    migrated++;
                }
                case SKIPPED -> skipped++;
                case FAILED -> failed++;
            }
        }

        for (SellerShopEntity shop : shopRepo.findByBannerUrlStartingWith(LOCAL_PREFIX)) {
            String oldUrl = shop.getBannerUrl();
            Outcome outcome = migrateUrl(oldUrl, "shop-banners", urlCache);
            switch (outcome) {
                case MIGRATED -> {
                    shop.setBannerUrl(urlCache.get(oldUrl));
                    shopRepo.save(shop);
                    migrated++;
                }
                case SKIPPED -> skipped++;
                case FAILED -> failed++;
            }
        }

        log.info("Seller local image migration finished: migrated={}, skipped={}, failed={}", migrated, skipped, failed);
        return new MigrationResult(migrated, skipped, failed);
    }

    private enum Outcome { MIGRATED, SKIPPED, FAILED }

    private Outcome migrateUrl(String oldUrl, String cloudFolder, Map<String, String> urlCache) {
        if (oldUrl == null || oldUrl.isBlank() || !oldUrl.startsWith(LOCAL_PREFIX)) {
            return Outcome.SKIPPED;
        }

        if (urlCache.containsKey(oldUrl)) {
            return Outcome.MIGRATED;
        }

        Path localFile = resolveLocalPath(oldUrl);
        if (!Files.exists(localFile)) {
            log.warn("Skipping migration, local file missing for URL {} at {}", oldUrl, localFile);
            return Outcome.SKIPPED;
        }

        try {
            String newUrl = cloudStorageService.uploadLocalFile(localFile, cloudFolder);
            urlCache.put(oldUrl, newUrl);
            deleteLocalFileQuietly(localFile);
            log.info("Migrated {} -> {}", oldUrl, newUrl);
            return Outcome.MIGRATED;
        } catch (Exception e) {
            log.error("Failed to migrate local image {}: {}", oldUrl, e.getMessage(), e);
            return Outcome.FAILED;
        }
    }

    private Path resolveLocalPath(String fileUrl) {
        String relative = fileUrl.startsWith(LOCAL_PREFIX)
                ? fileUrl.substring(LOCAL_PREFIX.length())
                : fileUrl;
        return Paths.get(uploadDir).resolve(relative).normalize();
    }

    private void deleteLocalFileQuietly(Path localFile) {
        try {
            Files.deleteIfExists(localFile);
        } catch (IOException e) {
            log.warn("Could not delete local file after migration: {}", localFile, e);
        }
    }
}
