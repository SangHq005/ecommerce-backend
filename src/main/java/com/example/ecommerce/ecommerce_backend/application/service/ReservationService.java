package com.example.ecommerce.ecommerce_backend.application.service;

public interface ReservationService {
    void reserve(String orderToken, Long skuId, Integer qty);
    void release(String orderToken);
    void commit(String orderToken);
    void restore(String orderToken);
}
