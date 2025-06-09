package com.library.service.impl;

import com.library.dto.CreateLoanRequestDTO;
import com.library.dto.LoanDTO;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.LoanStatus;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateLoanRequestException;
import com.library.exception.MaxLoansExceededException;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoanServiceImpl implements LoanService {
    
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    
    @Value("${library.loan.max-books-per-user:5}")
    private int maxBooksPerUser;
    
    @Value("${library.loan.default-loan-period-days:14}")
    private int defaultLoanPeriodDays;
    
    private static final List<LoanStatus> ACTIVE_LOAN_STATUSES = Arrays.asList(
        LoanStatus.REQUESTED, 
        LoanStatus.APPROVED, 
        LoanStatus.BORROWED
    );
    
    @Override
    public LoanDTO createLoanRequest(Long userId, CreateLoanRequestDTO request) {
        log.info("Creating loan request for user {} and book {}", userId, request.getBookId());
        
        // Check if user has reached loan limit
        if (!canUserBorrowMoreBooks(userId)) {
            throw new MaxLoansExceededException(
                String.format("User has reached the maximum loan limit of %d books", maxBooksPerUser)
            );
        }
        
        // Check if book exists and is available
        Book book = bookRepository.findById(request.getBookId())
            .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + request.getBookId()));
        
        if (!book.getIsLendable() || book.getAvailableCopiesForLoan() <= 0) {
            throw new BookNotAvailableException("Book is not available for loan");
        }
        
        // Check for duplicate active loan request
        loanRepository.findByUserIdAndBookIdAndStatusIn(userId, request.getBookId(), ACTIVE_LOAN_STATUSES)
            .ifPresent(loan -> {
                throw new DuplicateLoanRequestException(
                    "User already has an active loan request for this book"
                );
            });
        
        // Create loan entity
        Loan loan = Loan.builder()
            .userId(userId)
            .book(book)
            .userNotes(request.getUserNotes())
            .dueDate(LocalDateTime.now().plusDays(defaultLoanPeriodDays))
            .build();
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan request created successfully with id: {}", savedLoan.getId());
        
        return mapToDTO(savedLoan);
    }
    
    @Override
    public Page<LoanDTO> getUserLoans(Long userId, Pageable pageable) {
        return loanRepository.findByUserId(userId, pageable)
            .map(this::mapToDTO);
    }
    
    @Override
    public Page<LoanDTO> getUserLoansByStatus(Long userId, String status, Pageable pageable) {
        LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
        return loanRepository.findByUserIdAndStatus(userId, loanStatus, pageable)
            .map(this::mapToDTO);
    }
    
    @Override
    public LoanDTO getLoanDetails(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));
        return mapToDTO(loan);
    }
    
    @Override
    public long getUserActiveLoanCount(Long userId) {
        return loanRepository.countByUserIdAndStatusIn(userId, ACTIVE_LOAN_STATUSES);
    }
    
    @Override
    public boolean canUserBorrowMoreBooks(Long userId) {
        long activeLoans = getUserActiveLoanCount(userId);
        return activeLoans < maxBooksPerUser;
    }
    
    private LoanDTO mapToDTO(Loan loan) {
        LoanDTO dto = LoanDTO.builder()
            .id(loan.getId())
            .userId(loan.getUserId())
            .book(bookMapper.toDTO(loan.getBook()))
            .loanDate(loan.getLoanDate())
            .dueDate(loan.getDueDate())
            .returnDate(loan.getReturnDate())
            .status(loan.getStatus())
            .fineAmount(loan.getFineAmount())
            .finePaid(loan.getFinePaid())
            .userNotes(loan.getUserNotes())
            .notesByLibrarian(loan.getNotesByLibrarian())
            .createdAt(loan.getCreatedAt())
            .updatedAt(loan.getUpdatedAt())
            .build();
        
        // Calculate additional fields
        LocalDateTime now = LocalDateTime.now();
        if (loan.getStatus() == LoanStatus.BORROWED && loan.getDueDate() != null) {
            dto.setIsOverdue(now.isAfter(loan.getDueDate()));
            if (dto.isOverdue()) {
                dto.setDaysOverdue(ChronoUnit.DAYS.between(loan.getDueDate(), now));
            } else {
                dto.setDaysUntilDue(ChronoUnit.DAYS.between(now, loan.getDueDate()));
            }
        }
        
        return dto;
    }
}