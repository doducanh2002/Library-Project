package org.library.fileservice.service.impl;

import org.library.fileservice.service.MinioService;
import io.minio.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioServiceImpl implements MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioServiceImpl.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                logger.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("Error ensuring bucket exists: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ensure bucket exists", e);
        }
    }

    @Override
    public void uploadObject(String objectName, InputStream inputStream, long size, String contentType) {
        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            logger.info("Successfully uploaded object: {}", objectName);
        } catch (Exception e) {
            logger.error("Error uploading object {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to upload object to MinIO", e);
        }
    }

    @Override
    public InputStream getObject(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error retrieving object {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve object from MinIO", e);
        }
    }

    @Override
    public StatObjectResponse getObjectInfo(String objectName) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error getting object info for {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to get object information", e);
        }
    }

    @Override
    public List<String> listObjects(String prefix) {
        List<String> objectNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .build()
            );

            for (Result<Item> result : results) {
                objectNames.add(result.get().objectName());
            }
        } catch (Exception e) {
            logger.error("Error listing objects with prefix {}: {}", prefix, e.getMessage(), e);
            throw new RuntimeException("Failed to list objects", e);
        }
        return objectNames;
    }

    @Override
    public void deleteObject(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            logger.info("Successfully deleted object: {}", objectName);
        } catch (Exception e) {
            logger.error("Error deleting object {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Failed to delete object", e);
        }
    }

    public String getBucketName() {
        return bucketName;
    }
}