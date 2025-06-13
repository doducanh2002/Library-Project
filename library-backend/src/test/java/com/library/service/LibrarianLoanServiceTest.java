//package com.library.service;
//
//import com.library.dto.AdminLoanDTO;
//import com.library.dto.ApproveLoanRequestDTO;
//import com.library.dto.LoanDTO;
//import com.library.dto.ProcessReturnRequestDTO;
//import com.library.dto.RejectLoanRequestDTO;
//import com.library.entity.Book;
//import com.library.entity.Loan;
//import com.library.entity.LoanStatus;
//import com.library.exception.BookNotAvailableException;
//import com.library.mapper.BookMapper;
//import com.library.repository.BookRepository;
//import com.library.repository.LoanRepository;
//import com.library.service.impl.LibrarianLoanServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class LibrarianLoanServiceTest {
//
//    @Mock
//    private LoanRepository loanRepository;
//
//    @Mock
//    private BookRepository bookRepository;
//
//    @Mock
//    private FineCalculationService fineCalculationService;
//
//    @Mock
//    private BookMapper bookMapper;
//
//    @InjectMocks
//    private LibrarianLoanServiceImpl librarianLoanService;
//
//    private Book testBook;
//    private Loan requestedLoan;
//    private Loan borrowedLoan;
//    private Long librarianId = 100L;
//
//    @BeforeEach
//    void setUp() {
//        ReflectionTestUtils.setField(librarianLoanService, "defaultLoanPeriodDays", 14);
//
//        testBook = Book.builder()
//                .id(1L)
//                .title("Test Book")
//                .isbn("978-0123456789")
//                .isLendable(true)
//                .availableCopiesForLoan(5)
//                .build();
//
//        requestedLoan = Loan.builder()
//                .id(1L)
//                .userId(1L)
//                .book(testBook)
//                .loanDate(LocalDateTime.now())
//                .status(LoanStatus.REQUESTED)
//                .userNotes("Please approve")
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        borrowedLoan = Loan.builder()
//                .id(2L)
//                .userId(1L)
//                .book(testBook)
//                .loanDate(LocalDateTime.now().minusDays(7))
//                .dueDate(LocalDateTime.now().plusDays(7))
//                .status(LoanStatus.BORROWED)
//                .approvedBy(librarianId)
//                .build();
//    }
//
//    @Test
//    void approveLoan_Success() {
//        // Arrange
//        ApproveLoanRequestDTO request = new ApproveLoanRequestDTO();
//        request.setNotesByLibrarian("Approved for student research");
//
//        when(loanRepository.findById(1L)).thenReturn(Optional.of(requestedLoan));
//        when(loanRepository.save(any(Loan.class))).thenReturn(requestedLoan);
//        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
//
//        // Act
//        LoanDTO result = librarianLoanService.approveLoan(1L, librarianId, request);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(LoanStatus.APPROVED, requestedLoan.getStatus());
//        assertEquals(librarianId, requestedLoan.getApprovedBy());
//        assertNotNull(requestedLoan.getApprovedAt());
//        assertEquals("Approved for student research", requestedLoan.getNotesByLibrarian());
//        assertNotNull(requestedLoan.getDueDate());
//
//        // Verify book inventory was updated
//        verify(bookRepository).save(testBook);
//        assertEquals(4, testBook.getAvailableCopiesForLoan()); // 5 - 1 = 4
//    }
//
//    @Test
//    void approveLoan_WithCustomLoanPeriod() {
//        // Arrange
//        ApproveLoanRequestDTO request = new ApproveLoanRequestDTO();
//        request.setCustomLoanPeriodDays(21); // 3 weeks instead of default 2
//
//        when(loanRepository.findById(1L)).thenReturn(Optional.of(requestedLoan));
//        when(loanRepository.save(any(Loan.class))).thenReturn(requestedLoan);
//        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
//
//        // Act
//        librarianLoanService.approveLoan(1L, librarianId, request);
//
//        // Assert
//        assertNotNull(requestedLoan.getDueDate());
//        // Due date should be 21 days from now
//        assertTrue(requestedLoan.getDueDate().isAfter(LocalDateTime.now().plusDays(20)));
//        assertTrue(requestedLoan.getDueDate().isBefore(LocalDateTime.now().plusDays(22)));
//    }
//
//    @Test
//    void approveLoan_BookNotAvailable() {
//        // Arrange
//        testBook.setAvailableCopiesForLoan(0);
//        ApproveLoanRequestDTO request = new ApproveLoanRequestDTO();
//
//        when(loanRepository.findById(1L)).thenReturn(Optional.of(requestedLoan));
//
//        // Act & Assert
//        BookNotAvailableException exception = assertThrows(
//                BookNotAvailableException.class,
//                () -> librarianLoanService.approveLoan(1L, librarianId, request)
//        );
//
//        assertTrue(exception.getMessage().contains("no longer available"));
//        verify(bookRepository, never()).save(any());
//        verify(loanRepository, never()).save(any());
//    }
//
//    @Test
//    void approveLoan_InvalidStatus() {
//        // Arrange
//        requestedLoan.setStatus(LoanStatus.APPROVED); // Already approved
//        ApproveLoanRequestDTO request = new ApproveLoanRequestDTO();
//
//        when(loanRepository.findById(1L)).thenReturn(Optional.of(requestedLoan));
//
//        // Act & Assert
//        IllegalStateException exception = assertThrows(
//                IllegalStateException.class,
//                () -> librarianLoanService.approveLoan(1L, librarianId, request)
//        );
//
//        assertTrue(exception.getMessage().contains("Only requested loans can be approved"));
//    }
//
//    @Test
//    void rejectLoan_Success() {
//        // Arrange
//        RejectLoanRequestDTO request = new RejectLoanRequestDTO();
//        request.setRejectionReason("Book is currently being repaired");
//
//        when(loanRepository.findById(1L)).thenReturn(Optional.of(requestedLoan));
//        when(loanRepository.save(any(Loan.class))).thenReturn(requestedLoan);
//
//        // Act
//        LoanDTO result = librarianLoanService.rejectLoan(1L, librarianId, request);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(LoanStatus.CANCELLED, requestedLoan.getStatus());
//        assertEquals(librarianId, requestedLoan.getApprovedBy());
//        assertNotNull(requestedLoan.getApprovedAt());
//        assertEquals("Book is currently being repaired", requestedLoan.getNotesByLibrarian());
//
//        // Verify book inventory was NOT updated
//        verify(bookRepository, never()).save(any());
//    }
//
//    @Test
//    void processBookReturn_Success() {
//        // Arrange
//        ProcessReturnRequestDTO request = new ProcessReturnRequestDTO();
//        request.setReturnNotes("Book returned in good condition");
//        request.setDamageReported(false);
//
//        BigDecimal calculatedFine = new BigDecimal("15000");
//
//        when(loanRepository.findById(2L)).thenReturn(Optional.of(borrowedLoan));
//        when(fineCalculationService.calculateFine(borrowedLoan)).thenReturn(calculatedFine);
//        when(loanRepository.save(any(Loan.class))).thenReturn(borrowedLoan);
//        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
//
//        // Act
//        LoanDTO result = librarianLoanService.processBookReturn(2L, librarianId, request);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(LoanStatus.RETURNED, borrowedLoan.getStatus());
//        assertNotNull(borrowedLoan.getReturnDate());
//        assertEquals(librarianId, borrowedLoan.getReturnedTo());
//        assertEquals(calculatedFine, borrowedLoan.getFineAmount());
//        assertFalse(borrowedLoan.getFinePaid());
//        assertTrue(borrowedLoan.getNotesByLibrarian().contains("Book returned in good condition"));
//
//        // Verify book inventory was updated
//        verify(bookRepository).save(testBook);
//        assertEquals(6, testBook.getAvailableCopiesForLoan()); // 5 + 1 = 6
//    }
//
//    @Test
//    void processBookReturn_WithDamage() {
//        // Arrange
//        ProcessReturnRequestDTO request = new ProcessReturnRequestDTO();
//        request.setReturnNotes("Book returned with damages");
//        request.setDamageReported(true);
//        request.setDamageDescription("Cover is torn and pages 50-52 are missing");
//
//        when(loanRepository.findById(2L)).thenReturn(Optional.of(borrowedLoan));
//        when(fineCalculationService.calculateFine(borrowedLoan)).thenReturn(BigDecimal.ZERO);
//        when(loanRepository.save(any(Loan.class))).thenReturn(borrowedLoan);
//        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
//
//        // Act
//        librarianLoanService.processBookReturn(2L, librarianId, request);
//
//        // Assert
//        assertTrue(borrowedLoan.getNotesByLibrarian().contains("DAMAGE REPORTED"));
//        assertTrue(borrowedLoan.getNotesByLibrarian().contains("Cover is torn and pages 50-52 are missing"));
//    }
//
//    @Test
//    void processBookReturn_WithCustomFine() {
//        // Arrange
//        ProcessReturnRequestDTO request = new ProcessReturnRequestDTO();
//        request.setCustomFineAmount(new BigDecimal("25000")); // Custom fine amount
//
//        when(loanRepository.findById(2L)).thenReturn(Optional.of(borrowedLoan));
//        when(fineCalculationService.calculateFine(borrowedLoan)).thenReturn(new BigDecimal("15000"));
//        when(loanRepository.save(any(Loan.class))).thenReturn(borrowedLoan);
//        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
//
//        // Act
//        librarianLoanService.processBookReturn(2L, librarianId, request);
//
//        // Assert
//        assertEquals(new BigDecimal("25000"), borrowedLoan.getFineAmount());
//    }
//
//    @Test
//    void getOverdueLoans_Success() {
//        // Arrange
//        Loan overdueLoan = Loan.builder()
//                .id(3L)
//                .userId(2L)
//                .book(testBook)
//                .status(LoanStatus.BORROWED)
//                .dueDate(LocalDateTime.now().minusDays(5))
//                .build();
//
//        List<Loan> overdueLoans = Arrays.asList(overdueLoan);
//
//        when(loanRepository.findOverdueLoans(eq(LoanStatus.BORROWED), any(LocalDateTime.class)))
//                .thenReturn(overdueLoans);
//
//        // Act
//        List<AdminLoanDTO> result = librarianLoanService.getOverdueLoans();
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//
//        AdminLoanDTO adminLoan = result.get(0);
//        assertEquals(3L, adminLoan.getId());
//        assertTrue(adminLoan.isRequiresAction());
//        assertEquals("Book is overdue - follow up required", adminLoan.getActionRequired());
//    }
//
//    @Test
//    void updateOverdueLoans_Success() {
//        // Arrange
//        Loan overdueLoan = Loan.builder()
//                .id(3L)
//                .userId(2L)
//                .book(testBook)
//                .status(LoanStatus.BORROWED)
//                .dueDate(LocalDateTime.now().minusDays(3))
//                .build();
//
//        List<Loan> overdueLoans = Arrays.asList(overdueLoan);
//        BigDecimal fine = new BigDecimal("15000");
//
//        when(loanRepository.findOverdueLoans(eq(LoanStatus.BORROWED), any(LocalDateTime.class)))
//                .thenReturn(overdueLoans);
//        when(fineCalculationService.calculateFine(overdueLoan)).thenReturn(fine);
//
//        // Act
//        librarianLoanService.updateOverdueLoans();
//
//        // Assert
//        assertEquals(LoanStatus.OVERDUE, overdueLoan.getStatus());
//        assertEquals(fine, overdueLoan.getFineAmount());
//        verify(loanRepository).saveAll(overdueLoans);
//    }
//
//    @Test
//    void calculateAndUpdateFine_Success() {
//        // Arrange
//        BigDecimal calculatedFine = new BigDecimal("20000");
//
//        when(loanRepository.findById(2L)).thenReturn(Optional.of(borrowedLoan));
//        when(fineCalculationService.calculateFine(borrowedLoan)).thenReturn(calculatedFine);
//        when(loanRepository.save(borrowedLoan)).thenReturn(borrowedLoan);
//
//        // Act
//        LoanDTO result = librarianLoanService.calculateAndUpdateFine(2L);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(calculatedFine, borrowedLoan.getFineAmount());
//        verify(loanRepository).save(borrowedLoan);
//    }
//}