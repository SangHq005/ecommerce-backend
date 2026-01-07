package com.example.ecommerce.ecommerce_backend.api.filter;

import com.example.ecommerce.ecommerce_backend.application.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final StringRedisTemplate redis;

    public JwtAuthFilter(JwtService jwt, StringRedisTemplate redis) {
        this.jwt = jwt;
        this.redis = redis;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authz = request.getHeader("Authorization");

        if (authz == null || !authz.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authz.substring(7).trim();

        // chặn token rỗng / token không phải JWT compact
        if (token.isBlank() || !token.contains(".")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            Claims c = jwt.parse(token);

            // reject refresh token used as access
            if ("refresh".equals(String.valueOf(c.get("typ")))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String jti = c.getId();
            if (jti == null || jti.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String blk = redis.opsForValue().get("auth:blacklist:" + jti);
            if (blk != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
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
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
