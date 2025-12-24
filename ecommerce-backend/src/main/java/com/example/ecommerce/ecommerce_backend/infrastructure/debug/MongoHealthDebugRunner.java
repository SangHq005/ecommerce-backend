package com.example.ecommerce.ecommerce_backend.infrastructure.debug;

import com.mongodb.client.MongoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("debug")
@RequiredArgsConstructor
public class MongoHealthDebugRunner implements CommandLineRunner {

    private final Environment env;
    private final MongoClient mongoClient;

    @Override
    public void run(String... args) {
        log.info("MongoDB Configuration - URI: {}", env.getProperty("spring.mongodb.uri"));
        log.info("MongoDB Configuration - Username: {}", env.getProperty("spring.mongodb.username"));
        log.info("MongoDB Configuration - Database: {}", env.getProperty("spring.mongodb.database"));

        try {
            mongoClient.getDatabase("admin").runCommand(new org.bson.Document("ping", 1));
            log.info("MongoDB ping OK - Connection successful");
        } catch (Exception ex) {
            log.error("MongoDB ping FAILED: {}", ex.getMessage(), ex);
        }
    }
}
