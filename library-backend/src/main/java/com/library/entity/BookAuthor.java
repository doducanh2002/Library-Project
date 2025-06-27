package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "book_authors")
@Data
@NoArgsConstructor
@IdClass(BookAuthor.BookAuthorId.class)
public class BookAuthor {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @NotNull(message = "Book is required")
    private Book book;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "Author is required")
    private Author author;

    @Column(name = "author_role", length = 50)
    private String authorRole = "AUTHOR";

    // Constructor for convenience
    public BookAuthor(Book book, Author author, String role) {
        this.book = book;
        this.author = author;
        this.authorRole = role != null ? role : "AUTHOR";
    }

    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookAuthorId implements Serializable {
        private Long book;
        private Long author;
    }

    // Business validation
    @PrePersist
    @PreUpdate
    private void validateAuthorRole() {
        if (authorRole != null && 
            !authorRole.equals("AUTHOR") && 
            !authorRole.equals("CO_AUTHOR") && 
            !authorRole.equals("EDITOR") && 
            !authorRole.equals("TRANSLATOR")) {
            throw new IllegalStateException("Invalid author role: " + authorRole);
        }
    }
}