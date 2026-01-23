package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository")
@EnableMongoRepositories(basePackages = "com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository")
public class PersistenceConfig {
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
