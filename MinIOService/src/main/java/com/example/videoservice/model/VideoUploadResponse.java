package com.example.videoservice.model;


public class VideoUploadResponse {
    private boolean success;
    private String message;
    private VideoMetadata videoMetadata;
    private String videoId;

    // Constructors
    public VideoUploadResponse() {}

    public VideoUploadResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public VideoUploadResponse(boolean success, String message, VideoMetadata videoMetadata) {
        this.success = success;
        this.message = message;
        this.videoMetadata = videoMetadata;
        this.videoId = videoMetadata != null ? videoMetadata.getId() : null;
    }

    // Static factory methods
    public static VideoUploadResponse success(String message, VideoMetadata metadata) {
        return new VideoUploadResponse(true, message, metadata);
    }

    public static VideoUploadResponse error(String message) {
        return new VideoUploadResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public VideoMetadata getVideoMetadata() { return videoMetadata; }
    public void setVideoMetadata(VideoMetadata videoMetadata) {
        this.videoMetadata = videoMetadata;
        this.videoId = videoMetadata != null ? videoMetadata.getId() : null;
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
}