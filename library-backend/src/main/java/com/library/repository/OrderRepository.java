package com.library.repository;

import com.library.entity.Order;
import com.library.entity.OrderStatus;
import com.library.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by user
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

    // Find by order code
    Optional<Order> findByOrderCode(String orderCode);
    
    Optional<Order> findByOrderCodeAndUserId(String orderCode, Long userId);

    // Find by status
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
    
    Page<Order> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus, Pageable pageable);

    // Find by date range
    Page<Order> findByOrderDateBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    );

    // Statistics queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // User order statistics
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    // Recent orders
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByStatus(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    // Orders that need admin attention
    @Query("SELECT o FROM Order o WHERE o.status IN ('PAID', 'PROCESSING') ORDER BY o.orderDate ASC")
    List<Order> findOrdersNeedingAttention(Pageable pageable);
    
    // Dashboard specific queries
    Long countByOrderStatus(String status);
    
    Long countByOrderStatusAndCreatedAtAfter(String status, LocalDateTime date);
    
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT DATE(o.createdAt), SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'PAID' AND DATE(o.createdAt) = :date GROUP BY DATE(o.createdAt)")
    BigDecimal getTotalRevenueByDate(@Param("date") LocalDate date);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'PAID' AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    BigDecimal getTotalRevenueByMonth(@Param("year") int year, @Param("month") int month);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatusRaw();
    
    default Map<String, Long> countOrdersByStatus() {
        return countOrdersByStatusRaw().stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
    }
}