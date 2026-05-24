package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import com.example.ecommerce.ecommerce_backend.application.service.auth.JwtService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JwtService jwtService;
    private final StringRedisTemplate redis;

    public WebSocketAuthChannelInterceptor(JwtService jwtService, StringRedisTemplate redis) {
        this.jwtService = jwtService;
        this.redis = redis;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("STOMP connection attempt without valid Bearer Authorization header");
                throw new IllegalArgumentException("Unauthorized: Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7).trim();
            if (token.isBlank() || !token.contains(".")) {
                log.warn("STOMP connection attempt with malformed token");
                throw new IllegalArgumentException("Unauthorized: Invalid token format");
            }

            try {
                Claims c = jwtService.parse(token);

                if ("refresh".equals(String.valueOf(c.get("typ")))) {
                    log.warn("STOMP connection attempt using refresh token");
                    throw new IllegalArgumentException("Unauthorized: Refresh token not allowed");
                }

                String jti = c.getId();
                if (jti == null || jti.isBlank()) {
                    log.warn("STOMP connection attempt token missing JTI");
                    throw new IllegalArgumentException("Unauthorized: Token missing ID");
                }

                String blk = redis.opsForValue().get("auth:blacklist:" + jti);
                if (blk != null) {
                    log.warn("STOMP connection attempt token is blacklisted JTI={}", jti);
                    throw new IllegalArgumentException("Unauthorized: Token is blacklisted");
                }

                String userId = c.getSubject();
                Object rolesObj = c.get("roles");
                List<String> role = rolesObj instanceof List<?> l
                        ? l.stream().map(String::valueOf).toList()
                        : List.of();

                var authorities = role.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                accessor.setUser(authentication);
                log.info("STOMP connection authenticated successfully for userId={}", userId);
            } catch (Exception ex) {
                log.error("STOMP connection authentication failed", ex);
                throw new IllegalArgumentException("Unauthorized: " + ex.getMessage(), ex);
            }
        }

        return message;
    }
}
