package com.library.service;

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
import com.library.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Book testBook;
    private CreateLoanRequestDTO createLoanRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loanService, "maxBooksPerUser", 5);
        ReflectionTestUtils.setField(loanService, "defaultLoanPeriodDays", 14);

        testBook = Book.builder()
                .id(1L)
                .title("Test Book")
                .isbn("978-0123456789")
                .isLendable(true)
                .availableCopiesForLoan(5)
                .build();

        createLoanRequest = new CreateLoanRequestDTO();
        createLoanRequest.setBookId(1L);
        createLoanRequest.setUserNotes("Need this book for research");
    }

    @Test
    void createLoanRequest_Success() {
        // Arrange
        Long userId = 1L;

        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(loanRepository.findByUserIdAndBookIdAndStatusIn(eq(userId), eq(1L), any()))
                .thenReturn(Optional.empty());

        Loan savedLoan = Loan.builder()
                .id(1L)
                .userId(userId)
                .book(testBook)
                .status(LoanStatus.REQUESTED)
                .build();

        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        // Act
        LoanDTO result = loanService.createLoanRequest(userId, createLoanRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(LoanStatus.REQUESTED, result.getStatus());

        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void createLoanRequest_MaxLoansExceeded() {
        // Arrange
        Long userId = 1L;
        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(5L);

        // Act & Assert
        MaxLoansExceededException exception = assertThrows(
                MaxLoansExceededException.class,
                () -> loanService.createLoanRequest(userId, createLoanRequest)
        );

        assertTrue(exception.getMessage().contains("maximum loan limit"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoanRequest_BookNotFound() {
        // Arrange
        Long userId = 1L;
        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> loanService.createLoanRequest(userId, createLoanRequest)
        );

        assertTrue(exception.getMessage().contains("Book not found"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoanRequest_BookNotAvailable() {
        // Arrange
        Long userId = 1L;
        testBook.setAvailableCopiesForLoan(0);

        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act & Assert
        BookNotAvailableException exception = assertThrows(
                BookNotAvailableException.class,
                () -> loanService.createLoanRequest(userId, createLoanRequest)
        );

        assertTrue(exception.getMessage().contains("not available"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoanRequest_BookNotLendable() {
        // Arrange
        Long userId = 1L;
        testBook.setIsLendable(false);

        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act & Assert
        BookNotAvailableException exception = assertThrows(
                BookNotAvailableException.class,
                () -> loanService.createLoanRequest(userId, createLoanRequest)
        );

        assertTrue(exception.getMessage().contains("not available"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoanRequest_DuplicateRequest() {
        // Arrange
        Long userId = 1L;
        Loan existingLoan = Loan.builder()
                .id(2L)
                .userId(userId)
                .book(testBook)
                .status(LoanStatus.REQUESTED)
                .build();

        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(loanRepository.findByUserIdAndBookIdAndStatusIn(eq(userId), eq(1L), any()))
                .thenReturn(Optional.of(existingLoan));

        // Act & Assert
        DuplicateLoanRequestException exception = assertThrows(
                DuplicateLoanRequestException.class,
                () -> loanService.createLoanRequest(userId, createLoanRequest)
        );

        assertTrue(exception.getMessage().contains("already has an active loan"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void getUserActiveLoanCount_Success() {
        // Arrange
        Long userId = 1L;
        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(3L);

        // Act
        long result = loanService.getUserActiveLoanCount(userId);

        // Assert
        assertEquals(3L, result);
        verify(loanRepository).countByUserIdAndStatusIn(eq(userId), any());
    }

    @Test
    void canUserBorrowMoreBooks_CanBorrow() {
        // Arrange
        Long userId = 1L;
        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(3L);

        // Act
        boolean result = loanService.canUserBorrowMoreBooks(userId);

        // Assert
        assertTrue(result);
    }

    @Test
    void canUserBorrowMoreBooks_CannotBorrow() {
        // Arrange
        Long userId = 1L;
        when(loanRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(5L);

        // Act
        boolean result = loanService.canUserBorrowMoreBooks(userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void getLoanDetails_Success() {
        // Arrange
        Long loanId = 1L;
        Loan loan = Loan.builder()
                .id(loanId)
                .userId(1L)
                .book(testBook)
                .status(LoanStatus.REQUESTED)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        // Act
        LoanDTO result = loanService.getLoanDetails(loanId);

        // Assert
        assertNotNull(result);
        assertEquals(loanId, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(LoanStatus.REQUESTED, result.getStatus());
    }

    @Test
    void getLoanDetails_NotFound() {
        // Arrange
        Long loanId = 1L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loanService.getLoanDetails(loanId)
        );

        assertTrue(exception.getMessage().contains("Loan not found"));
    }
}