package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailDTO {
    private Long id;
    private String title;
    private String isbn;
    private String description;
    private String coverImageUrl;
    private String language;
    private Integer numberOfPages;
    private Integer publicationYear;
    private String edition;
    
    // Sales info
    private BigDecimal price;
    private Integer stockForSale;
    private Boolean isSellable;
    
    // Loan info
    private Integer totalCopiesForLoan;
    private Integer availableCopiesForLoan;
    private Boolean isLendable;
    
    // Related entities with full details
    private CategoryDTO category;
    private PublisherDTO publisher;
    private List<AuthorDTO> authors;
    
    // Statistics
    private Long totalLoans;
    private Long currentLoans;
    private Long totalSales;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}