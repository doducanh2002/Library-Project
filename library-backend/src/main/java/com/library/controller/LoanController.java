package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.CreateLoanRequestDTO;
import com.library.dto.LoanDTO;
import com.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Management", description = "APIs for managing book loans")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {
    
    private final LoanService loanService;
    
    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a loan request", description = "Submit a request to borrow a book")
    public ResponseEntity<BaseResponse<LoanDTO>> createLoanRequest(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateLoanRequestDTO request) {
        
        log.info("User {} requesting to borrow book {}", userId, request.getBookId());
        LoanDTO loan = loanService.createLoanRequest(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            BaseResponse.<LoanDTO>builder()
                .data(loan)
                .message("Loan request created successfully")
                .build()
        );
    }
    
    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's loan history", description = "Retrieve all loans for the authenticated user")
    public ResponseEntity<BaseResponse<Page<LoanDTO>>> getUserLoans(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<LoanDTO> loans = loanService.getUserLoans(userId, pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<LoanDTO>>builder()
                .data(loans)
                .message("User loans retrieved successfully")
                .build()
        );
    }
    
    @GetMapping("/my-loans/current")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current active loans", description = "Retrieve user's currently borrowed books")
    public ResponseEntity<BaseResponse<Page<LoanDTO>>> getCurrentLoans(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanDTO> loans = loanService.getUserLoansByStatus(userId, "BORROWED", pageable);
        
        return ResponseEntity.ok(
            BaseResponse.<Page<LoanDTO>>builder()
                .data(loans)
                .message("Current loans retrieved successfully")
                .build()
        );
    }
    
    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get loan details", description = "Retrieve detailed information about a specific loan")
    public ResponseEntity<BaseResponse<LoanDTO>> getLoanDetails(
            @PathVariable Long loanId) {
        
        LoanDTO loan = loanService.getLoanDetails(loanId);
        
        return ResponseEntity.ok(
            BaseResponse.<LoanDTO>builder()
                .data(loan)
                .message("Loan details retrieved successfully")
                .build()
        );
    }
    
    @GetMapping("/can-borrow")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check if user can borrow more books", description = "Check if the user has reached their borrowing limit")
    public ResponseEntity<BaseResponse<Boolean>> canUserBorrowMoreBooks(
            @RequestAttribute("userId") Long userId) {
        
        boolean canBorrow = loanService.canUserBorrowMoreBooks(userId);
        long activeLoanCount = loanService.getUserActiveLoanCount(userId);
        
        return ResponseEntity.ok(
            BaseResponse.<Boolean>builder()
                .data(canBorrow)
                .message(String.format("User has %d active loans", activeLoanCount))
                .build()
        );
    }
}