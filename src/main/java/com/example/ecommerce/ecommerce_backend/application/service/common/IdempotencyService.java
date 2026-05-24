package com.example.ecommerce.ecommerce_backend.application.service.common;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.IdempotencyKeyEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.IdempotencyKeyJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class IdempotencyService {

    public record Replay(int statusCode, String responseBodyJson) {}

    private final IdempotencyKeyJpaRepository repo;

    public IdempotencyService(IdempotencyKeyJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Replay begin(String scope, String idemKey, String requestHash, Duration ttl) {
        var now = Instant.now();
        var exp = now.plusSeconds(ttl.toSeconds());

        IdempotencyKeyEntity e = repo.findForUpdate(scope, idemKey).orElse(null);

        if (e == null) {
            e = new IdempotencyKeyEntity();
            e.setScope(scope);
            e.setIdemKey(idemKey);
            e.setRequestHash(requestHash);
            e.setStatus("IN_PROGRESS");
            e.setExpiresAt(exp);
            repo.save(e);
            return null;
        }

        if (e.getExpiresAt().isBefore(now)) {
            // expired -> restart
            e.setRequestHash(requestHash);
            e.setStatus("IN_PROGRESS");
            e.setResponseCode(null);
            e.setResponseBody(null);
            e.setExpiresAt(exp);
            repo.save(e);
            return null;
        }

        if (!e.getRequestHash().equals(requestHash)) {
            throw ApiException.conflict("Idempotency key conflict (different payload)");
        }

        if ("COMPLETED".equals(e.getStatus()) && e.getResponseCode() != null) {
            return new Replay(e.getResponseCode(), e.getResponseBody());
        }

        throw ApiException.conflict("Request is still processing");
    }

    @Transactional
    public void complete(String scope, String idemKey, int statusCode, String responseBodyJson) {
        IdempotencyKeyEntity e = repo.findForUpdate(scope, idemKey)
                .orElseThrow(() -> ApiException.notFound("Idempotency key not found"));
        e.setStatus("COMPLETED");
        e.setResponseCode(statusCode);
        e.setResponseBody(responseBodyJson);
        repo.save(e);
    }

    @Transactional
    public void fail(String scope, String idemKey) {
        repo.findForUpdate(scope, idemKey).ifPresent(e -> {
            e.setStatus("FAILED");
            repo.save(e);
        });
    }
}
