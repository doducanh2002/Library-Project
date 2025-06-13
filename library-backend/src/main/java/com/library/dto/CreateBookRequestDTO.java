package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequestDTO {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[0-9\\-X]{10,17}$", message = "Invalid ISBN format")
    private String isbn;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    private String coverImageUrl;
    
    @NotBlank(message = "Language is required")
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language = "vi";
    
    @Min(value = 1, message = "Number of pages must be at least 1")
    private Integer numberOfPages;
    
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 2100, message = "Publication year cannot be in far future")
    private Integer publicationYear;
    
    @Size(max = 50, message = "Edition must not exceed 50 characters")
    private String edition;
    
    // Sales configuration
    @DecimalMin(value = "0", message = "Price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock for sale cannot be negative")
    private Integer stockForSale = 0;
    
    private Boolean isSellable = false;
    
    // Loan configuration
    @Min(value = 0, message = "Total copies for loan cannot be negative")
    private Integer totalCopiesForLoan = 0;
    
    @Min(value = 0, message = "Available copies for loan cannot be negative")
    private Integer availableCopiesForLoan = 0;
    
    private Boolean isLendable = true;
    
    // Relationships
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    @NotNull(message = "Publisher is required")
    private Long publisherId;
    
    @NotEmpty(message = "At least one author is required")
    private List<Long> authorIds;
    
    @AssertTrue(message = "Available copies cannot exceed total copies for loan")
    private boolean isValidLoanCopies() {
        if (availableCopiesForLoan == null || totalCopiesForLoan == null) {
            return true;
        }
        return availableCopiesForLoan <= totalCopiesForLoan;
    }
    
    @AssertTrue(message = "If sellable, price must be greater than 0")
    private boolean isValidSellablePrice() {
        if (Boolean.TRUE.equals(isSellable)) {
            return price != null && price.compareTo(BigDecimal.ZERO) > 0;
        }
        return true;
    }
}