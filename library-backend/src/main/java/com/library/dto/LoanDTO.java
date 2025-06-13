package com.library.dto;

import com.library.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDTO {
    
    private Long id;
    private Long userId;
    private BookDTO book;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;
    private BigDecimal fineAmount;
    private Boolean finePaid;
    private String userNotes;
    private String notesByLibrarian;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private boolean isOverdue;
    private long daysUntilDue;
    private long daysOverdue;
}