package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.document.*;
import com.library.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@Slf4j
@Tag(name = "Documents", description = "Document management APIs")
@CrossOrigin(origins = "*")
public class DocumentController {
    
    private final DocumentService documentService;
    
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }
    
    // Public endpoints
    
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search documents", description = "Search and list documents with filters")
    public BaseResponse<Page<DocumentDTO>> searchDocuments(
            @Parameter(description = "Search term for title or description") 
            @RequestParam(required = false) String searchTerm,
            @Parameter(description = "File type filter") 
            @RequestParam(required = false) String fileType,
            @Parameter(description = "Access level filter") 
            @RequestParam(required = false) String accessLevel,
            @Parameter(description = "Book ID filter") 
            @RequestParam(required = false) Long bookId,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Searching documents with term: {}, fileType: {}, bookId: {}", 
                searchTerm, fileType, bookId);
        
        DocumentSearchCriteria criteria = DocumentSearchCriteria.builder()
                .searchTerm(searchTerm)
                .fileTypes(fileType != null ? List.of(fileType) : null)
                .bookId(bookId)
                .isActive(true)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .build();
        
        if (accessLevel != null) {
            try {
                criteria.setAccessLevel(com.library.entity.enums.AccessLevel.valueOf(accessLevel));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid access level provided: {}", accessLevel);
            }
        }
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<DocumentDTO> documents = documentService.searchDocuments(criteria, pageable);
        return BaseResponse.success(documents);
    }
    
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Advanced document search", description = "Search documents with comprehensive criteria")
    public BaseResponse<Page<DocumentDTO>> advancedSearch(
            @RequestBody @Valid DocumentSearchCriteria criteria,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Advanced document search with criteria: {}", criteria);
        
        Sort.Direction direction = criteria.getSortDirection().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, criteria.getSortBy()));
        
        Page<DocumentDTO> documents = documentService.searchDocuments(criteria, pageable);
        return BaseResponse.success(documents);
    }
    
    @GetMapping("/public")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get public documents", description = "Get all publicly accessible documents")
    public BaseResponse<Page<DocumentDTO>> getPublicDocuments(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting public documents");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentDTO> documents = documentService.getPublicDocuments(pageable);
        return BaseResponse.success(documents);
    }
    
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get document by ID", description = "Get document details by ID")
    public BaseResponse<DocumentDTO> getDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        log.info("Getting document with ID: {}", id);
        DocumentDTO document = documentService.getDocument(id);
        return BaseResponse.success(document);
    }
    
    @GetMapping("/book/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get documents by book", description = "Get all documents associated with a book")
    public BaseResponse<Page<DocumentDTO>> getDocumentsByBook(
            @Parameter(description = "Book ID") @PathVariable Long bookId,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting documents for book ID: {}", bookId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentDTO> documents = documentService.getDocumentsByBook(bookId, pageable);
        return BaseResponse.success(documents);
    }
    
    @GetMapping("/{id}/download")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Download document", description = "Get download URL for a document")
    public void downloadDocument(
            @Parameter(description = "Document ID") @PathVariable Long id,
            HttpServletResponse response) throws IOException {
        
        log.info("Download request for document ID: {}", id);
        String downloadUrl = documentService.getDownloadUrl(id);
        response.sendRedirect(downloadUrl);
    }
    
    @GetMapping("/{id}/download-url")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get download URL", description = "Get pre-signed download URL for a document")
    public BaseResponse<String> getDownloadUrl(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        
        log.info("Getting download URL for document ID: {}", id);
        String downloadUrl = documentService.getDownloadUrl(id);
        return BaseResponse.success(downloadUrl);
    }
    
    @GetMapping("/{id}/view")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "View document", description = "Get view URL for a document")
    public void viewDocument(
            @Parameter(description = "Document ID") @PathVariable Long id,
            HttpServletResponse response) throws IOException {
        
        log.info("View request for document ID: {}", id);
        String viewUrl = documentService.getViewUrl(id);
        response.sendRedirect(viewUrl);
    }
    
    @GetMapping("/{id}/view-url")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get view URL", description = "Get pre-signed view URL for a document")
    public BaseResponse<String> getViewUrl(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        
        log.info("Getting view URL for document ID: {}", id);
        String viewUrl = documentService.getViewUrl(id);
        return BaseResponse.success(viewUrl);
    }
    
    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get popular documents", description = "Get most downloaded documents")
    public BaseResponse<List<DocumentDTO>> getPopularDocuments(
            @Parameter(description = "Number of documents to return") 
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting top {} popular documents", limit);
        List<DocumentDTO> documents = documentService.getMostDownloadedDocuments(limit);
        return BaseResponse.success(documents);
    }
    
    // Protected endpoints (require authentication)
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload document", description = "Upload a new document (requires LIBRARIAN role)")
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
        
        log.info("Uploading document: {} with access level: {}", title, accessLevel);
        
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
    @Operation(summary = "Update document", description = "Update document metadata (requires permission)")
    public BaseResponse<DocumentDTO> updateDocument(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @RequestBody @Valid UpdateDocumentRequestDTO request) {
        
        log.info("Updating document ID: {}", id);
        DocumentDTO document = documentService.updateDocument(id, request);
        return BaseResponse.success(document);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete document", description = "Delete a document (requires permission)")
    public BaseResponse<String> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        
        log.info("Deleting document ID: {}", id);
        documentService.deleteDocument(id);
        return BaseResponse.success("Document deleted successfully");
    }
}