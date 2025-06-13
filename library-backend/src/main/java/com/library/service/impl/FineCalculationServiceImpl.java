package com.library.service.impl;

import com.library.entity.Loan;
import com.library.entity.LoanStatus;
import com.library.service.FineCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class FineCalculationServiceImpl implements FineCalculationService {
    
    @Value("${library.loan.fine-per-day:5000}")
    private BigDecimal finePerDay;
    
    @Value("${library.loan.max-fine-amount:50000}")
    private BigDecimal maxFineAmount;
    
    @Value("${library.loan.grace-period-days:0}")
    private int gracePeriodDays;
    
    @Override
    public BigDecimal calculateFine(Loan loan) {
        if (!isFineApplicable(loan)) {
            return BigDecimal.ZERO;
        }
        
        LocalDateTime effectiveReturnDate = loan.getReturnDate() != null 
            ? loan.getReturnDate() 
            : LocalDateTime.now();
            
        return calculateFine(loan.getDueDate(), effectiveReturnDate);
    }
    
    @Override
    public BigDecimal calculateFine(LocalDateTime dueDate, LocalDateTime returnDate) {
        if (dueDate == null || returnDate == null) {
            return BigDecimal.ZERO;
        }
        
        // Check if return is before due date
        if (!returnDate.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }
        
        // Calculate overdue days
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        
        // Apply grace period
        long fineDays = Math.max(0, overdueDays - gracePeriodDays);
        
        if (fineDays <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate fine
        BigDecimal totalFine = finePerDay.multiply(BigDecimal.valueOf(fineDays));
        
        // Apply maximum fine limit
        if (totalFine.compareTo(maxFineAmount) > 0) {
            log.info("Fine amount {} exceeds maximum {}. Capping at maximum.", 
                    totalFine, maxFineAmount);
            totalFine = maxFineAmount;
        }
        
        log.info("Calculated fine: {} VND for {} overdue days (grace period: {} days)", 
                totalFine, overdueDays, gracePeriodDays);
        
        return totalFine;
    }
    
    @Override
    public BigDecimal calculateDailyFineRate() {
        return finePerDay;
    }
    
    @Override
    public boolean isFineApplicable(Loan loan) {
        if (loan == null || loan.getDueDate() == null) {
            return false;
        }
        
        // Fine is applicable for returned or overdue loans
        return loan.getStatus() == LoanStatus.RETURNED || 
               loan.getStatus() == LoanStatus.OVERDUE ||
               (loan.getStatus() == LoanStatus.BORROWED && 
                LocalDateTime.now().isAfter(loan.getDueDate()));
    }
}