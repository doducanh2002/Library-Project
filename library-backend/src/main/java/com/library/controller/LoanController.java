package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.CreateLoanRequestDTO;
import com.library.dto.LoanDTO;
import com.library.dto.LoanHistoryDTO;
import com.library.dto.CurrentLoanDTO;
import com.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Loan Management", description = "APIs for managing book loans")
//@CrossOrigin(origins = "*")
public class LoanController {
    
    private final LoanService loanService;
    
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }
    
    @PostMapping("/loans/request")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a loan request", description = "Submit a request to borrow a book (USER role required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Loan request created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Duplicate loan request or loan limit exceeded")
    })
    public BaseResponse<LoanDTO> createLoanRequest(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Validated CreateLoanRequestDTO request) {
        
        log.info("User {} requesting to borrow book {}", userId, request.getBookId());
        LoanDTO loan = loanService.createLoanRequest(userId, request);
        return BaseResponse.success(loan);
    }
    
    @GetMapping("/loans/my-loans")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's loan history", description = "Retrieve all loans for the authenticated user (USER role required)")
    public BaseResponse<Page<LoanDTO>> getUserLoans(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting loans for user {}", userId);
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<LoanDTO> loans = loanService.getUserLoans(userId, pageable);
        return BaseResponse.success(loans);
    }
    
    @GetMapping("/loans/my-loans/current")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current active loans", description = "Retrieve user's currently borrowed books (USER role required)")
    public BaseResponse<Page<CurrentLoanDTO>> getCurrentLoans(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting current loans for user {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<CurrentLoanDTO> loans = loanService.getUserCurrentLoans(userId, pageable);
        return BaseResponse.success(loans);
    }
    
    @GetMapping("/loans/{loanId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get loan details", description = "Retrieve detailed information about a specific loan (USER role required)")
    public BaseResponse<LoanDTO> getLoanDetails(
            @Parameter(description = "Loan ID") @PathVariable Long loanId) {
        
        log.info("Getting loan details for loan {}", loanId);
        LoanDTO loan = loanService.getLoanDetails(loanId);
        return BaseResponse.success(loan);
    }
    
    @GetMapping("/loans/can-borrow")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check if user can borrow more books", description = "Check if the user has reached their borrowing limit (USER role required)")
    public BaseResponse<Boolean> canUserBorrowMoreBooks(
            @RequestAttribute("userId") Long userId) {
        
        log.info("Checking borrow eligibility for user {}", userId);
        boolean canBorrow = loanService.canUserBorrowMoreBooks(userId);
        long activeLoanCount = loanService.getUserActiveLoanCount(userId);
        
        return BaseResponse.success(canBorrow, String.format("User has %d active loans", activeLoanCount));
    }
    
    @GetMapping("/loans/my-history")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get detailed loan history", description = "Retrieve user's complete loan history with calculations (USER role required)")
    public BaseResponse<Page<LoanHistoryDTO>> getUserLoanHistory(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting loan history for user {}", userId);
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<LoanHistoryDTO> loanHistory = loanService.getUserLoanHistory(userId, pageable);
        return BaseResponse.success(loanHistory);
    }
    
    @GetMapping("/loans/current/{loanId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current loan details", description = "Retrieve detailed information about a current loan with due date calculations (USER role required)")
    public BaseResponse<CurrentLoanDTO> getCurrentLoanDetails(
            @Parameter(description = "Loan ID") @PathVariable Long loanId) {
        
        log.info("Getting current loan details for loan {}", loanId);
        CurrentLoanDTO currentLoan = loanService.getCurrentLoanDetails(loanId);
        return BaseResponse.success(currentLoan);
    }
}