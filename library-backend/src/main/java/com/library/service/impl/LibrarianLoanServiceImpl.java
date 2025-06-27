package com.library.service.impl;

import com.library.dto.AdminLoanDTO;
import com.library.dto.ApproveLoanRequestDTO;
import com.library.dto.LoanDTO;
import com.library.dto.ProcessReturnRequestDTO;
import com.library.dto.RejectLoanRequestDTO;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.LoanStatus;
import com.library.exception.BookNotAvailableException;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.service.FineCalculationService;
import com.library.service.LibrarianLoanService;
import com.library.mapper.LoanStatusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LibrarianLoanServiceImpl implements LibrarianLoanService {
    
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final FineCalculationService fineCalculationService;
    private final BookMapper bookMapper;
    
    @Value("${library.loan.default-loan-period-days:14}")
    private int defaultLoanPeriodDays;
    
    @Override
    public LoanDTO approveLoan(Long loanId, Long librarianId, ApproveLoanRequestDTO request) {
        log.info("Librarian {} approving loan {}", librarianId, loanId);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));
        
        if (loan.getStatus() != LoanStatus.REQUESTED) {
            throw new IllegalStateException("Only requested loans can be approved");
        }
        
        // Check book availability
        Book book = loan.getBook();
        if (!book.getIsLendable() || book.getAvailableCopiesForLoan() <= 0) {
            throw new BookNotAvailableException("Book is no longer available for loan");
        }
        
        // Update loan
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(librarianId);
        loan.setApprovedAt(LocalDateTime.now());
        loan.setNotesByLibrarian(request.getNotesByLibrarian());
        
        // Set custom loan period if provided
        if (request.getCustomLoanPeriodDays() != null && request.getCustomLoanPeriodDays() > 0) {
            loan.setDueDate(LocalDateTime.now().plusDays(request.getCustomLoanPeriodDays()));
        } else {
            loan.setDueDate(LocalDateTime.now().plusDays(defaultLoanPeriodDays));
        }
        
        // Update book inventory
        book.setAvailableCopiesForLoan(book.getAvailableCopiesForLoan() - 1);
        bookRepository.save(book);
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan {} approved successfully by librarian {}", loanId, librarianId);
        
        return mapToLoanDTO(savedLoan);
    }
    
    @Override
    public LoanDTO rejectLoan(Long loanId, Long librarianId, RejectLoanRequestDTO request) {
        log.info("Librarian {} rejecting loan {}", librarianId, loanId);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));
        
        if (loan.getStatus() != LoanStatus.REQUESTED) {
            throw new IllegalStateException("Only requested loans can be rejected");
        }
        
        // Update loan
        loan.setStatus(LoanStatus.CANCELLED);
        loan.setApprovedBy(librarianId);
        loan.setApprovedAt(LocalDateTime.now());
        loan.setNotesByLibrarian(request.getRejectionReason());
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan {} rejected by librarian {}", loanId, librarianId);
        
        return mapToLoanDTO(savedLoan);
    }
    
    @Override
    public LoanDTO processBookReturn(Long loanId, Long librarianId, ProcessReturnRequestDTO request) {
        log.info("Librarian {} processing return for loan {}", librarianId, loanId);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));
        
        if (loan.getStatus() != LoanStatus.BORROWED && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Only borrowed or overdue loans can be returned");
        }
        
        LocalDateTime returnDate = LocalDateTime.now();
        
        // Update loan
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(returnDate);
        loan.setReturnedTo(librarianId);
        
        // Add return notes
        String existingNotes = loan.getNotesByLibrarian() != null ? loan.getNotesByLibrarian() : "";
        String returnNotes = request.getReturnNotes() != null ? request.getReturnNotes() : "";
        if (!returnNotes.isEmpty()) {
            loan.setNotesByLibrarian(existingNotes + "\nReturn: " + returnNotes);
        }
        
        // Handle damage reporting
        if (request.getDamageReported() != null && request.getDamageReported()) {
            String damageNote = "DAMAGE REPORTED: " + 
                (request.getDamageDescription() != null ? request.getDamageDescription() : "No description provided");
            loan.setNotesByLibrarian(loan.getNotesByLibrarian() + "\n" + damageNote);
        }
        
        // Calculate fine
        BigDecimal calculatedFine = fineCalculationService.calculateFine(loan);
        BigDecimal finalFine = request.getCustomFineAmount() != null ? 
            request.getCustomFineAmount() : calculatedFine;
        
        loan.setFineAmount(finalFine);
        loan.setFinePaid(false);
        
        // Update book inventory
        Book book = loan.getBook();
        book.setAvailableCopiesForLoan(book.getAvailableCopiesForLoan() + 1);
        bookRepository.save(book);
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Book return processed for loan {}. Fine: {} VND", loanId, finalFine);
        
        return mapToLoanDTO(savedLoan);
    }
    
    @Override
    public Page<AdminLoanDTO> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable)
            .map(this::mapToAdminLoanDTO);
    }
    
    @Override
    public Page<AdminLoanDTO> getLoansByStatus(String status, Pageable pageable) {
        LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
        return loanRepository.findByStatus(loanStatus, pageable)
            .map(this::mapToAdminLoanDTO);
    }
    
    @Override
    public AdminLoanDTO getAdminLoanDetails(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));
        return mapToAdminLoanDTO(loan);
    }
    
    @Override
    public List<AdminLoanDTO> getOverdueLoans() {
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(
            LoanStatus.BORROWED, LocalDateTime.now());
        return overdueLoans.stream()
            .map(this::mapToAdminLoanDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public void updateOverdueLoans() {
        log.info("Updating overdue loan statuses");
        
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(
            LoanStatus.BORROWED, LocalDateTime.now());
        
        for (Loan loan : overdueLoans) {
            loan.setStatus(LoanStatus.OVERDUE);
            BigDecimal fine = fineCalculationService.calculateFine(loan);
            loan.setFineAmount(fine);
        }
        
        loanRepository.saveAll(overdueLoans);
        log.info("Updated {} loans to overdue status", overdueLoans.size());
    }
    
    @Override
    public LoanDTO calculateAndUpdateFine(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));
        
        BigDecimal fine = fineCalculationService.calculateFine(loan);
        loan.setFineAmount(fine);
        
        Loan savedLoan = loanRepository.save(loan);
        log.info("Updated fine for loan {}: {} VND", loanId, fine);
        
        return mapToLoanDTO(savedLoan);
    }
    
    @Override
    public List<AdminLoanDTO> getLoansWithUnpaidFines() {
        // This would need a custom query in the repository
        // For now, we'll get all loans and filter
        List<Loan> allLoans = loanRepository.findAll();
        return allLoans.stream()
            .filter(loan -> loan.getFineAmount() != null && 
                          loan.getFineAmount().compareTo(BigDecimal.ZERO) > 0 &&
                          !loan.getFinePaid())
            .map(this::mapToAdminLoanDTO)
            .collect(Collectors.toList());
    }
    
    private LoanDTO mapToLoanDTO(Loan loan) {
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
        
        // Add calculated fields
        if (loan.getDueDate() != null) {
            dto.setOverdue(LoanStatusMapper.isOverdue(loan.getDueDate()));
            if (dto.isOverdue()) {
                dto.setDaysOverdue(LoanStatusMapper.calculateDaysOverdue(loan.getDueDate()));
            } else {
                dto.setDaysUntilDue(LoanStatusMapper.calculateDaysUntilDue(loan.getDueDate()));
            }
        }
        
        return dto;
    }
    
    private AdminLoanDTO mapToAdminLoanDTO(Loan loan) {
        AdminLoanDTO dto = AdminLoanDTO.builder()
            .id(loan.getId())
            .userId(loan.getUserId())
            // TODO: Add user details from User service or repository
            .username("user" + loan.getUserId()) // Placeholder
            .userFullName("User " + loan.getUserId()) // Placeholder
            .userEmail("user" + loan.getUserId() + "@example.com") // Placeholder
            .book(bookMapper.toDTO(loan.getBook()))
            .loanDate(loan.getLoanDate())
            .dueDate(loan.getDueDate())
            .returnDate(loan.getReturnDate())
            .status(loan.getStatus())
            .statusDisplayName(LoanStatusMapper.getDisplayName(loan.getStatus()))
            .fineAmount(loan.getFineAmount())
            .finePaid(loan.getFinePaid())
            .userNotes(loan.getUserNotes())
            .notesByLibrarian(loan.getNotesByLibrarian())
            .approvedBy(loan.getApprovedBy())
            .approvedAt(loan.getApprovedAt())
            .returnedTo(loan.getReturnedTo())
            .createdAt(loan.getCreatedAt())
            .updatedAt(loan.getUpdatedAt())
            .build();
        
        // Calculate admin-specific fields
        if (loan.getDueDate() != null) {
            dto.setOverdue(LoanStatusMapper.isOverdue(loan.getDueDate()));
            dto.setUrgencyLevel(LoanStatusMapper.getUrgencyLevel(loan.getDueDate()));
            
            if (dto.isOverdue()) {
                dto.setDaysOverdue(LoanStatusMapper.calculateDaysOverdue(loan.getDueDate()));
            } else {
                dto.setDaysUntilDue(LoanStatusMapper.calculateDaysUntilDue(loan.getDueDate()));
            }
        }
        
        // Determine if action is required
        dto.setRequiresAction(determineActionRequired(loan));
        dto.setActionRequired(getActionRequiredDescription(loan));
        
        return dto;
    }
    
    private boolean determineActionRequired(Loan loan) {
        return switch (loan.getStatus()) {
            case REQUESTED -> true; // Needs approval/rejection
            case OVERDUE -> true;   // Needs follow-up
            case BORROWED -> loan.getDueDate() != null && 
                           LoanStatusMapper.calculateDaysUntilDue(loan.getDueDate()) <= 1; // Due soon
            default -> false;
        };
    }
    
    private String getActionRequiredDescription(Loan loan) {
        return switch (loan.getStatus()) {
            case REQUESTED -> "Loan request pending approval";
            case OVERDUE -> "Book is overdue - follow up required";
            case BORROWED -> {
                if (loan.getDueDate() != null) {
                    long daysUntil = LoanStatusMapper.calculateDaysUntilDue(loan.getDueDate());
                    if (daysUntil <= 0) {
                        yield "Book is due today";
                    } else if (daysUntil == 1) {
                        yield "Book is due tomorrow";
                    }
                }
                yield null;
            }
            default -> null;
        };
    }
}