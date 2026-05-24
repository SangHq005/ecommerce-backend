package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentCallbackResult;
import com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayStrategy;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.MomoConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete Strategy: MoMo payment gateway.
 * <p>
 * Registered automatically by Spring and discovered by
 * {@link com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentGatewayFactory}.
 * </p>
 *
 * <h3>MoMo orderId uniqueness</h3>
 * MoMo rejects duplicate {@code orderId} values, so we append the current
 * timestamp: {@code orderCode_<timestamp>}.  The original {@code orderCode} is
 * recovered during callback processing via {@link #extractOrderCode(String)}.
 */
@Service
public class MomoService implements PaymentGatewayStrategy {

    private static final Logger log = LoggerFactory.getLogger(MomoService.class);

    private final MomoConfig    momoConfig;
    private final RestTemplate  restTemplate;
    private final ObjectMapper  objectMapper;

    public MomoService(MomoConfig momoConfig) {
        this.momoConfig   = momoConfig;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = momoConfig.getTimeoutSeconds() != null && momoConfig.getTimeoutSeconds() > 0
                ? momoConfig.getTimeoutSeconds() * 1000
                : 15000;
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // PaymentGatewayStrategy — identity
    // -------------------------------------------------------------------------

    @Override
    public String getPaymentMethod() {
        return "MOMO";
    }

    // -------------------------------------------------------------------------
    // PaymentGatewayStrategy — URL creation
    // -------------------------------------------------------------------------

    /**
     * Calls the MoMo payment initiation API and returns the {@code payUrl}
     * that the user must be redirected to.
     *
     * @param order     the order to pay for
     * @param clientIp  not used by MoMo but required by the interface contract
     * @return MoMo payUrl, or throws {@link com.example.ecommerce.ecommerce_backend.api.exception.ApiException} on failure
     */
    @Override
    public String createPaymentUrl(OrderEntity order, String clientIp) {
        try {
            // MoMo requires a unique orderId per request; append timestamp to avoid duplicates on retries
            String uniqueId   = order.getOrderCode() + "_" + System.currentTimeMillis();
            String requestId  = uniqueId;
            String orderId    = uniqueId;
            String amount     = String.valueOf(order.getTotalAmount().longValue());
            String orderInfo  = "Payment for order " + order.getOrderCode();
            String redirectUrl = momoConfig.getReturnUrl();
            String ipnUrl     = momoConfig.getIpnUrl();
            String requestType = "captureWallet";

            // Store original orderCode in extraData for callback recovery
            String extraData = Base64.getEncoder().encodeToString(
                    ("orderCode=" + order.getOrderCode()).getBytes(StandardCharsets.UTF_8)
            );

            // MoMo signature format (alphabetical field order)
            String rawSignature = "accessKey="   + momoConfig.getAccessKey()
                    + "&amount="      + amount
                    + "&extraData="   + extraData
                    + "&ipnUrl="      + ipnUrl
                    + "&orderId="     + orderId
                    + "&orderInfo="   + orderInfo
                    + "&partnerCode=" + momoConfig.getPartnerCode()
                    + "&redirectUrl=" + redirectUrl
                    + "&requestId="   + requestId
                    + "&requestType=" + requestType;

            String signature = hmacSHA256(momoConfig.getSecretKey(), rawSignature);

            Map<String, String> body = new HashMap<>();
            body.put("partnerCode", momoConfig.getPartnerCode());
            body.put("requestId",   requestId);
            body.put("amount",      amount);
            body.put("orderId",     orderId);
            body.put("orderInfo",   orderInfo);
            body.put("redirectUrl", redirectUrl);
            body.put("ipnUrl",      ipnUrl);
            body.put("requestType", requestType);
            body.put("extraData",   extraData);
            body.put("lang",        "vi");
            body.put("signature",   signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> httpRequest = new HttpEntity<>(body, headers);

            log.info("Sending MoMo payment request for order: {}", orderId);
            ResponseEntity<String> response = restTemplate.postForEntity(momoConfig.getEndpoint(), httpRequest, String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("payUrl")) {
                    return root.get("payUrl").asText();
                }
                log.error("MoMo response does not contain payUrl: {}", response.getBody());
            }

            throw new IllegalStateException("MoMo did not return a payUrl for order " + order.getOrderCode());

        } catch (Exception e) {
            log.error("Error creating MoMo payment URL for order: {}", order.getOrderCode(), e);
            throw new RuntimeException("Failed to create MoMo payment URL: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // PaymentGatewayStrategy — callback handling
    // -------------------------------------------------------------------------

    /**
     * Validate the HMAC-SHA256 signature attached to a MoMo IPN callback.
     */
    @Override
    public boolean validateCallback(Map<String, String> params) {
        try {
            String accessKey     = momoConfig.getAccessKey();
            String amount        = params.get("amount");
            String extraData     = params.get("extraData");
            String message       = params.get("message");
            String orderId       = params.get("orderId");
            String orderInfo     = params.get("orderInfo");
            String orderType     = params.get("orderType");
            String partnerCode   = params.get("partnerCode");
            String payType       = params.get("payType");
            String requestId     = params.get("requestId");
            String responseTime  = params.get("responseTime");
            String resultCode    = params.get("resultCode");
            String transId       = params.get("transId");
            String signature     = params.get("signature");

            // MoMo callback signature format (alphabetical field order)
            String rawSignature = "accessKey="   + accessKey
                    + "&amount="       + amount
                    + "&extraData="    + extraData
                    + "&message="      + message
                    + "&orderId="      + orderId
                    + "&orderInfo="    + orderInfo
                    + "&orderType="    + orderType
                    + "&partnerCode="  + partnerCode
                    + "&payType="      + payType
                    + "&requestId="    + requestId
                    + "&responseTime=" + responseTime
                    + "&resultCode="   + resultCode
                    + "&transId="      + transId;

            String calculatedHash = hmacSHA256(momoConfig.getSecretKey(), rawSignature);

            log.debug("MoMo callback — received: {}, calculated: {}", signature, calculatedHash);

            if (!calculatedHash.equals(signature)) {
                log.error("MoMo signature mismatch! Expected: {}, Got: {}", calculatedHash, signature);
                return false;
            }
            return true;

        } catch (Exception e) {
            log.error("Error validating MoMo callback", e);
            return false;
        }
    }

    /**
     * Parse a pre-validated MoMo callback into a gateway-agnostic {@link PaymentCallbackResult}.
     * <p>Result code {@code "0"} indicates success; any other code is a failure.</p>
     */
    @Override
    public PaymentCallbackResult processCallback(Map<String, String> params) {
        String momoOrderId = params.get("orderId");
        String transId     = params.get("transId");
        String resultCode  = params.get("resultCode");
        String message     = params.get("message");

        // Recover the original platform orderCode from the MoMo orderId
        String orderCode = extractOrderCode(momoOrderId);

        Map<String, Object> raw = new HashMap<>(params);

        if ("0".equals(resultCode)) {
            log.info("MoMo callback SUCCESS — orderCode={}, transId={}", orderCode, transId);
            return PaymentCallbackResult.success(transId, orderCode, raw);
        }

        log.warn("MoMo callback FAILURE — orderCode={}, resultCode={}, message={}", orderCode, resultCode, message);
        return PaymentCallbackResult.failure(orderCode, raw);
    }

    // -------------------------------------------------------------------------
    // Public helpers (reused by PaymentService for redirect verification)
    // -------------------------------------------------------------------------

    /**
     * Extracts the original platform {@code orderCode} from a MoMo {@code orderId}.
     * <p>
     * MoMo orderId format: {@code <orderCode>_<timestamp>} where timestamp is ≥10 digits.
     * If the suffix is not a numeric timestamp, the full string is returned as-is.
     * </p>
     *
     * @param momoOrderId the orderId field received from MoMo
     * @return original platform orderCode
     */
    public String extractOrderCode(String momoOrderId) {
        if (momoOrderId == null || momoOrderId.isEmpty()) return momoOrderId;
        int lastUnderscore = momoOrderId.lastIndexOf('_');
        if (lastUnderscore == -1) return momoOrderId;
        String suffix = momoOrderId.substring(lastUnderscore + 1);
        if (suffix.matches("\\d{10,}")) {
            return momoOrderId.substring(0, lastUnderscore);
        }
        return momoOrderId;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

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
