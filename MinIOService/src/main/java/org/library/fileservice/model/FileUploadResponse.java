package org.library.fileservice.model;


public class FileUploadResponse {
    private boolean success;
    private String message;
    private String fileId;
    private FileMetadata fileMetadata;

    // Constructors
    public FileUploadResponse() {}

    public FileUploadResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public FileUploadResponse(boolean success, String message, FileMetadata fileMetadata) {
        this.success = success;
        this.message = message;
        this.fileMetadata = fileMetadata;
        this.fileId = fileMetadata != null ? fileMetadata.getId() : null;
    }

    // Static factory methods
    public static FileUploadResponse success(String message, FileMetadata metadata) {
        return new FileUploadResponse(true, message, metadata);
    }

    public static FileUploadResponse error(String message) {
        return new FileUploadResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public FileMetadata getFileMetadata() { return fileMetadata; }
    public void setFileMetadata(FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
        this.fileId = fileMetadata != null ? fileMetadata.getId() : null;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
}