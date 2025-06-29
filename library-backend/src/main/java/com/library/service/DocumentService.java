package com.library.service;

import com.library.dto.document.*;
import com.library.entity.Book;
import com.library.entity.Document;
import com.library.entity.DocumentAccessLog;
import com.library.entity.enums.AccessLevel;
import com.library.entity.enums.AccessType;
import com.library.exception.FileStorageException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.DocumentMapper;
import com.library.repository.BookRepository;
import com.library.repository.DocumentAccessLogRepository;
import com.library.repository.DocumentRepository;
import com.library.specification.DocumentSpecification;
import com.library.client.MinIOServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final BookRepository bookRepository;
    private final DocumentAccessControlService accessControlService;
    private final DocumentMapper documentMapper;
    private final HttpServletRequest request;
    private final MinIOServiceClient minioServiceClient;
    
    private static final String DOCUMENTS_FOLDER = "documents";
    private static final int DEFAULT_URL_EXPIRY_MINUTES = 60;
    
    public DocumentService(DocumentRepository documentRepository,
                          DocumentAccessLogRepository accessLogRepository,
                          BookRepository bookRepository,
                          DocumentAccessControlService accessControlService,
                          DocumentMapper documentMapper,
                          HttpServletRequest request,
                          MinIOServiceClient minioServiceClient) {
        this.documentRepository = documentRepository;
        this.accessLogRepository = accessLogRepository;
        this.bookRepository = bookRepository;
        this.accessControlService = accessControlService;
        this.documentMapper = documentMapper;
        this.request = request;
        this.minioServiceClient = minioServiceClient;
    }
    
    /**
     * Upload a new document
     */
    public DocumentDTO uploadDocument(MultipartFile file, CreateDocumentRequestDTO requestDTO) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        if (requestDTO == null) {
            throw new IllegalArgumentException("Request DTO cannot be null");
        }
        
        log.info("Uploading document: {}", requestDTO.getTitle());
        
        if (!accessControlService.canUploadDocuments()) {
            throw new AccessDeniedException("You don't have permission to upload documents");
        }
        
        // Validate file name
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid name");
        }
        
        // Create document entity
        Document document = documentMapper.toEntity(requestDTO);
        
        // Set file information
        document.setOriginalFileName(originalFileName);
        document.setFileName(generateFileName(originalFileName));
        document.setFileType(getFileExtension(originalFileName));
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        
        // Set uploader
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("User must be authenticated to upload documents");
        }
        document.setUploadedBy(userId);
        
        // Link to book if provided
        if (requestDTO.getBookId() != null) {
            Book book = bookRepository.findById(requestDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
            document.setBook(book);
        }
        
        // Upload file to MinIO service
        try {
            Map<String, Object> uploadResponse = minioServiceClient.uploadFile(file);
            
            // Extract file information from MinIO response
            if (uploadResponse.containsKey("data")) {
                Map<String, Object> metadata = (Map<String, Object>) uploadResponse.get("data");
                String fileId = (String) metadata.get("id");
                String storageKey = (String) metadata.get("storageKey");
                
                document.setObjectKey(storageKey != null ? storageKey : fileId);
                document.setBucketName("video-storage"); // From MinIO service configuration
                
                log.info("File uploaded to MinIO service successfully: {}", fileId);
            } else {
                throw new FileStorageException("Invalid response from MinIO service");
            }
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO service: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload file: " + e.getMessage());
        }
        
        // Save document
        Document savedDocument = documentRepository.save(document);
        log.info("Document uploaded successfully with ID: {}", savedDocument.getId());
        
        // Convert to DTO and return
        DocumentDTO dto = documentMapper.toDTO(savedDocument);
        enrichDocumentDTO(dto, savedDocument);
        return dto;
    }
    
    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public DocumentDTO getDocument(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        
        if (!accessControlService.canAccessDocument(document)) {
            logAccessAttempt(document, AccessType.ACCESS_DENIED);
            throw new AccessDeniedException("You don't have permission to access this document");
        }
        
        DocumentDTO dto = documentMapper.toDTO(document);
        enrichDocumentDTO(dto, document);
        
        // Log metadata access
        logAccessAttempt(document, AccessType.METADATA_ACCESS);
        
        return dto;
    }
    
    /**
     * Search documents with criteria
     */
    @Transactional(readOnly = true)
    public Page<DocumentDTO> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching documents with criteria: {}", criteria);
        
        Specification<Document> spec = DocumentSpecification.withCriteria(criteria);
        Page<Document> documents = documentRepository.findAll(spec, pageable);
        
        return documents.map(doc -> {
            DocumentDTO dto = documentMapper.toDTO(doc);
            enrichDocumentDTO(dto, doc);
            return dto;
        });
    }
    
    /**
     * Get documents by book ID
     */
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByBook(Long bookId, Pageable pageable) {
        Page<Document> documents = documentRepository.findByBookIdAndIsActiveTrue(bookId, pageable);
        
        return documents.map(doc -> {
            DocumentDTO dto = documentMapper.toDTO(doc);
            enrichDocumentDTO(dto, doc);
            return dto;
        });
    }
    
    /**
     * Get public documents
     */
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getPublicDocuments(Pageable pageable) {
        Page<Document> documents = documentRepository.findPublicDocuments(pageable);
        
        return documents.map(doc -> {
            DocumentDTO dto = documentMapper.toDTO(doc);
            dto.setCanAccess(true);
            return dto;
        });
    }
    
    /**
     * Get download URL for a document
     */
    public String getDownloadUrl(Long documentId) {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        
        if (!accessControlService.canAccessDocument(document)) {
            logAccessAttempt(document, AccessType.ACCESS_DENIED);
            throw new AccessDeniedException("You don't have permission to download this document");
        }
        
        // Increment download count and refresh entity
        documentRepository.incrementDownloadCount(documentId);
        documentRepository.flush(); // Ensure the update is flushed to DB
        
        // Log download access
        logAccessAttempt(document, AccessType.DOWNLOAD);
        
        // Generate download URL from MinIO service
        try {
            String downloadUrl = minioServiceClient.getDownloadUrl(document.getObjectKey());
            log.info("Generated download URL for document ID: {}", documentId);
            return downloadUrl;
        } catch (Exception e) {
            log.error("Failed to generate download URL for document {}: {}", documentId, e.getMessage(), e);
            throw new FileStorageException("Failed to generate download URL: " + e.getMessage());
        }
    }
    
    /**
     * Get view URL for a document (for browser viewing)
     */
    public String getViewUrl(Long documentId) {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        
        if (!accessControlService.canAccessDocument(document)) {
            logAccessAttempt(document, AccessType.ACCESS_DENIED);
            throw new AccessDeniedException("You don't have permission to view this document");
        }
        
        // Log view access
        logAccessAttempt(document, AccessType.VIEW);
        
        // Generate view URL from MinIO service
        try {
            String viewUrl = minioServiceClient.getViewUrl(document.getObjectKey());
            log.info("Generated view URL for document ID: {}", documentId);
            return viewUrl;
        } catch (Exception e) {
            log.error("Failed to generate view URL for document {}: {}", documentId, e.getMessage(), e);
            throw new FileStorageException("Failed to generate view URL: " + e.getMessage());
        }
    }
    
    /**
     * Update document metadata
     */
    public DocumentDTO updateDocument(Long id, UpdateDocumentRequestDTO requestDTO) {
        if (id == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        if (requestDTO == null) {
            throw new IllegalArgumentException("Update request DTO cannot be null");
        }
        
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        
        if (!accessControlService.canManageDocument(document)) {
            throw new AccessDeniedException("You don't have permission to update this document");
        }
        
        // Update document fields
        documentMapper.updateEntity(requestDTO, document);
        
        // Update book link if changed
        if (requestDTO.getBookId() != null && !requestDTO.getBookId().equals(
            document.getBook() != null ? document.getBook().getId() : null)) {
            if (Long.valueOf(0L).equals(requestDTO.getBookId())) {
                document.setBook(null);
            } else {
                Book book = bookRepository.findById(requestDTO.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", requestDTO.getBookId()));
                document.setBook(book);
            }
        }
        
        Document updatedDocument = documentRepository.save(document);
        log.info("Document updated successfully: {}", id);
        
        DocumentDTO dto = documentMapper.toDTO(updatedDocument);
        enrichDocumentDTO(dto, updatedDocument);
        return dto;
    }
    
    /**
     * Delete a document
     */
    public void deleteDocument(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        
        if (!accessControlService.canManageDocument(document)) {
            throw new AccessDeniedException("You don't have permission to delete this document");
        }
        
        // Delete file from MinIO service first
        try {
            if (document.getObjectKey() != null) {
                boolean deleted = minioServiceClient.deleteFile(document.getObjectKey());
                if (deleted) {
                    log.info("File deleted from MinIO service: {}", document.getObjectKey());
                } else {
                    log.warn("Failed to delete file from MinIO service: {}", document.getObjectKey());
                }
            }
        } catch (Exception e) {
            log.error("Error deleting file from MinIO service: {}", e.getMessage(), e);
            // Continue with soft delete even if MinIO deletion fails
        }
        
        // Soft delete - just mark as inactive
        documentRepository.softDeleteDocument(id);
        
        log.info("Document soft deleted: {}", id);
    }
    
    /**
     * Get document statistics
     */
    @Transactional(readOnly = true)
    public DocumentStatisticsDTO getStatistics() {
        if (!accessControlService.canViewStatistics()) {
            throw new AccessDeniedException("You don't have permission to view statistics");
        }
        
        DocumentStatisticsDTO stats = new DocumentStatisticsDTO();
        
        // Total documents
        stats.setTotalDocuments(documentRepository.countActiveDocuments());
        
        // Total downloads - use database aggregation
        Long totalDownloads = documentRepository.sumDownloadCounts();
        stats.setTotalDownloads(totalDownloads != null ? totalDownloads : 0L);
        
        // Total size - use database aggregation
        Long totalSize = documentRepository.sumFileSizes();
        stats.setTotalSize(totalSize != null ? totalSize : 0L);
        stats.setTotalSizeFormatted(formatFileSize(totalSize != null ? totalSize : 0L));
        
        // Documents by access level
        List<Object[]> accessLevelCounts = documentRepository.countDocumentsByAccessLevel();
        Map<String, Long> accessLevelMap = new HashMap<>();
        for (Object[] row : accessLevelCounts) {
            accessLevelMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.setDocumentsByAccessLevel(accessLevelMap);
        
        // Documents by file type - use database aggregation
        List<Object[]> fileTypeCounts = documentRepository.countDocumentsByFileType();
        Map<String, Long> fileTypeMap = new HashMap<>();
        for (Object[] row : fileTypeCounts) {
            fileTypeMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.setDocumentsByFileType(fileTypeMap);
        
        // Recent uploads
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusDays(30);
        
        stats.setDocumentsUploadedToday(
            documentRepository.findDocumentsInDateRange(todayStart, now, PageRequest.of(0, 1))
                .getTotalElements()
        );
        stats.setDocumentsUploadedThisWeek(
            documentRepository.findDocumentsInDateRange(weekStart, now, PageRequest.of(0, 1))
                .getTotalElements()
        );
        stats.setDocumentsUploadedThisMonth(
            documentRepository.findDocumentsInDateRange(monthStart, now, PageRequest.of(0, 1))
                .getTotalElements()
        );
        
        return stats;
    }
    
    /**
     * Get most downloaded documents
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getMostDownloadedDocuments(int limit) {
        List<Document> documents = documentRepository.findMostDownloadedDocuments(
            PageRequest.of(0, limit)
        );
        
        return documents.stream()
            .map(doc -> {
                DocumentDTO dto = documentMapper.toDTO(doc);
                enrichDocumentDTO(dto, doc);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get document access logs
     */
    @Transactional(readOnly = true)
    public Page<DocumentAccessLogDTO> getDocumentAccessLogs(Long documentId, Pageable pageable) {
        if (!accessControlService.canViewStatistics()) {
            throw new AccessDeniedException("You don't have permission to view access logs");
        }
        
        Page<DocumentAccessLog> logs = accessLogRepository.findByDocumentId(documentId, pageable);
        return logs.map(documentMapper::toAccessLogDTO);
    }
    
    // Helper methods
    
    private void enrichDocumentDTO(DocumentDTO dto, Document document) {
        // Check access permission
        dto.setCanAccess(accessControlService.canAccessDocument(document));
        
        // Add URLs only if user can access
        if (dto.getCanAccess()) {
            dto.setDownloadUrl("/api/v1/documents/" + document.getId() + "/download");
            dto.setViewUrl("/api/v1/documents/" + document.getId() + "/view");
        }
        
        // Add uploader name (would need user service integration)
        dto.setUploaderName("User " + document.getUploadedBy());
    }
    
    private void logAccessAttempt(Document document, AccessType accessType) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                log.warn("Cannot log access attempt - user not authenticated");
                return;
            }
            
            DocumentAccessLog log = DocumentAccessLog.builder()
                .document(document)
                .userId(userId)
                .accessType(accessType)
                .ipAddress(getClientIpAddress())
                .userAgent(request.getHeader("User-Agent"))
                .build();
            
            accessLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log document access", e);
        }
    }
    
    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String generateFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        return timestamp + "_" + uuid + "." + extension;
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty() || !fileName.contains(".")) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private String getCurrentUserId() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null ? authentication.getName() : null;
        } catch (Exception e) {
            log.warn("Failed to get current user ID", e);
            return null;
        }
    }
}