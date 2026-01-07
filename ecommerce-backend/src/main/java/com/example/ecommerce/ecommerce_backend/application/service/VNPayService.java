package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.config.VNPayConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "payment.vnpay.mock", havingValue = "false", matchIfMissing = true)
public class VNPayService {

    protected final VNPayConfig vnPayConfig;

    /**
     * Create payment URL for VNPay gateway
     *
     * @param order    The order to create payment for
     * @param ipAddress Client IP address
     * @return Payment URL to redirect user
     */
    public String createPaymentUrl(OrderEntity order, String ipAddress) {
        Map<String, String> vnpParams = new TreeMap<>();

        // Required parameters
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(order.getTotalAmount() * 100)); // VNPay requires amount in smallest unit
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", order.getOrderCode()); // Use order code as transaction reference
        vnpParams.put("vnp_OrderInfo", "Payment for order " + order.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);

        // Create date in format yyyyMMddHHmmss
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnpCreateDate = formatter.format(new Date());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        // Set expire date (15 minutes from now)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        calendar.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(calendar.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build query string
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            try {
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding parameter", e);
            }
        }

        // Generate secure hash
        String queryString = query.toString();
        String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), queryString);

        // Build final payment URL
        String paymentUrl = vnPayConfig.getUrl() + "?" + queryString + "&vnp_SecureHash=" + vnpSecureHash;

        log.info("Generated VNPay payment URL for order: {}", order.getOrderCode());
        return paymentUrl;
    }

    /**
     * Validate callback from VNPay
     *
     * @param vnpParams All parameters from VNPay callback
     * @return true if signature is valid
     */
    public boolean validateCallback(Map<String, String> vnpParams) {
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            return false;
        }

        // Remove hash and hash type from params
        vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");

        // Sort params
        Map<String, String> sortedParams = new TreeMap<>(vnpParams);

        // Build query string
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            try {
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding parameter", e);
            }
        }

        // Calculate hash
        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), query.toString());

        // Compare hashes
        return calculatedHash.equals(vnpSecureHash);
    }

    /**
     * Calculate HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
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
