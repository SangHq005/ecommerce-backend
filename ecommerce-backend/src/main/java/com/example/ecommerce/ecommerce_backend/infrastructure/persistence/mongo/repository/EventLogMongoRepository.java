package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository;


import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventLogMongoRepository extends MongoRepository<EventLogDocument, String> {}
