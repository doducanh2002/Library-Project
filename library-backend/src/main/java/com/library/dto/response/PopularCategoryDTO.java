package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularCategoryDTO {
    private Long categoryId;
    private String categoryName;
    private Long totalBooks;
    private Long totalLoans;
    private Long totalOrders;
    private Double percentage;
}