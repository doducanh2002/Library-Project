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
public class CategoryDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    // Parent category info
    private Long parentCategoryId;
    private String parentCategoryName;
    
    // Subcategories
    private List<CategoryDTO> subcategories;
    
    // Statistics
    private Long bookCount;
    private Long totalBooksInSubcategories;
}