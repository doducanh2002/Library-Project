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
public class CurrentLoanDTO {
    
    private Long id;
    private BookDTO book;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LoanStatus status;
    private String statusDisplayName;
    private BigDecimal fineAmount;
    private Boolean finePaid;
    private String userNotes;
    private String notesByLibrarian;
    
    // Calculated fields for current loans
    private boolean isOverdue;
    private long daysUntilDue;
    private long daysOverdue;
    private long totalLoanDays;
    private String dueDateFormatted;
    private String urgencyLevel; // LOW, MEDIUM, HIGH based on days until due
    
    // Renewal information
    private boolean canRenew;
    private int renewalCount;
    private int maxRenewals;
}