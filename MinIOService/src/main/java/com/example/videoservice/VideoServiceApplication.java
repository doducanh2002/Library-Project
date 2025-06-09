package com.example.videoservice;

import com.example.videoservice.model.CustomMultipartFile;
import com.example.videoservice.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;

@SpringBootApplication
public class VideoServiceApplication implements CommandLineRunner {

    @Autowired
    private VideoService videoService;

    // Hardcoded file path
    private static final String VIDEO_FILE_PATH = "C:\\Users\\ADMIN\\Downloads\\70796-538877060.mp4";

    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        File videoFile = new File(VIDEO_FILE_PATH);
        if (videoFile.exists() && videoFile.isFile()) {
            try {
                // Determine content type
                String contentType = Files.probeContentType(videoFile.toPath());
                if (contentType == null) {
                    contentType = "video/mp4"; // Default to MP4
                }

                // Create CustomMultipartFile
                MultipartFile multipartFile = new CustomMultipartFile(
                        "file",
                        videoFile.getName(),
                        contentType,
                        videoFile
                );

                // Upload the video
                videoService.uploadVideo(multipartFile);
                System.out.println("Successfully uploaded video on startup: " + videoFile.getName());
            } catch (Exception e) {
                System.err.println("Failed to upload video on startup: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Startup video file not found at: " + VIDEO_FILE_PATH);
        }
    }
}