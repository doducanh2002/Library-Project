package org.library.fileservice.exception;

import org.library.fileservice.model.FileUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<FileUploadResponse> handleVideoProcessingException(FileProcessingException ex) {
        logger.error("Video processing error: {}", ex.getMessage(), ex);
        FileUploadResponse response = FileUploadResponse.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<FileUploadResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        logger.error("File size limit exceeded: {}", ex.getMessage());
        FileUploadResponse response = FileUploadResponse.error("File size exceeds maximum allowed limit");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FileUploadResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Invalid argument: {}", ex.getMessage());
        FileUploadResponse response = FileUploadResponse.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        logger.error("Authentication failed: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", 401);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Authentication failed: " + ex.getMessage());
        errorResponse.put("path", "/api/v1/files");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        logger.error("Bad credentials provided: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", 401);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Invalid credentials provided");
        errorResponse.put("path", "/api/v1/files");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.error("Access denied: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", 403);
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "Access denied: " + ex.getMessage());
        errorResponse.put("path", "/api/v1/files");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FileUploadResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        FileUploadResponse response = FileUploadResponse.error("An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}