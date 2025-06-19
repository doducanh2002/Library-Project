package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.document.*;
import com.library.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/documents")
@Slf4j
@Tag(name = "Admin Documents", description = "Document management APIs for administrators")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminDocumentController {
    
    private final DocumentService documentService;
    
    public AdminDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload document", description = "Upload a new document (LIBRARIAN/ADMIN role required)")
    public BaseResponse<DocumentDTO> uploadDocument(
            @Parameter(description = "Document file") 
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document title") 
            @RequestParam("title") String title,
            @Parameter(description = "Document description") 
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Access level") 
            @RequestParam(value = "accessLevel", defaultValue = "PUBLIC") String accessLevel,
            @Parameter(description = "Book ID (optional)") 
            @RequestParam(value = "bookId", required = false) Long bookId,
            @Parameter(description = "Metadata JSON (optional)") 
            @RequestParam(value = "metadata", required = false) String metadata) {
        
        log.info("Admin uploading document: {} with access level: {}", title, accessLevel);
        
        CreateDocumentRequestDTO request = CreateDocumentRequestDTO.builder()
                .title(title)
                .description(description)
                .accessLevel(com.library.entity.enums.AccessLevel.valueOf(accessLevel))
                .bookId(bookId)
                .metadata(metadata)
                .build();
        
        DocumentDTO document = documentService.uploadDocument(file, request);
        return BaseResponse.success(document);
    }
    
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update document", description = "Update document metadata (LIBRARIAN/ADMIN role required)")
    public BaseResponse<DocumentDTO> updateDocument(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @RequestBody @Valid UpdateDocumentRequestDTO request) {
        
        log.info("Admin updating document ID: {}", id);
        DocumentDTO document = documentService.updateDocument(id, request);
        return BaseResponse.success(document);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete document", description = "Delete a document (LIBRARIAN/ADMIN role required)")
    public BaseResponse<String> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        
        log.info("Admin deleting document ID: {}", id);
        documentService.deleteDocument(id);
        return BaseResponse.success("Document deleted successfully");
    }
    
    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get document statistics", description = "Get comprehensive document statistics")
    public BaseResponse<DocumentStatisticsDTO> getStatistics() {
        log.info("Admin getting document statistics");
        DocumentStatisticsDTO statistics = documentService.getStatistics();
        return BaseResponse.success(statistics);
    }
    
    @GetMapping("/{id}/access-logs")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get document access logs", description = "Get access logs for a specific document")
    public BaseResponse<Page<DocumentAccessLogDTO>> getDocumentAccessLogs(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Admin getting access logs for document ID: {}", id);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "accessedAt"));
        Page<DocumentAccessLogDTO> logs = documentService.getDocumentAccessLogs(id, pageable);
        return BaseResponse.success(logs);
    }
    
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all documents", description = "Get all documents including inactive ones")
    public BaseResponse<Page<DocumentDTO>> getAllDocuments(
            @Parameter(description = "Include inactive documents") 
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin getting all documents, includeInactive: {}", includeInactive);
        
        DocumentSearchCriteria criteria = DocumentSearchCriteria.builder()
                .isActive(includeInactive ? null : true)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .build();
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<DocumentDTO> documents = documentService.searchDocuments(criteria, pageable);
        return BaseResponse.success(documents);
    }
    
    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Activate document", description = "Reactivate an inactive document")
    public BaseResponse<DocumentDTO> activateDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        
        log.info("Admin activating document ID: {}", id);
        UpdateDocumentRequestDTO request = UpdateDocumentRequestDTO.builder()
                .isActive(true)
                .build();
        DocumentDTO document = documentService.updateDocument(id, request);
        return BaseResponse.success(document);
    }
    
    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Deactivate document", description = "Deactivate a document without deleting")
    public BaseResponse<DocumentDTO> deactivateDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        
        log.info("Admin deactivating document ID: {}", id);
        UpdateDocumentRequestDTO request = UpdateDocumentRequestDTO.builder()
                .isActive(false)
                .build();
        DocumentDTO document = documentService.updateDocument(id, request);
        return BaseResponse.success(document);
    }
    
    @GetMapping("/by-uploader/{uploaderId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get documents by uploader", description = "Get all documents uploaded by a specific user")
    public BaseResponse<Page<DocumentDTO>> getDocumentsByUploader(
            @Parameter(description = "Uploader user ID") @PathVariable String uploaderId,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin getting documents uploaded by user: {}", uploaderId);
        
        DocumentSearchCriteria criteria = DocumentSearchCriteria.builder()
                .uploadedBy(uploaderId)
                .isActive(true)
                .sortBy("createdAt")
                .sortDirection("desc")
                .build();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentDTO> documents = documentService.searchDocuments(criteria, pageable);
        return BaseResponse.success(documents);
    }
    
    @GetMapping("/orphaned")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get orphaned documents", description = "Get documents not linked to any book")
    public BaseResponse<Page<DocumentDTO>> getOrphanedDocuments(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin getting orphaned documents");
        
        // Search for documents with null bookId
        DocumentSearchCriteria criteria = DocumentSearchCriteria.builder()
                .bookId(null)
                .isActive(true)
                .sortBy("createdAt")
                .sortDirection("desc")
                .build();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentDTO> documents = documentService.searchDocuments(criteria, pageable);
        return BaseResponse.success(documents);
    }
    
    @PutMapping("/bulk-update-access-level")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Bulk update access level", description = "Update access level for multiple documents")
    public BaseResponse<String> bulkUpdateAccessLevel(
            @Parameter(description = "Document IDs") @RequestParam List<Long> documentIds,
            @Parameter(description = "New access level") @RequestParam String accessLevel) {
        
        log.info("Admin bulk updating access level for {} documents to {}", documentIds.size(), accessLevel);
        
        // Update each document
        for (Long documentId : documentIds) {
            try {
                UpdateDocumentRequestDTO request = UpdateDocumentRequestDTO.builder()
                        .accessLevel(com.library.entity.enums.AccessLevel.valueOf(accessLevel))
                        .build();
                documentService.updateDocument(documentId, request);
            } catch (Exception e) {
                log.error("Failed to update document ID: {}", documentId, e);
            }
        }
        
        return BaseResponse.success("Access level updated for " + documentIds.size() + " documents");
    }
    
    @DeleteMapping("/bulk-delete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Bulk delete documents", description = "Delete multiple documents")
    public BaseResponse<String> bulkDeleteDocuments(
            @Parameter(description = "Document IDs") @RequestParam List<Long> documentIds) {
        
        log.info("Admin bulk deleting {} documents", documentIds.size());
        
        int deletedCount = 0;
        for (Long documentId : documentIds) {
            try {
                documentService.deleteDocument(documentId);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete document ID: {}", documentId, e);
            }
        }
        
        return BaseResponse.success(deletedCount + " documents deleted successfully");
    }
}