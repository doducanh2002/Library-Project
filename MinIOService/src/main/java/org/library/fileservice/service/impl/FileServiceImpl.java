package org.library.fileservice.service.impl;

import org.library.fileservice.exception.FileProcessingException;
import org.library.fileservice.model.FileMetadata;
import org.library.fileservice.service.FileService;
import org.library.fileservice.utill.FileUtils;
import io.minio.StatObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private MinioServiceImpl minioServiceImpl;

    @Value("${file.max-size:104857600}") // 100MB default
    private long maxFileSize;

    @Value("${file.upload-path:/files/}")
    private String uploadPath;

    private final ConcurrentMap<String, FileMetadata> fileMetadataStore = new ConcurrentHashMap<>();

    @Override
    public FileMetadata uploadFile(MultipartFile file) {
        logger.info("Starting file upload process for file: {}", file.getOriginalFilename());

        validateFile(file);

        try {
            String originalName = file.getOriginalFilename();
            String fileId = FileUtils.generateUniqueFileName(originalName);
            String storedName = fileId;
            String objectKey = uploadPath + storedName;
            String contentType = file.getContentType();
            String format = FileUtils.getFileExtension(originalName);

            // Upload to MinIO
            minioServiceImpl.uploadObject(objectKey, file.getInputStream(), file.getSize(), contentType);

            // Create metadata
            FileMetadata metadata = new FileMetadata(
                    fileId,
                    originalName,
                    storedName,
                    contentType,
                    file.getSize(),
                    format,
                    minioServiceImpl.getBucketName(),
                    objectKey
            );

            // Store metadata (in production, save to database)
            fileMetadataStore.put(fileId, metadata);

            logger.info("Successfully uploaded file with ID: {}", fileId);
            return metadata;

        } catch (Exception e) {
            logger.error("Failed to upload file: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public FileMetadata getFileMetadata(String fileId) {
        FileMetadata metadata = fileMetadataStore.get(fileId);
        if (metadata == null) {
            throw new FileProcessingException("File not found with ID: " + fileId);
        }
        return metadata;
    }

    @Override
    public InputStream getFileStream(String fileId) {
        FileMetadata metadata = getFileMetadata(fileId);
        try {
            return minioServiceImpl.getObject(metadata.getObjectKey());
        } catch (Exception e) {
            logger.error("Failed to retrieve file stream for ID {}: {}", fileId, e.getMessage(), e);
            throw new FileProcessingException("Failed to retrieve file stream", e);
        }
    }

    @Override
    public List<FileMetadata> getAllFiles() {
        return new ArrayList<>(fileMetadataStore.values());
    }

    @Override
    public StatObjectResponse getFileInfo(String fileId) {
        FileMetadata metadata = getFileMetadata(fileId);
        return minioServiceImpl.getObjectInfo(metadata.getObjectKey());
    }

    @Override
    public void deleteFile(String fileId) {
        FileMetadata metadata = getFileMetadata(fileId);
        try {
            minioServiceImpl.deleteObject(metadata.getObjectKey());
            fileMetadataStore.remove(fileId);
            logger.info("Successfully deleted file with ID: {}", fileId);
        } catch (Exception e) {
            logger.error("Failed to delete file {}: {}", fileId, e.getMessage(), e);
            throw new FileProcessingException("Failed to delete file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("File is empty or null");
        }

        if (!FileUtils.isFileSizeValid(file.getSize(), maxFileSize)) {
            throw new FileProcessingException(
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            file.getSize(), maxFileSize)
            );
        }

        logger.debug("File validation passed for: {}", file.getOriginalFilename());
    }
}