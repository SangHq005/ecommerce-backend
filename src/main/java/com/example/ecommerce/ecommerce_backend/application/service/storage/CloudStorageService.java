package com.example.ecommerce.ecommerce_backend.application.service.storage;

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
     * Upload a local file from disk to cloud storage (used for migration).
     *
     * @param localFile Path to the file on disk
     * @param folder Target folder/prefix
     * @return URL of uploaded file
     */
    String uploadLocalFile(java.nio.file.Path localFile, String folder);

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
