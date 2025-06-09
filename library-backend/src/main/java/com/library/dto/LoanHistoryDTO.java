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
public class LoanHistoryDTO {
    
    private Long id;
    private BookDTO book;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;
    private String statusDisplayName;
    private BigDecimal fineAmount;
    private Boolean finePaid;
    private String userNotes;
    private String notesByLibrarian;
    private LocalDateTime createdAt;
    
    // Calculated fields
    private long loanDurationDays;
    private boolean wasOverdue;
    private long daysOverdue;
    private boolean wasReturned;
}