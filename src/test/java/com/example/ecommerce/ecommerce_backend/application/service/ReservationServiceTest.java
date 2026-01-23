package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.StockReservationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.StockReservationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock
    private SkuJpaRepository skuRepo;

    @Mock
    private StockReservationJpaRepository resRepo;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private SkuEntity createSku(Long id, int stockOnHand, int reservedStock) {
        SkuEntity sku = new SkuEntity();
        try {
            var idField = SkuEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sku, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set SKU id", e);
        }
        sku.setStockOnHand(stockOnHand);
        sku.setReservedStock(reservedStock);
        return sku;
    }

    private StockReservationEntity createReservation(String orderToken, Long skuId, int qty, String status) {
        StockReservationEntity r = new StockReservationEntity();
        r.setOrderToken(orderToken);
        r.setSkuId(skuId);
        r.setQty(qty);
        r.setStatus(status);
        return r;
    }

    @Nested
    @DisplayName("reserve()")
    class ReserveTests {

        @Test
        @DisplayName("Should reserve stock successfully when sufficient stock available")
        void reserve_Success_WhenSufficientStock() {
            // Arrange
            String orderToken = "ORDER-001";
            Long skuId = 1L;
            int qty = 5;
            SkuEntity sku = createSku(skuId, 100, 10); // 90 available

            when(resRepo.findByOrderTokenAndSkuId(orderToken, skuId)).thenReturn(Optional.empty());
            when(skuRepo.findByIdForUpdate(skuId)).thenReturn(Optional.of(sku));

            // Act
            reservationService.reserve(orderToken, skuId, qty);

            // Assert
            assertEquals(15, sku.getReservedStock()); // 10 + 5
            verify(skuRepo).save(sku);

            ArgumentCaptor<StockReservationEntity> captor = ArgumentCaptor.forClass(StockReservationEntity.class);
            verify(resRepo).save(captor.capture());
            StockReservationEntity saved = captor.getValue();
            assertEquals(orderToken, saved.getOrderToken());
            assertEquals(skuId, saved.getSkuId());
            assertEquals(qty, saved.getQty());
            assertEquals("RESERVED", saved.getStatus());
            assertNotNull(saved.getExpiresAt());
            assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
        }

        @Test
        @DisplayName("Should be idempotent - return early for existing RESERVED reservation with same qty")
        void reserve_Idempotent_WhenExistingReservationWithSameQty() {
            // Arrange
            String orderToken = "ORDER-001";
            Long skuId = 1L;
            int qty = 5;
            StockReservationEntity existing = createReservation(orderToken, skuId, qty, "RESERVED");

            when(resRepo.findByOrderTokenAndSkuId(orderToken, skuId)).thenReturn(Optional.of(existing));

            // Act
            reservationService.reserve(orderToken, skuId, qty);

            // Assert - no additional calls
            verify(skuRepo, never()).findByIdForUpdate(anyLong());
            verify(skuRepo, never()).save(any());
            verify(resRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw conflict when reservation already COMMITTED")
        void reserve_ThrowsConflict_WhenReservationAlreadyCommitted() {
            // Arrange
            String orderToken = "ORDER-001";
            Long skuId = 1L;
            int qty = 5;
            StockReservationEntity existing = createReservation(orderToken, skuId, qty, "COMMITTED");

            when(resRepo.findByOrderTokenAndSkuId(orderToken, skuId)).thenReturn(Optional.of(existing));

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class, 
                () -> reservationService.reserve(orderToken, skuId, qty));
            assertEquals(409, ex.getStatus());
            assertTrue(ex.getMessage().contains("COMMITTED"));
        }

        @Test
        @DisplayName("Should throw conflict when qty mismatch for existing reservation")
        void reserve_ThrowsConflict_WhenQtyMismatch() {
            // Arrange
            String orderToken = "ORDER-001";
            Long skuId = 1L;
            StockReservationEntity existing = createReservation(orderToken, skuId, 5, "RESERVED");

            when(resRepo.findByOrderTokenAndSkuId(orderToken, skuId)).thenReturn(Optional.of(existing));

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class, 
                () -> reservationService.reserve(orderToken, skuId, 10)); // Different qty
            assertEquals(409, ex.getStatus());
            assertTrue(ex.getMessage().contains("mismatch"));
        }

        @Test
        @DisplayName("Should throw not found when SKU does not exist")
        void reserve_ThrowsNotFound_WhenSkuNotFound() {
            // Arrange
            String orderToken = "ORDER-001";
            Long skuId = 999L;

            when(resRepo.findByOrderTokenAndSkuId(orderToken, skuId)).thenReturn(Optional.empty());
            when(skuRepo.findByIdForUpdate(skuId)).thenReturn(Optional.empty());

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class, 
                () -> reservationService.reserve(orderToken, skuId, 5));
            assertEquals(404, ex.getStatus());
        }

        @Test
        @DisplayName("Should throw conflict when insufficient stock")
        void reserve_ThrowsConflict_WhenInsufficientStock() {
            // Arrange
            String orderToken = "ORDER-001";
            Long skuId = 1L;
            SkuEntity sku = createSku(skuId, 10, 8); // Only 2 available

            when(resRepo.findByOrderTokenAndSkuId(orderToken, skuId)).thenReturn(Optional.empty());
            when(skuRepo.findByIdForUpdate(skuId)).thenReturn(Optional.of(sku));

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class, 
                () -> reservationService.reserve(orderToken, skuId, 5)); // Requesting 5, only 2 available
            assertEquals(409, ex.getStatus());
            assertTrue(ex.getMessage().contains("Insufficient stock"));
        }

        @Test
        @DisplayName("Should reserve exact available stock (boundary)")
        void reserve_Success_WhenExactStockAvailable() {
            // Arrange
            String orderToken = "ORDER-001";
            Long skuId = 1L;
            int qty = 10;
            SkuEntity sku = createSku(skuId, 100, 90); // Exactly 10 available

            when(resRepo.findByOrderTokenAndSkuId(orderToken, skuId)).thenReturn(Optional.empty());
            when(skuRepo.findByIdForUpdate(skuId)).thenReturn(Optional.of(sku));

            // Act
            reservationService.reserve(orderToken, skuId, qty);

            // Assert
            assertEquals(100, sku.getReservedStock()); // Now fully reserved
            verify(skuRepo).save(sku);
        }
    }

    @Nested
    @DisplayName("release()")
    class ReleaseTests {

        @Test
        @DisplayName("Should release all RESERVED reservations for order token")
        void release_Success_ReleasesAllReserved() {
            // Arrange
            String orderToken = "ORDER-001";
            SkuEntity sku1 = createSku(1L, 100, 20);
            SkuEntity sku2 = createSku(2L, 50, 10);

            StockReservationEntity r1 = createReservation(orderToken, 1L, 5, "RESERVED");
            StockReservationEntity r2 = createReservation(orderToken, 2L, 3, "RESERVED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1, r2));
            when(skuRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(sku1));
            when(skuRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(sku2));

            // Act
            reservationService.release(orderToken);

            // Assert
            assertEquals(15, sku1.getReservedStock()); // 20 - 5
            assertEquals(7, sku2.getReservedStock());  // 10 - 3
            assertEquals("RELEASED", r1.getStatus());
            assertEquals("RELEASED", r2.getStatus());
            verify(skuRepo, times(2)).save(any());
            verify(resRepo, times(2)).save(any());
        }

        @Test
        @DisplayName("Should skip already RELEASED reservations")
        void release_SkipsAlreadyReleased() {
            // Arrange
            String orderToken = "ORDER-001";
            StockReservationEntity r1 = createReservation(orderToken, 1L, 5, "RELEASED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1));

            // Act
            reservationService.release(orderToken);

            // Assert
            verify(skuRepo, never()).findByIdForUpdate(anyLong());
        }

        @Test
        @DisplayName("Should skip COMMITTED reservations")
        void release_SkipsCommitted() {
            // Arrange
            String orderToken = "ORDER-001";
            StockReservationEntity r1 = createReservation(orderToken, 1L, 5, "COMMITTED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1));

            // Act
            reservationService.release(orderToken);

            // Assert
            verify(skuRepo, never()).findByIdForUpdate(anyLong());
        }

        @Test
        @DisplayName("Should not go below zero for reserved stock")
        void release_DoesNotGoBelowZero() {
            // Arrange
            String orderToken = "ORDER-001";
            SkuEntity sku = createSku(1L, 100, 3); // Reserved stock is less than reservation qty (data inconsistency scenario)
            StockReservationEntity r1 = createReservation(orderToken, 1L, 10, "RESERVED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1));
            when(skuRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(sku));

            // Act
            reservationService.release(orderToken);

            // Assert
            assertEquals(0, sku.getReservedStock()); // Math.max(0, 3 - 10) = 0
        }
    }

    @Nested
    @DisplayName("commit()")
    class CommitTests {

        @Test
        @DisplayName("Should commit reservation and decrement both stock_on_hand and reserved_stock")
        void commit_Success() {
            // Arrange
            String orderToken = "ORDER-001";
            SkuEntity sku = createSku(1L, 100, 20);
            StockReservationEntity r1 = createReservation(orderToken, 1L, 5, "RESERVED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1));
            when(skuRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(sku));

            // Act
            reservationService.commit(orderToken);

            // Assert
            assertEquals(95, sku.getStockOnHand()); // 100 - 5
            assertEquals(15, sku.getReservedStock()); // 20 - 5
            assertEquals("COMMITTED", r1.getStatus());
            verify(skuRepo).save(sku);
            verify(resRepo).save(r1);
        }

        @Test
        @DisplayName("Should skip already COMMITTED reservations")
        void commit_SkipsAlreadyCommitted() {
            // Arrange
            String orderToken = "ORDER-001";
            StockReservationEntity r1 = createReservation(orderToken, 1L, 5, "COMMITTED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1));

            // Act
            reservationService.commit(orderToken);

            // Assert
            verify(skuRepo, never()).findByIdForUpdate(anyLong());
        }

        @Test
        @DisplayName("Should throw conflict on stock underflow")
        void commit_ThrowsConflict_OnStockUnderflow() {
            // Arrange
            String orderToken = "ORDER-001";
            SkuEntity sku = createSku(1L, 3, 5); // Only 3 on hand, trying to commit 5
            StockReservationEntity r1 = createReservation(orderToken, 1L, 5, "RESERVED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1));
            when(skuRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(sku));

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class, 
                () -> reservationService.commit(orderToken));
            assertEquals(409, ex.getStatus());
            assertTrue(ex.getMessage().contains("underflow"));
        }

        @Test
        @DisplayName("Should commit multiple reservations for same order")
        void commit_MultipleReservations() {
            // Arrange
            String orderToken = "ORDER-001";
            SkuEntity sku1 = createSku(1L, 100, 20);
            SkuEntity sku2 = createSku(2L, 50, 10);
            StockReservationEntity r1 = createReservation(orderToken, 1L, 5, "RESERVED");
            StockReservationEntity r2 = createReservation(orderToken, 2L, 3, "RESERVED");

            when(resRepo.findByOrderToken(orderToken)).thenReturn(List.of(r1, r2));
            when(skuRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(sku1));
            when(skuRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(sku2));

            // Act
            reservationService.commit(orderToken);

            // Assert
            assertEquals(95, sku1.getStockOnHand());
            assertEquals(15, sku1.getReservedStock());
            assertEquals(47, sku2.getStockOnHand());
            assertEquals(7, sku2.getReservedStock());
            assertEquals("COMMITTED", r1.getStatus());
            assertEquals("COMMITTED", r2.getStatus());
        }
    }
}
