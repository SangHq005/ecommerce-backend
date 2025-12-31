package com.example.ecommerce.ecommerce_backend.application.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {

    /**
     * Upload file to cloud storage
     *
     * @param file File to upload
     * @param folder Target folder/prefix
     * @return URL of uploaded file
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Delete file from cloud storage
     *
     * @param fileUrl URL of file to delete
     */
    void deleteFile(String fileUrl);

    /**
     * Generate a presigned URL for temporary access
     *
     * @param fileKey File key/path
     * @param expirationMinutes Expiration time in minutes
     * @return Presigned URL
     */
    String generatePresignedUrl(String fileKey, int expirationMinutes);
}
