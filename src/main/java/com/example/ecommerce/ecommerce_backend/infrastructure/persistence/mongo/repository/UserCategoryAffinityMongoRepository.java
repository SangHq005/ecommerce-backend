package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.UserCategoryAffinityDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserCategoryAffinityMongoRepository extends MongoRepository<UserCategoryAffinityDoc, String> {

    Optional<UserCategoryAffinityDoc> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<UserCategoryAffinityDoc> findByUserIdOrderByScoreDesc(Long userId);
}
