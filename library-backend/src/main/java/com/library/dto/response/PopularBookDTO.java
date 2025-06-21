package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularBookDTO {
    private Long bookId;
    private String title;
    private String isbn;
    private String authorName;
    private Long totalLoans;
    private Long totalOrders;
    private Double avgRating;
    private String coverImageUrl;
}