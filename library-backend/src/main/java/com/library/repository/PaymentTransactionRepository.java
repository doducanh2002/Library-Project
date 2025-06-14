package com.library.repository;

import com.library.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByPaymentId(Long paymentId);

    Page<PaymentTransaction> findByPaymentId(Long paymentId, Pageable pageable);

    List<PaymentTransaction> findByTransactionType(PaymentTransaction.TransactionType transactionType);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentId = :paymentId ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByPaymentIdOrderByCreatedAtDesc(@Param("paymentId") Long paymentId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentTransaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.transactionType = :type")
    long countByTransactionType(@Param("type") PaymentTransaction.TransactionType type);
}