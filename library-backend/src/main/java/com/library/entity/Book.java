package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[0-9\\-X]{10,17}$", message = "Invalid ISBN format")
    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    @Min(value = 0, message = "Publication year must be positive")
    @Max(value = 9999, message = "Invalid publication year")
    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 500, message = "Cover image URL cannot exceed 500 characters")
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Size(max = 10, message = "Language code cannot exceed 10 characters")
    @Column(length = 10)
    private String language = "vi";

    @Min(value = 1, message = "Number of pages must be at least 1")
    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    @Size(max = 50, message = "Edition cannot exceed 50 characters")
    @Column(length = 50)
    private String edition;

    // Loan management fields
    @Min(value = 0, message = "Total copies for loan cannot be negative")
    @Column(name = "total_copies_for_loan", nullable = false)
    private Integer totalCopiesForLoan = 0;

    @Min(value = 0, message = "Available copies for loan cannot be negative")
    @Column(name = "available_copies_for_loan", nullable = false)
    private Integer availableCopiesForLoan = 0;

    @Column(name = "is_lendable", nullable = false)
    private Boolean isLendable = true;

    // Sales management fields
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock for sale cannot be negative")
    @Column(name = "stock_for_sale")
    private Integer stockForSale = 0;

    @Column(name = "is_sellable", nullable = false)
    private Boolean isSellable = false;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookAuthor> bookAuthors = new HashSet<>();

    // Metadata
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void addAuthor(Author author, String role) {
        BookAuthor bookAuthor = new BookAuthor(this, author, role);
        bookAuthors.add(bookAuthor);
    }

    public void removeAuthor(Author author) {
        bookAuthors.removeIf(bookAuthor -> bookAuthor.getAuthor().equals(author));
    }

    // Business validation methods
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        if (availableCopiesForLoan > totalCopiesForLoan) {
            throw new IllegalStateException("Available copies cannot exceed total copies");
        }
        
        if (isSellable && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalStateException("Sellable books must have a positive price");
        }
    }
}