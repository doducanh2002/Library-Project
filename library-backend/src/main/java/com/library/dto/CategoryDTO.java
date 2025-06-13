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
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long parentCategoryId;
    private String parentCategoryName;
    private Long bookCount;
}