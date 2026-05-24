package com.example.ecommerce.ecommerce_backend.application.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecommerce.ecommerce_backend.api.exception.ProductNotFoundException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock
    private ProductJpaRepository productRepo;

    @InjectMocks
    private ProductQueryService queryService;

    @Test
    void getActiveProduct_whenActive_returnsProduct() {
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setStatus("ACTIVE");

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        ProductEntity result = queryService.getActiveProduct(1L);
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(1L, result.getId());
    }

    @Test
    void getActiveProduct_whenInactive_throwsException() {
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setStatus("INACTIVE");

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> queryService.getActiveProduct(1L));
    }

    @Test
    void getActiveProduct_whenNotFound_throwsException() {
        when(productRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> queryService.getActiveProduct(1L));
    }
}
