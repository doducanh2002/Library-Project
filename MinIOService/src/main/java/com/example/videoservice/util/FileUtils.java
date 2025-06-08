package com.example.videoservice.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class FileUtils {

    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID() + "." + extension;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file name: No extension found");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public static boolean isVideoFile(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }
        String[] allowedTypes = {"video/mp4", "video/avi", "video/quicktime", "video/x-matroska", "video/webm", "video/x-flv"};
        for (String type : allowedTypes) {
            if (type.equals(file.getContentType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFileSizeValid(long fileSize, long maxFileSize) {
        return fileSize <= maxFileSize;
    }
}