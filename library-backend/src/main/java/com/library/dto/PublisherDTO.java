package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherDTO {
    private Long id;
    private String name;
    private String address;
    private String contactInfo;
    private String website;
    private String email;
    private Integer establishedYear;
    private LocalDateTime createdAt;
    
    // Statistics
    private Long bookCount;
    
    // Computed fields
    private Integer yearsInBusiness;
}