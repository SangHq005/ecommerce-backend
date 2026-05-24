package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentCallbackResult;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.VNPayConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock VNPay Strategy — used during local development and testing.
 * <p>
 * Activated when {@code payment.vnpay.mock=true} in application properties.
 * All callbacks are auto-approved; no real VNPay API calls are made.
 * </p>
 * <p>
 * Extends {@link VNPayService} so it remains a valid {@code PaymentGatewayStrategy}
 * and is auto-discovered by {@link com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayFactory}.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "payment.vnpay.mock", havingValue = "true")
public class MockVNPayService extends VNPayService {

    private static final Logger log = LoggerFactory.getLogger(MockVNPayService.class);

    public MockVNPayService(VNPayConfig vnPayConfig) {
        super(vnPayConfig);
    }

    @Override
    public String createPaymentUrl(OrderEntity order, String ipAddress) {
        log.warn("=== MOCK VNPAY MODE ===");
        log.info("Creating MOCK payment URL for order: {}", order.getOrderCode());

        // Return a mock URL that mimics a successful VNPay redirect back to our return URL
        String mockReturnUrl = vnPayConfig.getReturnUrl()
                + "?vnp_TmnCode="          + vnPayConfig.getTmnCode()
                + "&vnp_Amount="           + (order.getTotalAmount() * 100)
                + "&vnp_BankCode=MOCK"
                + "&vnp_BankTranNo=MOCK"   + System.currentTimeMillis()
                + "&vnp_CardType=ATM"
                + "&vnp_OrderInfo=Payment for order " + order.getOrderCode()
                + "&vnp_PayDate="          + System.currentTimeMillis()
                + "&vnp_ResponseCode=00"
                + "&vnp_TransactionNo="    + System.currentTimeMillis()
                + "&vnp_TransactionStatus=00"
                + "&vnp_TxnRef="           + order.getOrderCode()
                + "&vnp_SecureHash=MOCK_HASH_" + order.getOrderCode();

        log.info("Mock payment URL generated for order: {}", order.getOrderCode());
        log.warn("======================");
        return mockReturnUrl;
    }

    @Override
    public boolean validateCallback(Map<String, String> vnpParams) {
        log.warn("=== MOCK VNPAY MODE ===");
        String responseCode      = vnpParams.get("vnp_ResponseCode");
        String transactionStatus = vnpParams.get("vnp_TransactionStatus");
        boolean isValid = "00".equals(responseCode) && "00".equals(transactionStatus);
        log.info("Mock VNPay validation result: {}", isValid);
        log.warn("======================");
        return isValid;
    }

    @Override
    public PaymentCallbackResult processCallback(Map<String, String> params) {
        log.warn("=== MOCK VNPAY MODE — processCallback ===");
        String orderCode     = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String responseCode  = params.get("vnp_ResponseCode");

        Map<String, Object> raw = new HashMap<>(params);

        if ("00".equals(responseCode)) {
            log.info("Mock VNPay callback SUCCESS — orderCode={}", orderCode);
            return PaymentCallbackResult.success(transactionNo, orderCode, raw);
        }

        log.warn("Mock VNPay callback FAILURE — orderCode={}, responseCode={}", orderCode, responseCode);
        return PaymentCallbackResult.failure(orderCode, raw);
    }
}
