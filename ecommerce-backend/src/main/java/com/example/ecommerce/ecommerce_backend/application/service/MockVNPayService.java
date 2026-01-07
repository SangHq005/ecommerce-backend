package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.config.VNPayConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Mock VNPay Service for testing without real VNPay account
 *
 * To enable mock mode, set in application.properties:
 * payment.vnpay.mock=true
 *
 * Mock behavior:
 * - Always returns a mock payment URL
 * - Auto-approves all payments
 * - No real VNPay API calls
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "payment.vnpay.mock", havingValue = "true")
public class MockVNPayService extends VNPayService {

    public MockVNPayService(VNPayConfig vnPayConfig) {
        super(vnPayConfig);
    }

    @Override
    public String createPaymentUrl(OrderEntity order, String ipAddress) {
        log.warn("=== MOCK VNPAY MODE ===");
        log.info("Creating MOCK payment URL for order: {}", order.getOrderCode());

        // Return mock payment URL that redirects directly to return URL with success params
        String mockReturnUrl = vnPayConfig.getReturnUrl() +
                "?vnp_TmnCode=" + vnPayConfig.getTmnCode() +
                "&vnp_Amount=" + (order.getTotalAmount() * 100) +
                "&vnp_BankCode=MOCK" +
                "&vnp_BankTranNo=MOCK" + System.currentTimeMillis() +
                "&vnp_CardType=ATM" +
                "&vnp_OrderInfo=Payment for order " + order.getOrderCode() +
                "&vnp_PayDate=" + System.currentTimeMillis() +
                "&vnp_ResponseCode=00" + // 00 = Success
                "&vnp_TmnCode=" + vnPayConfig.getTmnCode() +
                "&vnp_TransactionNo=" + System.currentTimeMillis() +
                "&vnp_TransactionStatus=00" + // 00 = Success
                "&vnp_TxnRef=" + order.getOrderCode() +
                "&vnp_SecureHash=MOCK_HASH_" + order.getOrderCode();

        log.info("Mock payment URL: {}", mockReturnUrl);
        log.warn("======================");

        return mockReturnUrl;
    }

    @Override
    public boolean validateCallback(Map<String, String> vnpParams) {
        log.warn("=== MOCK VNPAY MODE ===");
        log.info("Validating MOCK callback");

        String responseCode = vnpParams.get("vnp_ResponseCode");
        String transactionStatus = vnpParams.get("vnp_TransactionStatus");

        // In mock mode, always validate successfully if response code is 00
        boolean isValid = "00".equals(responseCode) && "00".equals(transactionStatus);

        log.info("Mock validation result: {}", isValid);
        log.warn("======================");

        return isValid;
    }
}
