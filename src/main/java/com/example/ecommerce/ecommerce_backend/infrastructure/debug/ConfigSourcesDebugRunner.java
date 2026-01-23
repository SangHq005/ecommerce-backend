package com.example.ecommerce.ecommerce_backend.infrastructure.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Profile("debug")
public class ConfigSourcesDebugRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConfigSourcesDebugRunner.class);

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
