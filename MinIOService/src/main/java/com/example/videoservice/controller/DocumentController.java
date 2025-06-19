package com.example.videoservice.controller;

import com.example.videoservice.service.MinioService;
import io.minio.StatObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Document-specific controller for MinIO operations
 * Used by library-backend service
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private MinioService minioService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        logger.info("Received document upload request for file: {}", file.getOriginalFilename());

        try {
            String objectName = generateObjectName(file.getOriginalFilename());
            
            minioService.uploadObject(
                objectName, 
                file.getInputStream(), 
                file.getSize(), 
                file.getContentType()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("objectKey", objectName);
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());

            logger.info("Document upload completed successfully: {}", objectName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Document upload failed: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/download-url")
    public ResponseEntity<Map<String, Object>> getDownloadUrl(
            @RequestParam String objectKey,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        
        logger.info("Generating download URL for: {} with expiry: {} minutes", objectKey, expiryMinutes);

        try {
            // For now, return a direct access URL
            // In production, you would generate pre-signed URLs
            String downloadUrl = String.format("/api/documents/download/%s", objectKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", downloadUrl);
            response.put("expiryMinutes", expiryMinutes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to generate download URL for {}: {}", objectKey, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to generate download URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/view-url")
    public ResponseEntity<Map<String, Object>> getViewUrl(@RequestParam String objectKey) {
        logger.info("Generating view URL for: {}", objectKey);

        try {
            // For now, return a direct access URL
            String viewUrl = String.format("/api/documents/view/%s", objectKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", viewUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to generate view URL for {}: {}", objectKey, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to generate view URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/download/{objectKey}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String objectKey) {
        logger.info("Download request for document: {}", objectKey);

        try {
            InputStream inputStream = minioService.getObject(objectKey);
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            StatObjectResponse objectInfo = minioService.getObjectInfo(objectKey);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(objectInfo.contentType()))
                .header("Content-Disposition", "attachment; filename=\"" + objectKey + "\"")
                .body(content);

        } catch (Exception e) {
            logger.error("Failed to download document {}: {}", objectKey, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/view/{objectKey}")
    public ResponseEntity<byte[]> viewDocument(@PathVariable String objectKey) {
        logger.info("View request for document: {}", objectKey);

        try {
            InputStream inputStream = minioService.getObject(objectKey);
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            StatObjectResponse objectInfo = minioService.getObjectInfo(objectKey);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(objectInfo.contentType()))
                .header("Content-Disposition", "inline; filename=\"" + objectKey + "\"")
                .body(content);

        } catch (Exception e) {
            logger.error("Failed to view document {}: {}", objectKey, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteDocument(@RequestParam String objectKey) {
        logger.info("Delete request for document: {}", objectKey);

        try {
            minioService.deleteObject(objectKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document deleted successfully");
            response.put("objectKey", objectKey);

            logger.info("Document deleted successfully: {}", objectKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to delete document {}: {}", objectKey, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Delete failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> checkDocumentExists(@RequestParam String objectKey) {
        logger.info("Checking existence for document: {}", objectKey);

        try {
            StatObjectResponse objectInfo = minioService.getObjectInfo(objectKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", true);
            response.put("objectKey", objectKey);
            response.put("size", objectInfo.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.debug("Document does not exist: {}", objectKey);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", false);
            response.put("objectKey", objectKey);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/size")
    public ResponseEntity<Map<String, Object>> getDocumentSize(@RequestParam String objectKey) {
        logger.info("Getting size for document: {}", objectKey);

        try {
            StatObjectResponse objectInfo = minioService.getObjectInfo(objectKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("size", objectInfo.size());
            response.put("objectKey", objectKey);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get document size for {}: {}", objectKey, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get document size: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MinIO Document Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Generate unique object name for document storage
     */
    private String generateObjectName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        
        return "documents/" + timestamp + "_" + uuid + "." + extension;
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}