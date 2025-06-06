package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchCriteria {
    
    // Text search
    private String keyword; // Search in title, description, ISBN
    private String title;
    private String isbn;
    
    // Filters
    private List<Long> categoryIds;
    private List<Long> publisherIds;
    private List<Integer> authorIds;
    private String language;
    private Integer publicationYear;
    private Integer publicationYearFrom;
    private Integer publicationYearTo;
    
    // Availability filters
    private Boolean isLendable;
    private Boolean isSellable;
    private Boolean availableForLoan; // has available copies > 0
    private Boolean availableForSale; // has stock > 0
    
    // Price range
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Page count range
    private Integer minPages;
    private Integer maxPages;
    
    // Sorting
    private String sortBy = "createdAt"; // Default sort field
    private String sortDirection = "DESC"; // ASC or DESC
    
    // Special filters
    private Boolean recentlyAdded; // Added within last 30 days
    private Boolean popular; // Has more than average loans/sales
    
    // Helper methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasCategoryFilter() {
        return categoryIds != null && !categoryIds.isEmpty();
    }
    
    public boolean hasPublisherFilter() {
        return publisherIds != null && !publisherIds.isEmpty();
    }
    
    public boolean hasAuthorFilter() {
        return authorIds != null && !authorIds.isEmpty();
    }
    
    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }
    
    public boolean hasPageRange() {
        return minPages != null || maxPages != null;
    }
    
    public boolean hasPublicationYearRange() {
        return publicationYearFrom != null || publicationYearTo != null;
    }
}