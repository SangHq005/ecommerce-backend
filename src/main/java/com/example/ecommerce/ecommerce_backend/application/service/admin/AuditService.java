package com.example.ecommerce.ecommerce_backend.application.service.admin;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final EventLogMongoRepository eventRepo;

    public AuditService(EventLogMongoRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Transactional(readOnly = true)
    public Page<EventLogDocument> getLogs(String type, Pageable pageable) {
        if (type != null && !type.isBlank()) {
            return eventRepo.findByType(type, pageable);
        }
        return eventRepo.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public void log(String type, Long actorId, String actorRole, String message) {
        eventRepo.save(new EventLogDocument(
            type,
            actorRole + "_" + actorId,
            java.time.Instant.now(),
            null,
            java.util.Map.of("actorId", actorId, "actorRole", actorRole, "message", message)
        ));
    }
}
