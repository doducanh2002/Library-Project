package com.example.videoservice.controller;

import com.example.videoservice.model.VideoMetadata;
import com.example.videoservice.model.VideoUploadResponse;
import com.example.videoservice.service.VideoService;
import com.example.videoservice.service.impl.VideoServiceImpl;
import io.minio.StatObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoService videoService;

    @PostMapping("/upload")
    public ResponseEntity<VideoUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        logger.info("Received video upload request for file: {}", file.getOriginalFilename());

        try {
            VideoMetadata metadata = videoService.uploadVideo(file);
            VideoUploadResponse response = VideoUploadResponse.success(
                    "Video uploaded successfully", metadata
            );

            logger.info("Video upload completed successfully with ID: {}", metadata.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Video upload failed: {}", e.getMessage(), e);
            VideoUploadResponse response = VideoUploadResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<InputStreamResource> getVideo(@PathVariable String videoId) {
        logger.info("Received request to retrieve video with ID: {}", videoId);

        try {
            VideoMetadata metadata = videoService.getVideoMetadata(videoId);
            InputStream videoStream = videoService.getVideoStream(videoId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + metadata.getOriginalName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, metadata.getContentType());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .body(new InputStreamResource(videoStream));

        } catch (Exception e) {
            logger.error("Failed to retrieve video {}: {}", videoId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{videoId}/info")
    public ResponseEntity<VideoMetadata> getVideoInfo(@PathVariable String videoId) {
        logger.info("Received request for video info with ID: {}", videoId);

        try {
            VideoMetadata metadata = videoService.getVideoMetadata(videoId);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            logger.error("Failed to get video info for {}: {}", videoId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<VideoMetadata>> getAllVideos() {
        logger.info("Received request to list all videos");

        try {
            List<VideoMetadata> videos = videoService.getAllVideos();
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            logger.error("Failed to retrieve video list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{videoId}/stream")
    public ResponseEntity<InputStreamResource> streamVideo(@PathVariable String videoId) {
        logger.info("Received request to stream video with ID: {}", videoId);

        try {
            VideoMetadata metadata = videoService.getVideoMetadata(videoId);
            InputStream videoStream = videoService.getVideoStream(videoId);
            StatObjectResponse objectInfo = videoService.getVideoInfo(videoId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, metadata.getContentType());
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(objectInfo.size()));
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .body(new InputStreamResource(videoStream));

        } catch (Exception e) {
            logger.error("Failed to stream video {}: {}", videoId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Video service is running");
    }
}