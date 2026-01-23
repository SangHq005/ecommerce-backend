package com.example.ecommerce.ecommerce_backend.application.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ecommerce.ecommerce_backend.infrastructure.config.MomoConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MomoService {

    private static final Logger log = LoggerFactory.getLogger(MomoService.class);

    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MomoService(MomoConfig momoConfig) {
        this.momoConfig = momoConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String createPaymentUrl(OrderEntity order) {
        try {
            // MoMo requires unique orderId for each request
            // Use format: orderCode_timestamp to ensure uniqueness on retries
            String uniqueId = order.getOrderCode() + "_" + System.currentTimeMillis();
            String requestId = uniqueId;
            String orderId = uniqueId; // Must be unique per request to avoid "duplicate orderId" error
            // Momo requires amount as String (integer)
            String amount = String.valueOf(order.getTotalAmount().longValue()); 
            String orderInfo = "Payment for order " + order.getOrderCode();
            String redirectUrl = momoConfig.getReturnUrl();
            String ipnUrl = momoConfig.getIpnUrl();
            String requestType = "captureWallet";
            // Store original orderCode in extraData for callback processing
            String extraData = java.util.Base64.getEncoder().encodeToString(
                ("orderCode=" + order.getOrderCode()).getBytes(StandardCharsets.UTF_8)
            );

            // Signature format: 
            // accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + redirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            String signature = hmacSHA256(momoConfig.getSecretKey(), rawSignature);

            Map<String, String> map = new HashMap<>();
            map.put("partnerCode", momoConfig.getPartnerCode());
            map.put("requestId", requestId);
            map.put("amount", amount);
            map.put("orderId", orderId);
            map.put("orderInfo", orderInfo);
            map.put("redirectUrl", redirectUrl);
            map.put("ipnUrl", ipnUrl);
            map.put("requestType", requestType);
            map.put("extraData", extraData);
            map.put("lang", "vi");
            map.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);

            log.info("Sending MoMo payment request for order: {}", orderId);
            ResponseEntity<String> response = restTemplate.postForEntity(momoConfig.getEndpoint(), request, String.class);
            
            if (response.getBody() != null) {
                 JsonNode root = objectMapper.readTree(response.getBody());
                 if (root.has("payUrl")) {
                     return root.get("payUrl").asText();
                 } else {
                     log.error("MoMo response does not contain payUrl: {}", response.getBody());
                 }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error creating Momo payment", e);
            return null;
        }
    }

    public boolean validateCallback(Map<String, String> params) {
        try {
            String accessKey = momoConfig.getAccessKey();
            String amount = params.get("amount");
            String extraData = params.get("extraData");
            String message = params.get("message");
            String orderId = params.get("orderId");
            String orderInfo = params.get("orderInfo");
            String orderType = params.get("orderType");
            String partnerCode = params.get("partnerCode");
            String payType = params.get("payType");
            String requestId = params.get("requestId");
            String responseTime = params.get("responseTime");
            String resultCode = params.get("resultCode");
            String transId = params.get("transId");
            String signature = params.get("signature");

            // Format: accessKey=$accessKey&amount=$amount&extraData=$extraData&message=$message&orderId=$orderId&orderInfo=$orderInfo&orderType=$orderType&partnerCode=$partnerCode&payType=$payType&requestId=$requestId&responseTime=$responseTime&resultCode=$resultCode&transId=$transId
            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + resultCode +
                    "&transId=" + transId;

            String calculatedHash = hmacSHA256(momoConfig.getSecretKey(), rawSignature);
            
            log.info("MoMo Callback Validation:");
            log.info("Received Signature: {}", signature);
            log.info("Calculated Signature: {}", calculatedHash);
            log.info("Raw String: {}", rawSignature);
            
            if (!calculatedHash.equals(signature)) {
                log.error("Signature mismatch! Expected: {}, Got: {}", calculatedHash, signature);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating MoMo callback", e);
            return false;
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKey);
            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error calculating HMAC SHA256", e);
            return "";
        }
    }
}
