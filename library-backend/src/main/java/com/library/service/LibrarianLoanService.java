package com.library.service;

import com.library.dto.AdminLoanDTO;
import com.library.dto.ApproveLoanRequestDTO;
import com.library.dto.LoanDTO;
import com.library.dto.ProcessReturnRequestDTO;
import com.library.dto.RejectLoanRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LibrarianLoanService {
    
    // Loan approval/rejection
    LoanDTO approveLoan(Long loanId, Long librarianId, ApproveLoanRequestDTO request);
    
    LoanDTO rejectLoan(Long loanId, Long librarianId, RejectLoanRequestDTO request);
    
    // Book return processing
    LoanDTO processBookReturn(Long loanId, Long librarianId, ProcessReturnRequestDTO request);
    
    // Loan management
    Page<AdminLoanDTO> getAllLoans(Pageable pageable);
    
    Page<AdminLoanDTO> getLoansByStatus(String status, Pageable pageable);
    
    AdminLoanDTO getAdminLoanDetails(Long loanId);
    
    // Overdue management
    List<AdminLoanDTO> getOverdueLoans();
    
    void updateOverdueLoans();
    
    // Fine management
    LoanDTO calculateAndUpdateFine(Long loanId);
    
    List<AdminLoanDTO> getLoansWithUnpaidFines();
}