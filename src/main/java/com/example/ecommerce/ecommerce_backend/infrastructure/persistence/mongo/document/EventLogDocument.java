package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Setter
@Getter
@Document(collection = "event_log")
public class EventLogDocument {
    @Id
    private String id;

    private String type;
    private String aggregateId;
    private Instant createdAt;
    private String correlationId;
    private Map<String, Object> payload;

    public EventLogDocument() {}

    public EventLogDocument(String type, String aggregateId, Instant createdAt, String correlationId, Map<String, Object> payload) {
        this.type = type;
        this.aggregateId = aggregateId;
        this.createdAt = createdAt;
        this.correlationId = correlationId;
        this.payload = payload;
    }

}
