package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.dto.BookSearchCriteria;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;
import com.library.service.SearchService;
import com.library.specification.BookSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    
    @Override
    public Page<BookDTO> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching books with criteria: {}", criteria);
        
        Specification<Book> spec = BookSpecification.withCriteria(criteria);
        Page<Book> books = bookRepository.findAll(spec, pageable);
        
        log.debug("Found {} books matching criteria", books.getTotalElements());
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> fullTextSearch(String searchText, Pageable pageable) {
        log.debug("Performing full-text search with text: {}", searchText);
        
        if (searchText == null || searchText.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Use PostgreSQL full-text search capabilities
        Page<Book> books = bookRepository.findByFullTextSearch(searchText.trim(), pageable);
        
        log.debug("Full-text search found {} books", books.getTotalElements());
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchByCategories(List<Long> categoryIds, Pageable pageable) {
        log.debug("Searching books by categories: {}", categoryIds);
        
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        Specification<Book> spec = BookSpecification.belongsToCategories(categoryIds);
        Page<Book> books = bookRepository.findAll(spec, pageable);
        
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchByAuthors(List<Integer> authorIds, Pageable pageable) {
        log.debug("Searching books by authors: {}", authorIds);
        
        if (authorIds == null || authorIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        Page<Book> books = bookRepository.findByMultipleAuthors(authorIds, pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchAvailableForLoan(Pageable pageable) {
        log.debug("Searching books available for loan");
        
        Page<Book> books = bookRepository.findAvailableForLoan(pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchAvailableForSale(Pageable pageable) {
        log.debug("Searching books available for sale");
        
        Page<Book> books = bookRepository.findAvailableForSale(pageable);
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchRecentlyAdded(int days, Pageable pageable) {
        log.debug("Searching books added in the last {} days", days);
        
        Specification<Book> spec = BookSpecification.recentlyAdded(days);
        Page<Book> books = bookRepository.findAll(spec, pageable);
        
        return books.map(bookMapper::toDTO);
    }
    
    @Override
    public List<String> getSearchSuggestions(String partialText, int limit) {
        log.debug("Getting search suggestions for: {}", partialText);
        
        if (partialText == null || partialText.trim().length() < 2) {
            return new ArrayList<>();
        }
        
        // Get suggestions from book titles
        List<String> titleSuggestions = bookRepository.findTitleSuggestions(
            partialText.trim(), Pageable.ofSize(Math.min(limit, 10)));
        
        // Get suggestions from author names
        List<String> authorSuggestions = bookRepository.findAuthorSuggestions(
            partialText.trim(), Pageable.ofSize(Math.min(limit, 5)));
        
        // Combine and limit results
        List<String> allSuggestions = new ArrayList<>(titleSuggestions);
        allSuggestions.addAll(authorSuggestions);
        
        return allSuggestions.stream()
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getPopularSearchTerms(int limit) {
        log.debug("Getting popular search terms, limit: {}", limit);
        
        // This would typically come from search analytics/logs
        // For now, return some common terms based on available books
        return bookRepository.findPopularKeywords(Math.min(limit, 20));
    }
    
    @Override
    public Page<BookDTO> advancedSearch(BookSearchCriteria criteria, Pageable pageable) {
        log.debug("Performing advanced search with criteria: {}", criteria);
        
        // Start with base specification
        Specification<Book> spec = Specification.where(null);
        
        // Add keyword search with enhanced full-text capabilities
        if (criteria.hasKeyword()) {
            spec = spec.and(BookSpecification.enhancedKeywordSearch(criteria.getKeyword()));
        }
        
        // Add category filter with subcategory support
        if (criteria.hasCategoryFilter()) {
            spec = spec.and(BookSpecification.belongsToCategories(criteria.getCategoryIds()));
        }
        
        // Add author filter
        if (criteria.hasAuthorFilter()) {
            spec = spec.and(BookSpecification.writtenByMultipleAuthors(criteria.getAuthorIds()));
        }
        
        // Add publisher filter
        if (criteria.hasPublisherFilter()) {
            spec = spec.and(BookSpecification.publishedByMultiple(criteria.getPublisherIds()));
        }
        
        // Add availability filters
        if (criteria.getAvailableForLoan() != null && criteria.getAvailableForLoan()) {
            spec = spec.and(BookSpecification.availableForLoan());
        }
        
        if (criteria.getAvailableForSale() != null && criteria.getAvailableForSale()) {
            spec = spec.and(BookSpecification.availableForSale());
        }
        
        // Add price range filter
        if (criteria.hasPriceRange()) {
            spec = spec.and(BookSpecification.priceRange(criteria.getMinPrice(), criteria.getMaxPrice()));
        }
        
        // Add publication year range
        if (criteria.hasPublicationYearRange()) {
            spec = spec.and(BookSpecification.publicationYearBetween(
                criteria.getPublicationYearFrom(), criteria.getPublicationYearTo()));
        }
        
        // Add page range filter
        if (criteria.hasPageRange()) {
            spec = spec.and(BookSpecification.pageCountRange(
                criteria.getMinPages(), criteria.getMaxPages()));
        }
        
        // Add language filter
        if (criteria.getLanguage() != null && !criteria.getLanguage().trim().isEmpty()) {
            spec = spec.and(BookSpecification.byLanguage(criteria.getLanguage()));
        }
        
        // Add recently added filter
        if (criteria.getRecentlyAdded() != null && criteria.getRecentlyAdded()) {
            spec = spec.and(BookSpecification.recentlyAdded(30)); // Last 30 days
        }
        
        // Execute search with performance optimization
        Page<Book> books = bookRepository.findAll(spec, pageable);
        
        log.debug("Advanced search found {} books", books.getTotalElements());
        return books.map(bookMapper::toDTO);
    }
    
    /**
     * Helper method to build complex search specifications
     */
    private Specification<Book> buildComplexSearchSpec(BookSearchCriteria criteria) {
        Specification<Book> spec = Specification.where(null);
        
        // Priority-based search: exact matches first, then partial matches
        if (criteria.hasKeyword()) {
            String keyword = criteria.getKeyword().trim();
            
            // Exact title match gets highest priority
            Specification<Book> exactTitle = BookSpecification.exactTitleMatch(keyword);
            
            // ISBN match gets second priority  
            Specification<Book> isbnMatch = BookSpecification.isbnMatch(keyword);
            
            // Partial title/description match gets lower priority
            Specification<Book> partialMatch = BookSpecification.hasKeyword(keyword);
            
            spec = spec.and(exactTitle.or(isbnMatch).or(partialMatch));
        }
        
        return spec;
    }
    
    /**
     * Performance optimization for large result sets
     */
    private Page<Book> executeOptimizedSearch(Specification<Book> spec, Pageable pageable) {
        // For large result sets, use different strategies
        if (pageable.getPageSize() > 100) {
            log.warn("Large page size requested: {}, consider pagination", pageable.getPageSize());
        }
        
        return bookRepository.findAll(spec, pageable);
    }
}