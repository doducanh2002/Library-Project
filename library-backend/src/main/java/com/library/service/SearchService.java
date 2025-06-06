package com.library.service;

import com.library.dto.BookDTO;
import com.library.dto.BookSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for advanced search functionality
 */
public interface SearchService {
    
    /**
     * Search books using full-text search with PostgreSQL capabilities
     * @param criteria Search criteria
     * @param pageable Pagination information
     * @return Page of books matching the criteria
     */
    Page<BookDTO> searchBooks(BookSearchCriteria criteria, Pageable pageable);
    
    /**
     * Full-text search in book title and description
     * @param searchText Text to search for
     * @param pageable Pagination information
     * @return Page of books matching the search text
     */
    Page<BookDTO> fullTextSearch(String searchText, Pageable pageable);
    
    /**
     * Search books by multiple categories
     * @param categoryIds List of category IDs
     * @param pageable Pagination information
     * @return Page of books in the specified categories
     */
    Page<BookDTO> searchByCategories(List<Long> categoryIds, Pageable pageable);
    
    /**
     * Search books by multiple authors
     * @param authorIds List of author IDs
     * @param pageable Pagination information
     * @return Page of books by the specified authors
     */
    Page<BookDTO> searchByAuthors(List<Integer> authorIds, Pageable pageable);
    
    /**
     * Search available books for loan
     * @param pageable Pagination information
     * @return Page of books available for loan
     */
    Page<BookDTO> searchAvailableForLoan(Pageable pageable);
    
    /**
     * Search available books for sale
     * @param pageable Pagination information
     * @return Page of books available for sale
     */
    Page<BookDTO> searchAvailableForSale(Pageable pageable);
    
    /**
     * Search recently added books
     * @param days Number of days to look back
     * @param pageable Pagination information
     * @return Page of recently added books
     */
    Page<BookDTO> searchRecentlyAdded(int days, Pageable pageable);
    
    /**
     * Get search suggestions based on partial input
     * @param partialText Partial search text
     * @param limit Maximum number of suggestions
     * @return List of search suggestions
     */
    List<String> getSearchSuggestions(String partialText, int limit);
    
    /**
     * Get popular search terms
     * @param limit Maximum number of terms to return
     * @return List of popular search terms
     */
    List<String> getPopularSearchTerms(int limit);
    
    /**
     * Advanced search with complex filters and sorting
     * @param criteria Comprehensive search criteria
     * @param pageable Pagination and sorting information
     * @return Page of books matching all criteria
     */
    Page<BookDTO> advancedSearch(BookSearchCriteria criteria, Pageable pageable);
}