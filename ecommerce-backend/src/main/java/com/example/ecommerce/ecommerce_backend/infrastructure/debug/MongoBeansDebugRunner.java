package com.example.ecommerce.ecommerce_backend.infrastructure.debug;

import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Profile("debug")
public class MongoBeansDebugRunner implements CommandLineRunner {

    private final ApplicationContext ctx;

    public MongoBeansDebugRunner(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run(String... args) {
        Map<String, MongoClient> clients = ctx.getBeansOfType(MongoClient.class);
        log.info("MongoClient beans = {}", clients.keySet());
        log.info("MongoClient bean count = {}", clients.size());
    }
}
