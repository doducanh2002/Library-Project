package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.BookDTO;
import com.library.dto.BookSearchCriteria;
import com.library.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search", description = "Advanced search APIs for books")
@CrossOrigin(origins = "*")
public class SearchController {
    
    private final SearchService searchService;
    
    @GetMapping("/fulltext")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Full-text search", description = "Perform full-text search using PostgreSQL capabilities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public BaseResponse<Page<BookDTO>> fullTextSearch(
            @Parameter(description = "Search text") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "relevance") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Full-text search request: q={}, page={}, size={}", q, page, size);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, 
            sortBy.equals("relevance") ? "createdAt" : sortBy));
        
        Page<BookDTO> results = searchService.fullTextSearch(q, pageable);
        return BaseResponse.success(results);
    }
    
    @PostMapping("/advanced")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Advanced search", description = "Advanced search with comprehensive filters and performance optimization")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria")
    })
    public BaseResponse<Page<BookDTO>> advancedSearch(
            @RequestBody BookSearchCriteria criteria,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Advanced search request with criteria: {}", criteria);
        
        // Use sorting from criteria or default
        Sort.Direction direction = criteria.getSortDirection().equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, criteria.getSortBy()));
        
        Page<BookDTO> results = searchService.advancedSearch(criteria, pageable);
        return BaseResponse.success(results);
    }
    
    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search by categories", description = "Search books by multiple categories")
    public BaseResponse<Page<BookDTO>> searchByCategories(
            @Parameter(description = "Category IDs (comma-separated)") @RequestParam List<Long> categoryIds,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Search by categories: {}", categoryIds);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> results = searchService.searchByCategories(categoryIds, pageable);
        return BaseResponse.success(results);
    }
    
    @GetMapping("/authors")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search by authors", description = "Search books by multiple authors")
    public BaseResponse<Page<BookDTO>> searchByAuthors(
            @Parameter(description = "Author IDs (comma-separated)") @RequestParam List<Integer> authorIds,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Search by authors: {}", authorIds);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> results = searchService.searchByAuthors(authorIds, pageable);
        return BaseResponse.success(results);
    }
    
    @GetMapping("/available-for-loan")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search available for loan", description = "Get books currently available for loan")
    public BaseResponse<Page<BookDTO>> searchAvailableForLoan(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Search books available for loan");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> results = searchService.searchAvailableForLoan(pageable);
        return BaseResponse.success(results);
    }
    
    @GetMapping("/available-for-sale")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search available for sale", description = "Get books currently available for sale")
    public BaseResponse<Page<BookDTO>> searchAvailableForSale(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Search books available for sale");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> results = searchService.searchAvailableForSale(pageable);
        return BaseResponse.success(results);
    }
    
    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search recently added", description = "Get recently added books")
    public BaseResponse<Page<BookDTO>> searchRecentlyAdded(
            @Parameter(description = "Number of days to look back") @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Search recently added books (last {} days)", days);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> results = searchService.searchRecentlyAdded(days, pageable);
        return BaseResponse.success(results);
    }
    
    @GetMapping("/suggestions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get search suggestions", description = "Get autocomplete suggestions for search")
    public BaseResponse<List<String>> getSearchSuggestions(
            @Parameter(description = "Partial search text") @RequestParam String q,
            @Parameter(description = "Maximum number of suggestions") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Get search suggestions for: {}", q);
        
        List<String> suggestions = searchService.getSearchSuggestions(q, limit);
        return BaseResponse.success(suggestions);
    }
    
    @GetMapping("/popular-terms")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get popular search terms", description = "Get popular search terms for suggestions")
    public BaseResponse<List<String>> getPopularSearchTerms(
            @Parameter(description = "Maximum number of terms") @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Get popular search terms, limit: {}", limit);
        
        List<String> terms = searchService.getPopularSearchTerms(limit);
        return BaseResponse.success(terms);
    }
    
    @GetMapping("/filters/performance")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Performance optimized search", description = "Search with performance optimizations for large datasets")
    public BaseResponse<Page<BookDTO>> performanceOptimizedSearch(
            @RequestBody(required = false) BookSearchCriteria criteria,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {
        
        log.info("Performance optimized search with criteria: {}", criteria);
        
        // For performance, limit page size and use simpler sorting
        int limitedSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, limitedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (criteria == null) {
            criteria = new BookSearchCriteria();
        }
        
        Page<BookDTO> results = searchService.searchBooks(criteria, pageable);
        return BaseResponse.success(results);
    }
}