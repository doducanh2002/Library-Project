package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherDetailDTO {
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
    private List<BookDTO> books;
    
    // Computed fields
    private Integer yearsInBusiness;
}