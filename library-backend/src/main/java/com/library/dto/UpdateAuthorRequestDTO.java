package com.library.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAuthorRequestDTO {
    
    @Size(min = 2, max = 100, message = "Author name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Biography cannot exceed 1000 characters")
    private String biography;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    @Past(message = "Death date must be in the past")
    private LocalDate deathDate;
    
    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    private String nationality;
    
    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    private String website;
}