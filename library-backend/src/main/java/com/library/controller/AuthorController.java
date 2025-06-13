package com.library.controller;

import com.library.dto.AuthorDTO;
import com.library.dto.AuthorDetailDTO;
import com.library.dto.BaseResponse;
import com.library.dto.CreateAuthorRequestDTO;
import com.library.dto.UpdateAuthorRequestDTO;
import com.library.service.AuthorService;
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
@Tag(name = "Author Management", description = "APIs for managing book authors")
@CrossOrigin(origins = "*")
public class AuthorController {
    
    private final AuthorService authorService;
    
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }
    
    // Public endpoints
    
    @GetMapping("/authors")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all authors with pagination", description = "Retrieve paginated list of all authors")
    public BaseResponse<Page<AuthorDTO>> getAllAuthors(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/v1/authors - Fetching authors with pagination");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AuthorDTO> authors = authorService.getAllAuthors(pageable);
        return BaseResponse.success(authors);
    }
    
    @GetMapping("/authors/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search authors by keyword", description = "Search authors by name or biography")
    public BaseResponse<Page<AuthorDTO>> searchAuthors(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/v1/authors/search - Searching authors with keyword: {}", keyword);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AuthorDTO> authors = authorService.searchAuthors(keyword, pageable);
        return BaseResponse.success(authors);
    }
    
    @GetMapping("/authors/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get author by ID", description = "Retrieve detailed information about a specific author")
    public BaseResponse<AuthorDetailDTO> getAuthorById(
            @Parameter(description = "Author ID") @PathVariable Long authorId) {
        log.info("GET /api/v1/authors/{} - Fetching author details", authorId);
        
        AuthorDetailDTO author = authorService.getAuthorById(authorId);
        return BaseResponse.success(author);
    }
    
    @GetMapping("/authors/{authorId}/books")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get author with books", description = "Retrieve author information including their books")
    public BaseResponse<AuthorDetailDTO> getAuthorWithBooks(
            @Parameter(description = "Author ID") @PathVariable Long authorId) {
        log.info("GET /api/v1/authors/{}/books - Fetching author with books", authorId);
        
        AuthorDetailDTO author = authorService.getAuthorWithBooks(authorId);
        return BaseResponse.success(author);
    }
    
    @GetMapping("/authors/name/{name}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search authors by name", description = "Search authors by name (partial match)")
    public BaseResponse<List<AuthorDTO>> searchAuthorsByName(
            @Parameter(description = "Author name") @PathVariable String name) {
        log.info("GET /api/v1/authors/name/{} - Searching authors by name", name);
        
        List<AuthorDTO> authors = authorService.searchAuthorsByName(name);
        return BaseResponse.success(authors);
    }
    
    @GetMapping("/authors/nationality/{nationality}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get authors by nationality", description = "Retrieve authors by nationality")
    public BaseResponse<List<AuthorDTO>> getAuthorsByNationality(
            @Parameter(description = "Nationality") @PathVariable String nationality) {
        log.info("GET /api/v1/authors/nationality/{} - Fetching authors by nationality", nationality);
        
        List<AuthorDTO> authors = authorService.getAuthorsByNationality(nationality);
        return BaseResponse.success(authors);
    }
    
    @GetMapping("/authors/prolific")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get most prolific authors", description = "Retrieve authors with most books")
    public BaseResponse<List<AuthorDTO>> getMostProlificAuthors(
            @Parameter(description = "Number of authors to return") @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/v1/authors/prolific - Fetching most prolific authors with limit: {}", limit);
        
        List<AuthorDTO> authors = authorService.getMostProlificAuthors(limit);
        return BaseResponse.success(authors);
    }
    
    @GetMapping("/authors/nationalities")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all nationalities", description = "Retrieve list of all author nationalities")
    public BaseResponse<List<String>> getAllNationalities() {
        log.info("GET /api/v1/authors/nationalities - Fetching all nationalities");
        
        List<String> nationalities = authorService.getAllNationalities();
        return BaseResponse.success(nationalities);
    }
    
    // Admin endpoints
    
    @PostMapping("/admin/authors")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create new author", description = "Create a new author (Admin/Librarian only)")
    public BaseResponse<AuthorDetailDTO> createAuthor(
            @RequestBody @Validated CreateAuthorRequestDTO createRequest) {
        log.info("POST /api/v1/admin/authors - Creating new author: {}", createRequest.getName());
        
        AuthorDetailDTO author = authorService.createAuthor(createRequest);
        return BaseResponse.success(author);
    }
    
    @PutMapping("/admin/authors/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update author", description = "Update an existing author (Admin/Librarian only)")
    public BaseResponse<AuthorDetailDTO> updateAuthor(
            @Parameter(description = "Author ID") @PathVariable Long authorId,
            @RequestBody @Validated UpdateAuthorRequestDTO updateRequest) {
        log.info("PUT /api/v1/admin/authors/{} - Updating author", authorId);
        
        AuthorDetailDTO author = authorService.updateAuthor(authorId, updateRequest);
        return BaseResponse.success(author);
    }
    
    @DeleteMapping("/admin/authors/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Delete author", description = "Delete an author (Admin/Librarian only)")
    public BaseResponse<String> deleteAuthor(
            @Parameter(description = "Author ID") @PathVariable Long authorId) {
        log.info("DELETE /api/v1/admin/authors/{} - Deleting author", authorId);
        
        authorService.deleteAuthor(authorId);
        return BaseResponse.success("Author deleted successfully");
    }
    
    @GetMapping("/admin/authors")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all authors for admin", description = "Retrieve all authors with admin details (Admin/Librarian only)")
    public BaseResponse<Page<AuthorDTO>> getAllAuthorsForAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/v1/admin/authors - Fetching authors for admin");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AuthorDTO> authors = authorService.getAllAuthors(pageable);
        return BaseResponse.success(authors);
    }
}