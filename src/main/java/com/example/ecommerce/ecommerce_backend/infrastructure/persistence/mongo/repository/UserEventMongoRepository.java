package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.UserEventDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import java.time.Instant;
import java.util.List;

public interface UserEventMongoRepository extends MongoRepository<UserEventDoc, String> {

    // Example: Find top viewed products in last X days
    // This is better done with Aggregation but simple counts work for small scale

    @Aggregation(pipeline = {
        "{ '$match': { 'eventType': ?0, 'timestamp': { '$gte': ?1 } } }",
        "{ '$group': { '_id': '$productId', 'count': { '$sum': 1 } } }",
        "{ '$sort': { 'count': -1 } }",
        "{ '$limit': 10 }"
    })
    List<ProductIdCount> findTopProductsByEventType(String eventType, Instant since);

    List<UserEventDoc> findByUserIdAndEventTypeAndTimestampAfter(Long userId, String eventType, Instant timestamp);

    public static class ProductIdCount {
        private Long _id;
        private Long count;

        public Long get_id() { return _id; }
        public void set_id(Long _id) { this._id = _id; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}
