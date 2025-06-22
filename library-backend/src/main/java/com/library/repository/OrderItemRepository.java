package com.library.repository;

import com.library.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find by order
    List<OrderItem> findByOrderIdOrderById(Long orderId);

    // Find by book
    List<OrderItem> findByBookIdOrderByOrderOrderDateDesc(Long bookId);

    // Statistics queries
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi JOIN oi.order o WHERE oi.book.id = :bookId AND o.paymentStatus = 'PAID'")
    Long getTotalSoldQuantityByBookId(@Param("bookId") Long bookId);

    @Query("SELECT oi.book.id, SUM(oi.quantity) as totalSold FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.paymentStatus = 'PAID' AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.book.id ORDER BY totalSold DESC")
    List<Object[]> getBestSellingBooks(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Revenue by book
    @Query("SELECT SUM(oi.itemTotalPrice) FROM OrderItem oi JOIN oi.order o WHERE oi.book.id = :bookId AND o.paymentStatus = 'PAID'")
    BigDecimal getTotalRevenueByBookId(@Param("bookId") Long bookId);
    
    // Check if user has purchased a book with specific order statuses
    @Query(value = "SELECT CASE WHEN COUNT(oi.id) > 0 THEN true ELSE false END FROM order_items oi " +
           "JOIN orders o ON oi.order_id = o.id " +
           "WHERE oi.book_id = :bookId AND CAST(o.user_id AS VARCHAR) = :userId " +
           "AND o.status IN (:statuses)", nativeQuery = true)
    boolean existsByBookIdAndOrderUserIdAndOrderStatusIn(
        @Param("bookId") Long bookId,
        @Param("userId") String userId,
        @Param("statuses") java.util.List<String> statuses
    );
}