package com.example.videoservice.service;

import com.example.videoservice.model.VideoMetadata;
import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface VideoService {
    VideoMetadata uploadVideo(MultipartFile file);

    VideoMetadata getVideoMetadata(String videoId);

    InputStream getVideoStream(String videoId);

    List<VideoMetadata> getAllVideos();

    StatObjectResponse getVideoInfo(String videoId);
}
