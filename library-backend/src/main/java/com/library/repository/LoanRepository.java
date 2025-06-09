package com.library.repository;

import com.library.entity.Loan;
import com.library.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    Page<Loan> findByUserId(Long userId, Pageable pageable);
    
    Page<Loan> findByUserIdAndStatus(Long userId, LoanStatus status, Pageable pageable);
    
    List<Loan> findByUserIdAndStatusIn(Long userId, List<LoanStatus> statuses);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.userId = :userId AND l.status IN :statuses")
    long countByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<LoanStatus> statuses);
    
    @Query("SELECT l FROM Loan l WHERE l.status = :status AND l.dueDate < :currentDate")
    List<Loan> findOverdueLoans(@Param("status") LoanStatus status, @Param("currentDate") LocalDateTime currentDate);
    
    Optional<Loan> findByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, List<LoanStatus> statuses);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.book.id = :bookId AND l.status IN :statuses")
    long countActiveLoansForBook(@Param("bookId") Long bookId, @Param("statuses") List<LoanStatus> statuses);
    
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    
    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId ORDER BY l.createdAt DESC")
    Page<Loan> findByBookId(@Param("bookId") Long bookId, Pageable pageable);
    
    @Query("SELECT l FROM Loan l WHERE l.dueDate BETWEEN :startDate AND :endDate AND l.status = :status")
    List<Loan> findLoansApproachingDueDate(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate,
        @Param("status") LoanStatus status
    );
}