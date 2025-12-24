package com.example.ecommerce.ecommerce_backend.infrastructure.debug;

import com.mongodb.client.MongoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@Profile("debug")
@RequiredArgsConstructor
public class MongoAuthDebugRunner implements CommandLineRunner {

    private final MongoClient mongoClient;

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
