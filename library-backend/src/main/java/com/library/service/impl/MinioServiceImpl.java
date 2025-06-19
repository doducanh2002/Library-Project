package com.library.service.impl;

import com.library.client.MinioServiceClient;
import com.library.config.MinioConfig;
import com.library.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO Service implementation using HTTP client to MinIO Service (microservices)
 */
@Service
@Slf4j
public class MinioServiceImpl implements MinioService {
    
    private final MinioServiceClient minioServiceClient;
    
    public MinioServiceImpl(MinioServiceClient minioServiceClient) {
        this.minioServiceClient = minioServiceClient;
    }
    
    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        log.info("Delegating file upload to MinIO Service: {} in folder {}", 
                file.getOriginalFilename(), folder);
        return minioServiceClient.uploadFile(file, folder);
    }
    
    @Override
    public String generateDownloadUrl(String objectKey, int expiryMinutes) {
        log.debug("Delegating download URL generation to MinIO Service for: {}", objectKey);
        return minioServiceClient.generateDownloadUrl(objectKey, expiryMinutes);
    }
    
    @Override
    public String generateViewUrl(String objectKey) {
        log.debug("Delegating view URL generation to MinIO Service for: {}", objectKey);
        return minioServiceClient.generateViewUrl(objectKey);
    }
    
    @Override
    public void deleteFile(String objectKey) throws Exception {
        log.info("Delegating file deletion to MinIO Service: {}", objectKey);
        minioServiceClient.deleteFile(objectKey);
    }
    
    @Override
    public boolean fileExists(String objectKey) {
        log.debug("Delegating file existence check to MinIO Service: {}", objectKey);
        return minioServiceClient.fileExists(objectKey);
    }
    
    @Override
    public long getFileSize(String objectKey) throws Exception {
        log.debug("Delegating file size retrieval to MinIO Service: {}", objectKey);
        return minioServiceClient.getFileSize(objectKey);
    }
    
    @Override
    public MinioConfig getMinioConfig() {
        log.debug("Getting MinIO configuration from client");
        return minioServiceClient.getMinioConfig();
    }
}