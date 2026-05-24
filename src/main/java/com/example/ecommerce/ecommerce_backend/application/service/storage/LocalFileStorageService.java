package com.example.ecommerce.ecommerce_backend.application.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public String save(String folder, MultipartFile file) {
        try {
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) ext = original.substring(original.lastIndexOf('.'));
            String name = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ext;

            Path dir = Path.of(uploadDir, folder);
            Files.createDirectories(dir);

            Path target = dir.resolve(name);
            Files.write(target, file.getBytes());

            // URL served by /files/**
            return "/files/" + folder + "/" + name;
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload failed");
        }
    }
}
