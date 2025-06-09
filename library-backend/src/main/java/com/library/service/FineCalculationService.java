package com.library.service;

import com.library.entity.Loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FineCalculationService {
    
    BigDecimal calculateFine(Loan loan);
    
    BigDecimal calculateFine(LocalDateTime dueDate, LocalDateTime returnDate);
    
    BigDecimal calculateDailyFineRate();
    
    boolean isFineApplicable(Loan loan);
}