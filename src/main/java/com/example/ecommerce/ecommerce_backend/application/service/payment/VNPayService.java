package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentCallbackResult;
import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayStrategy;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.VNPayConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Concrete Strategy: VNPay payment gateway.
 * <p>
 * Registered automatically by Spring and discovered by
 * {@link com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayFactory}.
 * When {@code payment.vnpay.mock=true}, Spring loads {@link MockVNPayService} instead.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "payment.vnpay.mock", havingValue = "false", matchIfMissing = true)
public class VNPayService implements PaymentGatewayStrategy {

    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);

    protected final VNPayConfig vnPayConfig;

    public VNPayService(VNPayConfig vnPayConfig) {
        this.vnPayConfig = vnPayConfig;
    }

    // -------------------------------------------------------------------------
    // PaymentGatewayStrategy — identity
    // -------------------------------------------------------------------------

    @Override
    public String getPaymentMethod() {
        return "VNPAY";
    }

    // -------------------------------------------------------------------------
    // PaymentGatewayStrategy — URL creation
    // -------------------------------------------------------------------------

    /**
     * Generate a VNPay checkout URL for the given order.
     *
     * @param order     the order to pay for
     * @param ipAddress caller IP address required by VNPay
     * @return fully-signed payment URL
     */
    @Override
    public String createPaymentUrl(OrderEntity order, String ipAddress) {
        Map<String, String> vnpParams = new TreeMap<>();

        // Required parameters
        vnpParams.put("vnp_Version",   vnPayConfig.getVersion());
        vnpParams.put("vnp_Command",   vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode",   vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount",    String.valueOf(order.getTotalAmount() * 100)); // VNPay expects smallest unit
        vnpParams.put("vnp_CurrCode",  "VND");
        vnpParams.put("vnp_TxnRef",    order.getOrderCode());
        vnpParams.put("vnp_OrderInfo", "Payment for order " + order.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale",    "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr",    ipAddress);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        vnpParams.put("vnp_CreateDate", formatter.format(new Date()));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        calendar.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

        // Build query string
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            try {
                if (query.length() > 0) query.append('&');
                query.append(URLEncoder.encode(entry.getKey(),   StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding VNPay parameter", e);
            }
        }

        String queryString  = query.toString();
        String secureHash   = hmacSHA512(vnPayConfig.getHashSecret(), queryString);
        String paymentUrl   = vnPayConfig.getUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;

        log.info("Generated VNPay payment URL for order: {}", order.getOrderCode());
        return paymentUrl;
    }

    // -------------------------------------------------------------------------
    // PaymentGatewayStrategy — callback handling
    // -------------------------------------------------------------------------

    /**
     * Validate the HMAC-SHA512 signature attached to a VNPay callback.
     *
     * @param vnpParams all parameters received from VNPay (will be mutated to strip hash fields)
     * @return {@code true} if the signature is authentic
     */
    @Override
    public boolean validateCallback(Map<String, String> vnpParams) {
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            return false;
        }

        // Remove hash fields before computing expected hash
        vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");

        Map<String, String> sortedParams = new TreeMap<>(vnpParams);

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            try {
                if (query.length() > 0) query.append('&');
                query.append(URLEncoder.encode(entry.getKey(),   StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding VNPay parameter", e);
            }
        }

        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), query.toString());
        return calculatedHash.equals(vnpSecureHash);
    }

    /**
     * Parse a pre-validated VNPay callback into a gateway-agnostic {@link PaymentCallbackResult}.
     * <p>Response code {@code "00"} indicates success; any other code is treated as failure.</p>
     */
    @Override
    public PaymentCallbackResult processCallback(Map<String, String> params) {
        String responseCode  = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String orderCode     = params.get("vnp_TxnRef");

        Map<String, Object> raw = new HashMap<>(params);

        if ("00".equals(responseCode)) {
            log.info("VNPay callback SUCCESS — orderCode={}, transactionNo={}", orderCode, transactionNo);
            return PaymentCallbackResult.success(transactionNo, orderCode, raw);
        }

        log.warn("VNPay callback FAILURE — orderCode={}, responseCode={}", orderCode, responseCode);
        return PaymentCallbackResult.failure(orderCode, raw);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Compute HMAC-SHA512 of {@code data} using {@code key}.
     */
    protected String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error calculating HMAC SHA512", e);
            return "";
        }
    }
}
