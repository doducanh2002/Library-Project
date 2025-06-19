package com.library.client;

import com.library.config.MinioConfig;
import com.library.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP Client for MinIO Service integration in microservices architecture
 */
@Component
@Slf4j
public class MinioServiceClient {
    
    private final RestTemplate restTemplate;
    private final MinioConfig minioConfig;
    
    @Value("${minio.service.url:http://localhost:8083}")
    private String minioServiceUrl;
    
    public MinioServiceClient(RestTemplate restTemplate, MinioConfig minioConfig) {
        this.restTemplate = restTemplate;
        this.minioConfig = minioConfig;
    }
    
    /**
     * Upload file to MinIO through MinIO Service
     */
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        log.info("Uploading file {} to folder {} via MinIO Service", file.getOriginalFilename(), folder);
        
        try {
            // Generate unique object key
            String objectKey = generateObjectKey(file.getOriginalFilename(), folder);
            
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return objectKey;
                }
            });
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Call MinIO Service upload endpoint
            String uploadUrl = minioServiceUrl + "/api/documents/upload";
            ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("File uploaded successfully with object key: {}", objectKey);
                return objectKey;
            } else {
                throw new FileStorageException("Failed to upload file to MinIO Service");
            }
            
        } catch (IOException e) {
            log.error("Error reading file content", e);
            throw new FileStorageException("Error reading file content", e);
        } catch (Exception e) {
            log.error("Error uploading file to MinIO Service", e);
            throw new FileStorageException("Failed to upload file", e);
        }
    }
    
    /**
     * Generate download URL through MinIO Service
     */
    public String generateDownloadUrl(String objectKey, int expiryMinutes) {
        log.info("Generating download URL for object: {} with expiry: {} minutes", objectKey, expiryMinutes);
        
        try {
            String downloadUrl = String.format("%s/api/documents/download-url?objectKey=%s&expiryMinutes=%d", 
                    minioServiceUrl, objectKey, expiryMinutes);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(downloadUrl, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("url");
            } else {
                throw new FileStorageException("Failed to generate download URL");
            }
            
        } catch (Exception e) {
            log.error("Error generating download URL for object: {}", objectKey, e);
            throw new FileStorageException("Failed to generate download URL", e);
        }
    }
    
    /**
     * Generate view URL through MinIO Service
     */
    public String generateViewUrl(String objectKey) {
        log.info("Generating view URL for object: {}", objectKey);
        
        try {
            String viewUrl = String.format("%s/api/documents/view-url?objectKey=%s", 
                    minioServiceUrl, objectKey);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(viewUrl, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("url");
            } else {
                throw new FileStorageException("Failed to generate view URL");
            }
            
        } catch (Exception e) {
            log.error("Error generating view URL for object: {}", objectKey, e);
            throw new FileStorageException("Failed to generate view URL", e);
        }
    }
    
    /**
     * Delete file through MinIO Service
     */
    public void deleteFile(String objectKey) throws Exception {
        log.info("Deleting file with object key: {}", objectKey);
        
        try {
            String deleteUrl = String.format("%s/api/documents/delete?objectKey=%s", 
                    minioServiceUrl, objectKey);
            
            restTemplate.delete(deleteUrl);
            log.info("File deleted successfully: {}", objectKey);
            
        } catch (Exception e) {
            log.error("Error deleting file: {}", objectKey, e);
            throw new FileStorageException("Failed to delete file", e);
        }
    }
    
    /**
     * Check if file exists through MinIO Service
     */
    public boolean fileExists(String objectKey) {
        log.debug("Checking if file exists: {}", objectKey);
        
        try {
            String existsUrl = String.format("%s/api/documents/exists?objectKey=%s", 
                    minioServiceUrl, objectKey);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(existsUrl, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (Boolean) response.getBody().get("exists");
            }
            return false;
            
        } catch (Exception e) {
            log.warn("Error checking file existence for {}: {}", objectKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get file size through MinIO Service
     */
    public long getFileSize(String objectKey) throws Exception {
        log.debug("Getting file size for: {}", objectKey);
        
        try {
            String sizeUrl = String.format("%s/api/documents/size?objectKey=%s", 
                    minioServiceUrl, objectKey);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(sizeUrl, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ((Number) response.getBody().get("size")).longValue();
            } else {
                throw new FileStorageException("Failed to get file size");
            }
            
        } catch (Exception e) {
            log.error("Error getting file size for {}: {}", objectKey, e.getMessage());
            throw new FileStorageException("Failed to get file size", e);
        }
    }
    
    /**
     * Get MinIO configuration
     */
    public MinioConfig getMinioConfig() {
        return minioConfig;
    }
    
    /**
     * Generate unique object key for file storage
     */
    private String generateObjectKey(String originalFilename, String folder) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        
        String fileName = timestamp + "_" + uuid + "." + extension;
        return folder + "/" + fileName;
    }
    
    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}