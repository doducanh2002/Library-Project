package com.library.service;

import com.library.dto.CreateLoanRequestDTO;
import com.library.dto.LoanDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanService {
    
    LoanDTO createLoanRequest(Long userId, CreateLoanRequestDTO request);
    
    Page<LoanDTO> getUserLoans(Long userId, Pageable pageable);
    
    Page<LoanDTO> getUserLoansByStatus(Long userId, String status, Pageable pageable);
    
    LoanDTO getLoanDetails(Long loanId);
    
    long getUserActiveLoanCount(Long userId);
    
    boolean canUserBorrowMoreBooks(Long userId);
}