package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository;


import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventLogMongoRepository extends MongoRepository<EventLogDocument, String> {
    Page<EventLogDocument> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<EventLogDocument> findByType(String type, Pageable pageable);
}
