package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.config.GroqConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with Groq API (OpenAI-compatible endpoint)
 */
@Service
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);

    private final GroqConfig groqConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GroqClient(GroqConfig groqConfig) {
        this.groqConfig = groqConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate text response using Groq API
     * 
     * @param systemPrompt System prompt for the AI
     * @param userMessage User message/query
     * @param conversationHistory Optional conversation history
     * @return Generated text response
     */
    public String generateText(String systemPrompt, String userMessage, List<Map<String, String>> conversationHistory) {
        if (groqConfig.getApiKey() == null || groqConfig.getApiKey().isEmpty()) {
            log.warn("Groq API key is not configured. Returning default response.");
            return "Xin lỗi, dịch vụ AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.";
        }

        try {
            String url = groqConfig.getBaseUrl() + "/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqConfig.getApiKey());

            // Build messages list
            List<Map<String, String>> messages = new ArrayList<>();
            
            // Add system message if provided
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            // Add conversation history
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                messages.addAll(conversationHistory);
            }

            // Add current user message
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.debug("Calling Groq API: {}", url);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode choices = jsonResponse.get("choices");
                
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);
                    JsonNode message = firstChoice.get("message");
                    if (message != null && message.has("content")) {
                        String content = message.get("content").asText();
                        log.debug("Groq API response received: {} characters", content.length());
                        return content;
                    }
                }
                
                log.warn("Unexpected Groq API response format: {}", response.getBody());
                return "Xin lỗi, không thể xử lý phản hồi từ AI.";
            } else {
                log.error("Groq API error: Status {}, Body: {}", response.getStatusCode(), response.getBody());
                return "Xin lỗi, có lỗi xảy ra khi gọi dịch vụ AI.";
            }

        } catch (Exception e) {
            log.error("Error calling Groq API", e);
            return "Xin lỗi, không thể kết nối đến dịch vụ AI. Vui lòng thử lại sau.";
        }
    }

    /**
     * Generate text with product context
     */
    public String generateTextWithProductContext(String query, String productContext) {
        String systemPrompt = "Bạn là trợ lý AI thông minh của ShopMart - một nền tảng thương mại điện tử.\n\n" +
                "BẠN CÓ THỂ:\n" +
                "1. Trả lời câu hỏi về sản phẩm dựa trên thông tin được cung cấp\n" +
                "2. So sánh sản phẩm\n" +
                "3. Đưa ra lời khuyên mua hàng\n" +
                "4. Trả lời các câu hỏi chung về mua sắm\n\n" +
                "NGÔN NGỮ: Luôn trả lời bằng tiếng Việt (trừ khi người dùng yêu cầu ngôn ngữ khác).\n" +
                "PHONG CÁCH: Thân thiện, chuyên nghiệp, và hữu ích.\n" +
                "ĐỊNH DẠNG: Sử dụng Markdown để định dạng câu trả lời (tiêu đề, danh sách, in đậm, v.v.).";

        String fullQuery = productContext != null && !productContext.isEmpty()
                ? "Thông tin sản phẩm:\n" + productContext + "\n\nCâu hỏi của người dùng: " + query
                : query;

        return generateText(systemPrompt, fullQuery, null);
    }
}
