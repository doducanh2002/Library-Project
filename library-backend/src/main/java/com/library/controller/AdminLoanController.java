package com.library.controller;

import com.library.dto.AdminLoanDTO;
import com.library.dto.ApproveLoanRequestDTO;
import com.library.dto.BaseResponse;
import com.library.dto.LoanDTO;
import com.library.dto.ProcessReturnRequestDTO;
import com.library.dto.RejectLoanRequestDTO;
import com.library.service.LibrarianLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Admin Loan Management", description = "APIs for librarians to manage book loans")
@CrossOrigin(origins = "*")
public class AdminLoanController {
    
    private final LibrarianLoanService librarianLoanService;
    
    public AdminLoanController(LibrarianLoanService librarianLoanService) {
        this.librarianLoanService = librarianLoanService;
    }
    
    @GetMapping("/admin/loans")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all loans", description = "Retrieve all loans with admin view (LIBRARIAN role required)")
    public BaseResponse<Page<AdminLoanDTO>> getAllLoans(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting all loans for admin");
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AdminLoanDTO> loans = librarianLoanService.getAllLoans(pageable);
        return BaseResponse.success(loans);
    }
    
    @GetMapping("/admin/loans/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get loans by status", description = "Retrieve loans filtered by status (LIBRARIAN role required)")
    public BaseResponse<Page<AdminLoanDTO>> getLoansByStatus(
            @Parameter(description = "Loan status") @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting loans by status: {}", status);
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AdminLoanDTO> loans = librarianLoanService.getLoansByStatus(status, pageable);
        return BaseResponse.success(loans);
    }
    
    @GetMapping("/admin/loans/{loanId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get loan details", description = "Retrieve detailed loan information for admin (LIBRARIAN role required)")
    public BaseResponse<AdminLoanDTO> getAdminLoanDetails(
            @Parameter(description = "Loan ID") @PathVariable Long loanId) {
        
        log.info("Getting admin loan details for loan {}", loanId);
        AdminLoanDTO loan = librarianLoanService.getAdminLoanDetails(loanId);
        return BaseResponse.success(loan);
    }
    
    @PostMapping("/admin/loans/{loanId}/approve")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Approve loan request", description = "Approve a pending loan request (LIBRARIAN role required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan approved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid loan state or book not available"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public BaseResponse<LoanDTO> approveLoan(
            @Parameter(description = "Loan ID") @PathVariable Long loanId,
            @RequestAttribute("userId") Long librarianId,
            @RequestBody @Validated ApproveLoanRequestDTO request) {
        
        log.info("Librarian {} approving loan {}", librarianId, loanId);
        LoanDTO loan = librarianLoanService.approveLoan(loanId, librarianId, request);
        return BaseResponse.success(loan);
    }
    
    @PostMapping("/admin/loans/{loanId}/reject")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Reject loan request", description = "Reject a pending loan request (LIBRARIAN role required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid loan state"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public BaseResponse<LoanDTO> rejectLoan(
            @Parameter(description = "Loan ID") @PathVariable Long loanId,
            @RequestAttribute("userId") Long librarianId,
            @RequestBody @Validated RejectLoanRequestDTO request) {
        
        log.info("Librarian {} rejecting loan {}", librarianId, loanId);
        LoanDTO loan = librarianLoanService.rejectLoan(loanId, librarianId, request);
        return BaseResponse.success(loan);
    }
    
    @PostMapping("/admin/loans/{loanId}/return")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Process book return", description = "Process the return of a borrowed book (LIBRARIAN role required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book return processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid loan state"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public BaseResponse<LoanDTO> processBookReturn(
            @Parameter(description = "Loan ID") @PathVariable Long loanId,
            @RequestAttribute("userId") Long librarianId,
            @RequestBody @Validated ProcessReturnRequestDTO request) {
        
        log.info("Librarian {} processing return for loan {}", librarianId, loanId);
        LoanDTO loan = librarianLoanService.processBookReturn(loanId, librarianId, request);
        return BaseResponse.success(loan);
    }
    
    @GetMapping("/admin/loans/overdue")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get overdue loans", description = "Retrieve all overdue loans requiring attention (LIBRARIAN role required)")
    public BaseResponse<List<AdminLoanDTO>> getOverdueLoans() {
        
        log.info("Getting overdue loans");
        List<AdminLoanDTO> overdueLoans = librarianLoanService.getOverdueLoans();
        return BaseResponse.success(overdueLoans, String.format("Found %d overdue loans", overdueLoans.size()));
    }
    
    @PostMapping("/admin/loans/update-overdue")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update overdue status", description = "Update loan statuses and calculate fines for overdue books (LIBRARIAN role required)")
    public BaseResponse<String> updateOverdueLoans() {
        
        log.info("Updating overdue loans");
        librarianLoanService.updateOverdueLoans();
        return BaseResponse.success("Overdue loans updated successfully");
    }
    
    @PostMapping("/admin/loans/{loanId}/calculate-fine")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Calculate and update fine", description = "Calculate and update fine amount for a loan (LIBRARIAN role required)")
    public BaseResponse<LoanDTO> calculateAndUpdateFine(
            @Parameter(description = "Loan ID") @PathVariable Long loanId) {
        
        log.info("Calculating fine for loan {}", loanId);
        LoanDTO loan = librarianLoanService.calculateAndUpdateFine(loanId);
        return BaseResponse.success(loan);
    }
    
    @GetMapping("/admin/loans/unpaid-fines")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get loans with unpaid fines", description = "Retrieve all loans with outstanding fine amounts (LIBRARIAN role required)")
    public BaseResponse<List<AdminLoanDTO>> getLoansWithUnpaidFines() {
        
        log.info("Getting loans with unpaid fines");
        List<AdminLoanDTO> loansWithFines = librarianLoanService.getLoansWithUnpaidFines();
        return BaseResponse.success(loansWithFines, String.format("Found %d loans with unpaid fines", loansWithFines.size()));
    }
}