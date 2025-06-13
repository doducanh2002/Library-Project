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
public class AdminLoanDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private String userFullName;
    private String userEmail;
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
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private Long returnedTo;
    private String returnedToName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Calculated fields for admin view
    private boolean isOverdue;
    private long daysOverdue;
    private long daysUntilDue;
    private String urgencyLevel;
    private boolean requiresAction; // For loans that need librarian attention
    private String actionRequired; // Description of what action is needed
}