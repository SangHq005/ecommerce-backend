package com.example.ecommerce.ecommerce_backend.api.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Custom health indicator for database connectivity and performance
 */
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private static final int QUERY_TIMEOUT_SECONDS = 3;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

                // Simple query to check database connectivity
                ResultSet resultSet = statement.executeQuery("SELECT 1");

                if (resultSet.next()) {
                    long responseTime = System.currentTimeMillis() - startTime;

                    // Check database version and other metadata
                    String dbVersion = connection.getMetaData().getDatabaseProductVersion();
                    String dbProduct = connection.getMetaData().getDatabaseProductName();

                    Health.Builder builder = Health.up()
                            .withDetail("database", dbProduct)
                            .withDetail("version", dbVersion)
                            .withDetail("responseTime", responseTime + "ms");

                    // Add warning if response time is slow
                    if (responseTime > 1000) {
                        builder.withDetail("warning", "Slow database response time");
                    }

                    return builder.build();
                } else {
                    return Health.down()
                            .withDetail("error", "Query returned no results")
                            .build();
                }
            }
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}