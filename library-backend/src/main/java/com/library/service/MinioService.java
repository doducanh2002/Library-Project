package com.library.service;

import com.library.config.MinioConfig;
import com.library.exception.FileStorageException;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class MinioService {
    
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
        "txt", "epub", "mobi", "jpg", "jpeg", "png", "gif"
    );
    
    public MinioService(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
        initializeBucket();
    }
    
    private void initializeBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .region(minioConfig.getRegion())
                        .build()
                );
                log.info("Created MinIO bucket: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket", e);
            throw new FileStorageException("Could not initialize storage bucket");
        }
    }
    
    /**
     * Upload a file to MinIO
     * @param file The file to upload
     * @param folder The folder path within the bucket
     * @return The object key of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);
        
        String fileName = generateFileName(file.getOriginalFilename());
        String objectKey = folder + "/" + fileName;
        
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            log.info("Successfully uploaded file: {} to MinIO", objectKey);
            return objectKey;
            
        } catch (Exception e) {
            log.error("Error uploading file to MinIO", e);
            throw new FileStorageException("Could not upload file: " + e.getMessage());
        }
    }
    
    /**
     * Download a file from MinIO
     * @param objectKey The object key of the file
     * @return InputStream of the file
     */
    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error downloading file from MinIO: {}", objectKey, e);
            throw new FileStorageException("Could not download file: " + e.getMessage());
        }
    }
    
    /**
     * Generate a pre-signed URL for downloading
     * @param objectKey The object key of the file
     * @param expiryMinutes URL expiry time in minutes
     * @return Pre-signed URL
     */
    public String generateDownloadUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating download URL for: {}", objectKey, e);
            throw new FileStorageException("Could not generate download URL: " + e.getMessage());
        }
    }
    
    /**
     * Generate a pre-signed URL for viewing (browser-compatible)
     * @param objectKey The object key of the file
     * @return Pre-signed URL for viewing
     */
    public String generateViewUrl(String objectKey) {
        try {
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-type", "application/pdf");
            reqParams.put("response-content-disposition", "inline");
            
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .expiry(2, TimeUnit.HOURS)
                    .extraQueryParams(reqParams)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating view URL for: {}", objectKey, e);
            throw new FileStorageException("Could not generate view URL: " + e.getMessage());
        }
    }
    
    /**
     * Delete a file from MinIO
     * @param objectKey The object key of the file to delete
     */
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .build()
            );
            log.info("Successfully deleted file: {} from MinIO", objectKey);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", objectKey, e);
            throw new FileStorageException("Could not delete file: " + e.getMessage());
        }
    }
    
    /**
     * Check if a file exists in MinIO
     * @param objectKey The object key to check
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String objectKey) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get file metadata
     * @param objectKey The object key
     * @return Map of metadata
     */
    public Map<String, String> getFileMetadata(String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .build()
            );
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("size", String.valueOf(stat.size()));
            metadata.put("contentType", stat.contentType());
            metadata.put("lastModified", stat.lastModified().toString());
            metadata.put("etag", stat.etag());
            
            return metadata;
        } catch (Exception e) {
            log.error("Error getting file metadata: {}", objectKey, e);
            throw new FileStorageException("Could not get file metadata: " + e.getMessage());
        }
    }
    
    /**
     * List files in a folder
     * @param prefix The folder prefix
     * @return List of object keys
     */
    public List<String> listFiles(String prefix) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .prefix(prefix)
                    .recursive(true)
                    .build()
            );
            
            return StreamSupport.stream(results.spliterator(), false)
                .map(result -> {
                    try {
                        return result.get().objectName();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error listing files with prefix: {}", prefix, e);
            throw new FileStorageException("Could not list files: " + e.getMessage());
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds maximum limit of 100MB");
        }
        
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_FILE_TYPES.contains(fileExtension.toLowerCase())) {
            throw new FileStorageException("File type not allowed: " + fileExtension);
        }
    }
    
    private String generateFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        return timestamp + "_" + uuid + "." + extension;
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}