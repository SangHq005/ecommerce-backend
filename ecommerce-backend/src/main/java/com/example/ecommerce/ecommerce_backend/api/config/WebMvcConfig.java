package com.example.ecommerce.ecommerce_backend.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC Configuration for serving static resources
 * Configures resource handlers for uploaded files and static assets
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * Configure resource handlers to serve static files
     * Maps URL patterns to filesystem locations
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve upload directory to absolute path
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString() + "/";

        log.info("Configuring static resource handler for uploads at: {}", uploadLocation);

        // Serve uploaded files (images, documents, etc.)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(3600); // Cache for 1 hour

        // Add more resource handlers if needed
        // Example: registry.addResourceHandler("/static/**")
        //                  .addResourceLocations("classpath:/static/");
    }
}
