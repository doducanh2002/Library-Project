package com.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePublisherRequestDTO {
    
    @Size(min = 2, max = 100, message = "Publisher name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;
    
    @Size(max = 255, message = "Contact info cannot exceed 255 characters")
    private String contactInfo;
    
    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    private String website;
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @Min(value = 1400, message = "Established year must be after 1400")
    @Max(value = 9999, message = "Invalid established year")
    private Integer establishedYear;
}