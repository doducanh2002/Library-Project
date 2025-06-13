package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDetailDTO {
    private Long id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String nationality;
    private String website;
    private LocalDateTime createdAt;
    
    // Statistics
    private Long bookCount;
    private List<BookDTO> books;
    
    // Computed fields
    private Integer age;
    private Boolean isAlive;
}