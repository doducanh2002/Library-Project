package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
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
    
    // Category
    private Long categoryId;
    private String categoryName;
    
    // Publisher
    private Long publisherId;
    private String publisherName;
    
    // Authors (comma-separated string for simple display)
    private String authors;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor for dashboard query
    public BookDTO(Long id, String title, String isbn, BigDecimal price, Integer stockForSale,
                   Integer availableCopiesForLoan, Boolean isLendable, Boolean isSellable) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.price = price;
        this.stockForSale = stockForSale;
        this.availableCopiesForLoan = availableCopiesForLoan;
        this.isLendable = isLendable;
        this.isSellable = isSellable;
    }
}