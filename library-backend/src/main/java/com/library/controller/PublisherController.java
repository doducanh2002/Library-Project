package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.CreatePublisherRequestDTO;
import com.library.dto.PublisherDTO;
import com.library.dto.PublisherDetailDTO;
import com.library.dto.UpdatePublisherRequestDTO;
import com.library.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Publisher Management", description = "APIs for managing book publishers")
//@CrossOrigin(origins = "*")
public class PublisherController {
    
    private final PublisherService publisherService;
    
    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }
    
    // Public endpoints
    
    @GetMapping("/publishers")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all publishers with pagination", description = "Retrieve paginated list of all publishers")
    public BaseResponse<Page<PublisherDTO>> getAllPublishers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/v1/publishers - Fetching publishers with pagination");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublisherDTO> publishers = publisherService.getAllPublishers(pageable);
        return BaseResponse.success(publishers);
    }
    
    @GetMapping("/publishers/{publisherId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get publisher by ID", description = "Retrieve detailed information about a specific publisher")
    public BaseResponse<PublisherDetailDTO> getPublisherById(
            @Parameter(description = "Publisher ID") @PathVariable Long publisherId) {
        log.info("GET /api/v1/publishers/{} - Fetching publisher details", publisherId);
        
        PublisherDetailDTO publisher = publisherService.getPublisherById(publisherId);
        return BaseResponse.success(publisher);
    }
    
    @GetMapping("/publishers/{publisherId}/books")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get publisher with books", description = "Retrieve publisher information including their books")
    public BaseResponse<PublisherDetailDTO> getPublisherWithBooks(
            @Parameter(description = "Publisher ID") @PathVariable Long publisherId) {
        log.info("GET /api/v1/publishers/{}/books - Fetching publisher with books", publisherId);
        
        PublisherDetailDTO publisher = publisherService.getPublisherWithBooks(publisherId);
        return BaseResponse.success(publisher);
    }
    
    @GetMapping("/publishers/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search publishers by name", description = "Search publishers by name (partial match)")
    public BaseResponse<List<PublisherDTO>> searchPublishersByName(
            @Parameter(description = "Publisher name") @RequestParam String name) {
        log.info("GET /api/v1/publishers/search - Searching publishers by name: {}", name);
        
        List<PublisherDTO> publishers = publisherService.searchPublishersByName(name);
        return BaseResponse.success(publishers);
    }
    
    @GetMapping("/publishers/established")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get publishers by established year range", description = "Retrieve publishers established within a year range")
    public BaseResponse<List<PublisherDTO>> getPublishersByEstablishedYear(
            @Parameter(description = "Start year") @RequestParam Integer startYear,
            @Parameter(description = "End year") @RequestParam Integer endYear) {
        log.info("GET /api/v1/publishers/established - Fetching publishers between {} and {}", startYear, endYear);
        
        List<PublisherDTO> publishers = publisherService.getPublishersByEstablishedYear(startYear, endYear);
        return BaseResponse.success(publishers);
    }
    
    @GetMapping("/publishers/active")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get most active publishers", description = "Retrieve publishers with most books")
    public BaseResponse<List<PublisherDTO>> getMostActivePublishers(
            @Parameter(description = "Number of publishers to return") @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/v1/publishers/active - Fetching most active publishers with limit: {}", limit);
        
        List<PublisherDTO> publishers = publisherService.getMostActivePublishers(limit);
        return BaseResponse.success(publishers);
    }
    
    // Admin endpoints
    
    @PostMapping("/admin/publishers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create new publisher", description = "Create a new publisher (Admin/Librarian only)")
    public BaseResponse<PublisherDetailDTO> createPublisher(
            @RequestBody @Validated CreatePublisherRequestDTO createRequest) {
        log.info("POST /api/v1/admin/publishers - Creating new publisher: {}", createRequest.getName());
        
        PublisherDetailDTO publisher = publisherService.createPublisher(createRequest);
        return BaseResponse.success(publisher);
    }
    
    @PutMapping("/admin/publishers/{publisherId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update publisher", description = "Update an existing publisher (Admin/Librarian only)")
    public BaseResponse<PublisherDetailDTO> updatePublisher(
            @Parameter(description = "Publisher ID") @PathVariable Long publisherId,
            @RequestBody @Validated UpdatePublisherRequestDTO updateRequest) {
        log.info("PUT /api/v1/admin/publishers/{} - Updating publisher", publisherId);
        
        PublisherDetailDTO publisher = publisherService.updatePublisher(publisherId, updateRequest);
        return BaseResponse.success(publisher);
    }
    
    @DeleteMapping("/admin/publishers/{publisherId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Delete publisher", description = "Delete a publisher (Admin/Librarian only)")
    public BaseResponse<String> deletePublisher(
            @Parameter(description = "Publisher ID") @PathVariable Long publisherId) {
        log.info("DELETE /api/v1/admin/publishers/{} - Deleting publisher", publisherId);
        
        publisherService.deletePublisher(publisherId);
        return BaseResponse.success("Publisher deleted successfully");
    }
    
    @GetMapping("/admin/publishers")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all publishers for admin", description = "Retrieve all publishers with admin details (Admin/Librarian only)")
    public BaseResponse<Page<PublisherDTO>> getAllPublishersForAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/v1/admin/publishers - Fetching publishers for admin");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublisherDTO> publishers = publisherService.getAllPublishers(pageable);
        return BaseResponse.success(publishers);
    }
    
    // Utility endpoints
    
    @GetMapping("/publishers/check-name")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check publisher name availability", description = "Check if a publisher name is available")
    public BaseResponse<Boolean> checkNameAvailability(
            @Parameter(description = "Publisher name to check") @RequestParam String name) {
        log.debug("GET /api/v1/publishers/check-name - Checking availability for name: {}", name);
        
        boolean exists = publisherService.existsByName(name);
        return BaseResponse.success(!exists);
    }
}