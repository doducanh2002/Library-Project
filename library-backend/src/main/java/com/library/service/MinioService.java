package com.library.service;

import com.library.config.MinioConfig;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for MinIO operations through MinIO Service (microservices)
 */
public interface MinioService {
    
    /**
     * Upload a file to MinIO storage
     * @param file The file to upload
     * @param folder The folder path in the bucket
     * @return The object key/path of the uploaded file
     * @throws Exception if upload fails
     */
    String uploadFile(MultipartFile file, String folder) throws Exception;
    
    /**
     * Generate a pre-signed download URL for a file
     * @param objectKey The object key/path
     * @param expiryMinutes URL expiry time in minutes
     * @return Pre-signed download URL
     */
    String generateDownloadUrl(String objectKey, int expiryMinutes);
    
    /**
     * Generate a pre-signed view URL for a file
     * @param objectKey The object key/path
     * @return Pre-signed view URL
     */
    String generateViewUrl(String objectKey);
    
    /**
     * Delete a file from MinIO storage
     * @param objectKey The object key/path to delete
     * @throws Exception if deletion fails
     */
    void deleteFile(String objectKey) throws Exception;
    
    /**
     * Check if a file exists in storage
     * @param objectKey The object key/path to check
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String objectKey);
    
    /**
     * Get file size in bytes
     * @param objectKey The object key/path
     * @return File size in bytes
     * @throws Exception if operation fails
     */
    long getFileSize(String objectKey) throws Exception;
    
    /**
     * Get MinIO configuration
     * @return MinioConfig instance
     */
    MinioConfig getMinioConfig();
}