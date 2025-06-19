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
    private final MinioService minioService;
    private final DocumentAccessControlService accessControlService;
    private final DocumentMapper documentMapper;
    private final HttpServletRequest request;
    
    private static final String DOCUMENTS_FOLDER = "documents";
    private static final int DEFAULT_URL_EXPIRY_MINUTES = 60;
    
    public DocumentService(DocumentRepository documentRepository,
                          DocumentAccessLogRepository accessLogRepository,
                          BookRepository bookRepository,
                          MinioService minioService,
                          DocumentAccessControlService accessControlService,
                          DocumentMapper documentMapper,
                          HttpServletRequest request) {
        this.documentRepository = documentRepository;
        this.accessLogRepository = accessLogRepository;
        this.bookRepository = bookRepository;
        this.minioService = minioService;
        this.accessControlService = accessControlService;
        this.documentMapper = documentMapper;
        this.request = request;
    }
    
    /**
     * Upload a new document
     */
    public DocumentDTO uploadDocument(MultipartFile file, CreateDocumentRequestDTO requestDTO) {
        log.info("Uploading document: {}", requestDTO.getTitle());
        
        if (!accessControlService.canUploadDocuments()) {
            throw new AccessDeniedException("You don't have permission to upload documents");
        }
        
        // Create document entity
        Document document = documentMapper.toEntity(requestDTO);
        
        // Set file information
        document.setOriginalFileName(file.getOriginalFilename());
        document.setFileName(generateFileName(file.getOriginalFilename()));
        document.setFileType(getFileExtension(file.getOriginalFilename()));
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setBucketName(minioService.getMinioConfig().getBucketName());
        
        // Set uploader
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        document.setUploadedBy(userId);
        
        // Link to book if provided
        if (requestDTO.getBookId() != null) {
            Book book = bookRepository.findById(requestDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
            document.setBook(book);
        }
        
        // Upload file to MinIO
        try {
            String objectKey = minioService.uploadFile(file, DOCUMENTS_FOLDER);
            document.setObjectKey(objectKey);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new FileStorageException("Failed to upload file");
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
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
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
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        if (!accessControlService.canAccessDocument(document)) {
            logAccessAttempt(document, AccessType.ACCESS_DENIED);
            throw new AccessDeniedException("You don't have permission to download this document");
        }
        
        // Increment download count
        documentRepository.incrementDownloadCount(documentId);
        
        // Log download access
        logAccessAttempt(document, AccessType.DOWNLOAD);
        
        // Generate pre-signed URL
        return minioService.generateDownloadUrl(document.getObjectKey(), DEFAULT_URL_EXPIRY_MINUTES);
    }
    
    /**
     * Get view URL for a document (for browser viewing)
     */
    public String getViewUrl(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        if (!accessControlService.canAccessDocument(document)) {
            logAccessAttempt(document, AccessType.ACCESS_DENIED);
            throw new AccessDeniedException("You don't have permission to view this document");
        }
        
        // Log view access
        logAccessAttempt(document, AccessType.VIEW);
        
        // Generate pre-signed URL for viewing
        return minioService.generateViewUrl(document.getObjectKey());
    }
    
    /**
     * Update document metadata
     */
    public DocumentDTO updateDocument(Long id, UpdateDocumentRequestDTO requestDTO) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        if (!accessControlService.canManageDocument(document)) {
            throw new AccessDeniedException("You don't have permission to update this document");
        }
        
        // Update document fields
        documentMapper.updateEntity(requestDTO, document);
        
        // Update book link if changed
        if (requestDTO.getBookId() != null && !requestDTO.getBookId().equals(
            document.getBook() != null ? document.getBook().getId() : null)) {
            if (requestDTO.getBookId() == 0L) {
                document.setBook(null);
            } else {
                Book book = bookRepository.findById(requestDTO.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
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
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        if (!accessControlService.canManageDocument(document)) {
            throw new AccessDeniedException("You don't have permission to delete this document");
        }
        
        // Soft delete - just mark as inactive
        documentRepository.softDeleteDocument(id);
        
        // Optionally delete from MinIO (commented out for soft delete)
        // minioService.deleteFile(document.getObjectKey());
        
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
        stats.setTotalDocuments(documentRepository.count());
        
        // Total downloads
        List<Document> allDocs = documentRepository.findAll();
        long totalDownloads = allDocs.stream()
            .mapToLong(doc -> doc.getDownloadCount() != null ? doc.getDownloadCount() : 0)
            .sum();
        stats.setTotalDownloads(totalDownloads);
        
        // Total size
        long totalSize = allDocs.stream()
            .mapToLong(doc -> doc.getFileSize() != null ? doc.getFileSize() : 0)
            .sum();
        stats.setTotalSize(totalSize);
        stats.setTotalSizeFormatted(formatFileSize(totalSize));
        
        // Documents by access level
        List<Object[]> accessLevelCounts = documentRepository.countDocumentsByAccessLevel();
        Map<String, Long> accessLevelMap = new HashMap<>();
        for (Object[] row : accessLevelCounts) {
            accessLevelMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.setDocumentsByAccessLevel(accessLevelMap);
        
        // Documents by file type
        Map<String, Long> fileTypeMap = allDocs.stream()
            .filter(doc -> doc.getIsActive())
            .collect(Collectors.groupingBy(
                Document::getFileType,
                Collectors.counting()
            ));
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
            DocumentAccessLog log = DocumentAccessLog.builder()
                .document(document)
                .userId(SecurityContextHolder.getContext().getAuthentication().getName())
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
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}