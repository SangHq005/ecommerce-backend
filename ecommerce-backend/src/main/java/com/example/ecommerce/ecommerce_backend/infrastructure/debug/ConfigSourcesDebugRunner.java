package com.example.ecommerce.ecommerce_backend.infrastructure.debug;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("debug")
public class ConfigSourcesDebugRunner implements CommandLineRunner {

    private final ConfigurableEnvironment env;

    public ConfigSourcesDebugRunner(ConfigurableEnvironment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) {
        log.info("Spring Configuration - config.name: {}", env.getProperty("spring.config.name"));
        log.info("Spring Configuration - config.location: {}", env.getProperty("spring.config.location"));
        log.info("Spring Configuration - profiles.active: {}", env.getProperty("spring.profiles.active"));

        log.info("PropertySources (top to bottom):");
        for (PropertySource<?> ps : env.getPropertySources()) {
            log.info(" - {}", ps.getName());
        }
    }
}
