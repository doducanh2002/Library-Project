package com.library.service;

import com.library.dto.LoanHistoryDTO;
import com.library.dto.CurrentLoanDTO;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.LoanStatus;
import com.library.mapper.BookMapper;
import com.library.repository.LoanRepository;
import com.library.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanHistoryServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Book testBook;
    private Loan completedLoan;
    private Loan currentLoan;
    private Loan overdueLoan;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loanService, "maxRenewals", 2);

        testBook = Book.builder()
                .id(1L)
                .title("Test Book")
                .isbn("978-0123456789")
                .build();

        // Completed loan (returned)
        completedLoan = Loan.builder()
                .id(1L)
                .userId(1L)
                .book(testBook)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(6))
                .returnDate(LocalDateTime.now().minusDays(5))
                .status(LoanStatus.RETURNED)
                .userNotes("Great book!")
                .createdAt(LocalDateTime.now().minusDays(20))
                .build();

        // Current loan (borrowed, not overdue)
        currentLoan = Loan.builder()
                .id(2L)
                .userId(1L)
                .book(testBook)
                .loanDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(9))
                .status(LoanStatus.BORROWED)
                .userNotes("Reading now")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        // Overdue loan
        overdueLoan = Loan.builder()
                .id(3L)
                .userId(1L)
                .book(testBook)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(3))
                .status(LoanStatus.OVERDUE)
                .userNotes("Need more time")
                .createdAt(LocalDateTime.now().minusDays(20))
                .build();
    }

    @Test
    void getUserLoanHistory_Success() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Loan> loans = Arrays.asList(completedLoan, currentLoan, overdueLoan);
        Page<Loan> loanPage = new PageImpl<>(loans, pageable, loans.size());

        when(loanRepository.findByUserId(userId, pageable)).thenReturn(loanPage);

        // Act
        Page<LoanHistoryDTO> result = loanService.getUserLoanHistory(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        
        LoanHistoryDTO firstLoan = result.getContent().get(0);
        assertEquals(completedLoan.getId(), firstLoan.getId());
        assertEquals(LoanStatus.RETURNED, firstLoan.getStatus());
        assertTrue(firstLoan.isWasReturned());
        assertTrue(firstLoan.isWasOverdue()); // Returned after due date
        
        verify(loanRepository).findByUserId(userId, pageable);
    }

    @Test
    void getUserCurrentLoans_Success() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Loan> currentLoans = Arrays.asList(currentLoan, overdueLoan);

        when(loanRepository.findByUserIdAndStatusIn(eq(userId), any())).thenReturn(currentLoans);

        // Act
        Page<CurrentLoanDTO> result = loanService.getUserCurrentLoans(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        
        CurrentLoanDTO firstCurrentLoan = result.getContent().get(0);
        assertEquals(currentLoan.getId(), firstCurrentLoan.getId());
        assertEquals(LoanStatus.BORROWED, firstCurrentLoan.getStatus());
        assertFalse(firstCurrentLoan.isIsOverdue());
        assertTrue(firstCurrentLoan.getDaysUntilDue() > 0);
        assertEquals("LOW", firstCurrentLoan.getUrgencyLevel()); // More than 3 days remaining
        
        CurrentLoanDTO secondCurrentLoan = result.getContent().get(1);
        assertEquals(overdueLoan.getId(), secondCurrentLoan.getId());
        assertEquals(LoanStatus.OVERDUE, secondCurrentLoan.getStatus());
        assertTrue(secondCurrentLoan.isIsOverdue());
        assertTrue(secondCurrentLoan.getDaysOverdue() > 0);
        assertEquals("OVERDUE", secondCurrentLoan.getUrgencyLevel());
        
        verify(loanRepository).findByUserIdAndStatusIn(eq(userId), any());
    }

    @Test
    void getCurrentLoanDetails_Success() {
        // Arrange
        Long loanId = 2L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(currentLoan));

        // Act
        CurrentLoanDTO result = loanService.getCurrentLoanDetails(loanId);

        // Assert
        assertNotNull(result);
        assertEquals(loanId, result.getId());
        assertEquals(LoanStatus.BORROWED, result.getStatus());
        assertEquals("Borrowed", result.getStatusDisplayName());
        assertFalse(result.isIsOverdue());
        assertTrue(result.getDaysUntilDue() > 0);
        assertEquals("LOW", result.getUrgencyLevel());
        assertTrue(result.isCanRenew()); // Can renew since not overdue and within renewal limit
        assertEquals(2, result.getMaxRenewals());
        assertEquals(0, result.getRenewalCount());
        
        verify(loanRepository).findById(loanId);
    }

    @Test
    void getCurrentLoanDetails_NotFound() {
        // Arrange
        Long loanId = 999L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loanService.getCurrentLoanDetails(loanId)
        );
        
        assertTrue(exception.getMessage().contains("Loan not found"));
        verify(loanRepository).findById(loanId);
    }

    @Test
    void getUserCurrentLoans_EmptyResult() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Loan> emptyLoans = Arrays.asList();

        when(loanRepository.findByUserIdAndStatusIn(eq(userId), any())).thenReturn(emptyLoans);

        // Act
        Page<CurrentLoanDTO> result = loanService.getUserCurrentLoans(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        
        verify(loanRepository).findByUserIdAndStatusIn(eq(userId), any());
    }

    @Test
    void loanHistoryDTO_CalculatesFieldsCorrectly() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        // Create a loan that was returned late
        Loan lateLoan = Loan.builder()
                .id(4L)
                .userId(userId)
                .book(testBook)
                .loanDate(LocalDateTime.now().minusDays(30))
                .dueDate(LocalDateTime.now().minusDays(16))
                .returnDate(LocalDateTime.now().minusDays(10))
                .status(LoanStatus.RETURNED)
                .build();
        
        List<Loan> loans = Arrays.asList(lateLoan);
        Page<Loan> loanPage = new PageImpl<>(loans, pageable, loans.size());

        when(loanRepository.findByUserId(userId, pageable)).thenReturn(loanPage);

        // Act
        Page<LoanHistoryDTO> result = loanService.getUserLoanHistory(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        LoanHistoryDTO loanHistoryDTO = result.getContent().get(0);
        assertEquals(20, loanHistoryDTO.getLoanDurationDays()); // 30 - 10 = 20 days
        assertTrue(loanHistoryDTO.isWasOverdue()); // Returned after due date
        assertEquals(6, loanHistoryDTO.getDaysOverdue()); // 16 - 10 = 6 days late
        assertTrue(loanHistoryDTO.isWasReturned());
    }
}