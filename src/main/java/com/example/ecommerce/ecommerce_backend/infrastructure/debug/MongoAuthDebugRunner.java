package com.example.ecommerce.ecommerce_backend.infrastructure.debug;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Profile("debug")
public class MongoAuthDebugRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoAuthDebugRunner.class);

    private final MongoClient mongoClient;

    public MongoAuthDebugRunner(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void run(String... args) {
        try {
            // 1) Does the server consider this connection authenticated?
            Document status = mongoClient.getDatabase("admin")
                    .runCommand(new Document("connectionStatus", 1));
            log.info("MongoDB connectionStatus = {}", status.toJson());
        } catch (Exception ex) {
            log.error("MongoDB connectionStatus FAILED: {}", ex.getMessage());
        }

        try {
            // 2) Try an insert that should require auth
            mongoClient.getDatabase("ecommerce")
                    .getCollection("event_log")
                    .insertOne(new Document("type", "BOOT_TEST")
                            .append("createdAt", Instant.now().toString()));
            log.info("MongoDB direct insert OK");
        } catch (Exception ex) {
            log.error("MongoDB direct insert FAILED: {}", ex.getMessage());
        }
    }
}
