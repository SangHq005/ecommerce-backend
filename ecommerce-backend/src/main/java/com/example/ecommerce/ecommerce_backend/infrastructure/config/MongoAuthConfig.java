package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoAuthConfig {

    private String mongoUri(Environment env) {
        String uri =
                env.getProperty("spring.mongodb.uri",
                        env.getProperty("spring.data.mongodb.uri",
                                env.getProperty("SPRING_MONGODB_URI",
                                        env.getProperty("SPRING_DATA_MONGODB_URI"))));

        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException(
                    "MongoDB URI missing. Provide spring.mongodb.uri or spring.data.mongodb.uri"
            );
        }
        return uri;
    }

    @Bean
    @Primary
    public MongoClient mongoClient(Environment env) {
        return MongoClients.create(mongoUri(env));
    }

    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabaseFactory(Environment env) {
        return new SimpleMongoClientDatabaseFactory(new ConnectionString(mongoUri(env)));
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }
}
