package com.example.videoservice.service.impl;

import com.example.videoservice.exception.VideoProcessingException;
import com.example.videoservice.model.VideoMetadata;
import com.example.videoservice.service.VideoService;
import com.example.videoservice.utill.FileUtils;
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
public class VideoServiceImpl implements VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Autowired
    private MinioServiceImpl minioServiceImpl;

    @Value("${video.max-size}")
    private long maxFileSize;

    @Value("${video.upload-path}")
    private String uploadPath;

    private final ConcurrentMap<String, VideoMetadata> videoMetadataStore = new ConcurrentHashMap<>();

    @Override
    public VideoMetadata uploadVideo(MultipartFile file) {
        logger.info("Starting video upload process for file: {}", file.getOriginalFilename());

        validateVideoFile(file);

        try {
            String originalName = file.getOriginalFilename();
            String videoId = FileUtils.generateUniqueFileName(originalName);
            String storedName = videoId;
            String objectKey = uploadPath + storedName;
            String contentType = file.getContentType();
            String format = FileUtils.getFileExtension(originalName);

            // Upload to MinIO
            minioServiceImpl.uploadObject(objectKey, file.getInputStream(), file.getSize(), contentType);

            // Create metadata
            VideoMetadata metadata = new VideoMetadata(
                    videoId,
                    originalName,
                    storedName,
                    contentType,
                    file.getSize(),
                    format,
                    minioServiceImpl.getBucketName(),
                    objectKey
            );

            // Store metadata (in production, save to database)
            videoMetadataStore.put(videoId, metadata);

            logger.info("Successfully uploaded video with ID: {}", videoId);
            return metadata;

        } catch (Exception e) {
            logger.error("Failed to upload video: {}", e.getMessage(), e);
            throw new VideoProcessingException("Failed to upload video: " + e.getMessage(), e);
        }
    }

    @Override
    public VideoMetadata getVideoMetadata(String videoId) {
        VideoMetadata metadata = videoMetadataStore.get(videoId);
        if (metadata == null) {
            throw new VideoProcessingException("Video not found with ID: " + videoId);
        }
        return metadata;
    }

    @Override
    public InputStream getVideoStream(String videoId) {
        VideoMetadata metadata = getVideoMetadata(videoId);
        try {
            return minioServiceImpl.getObject(metadata.getObjectKey());
        } catch (Exception e) {
            logger.error("Failed to retrieve video stream for ID {}: {}", videoId, e.getMessage(), e);
            throw new VideoProcessingException("Failed to retrieve video stream", e);
        }
    }

    @Override
    public List<VideoMetadata> getAllVideos() {
        return new ArrayList<>(videoMetadataStore.values());
    }

    @Override
    public StatObjectResponse getVideoInfo(String videoId) {
        VideoMetadata metadata = getVideoMetadata(videoId);
        return minioServiceImpl.getObjectInfo(metadata.getObjectKey());
    }

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new VideoProcessingException("File is empty or null");
        }

        if (!FileUtils.isVideoFile(file)) {
            throw new VideoProcessingException("File is not a valid video format. Supported formats: MP4, AVI, MOV, MKV, WebM, FLV");
        }

        if (!FileUtils.isFileSizeValid(file.getSize(), maxFileSize)) {
            throw new VideoProcessingException(
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            file.getSize(), maxFileSize)
            );
        }

        logger.debug("Video file validation passed for: {}", file.getOriginalFilename());
    }
}