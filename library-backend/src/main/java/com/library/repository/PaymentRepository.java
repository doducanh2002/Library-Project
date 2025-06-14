package com.library.repository;

import com.library.entity.Payment;
import com.library.entity.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);

    Optional<Payment> findByVnpTransactionNo(String vnpTransactionNo);

    List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    Page<Payment> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status AND p.expiresAt < :now")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND p.expiresAt < :now")
    List<Payment> findExpiredPendingPayments(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = :status")
    long countByPaymentStatus(@Param("status") PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    boolean existsByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);
}