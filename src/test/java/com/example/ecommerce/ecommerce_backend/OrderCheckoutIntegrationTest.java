package com.example.ecommerce.ecommerce_backend;

import com.example.ecommerce.ecommerce_backend.api.controller.CheckoutController;
import com.example.ecommerce.ecommerce_backend.api.dto.order.CheckoutRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.application.service.order.OrderService;
import com.example.ecommerce.ecommerce_backend.api.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(CheckoutController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class) // Ensure exception handler is loaded
public class OrderCheckoutIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    @MockBean private OrderService orderService;
    @MockBean private com.example.ecommerce.ecommerce_backend.application.service.auth.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void checkout_ShouldReturn200_WhenDataIsValid() throws Exception {
        CheckoutRequest.Item itemDto = new CheckoutRequest.Item(1L, 2L, 2);
        CheckoutRequest req = new CheckoutRequest(List.of(itemDto), 10L, "COD", "Test Note", null);

        OrderResponse mockResponse = new OrderResponse(
                "ORD-123", "PENDING", 200000L, "VND", java.time.LocalDateTime.now(), "COD", 0L, 0L, "Test Note", 10L, "Test User", "0123456789", "123 Test St", List.of()
        );

        when(orderService.checkout(anyLong(), anyString(), org.mockito.ArgumentMatchers.any(CheckoutRequest.class)))
                .thenReturn(List.of(mockResponse));

        mockMvc.perform(post("/api/v1/checkout")
                        .header("Idempotency-Key", "idem-123")
                        .with(csrf()) // Spring Security test often requires CSRF token for POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orderCode", is("ORD-123")));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void checkout_ShouldReturn400_WhenServiceThrowsException() throws Exception {
        CheckoutRequest.Item itemDto = new CheckoutRequest.Item(1L, 999L, 2);
        CheckoutRequest req = new CheckoutRequest(List.of(itemDto), 10L, "COD", "Test Note", null);

        when(orderService.checkout(anyLong(), anyString(), org.mockito.ArgumentMatchers.any(CheckoutRequest.class)))
                .thenThrow(com.example.ecommerce.ecommerce_backend.api.exception.ApiException.badRequest("SKU not found"));

        mockMvc.perform(post("/api/v1/checkout")
                        .header("Idempotency-Key", "idem-fail")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")));
    }
}
