package com.library.controller;

import com.library.dto.AdminLoanDTO;
import com.library.dto.ApproveLoanRequestDTO;
import com.library.dto.BaseResponse;
import com.library.dto.LoanDTO;
import com.library.dto.ProcessReturnRequestDTO;
import com.library.dto.RejectLoanRequestDTO;
import com.library.service.LibrarianLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Loan Management", description = "APIs for librarians to manage book loans")
@SecurityRequirement(name = "bearerAuth")
public class AdminLoanController {
    
    private final LibrarianLoanService librarianLoanService;
    
    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all loans", description = "Retrieve all loans with admin view")
    public ResponseEntity<BaseResponse<Page<AdminLoanDTO>>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AdminLoanDTO> loans = librarianLoanService.getAllLoans(pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<AdminLoanDTO>>builder()
                .data(loans)
                .message("All loans retrieved successfully")
                .build()
        );
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get loans by status", description = "Retrieve loans filtered by status")
    public ResponseEntity<BaseResponse<Page<AdminLoanDTO>>> getLoansByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AdminLoanDTO> loans = librarianLoanService.getLoansByStatus(status, pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<AdminLoanDTO>>builder()
                .data(loans)
                .message(String.format("Loans with status '%s' retrieved successfully", status))
                .build()
        );
    }
    
    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get loan details", description = "Retrieve detailed loan information for admin")
    public ResponseEntity<BaseResponse<AdminLoanDTO>> getAdminLoanDetails(
            @PathVariable Long loanId) {
        
        AdminLoanDTO loan = librarianLoanService.getAdminLoanDetails(loanId);
        
        return ResponseEntity.ok(
            BaseResponse.<AdminLoanDTO>builder()
                .data(loan)
                .message("Loan details retrieved successfully")
                .build()
        );
    }
    
    @PostMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Approve loan request", description = "Approve a pending loan request")
    public ResponseEntity<BaseResponse<LoanDTO>> approveLoan(
            @PathVariable Long loanId,
            @RequestAttribute("userId") Long librarianId,
            @Valid @RequestBody ApproveLoanRequestDTO request) {
        
        log.info("Librarian {} approving loan {}", librarianId, loanId);
        LoanDTO loan = librarianLoanService.approveLoan(loanId, librarianId, request);
        
        return ResponseEntity.ok(
            BaseResponse.<LoanDTO>builder()
                .data(loan)
                .message("Loan approved successfully")
                .build()
        );
    }
    
    @PostMapping("/{loanId}/reject")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Reject loan request", description = "Reject a pending loan request")
    public ResponseEntity<BaseResponse<LoanDTO>> rejectLoan(
            @PathVariable Long loanId,
            @RequestAttribute("userId") Long librarianId,
            @Valid @RequestBody RejectLoanRequestDTO request) {
        
        log.info("Librarian {} rejecting loan {}", librarianId, loanId);
        LoanDTO loan = librarianLoanService.rejectLoan(loanId, librarianId, request);
        
        return ResponseEntity.ok(
            BaseResponse.<LoanDTO>builder()
                .data(loan)
                .message("Loan rejected successfully")
                .build()
        );
    }
    
    @PostMapping("/{loanId}/return")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Process book return", description = "Process the return of a borrowed book")
    public ResponseEntity<BaseResponse<LoanDTO>> processBookReturn(
            @PathVariable Long loanId,
            @RequestAttribute("userId") Long librarianId,
            @Valid @RequestBody ProcessReturnRequestDTO request) {
        
        log.info("Librarian {} processing return for loan {}", librarianId, loanId);
        LoanDTO loan = librarianLoanService.processBookReturn(loanId, librarianId, request);
        
        return ResponseEntity.ok(
            BaseResponse.<LoanDTO>builder()
                .data(loan)
                .message("Book return processed successfully")
                .build()
        );
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get overdue loans", description = "Retrieve all overdue loans requiring attention")
    public ResponseEntity<BaseResponse<List<AdminLoanDTO>>> getOverdueLoans() {
        
        List<AdminLoanDTO> overdueLoans = librarianLoanService.getOverdueLoans();
        
        return ResponseEntity.ok(
            BaseResponse.<List<AdminLoanDTO>>builder()
                .data(overdueLoans)
                .message(String.format("Found %d overdue loans", overdueLoans.size()))
                .build()
        );
    }
    
    @PostMapping("/update-overdue")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update overdue status", description = "Update loan statuses and calculate fines for overdue books")
    public ResponseEntity<BaseResponse<Void>> updateOverdueLoans() {
        
        librarianLoanService.updateOverdueLoans();
        
        return ResponseEntity.ok(
            BaseResponse.<Void>builder()
                .message("Overdue loans updated successfully")
                .build()
        );
    }
    
    @PostMapping("/{loanId}/calculate-fine")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Calculate and update fine", description = "Calculate and update fine amount for a loan")
    public ResponseEntity<BaseResponse<LoanDTO>> calculateAndUpdateFine(
            @PathVariable Long loanId) {
        
        LoanDTO loan = librarianLoanService.calculateAndUpdateFine(loanId);
        
        return ResponseEntity.ok(
            BaseResponse.<LoanDTO>builder()
                .data(loan)
                .message("Fine calculated and updated successfully")
                .build()
        );
    }
    
    @GetMapping("/unpaid-fines")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get loans with unpaid fines", description = "Retrieve all loans with outstanding fine amounts")
    public ResponseEntity<BaseResponse<List<AdminLoanDTO>>> getLoansWithUnpaidFines() {
        
        List<AdminLoanDTO> loansWithFines = librarianLoanService.getLoansWithUnpaidFines();
        
        return ResponseEntity.ok(
            BaseResponse.<List<AdminLoanDTO>>builder()
                .data(loansWithFines)
                .message(String.format("Found %d loans with unpaid fines", loansWithFines.size()))
                .build()
        );
    }
}