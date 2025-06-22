package com.library.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MinIOServiceClient {
    
    private final WebClient webClient;
    
    @Value("${minio-service.base-url:http://localhost:8080}")
    private String minioServiceBaseUrl;
    
    public MinIOServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }
    
    /**
     * Upload file to MinIO service
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        try {
            log.info("Uploading file to MinIO service: {}", file.getOriginalFilename());
            
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            }).header("Content-Type", file.getContentType());
            
            ResponseEntity<Map> response = webClient
                    .post()
                    .uri(minioServiceBaseUrl + "/api/videos/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .toEntity(Map.class)
                    .block();
            
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully uploaded file: {}", file.getOriginalFilename());
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to upload file to MinIO service");
            }
            
        } catch (WebClientResponseException e) {
            log.error("MinIO service error during upload: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("MinIO service upload failed: " + e.getMessage());
        } catch (IOException e) {
            log.error("IO error during file upload: {}", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during file upload: {}", e.getMessage(), e);
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
    
    /**
     * Get file stream from MinIO service
     */
    public InputStream getFileStream(String fileId) {
        try {
            log.info("Getting file stream from MinIO service: {}", fileId);
            
            ResponseEntity<InputStreamResource> response = webClient
                    .get()
                    .uri(minioServiceBaseUrl + "/api/videos/" + fileId)
                    .retrieve()
                    .toEntity(InputStreamResource.class)
                    .block();
            
            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getInputStream();
            } else {
                throw new RuntimeException("Failed to get file stream from MinIO service");
            }
            
        } catch (WebClientResponseException e) {
            log.error("MinIO service error during file retrieval: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("MinIO service file retrieval failed: " + e.getMessage());
        } catch (IOException e) {
            log.error("IO error during file retrieval: {}", e.getMessage());
            throw new RuntimeException("File retrieval failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during file retrieval: {}", e.getMessage(), e);
            throw new RuntimeException("File retrieval failed: " + e.getMessage());
        }
    }
    
    /**
     * Get file metadata from MinIO service
     */
    public Map<String, Object> getFileInfo(String fileId) {
        try {
            log.info("Getting file info from MinIO service: {}", fileId);
            
            ResponseEntity<Map> response = webClient
                    .get()
                    .uri(minioServiceBaseUrl + "/api/videos/" + fileId + "/info")
                    .retrieve()
                    .toEntity(Map.class)
                    .block();
            
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get file info from MinIO service");
            }
            
        } catch (WebClientResponseException e) {
            log.error("MinIO service error during file info retrieval: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("MinIO service file info retrieval failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during file info retrieval: {}", e.getMessage(), e);
            throw new RuntimeException("File info retrieval failed: " + e.getMessage());
        }
    }
    
    /**
     * Delete file from MinIO service
     */
    public boolean deleteFile(String fileId) {
        try {
            log.info("Deleting file from MinIO service: {}", fileId);
            
            ResponseEntity<Void> response = webClient
                    .delete()
                    .uri(minioServiceBaseUrl + "/api/videos/" + fileId)
                    .retrieve()
                    .toEntity(Void.class)
                    .block();
            
            boolean success = response != null && response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Successfully deleted file: {}", fileId);
            }
            return success;
            
        } catch (WebClientResponseException e) {
            log.error("MinIO service error during file deletion: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during file deletion: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if MinIO service is healthy
     */
    public boolean isServiceHealthy() {
        try {
            ResponseEntity<String> response = webClient
                    .get()
                    .uri(minioServiceBaseUrl + "/api/videos/health")
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            
            return response != null && response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("MinIO service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate presigned URL for file download (if MinIO service supports it)
     */
    public String getDownloadUrl(String fileId) {
        // For now, return direct stream URL
        // You can extend MinIO service to provide presigned URLs
        return minioServiceBaseUrl + "/api/videos/" + fileId;
    }
    
    /**
     * Generate presigned URL for file viewing (if MinIO service supports it)
     */
    public String getViewUrl(String fileId) {
        // For now, return direct stream URL
        // You can extend MinIO service to provide presigned URLs for viewing
        return minioServiceBaseUrl + "/api/videos/" + fileId + "/stream";
    }
}