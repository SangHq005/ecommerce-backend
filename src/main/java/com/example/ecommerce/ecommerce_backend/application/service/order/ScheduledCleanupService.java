package com.example.ecommerce.ecommerce_backend.application.service.order;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshSessionEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshTokenEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.StockReservationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PasswordResetTokenJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefreshSessionJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefreshTokenJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.StockReservationJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshSessionEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RefreshTokenEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.StockReservationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.PasswordResetTokenJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefreshSessionJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RefreshTokenJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.StockReservationJpaRepository;

/**
 * Scheduled cleanup service for removing expired/old data
 * Runs periodic jobs to maintain database health and performance
 */
@Service
public class ScheduledCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledCleanupService.class);

    private final PasswordResetTokenJpaRepository passwordResetTokenRepository;
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final RefreshSessionJpaRepository refreshSessionRepository;
    private final StockReservationJpaRepository stockReservationRepository;
    private final SkuJpaRepository skuRepository;

    public ScheduledCleanupService(
            PasswordResetTokenJpaRepository passwordResetTokenRepository,
            RefreshTokenJpaRepository refreshTokenRepository,
            RefreshSessionJpaRepository refreshSessionRepository,
            StockReservationJpaRepository stockReservationRepository,
            SkuJpaRepository skuRepository
    ) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.stockReservationRepository = stockReservationRepository;
        this.skuRepository = skuRepository;
    }

    /**
     * Cleanup expired password reset tokens
     * Runs daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredPasswordResetTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        try {
            LocalDateTime now = LocalDateTime.now();
            passwordResetTokenRepository.deleteExpiredTokens(now);
            passwordResetTokenRepository.flush();
            log.info("Completed cleanup of expired password reset tokens");
        } catch (Exception e) {
            log.error("Failed to cleanup expired password reset tokens", e);
        }
    }

    /**
     * Cleanup expired and revoked refresh tokens
     * Runs daily at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoffDate = now.minusDays(30); // Keep tokens for 30 days

            // Find and delete expired tokens
            List<RefreshTokenEntity> expiredTokens = refreshTokenRepository.findAll()
                    .stream()
                    .filter(token -> token.getExpiresAt().isBefore(now) ||
                                   (token.getRevokedAt() != null && token.getRevokedAt().isBefore(cutoffDate)))
                    .toList();

            if (!expiredTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(expiredTokens);
                log.info("Deleted {} expired/revoked refresh tokens", expiredTokens.size());
            } else {
                log.info("No expired refresh tokens to cleanup");
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired refresh tokens", e);
        }
    }

    /**
     * Cleanup old refresh sessions (older than 30 days)
     * Runs weekly on Sunday at 4:00 AM
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    @Transactional
    public void cleanupOldRefreshSessions() {
        log.info("Starting cleanup of old refresh sessions");
        try {
            // Calculate cutoff date (30 days ago)
            Instant cutoffInstant = Instant.now().minus(30, ChronoUnit.DAYS);

            // Find and delete old sessions
            List<RefreshSessionEntity> oldSessions = refreshSessionRepository.findAll()
                    .stream()
                    .filter(session -> session.getCreatedAt() != null &&
                                     session.getCreatedAt().isBefore(cutoffInstant))
                    .toList();

            if (!oldSessions.isEmpty()) {
                refreshSessionRepository.deleteAll(oldSessions);
                log.info("Deleted {} old refresh sessions", oldSessions.size());
            } else {
                log.info("No old refresh sessions to cleanup");
            }
        } catch (Exception e) {
            log.error("Failed to cleanup old refresh sessions", e);
        }
    }

    /**
     * Release expired stock reservations
     * Runs every minute to quickly free up inventory
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void releaseExpiredStockReservations() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<StockReservationEntity> expiredReservations =
                    stockReservationRepository.findExpiredReservations(now);

            if (!expiredReservations.isEmpty()) {
                // Release each reservation and decrement SKU reserved_stock
                for (StockReservationEntity reservation : expiredReservations) {
                    // Decrement reserved_stock on the SKU
                    SkuEntity sku = skuRepository.findByIdForUpdate(reservation.getSkuId())
                            .orElse(null);
                    if (sku != null) {
                        sku.setReservedStock(Math.max(0, sku.getReservedStock() - reservation.getQty()));
                        skuRepository.save(sku);
                    }

                    reservation.setStatus("RELEASED");
                    stockReservationRepository.save(reservation);
                }

                log.info("Released {} expired stock reservations", expiredReservations.size());
            }
        } catch (Exception e) {
            log.error("Failed to release expired stock reservations", e);
        }
    }

    /**
     * Log cleanup service status
     * Runs every hour for monitoring
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void logCleanupServiceStatus() {
        log.debug("Cleanup service is running. Scheduled cleanups: " +
                "Password tokens (2AM daily), " +
                "Refresh tokens (3AM daily), " +
                "Sessions (Sunday 4AM), " +
                "Stock reservations (every minute)");
    }
}
