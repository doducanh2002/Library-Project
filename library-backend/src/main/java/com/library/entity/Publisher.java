package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publishers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 2, max = 100, message = "Publisher name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Size(max = 255, message = "Contact info cannot exceed 255 characters")
    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    @Column(length = 255)
    private String website;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(length = 100)
    private String email;

    @Min(value = 1400, message = "Established year must be after 1400")
    @Max(value = 9999, message = "Invalid established year")
    @Column(name = "established_year")
    private Integer establishedYear;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Book> books = new ArrayList<>();

    // Business validation
    @PrePersist
    @PreUpdate
    private void validateEstablishedYear() {
        if (establishedYear != null) {
            int currentYear = Year.now().getValue();
            if (establishedYear > currentYear) {
                throw new IllegalStateException("Established year cannot be in the future");
            }
        }
    }
}