package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Author name is required")
    @Size(min = 2, max = 100, message = "Author name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Past(message = "Birth date must be in the past")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Past(message = "Death date must be in the past")
    @Column(name = "death_date")
    private LocalDate deathDate;

    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    @Column(length = 50)
    private String nationality;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    @Column(length = 255)
    private String website;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<BookAuthor> bookAuthors = new HashSet<>();

    // Business validation
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (deathDate != null && birthDate != null && deathDate.isBefore(birthDate)) {
            throw new IllegalStateException("Death date cannot be before birth date");
        }
    }

    // Helper methods
    public Set<Book> getBooks() {
        Set<Book> books = new HashSet<>();
        for (BookAuthor bookAuthor : bookAuthors) {
            books.add(bookAuthor.getBook());
        }
        return books;
    }
}