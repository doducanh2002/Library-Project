package com.library.specification;

import com.library.dto.BookSearchCriteria;
import com.library.entity.Book;
import com.library.entity.BookAuthor;
import com.library.entity.Author;
import com.library.entity.Category;
import com.library.entity.Publisher;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> withCriteria(BookSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search - search in title, description, and ISBN
            if (criteria.hasKeyword()) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), keyword);
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), keyword);
                Predicate isbnPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("isbn")), keyword);
                
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate, isbnPredicate));
            }

            // Exact title search
            if (criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    "%" + criteria.getTitle().toLowerCase() + "%"));
            }

            // Exact ISBN search
            if (criteria.getIsbn() != null && !criteria.getIsbn().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("isbn"), criteria.getIsbn()));
            }

            // Category filter
            if (criteria.hasCategoryFilter()) {
                Join<Book, Category> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(categoryJoin.get("id").in(criteria.getCategoryIds()));
            }

            // Publisher filter
            if (criteria.hasPublisherFilter()) {
                Join<Book, Publisher> publisherJoin = root.join("publisher", JoinType.INNER);
                predicates.add(publisherJoin.get("id").in(criteria.getPublisherIds()));
            }

            // Author filter
            if (criteria.hasAuthorFilter()) {
                Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthors", JoinType.INNER);
                Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author", JoinType.INNER);
                predicates.add(authorJoin.get("id").in(criteria.getAuthorIds()));
            }

            // Language filter
            if (criteria.getLanguage() != null && !criteria.getLanguage().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("language"), criteria.getLanguage()));
            }

            // Publication year filters
            if (criteria.getPublicationYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("publicationYear"), criteria.getPublicationYear()));
            }

            if (criteria.hasPublicationYearRange()) {
                if (criteria.getPublicationYearFrom() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("publicationYear"), criteria.getPublicationYearFrom()));
                }
                if (criteria.getPublicationYearTo() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("publicationYear"), criteria.getPublicationYearTo()));
                }
            }

            // Availability filters
            if (criteria.getIsLendable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isLendable"), criteria.getIsLendable()));
            }

            if (criteria.getIsSellable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isSellable"), criteria.getIsSellable()));
            }

            if (criteria.getAvailableForLoan() != null && criteria.getAvailableForLoan()) {
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("isLendable")),
                    criteriaBuilder.greaterThan(root.get("availableCopiesForLoan"), 0)
                ));
            }

            if (criteria.getAvailableForSale() != null && criteria.getAvailableForSale()) {
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("isSellable")),
                    criteriaBuilder.greaterThan(root.get("stockForSale"), 0)
                ));
            }

            // Price range filter
            if (criteria.hasPriceRange()) {
                if (criteria.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), criteria.getMaxPrice()));
                }
            }

            // Page count range filter
            if (criteria.hasPageRange()) {
                if (criteria.getMinPages() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("numberOfPages"), criteria.getMinPages()));
                }
                if (criteria.getMaxPages() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("numberOfPages"), criteria.getMaxPages()));
                }
            }

            // Recently added filter (within last 30 days)
            if (criteria.getRecentlyAdded() != null && criteria.getRecentlyAdded()) {
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), thirtyDaysAgo));
            }

            // Apply distinct to avoid duplicates when joining with authors
            if (criteria.hasAuthorFilter()) {
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Individual specifications for reuse
    public static Specification<Book> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            String searchKeyword = "%" + keyword.toLowerCase() + "%";
            Predicate titlePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")), searchKeyword);
            Predicate descriptionPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")), searchKeyword);
            Predicate isbnPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("isbn")), searchKeyword);
            
            return criteriaBuilder.or(titlePredicate, descriptionPredicate, isbnPredicate);
        };
    }

    public static Specification<Book> belongsToCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Book, Category> categoryJoin = root.join("category", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Book> belongsToCategories(List<Long> categoryIds) {
        return (root, query, criteriaBuilder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Book, Category> categoryJoin = root.join("category", JoinType.INNER);
            return categoryJoin.get("id").in(categoryIds);
        };
    }

    public static Specification<Book> writtenByAuthor(Integer authorId) {
        return (root, query, criteriaBuilder) -> {
            if (authorId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthors", JoinType.INNER);
            Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author", JoinType.INNER);
            query.distinct(true); // Avoid duplicates
            return criteriaBuilder.equal(authorJoin.get("id"), authorId);
        };
    }

    public static Specification<Book> publishedBy(Long publisherId) {
        return (root, query, criteriaBuilder) -> {
            if (publisherId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Book, Publisher> publisherJoin = root.join("publisher", JoinType.INNER);
            return criteriaBuilder.equal(publisherJoin.get("id"), publisherId);
        };
    }

    public static Specification<Book> availableForLoan() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.isTrue(root.get("isLendable")),
                criteriaBuilder.greaterThan(root.get("availableCopiesForLoan"), 0)
            );
    }

    public static Specification<Book> availableForSale() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.isTrue(root.get("isSellable")),
                criteriaBuilder.greaterThan(root.get("stockForSale"), 0)
            );
    }

    public static Specification<Book> priceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Book> recentlyAdded(int days) {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime daysAgo = LocalDateTime.now().minusDays(days);
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), daysAgo);
        };
    }

    public static Specification<Book> byLanguage(String language) {
        return (root, query, criteriaBuilder) -> {
            if (language == null || language.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("language"), language);
        };
    }

    public static Specification<Book> publicationYearBetween(Integer fromYear, Integer toYear) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (fromYear != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("publicationYear"), fromYear));
            }
            if (toYear != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("publicationYear"), toYear));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    // Enhanced search methods for SearchService
    
    public static Specification<Book> enhancedKeywordSearch(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            String searchKeyword = "%" + keyword.toLowerCase() + "%";
            
            // Create predicates for different fields with different weights
            Predicate titlePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")), searchKeyword);
            Predicate descriptionPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")), searchKeyword);
            Predicate isbnPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("isbn")), searchKeyword);
            
            // Also search in author names
            Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthors", JoinType.LEFT);
            Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author", JoinType.LEFT);
            Predicate authorPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(authorJoin.get("name")), searchKeyword);
            
            query.distinct(true);
            return criteriaBuilder.or(titlePredicate, descriptionPredicate, isbnPredicate, authorPredicate);
        };
    }
    
    public static Specification<Book> exactTitleMatch(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.trim().isEmpty()) {
                return criteriaBuilder.disjunction(); // Always false
            }
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("title")), title.toLowerCase());
        };
    }
    
    public static Specification<Book> isbnMatch(String isbn) {
        return (root, query, criteriaBuilder) -> {
            if (isbn == null || isbn.trim().isEmpty()) {
                return criteriaBuilder.disjunction(); // Always false
            }
            return criteriaBuilder.equal(root.get("isbn"), isbn);
        };
    }
    
    public static Specification<Book> writtenByMultipleAuthors(List<Integer> authorIds) {
        return (root, query, criteriaBuilder) -> {
            if (authorIds == null || authorIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthors", JoinType.INNER);
            Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author", JoinType.INNER);
            query.distinct(true);
            return authorJoin.get("id").in(authorIds);
        };
    }
    
    public static Specification<Book> publishedByMultiple(List<Long> publisherIds) {
        return (root, query, criteriaBuilder) -> {
            if (publisherIds == null || publisherIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Book, Publisher> publisherJoin = root.join("publisher", JoinType.INNER);
            return publisherJoin.get("id").in(publisherIds);
        };
    }
    
    public static Specification<Book> pageCountRange(Integer minPages, Integer maxPages) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minPages != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("numberOfPages"), minPages));
            }
            if (maxPages != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("numberOfPages"), maxPages));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<Book> inMultipleCategories(List<Long> categoryIds) {
        return (root, query, criteriaBuilder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            // Include both direct category matches and parent category matches
            Join<Book, Category> categoryJoin = root.join("category", JoinType.INNER);
            Predicate directMatch = categoryJoin.get("id").in(categoryIds);
            Predicate parentMatch = categoryJoin.get("parentCategory").get("id").in(categoryIds);
            
            return criteriaBuilder.or(directMatch, parentMatch);
        };
    }
    
    public static Specification<Book> availableAndInStock() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("isLendable")),
                    criteriaBuilder.greaterThan(root.get("availableCopiesForLoan"), 0)
                ),
                criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("isSellable")),
                    criteriaBuilder.greaterThan(root.get("stockForSale"), 0)
                )
            );
    }
    
    public static Specification<Book> withPerformanceOptimization() {
        return (root, query, criteriaBuilder) -> {
            // Add fetch joins for commonly accessed relationships
            if (query.getResultType().equals(Book.class)) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("publisher", JoinType.LEFT);
                // Note: bookAuthors fetch might cause N+1, handle carefully
            }
            return criteriaBuilder.conjunction();
        };
    }
    
    public static Specification<Book> popularBooks(int minimumInteractions) {
        return (root, query, criteriaBuilder) -> {
            // This is a placeholder for popularity logic
            // In real implementation, this would join with loans/orders tables
            // For now, we'll use creation date as a proxy for popularity
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            return criteriaBuilder.greaterThan(root.get("createdAt"), thirtyDaysAgo);
        };
    }
}