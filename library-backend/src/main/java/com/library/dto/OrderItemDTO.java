package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private Long id;
    private Long bookId;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal itemTotalPrice;

    // Snapshot data
    private String bookTitle;
    private String bookIsbn;

    // Current book info (if book still exists)
    private String bookCoverImageUrl;
    private String bookDescription;
    private Boolean bookStillAvailable;
}