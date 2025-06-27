package org.library.fileservice.controller;

import org.library.fileservice.model.FileMetadata;
import org.library.fileservice.model.FileUploadResponse;
import org.library.fileservice.service.FileService;
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
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Received file upload request for file: {}", file.getOriginalFilename());

        try {
            FileMetadata metadata = fileService.uploadFile(file);
            FileUploadResponse response = FileUploadResponse.success(
                    "File uploaded successfully", metadata
            );

            logger.info("File upload completed successfully with ID: {}", metadata.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("File upload failed: {}", e.getMessage(), e);
            FileUploadResponse response = FileUploadResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String fileId) {
        logger.info("Received request to retrieve file with ID: {}", fileId);

        try {
            FileMetadata metadata = fileService.getFileMetadata(fileId);
            InputStream fileStream = fileService.getFileStream(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + metadata.getOriginalName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, metadata.getContentType());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .body(new InputStreamResource(fileStream));

        } catch (Exception e) {
            logger.error("Failed to retrieve file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{fileId}/info")
    public ResponseEntity<FileMetadata> getFileInfo(@PathVariable String fileId) {
        logger.info("Received request for file info with ID: {}", fileId);

        try {
            FileMetadata metadata = fileService.getFileMetadata(fileId);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            logger.error("Failed to get file info for {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getAllFiles() {
        logger.info("Received request to list all files");

        try {
            List<FileMetadata> files = fileService.getAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            logger.error("Failed to retrieve file list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileId) {
        logger.info("Received request to download file with ID: {}", fileId);

        try {
            FileMetadata metadata = fileService.getFileMetadata(fileId);
            InputStream fileStream = fileService.getFileStream(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + metadata.getOriginalName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));

        } catch (Exception e) {
            logger.error("Failed to download file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<FileUploadResponse> deleteFile(@PathVariable String fileId) {
        logger.info("Received request to delete file with ID: {}", fileId);

        try {
            fileService.deleteFile(fileId);
            FileUploadResponse response = FileUploadResponse.success("File deleted successfully", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete file {}: {}", fileId, e.getMessage(), e);
            FileUploadResponse response = FileUploadResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("File service is running");
    }
}