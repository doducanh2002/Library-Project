package com.library.mapper;

import com.library.entity.LoanStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LoanStatusMapper {
    
    public static String getDisplayName(LoanStatus status) {
        if (status == null) {
            return "Unknown";
        }
        
        return switch (status) {
            case REQUESTED -> "Requested";
            case APPROVED -> "Approved";
            case BORROWED -> "Borrowed";
            case RETURNED -> "Returned";
            case OVERDUE -> "Overdue";
            case CANCELLED -> "Cancelled";
        };
    }
    
    public static String getUrgencyLevel(LocalDateTime dueDate) {
        if (dueDate == null) {
            return "UNKNOWN";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long daysUntilDue = ChronoUnit.DAYS.between(now, dueDate);
        
        if (daysUntilDue < 0) {
            return "OVERDUE";
        } else if (daysUntilDue <= 1) {
            return "HIGH"; // Due within 1 day
        } else if (daysUntilDue <= 3) {
            return "MEDIUM"; // Due within 3 days
        } else {
            return "LOW"; // More than 3 days remaining
        }
    }
    
    public static boolean isOverdue(LocalDateTime dueDate) {
        if (dueDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate);
    }
    
    public static long calculateDaysUntilDue(LocalDateTime dueDate) {
        if (dueDate == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        return ChronoUnit.DAYS.between(now, dueDate);
    }
    
    public static long calculateDaysOverdue(LocalDateTime dueDate) {
        if (dueDate == null || !isOverdue(dueDate)) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        return ChronoUnit.DAYS.between(dueDate, now);
    }
    
    public static long calculateLoanDuration(LocalDateTime loanDate, LocalDateTime returnDate) {
        if (loanDate == null) {
            return 0;
        }
        LocalDateTime endDate = returnDate != null ? returnDate : LocalDateTime.now();
        return ChronoUnit.DAYS.between(loanDate, endDate);
    }
    
    public static boolean canRenewLoan(LoanStatus status, int currentRenewals, int maxRenewals, 
                                      LocalDateTime dueDate) {
        // Can only renew if:
        // 1. Status is BORROWED
        // 2. Not exceeded max renewals
        // 3. Not overdue (or within grace period)
        return status == LoanStatus.BORROWED 
               && currentRenewals < maxRenewals 
               && !isOverdue(dueDate);
    }
}