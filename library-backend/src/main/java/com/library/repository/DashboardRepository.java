package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.library.entity.Book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface DashboardRepository extends JpaRepository<Book, Long> {
    
    // User Statistics - Note: User data is fetched from Authentication Service
    // These methods will be implemented in DashboardService to call Authentication Service
    // For now, using placeholder queries that will be replaced with service calls
    
    @Query("SELECT COUNT(DISTINCT l.userId) FROM Loan l")
    Long countActiveUsersFromLoans();
    
    @Query("SELECT COUNT(DISTINCT o.userId) FROM Order o WHERE o.createdAt >= :startDate")
    Long countNewUsersFromOrders(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(DISTINCT l.userId) FROM Loan l WHERE DATE(l.createdAt) = :date")
    Long countNewUsersFromLoansToday(@Param("date") LocalDate date);
    
    // Book Statistics
    @Query("SELECT COUNT(b) FROM Book b")
    Long countTotalBooks();
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopiesForLoan > 0")
    Long countAvailableBooks();
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status IN ('BORROWED', 'OVERDUE')")
    Long countBorrowedBooks();
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.stockForSale = 0 AND b.isSellable = true")
    Long countOutOfStockBooks();
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.createdAt >= :startDate")
    Long countNewBooks(@Param("startDate") LocalDateTime startDate);
    
    // Loan Statistics
    @Query("SELECT COUNT(l) FROM Loan l")
    Long countTotalLoans();
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status IN ('BORROWED', 'APPROVED')")
    Long countActiveLoans();
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'OVERDUE'")
    Long countOverdueLoans();
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.loanDate >= :startDate")
    Long countLoansThisMonth(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate >= :startDate AND l.status = 'RETURNED'")
    Long countReturnsThisMonth(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COALESCE(SUM(l.fineAmount), 0) FROM Loan l WHERE l.finePaid = true")
    BigDecimal getTotalFinesCollected();
    
    @Query("SELECT COALESCE(SUM(l.fineAmount), 0) FROM Loan l WHERE l.finePaid = false AND l.fineAmount > 0")
    BigDecimal getPendingFines();
    
    @Query("SELECT AVG(DATEDIFF(COALESCE(l.returnDate, CURRENT_DATE), l.loanDate)) FROM Loan l WHERE l.status IN ('RETURNED', 'BORROWED')")
    Double getAverageLoanDuration();
    
    // Revenue Statistics
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :startDate AND o.status NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal getRevenueThisMonth(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE DATE(o.orderDate) = :date AND o.status NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal getRevenueToday(@Param("date") LocalDate date);
    
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal getAverageOrderValue();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')")
    Long countTotalOrders();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate AND o.status NOT IN ('CANCELLED', 'REFUNDED')")
    Long countOrdersThisMonth(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN ('PENDING_PAYMENT', 'PROCESSING')")
    Long countPendingOrders();
    
    // Popular Books
    @Query("""
        SELECT b.id, b.title, 
               COALESCE((SELECT STRING_AGG(a.name, ', ') FROM BookAuthor ba JOIN Author a ON ba.authorId = a.id WHERE ba.bookId = b.id), 'Unknown') as author,
               COUNT(l.id) as borrowCount,
               COALESCE((SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.bookId = b.id), 0) as purchaseCount,
               b.coverImageUrl
        FROM Book b
        LEFT JOIN Loan l ON b.id = l.bookId AND l.status IN ('RETURNED', 'BORROWED')
        GROUP BY b.id, b.title, b.coverImageUrl
        ORDER BY (COUNT(l.id) + COALESCE((SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.bookId = b.id), 0)) DESC
        LIMIT 10
        """)
    List<Object[]> getPopularBooks();
    
    // Top Borrowers - Using only loan data, user details fetched separately
    @Query("""
        SELECT l.userId,
               COUNT(l.id) as totalBorrowed,
               COUNT(CASE WHEN l.status IN ('BORROWED', 'OVERDUE') THEN 1 END) as currentBorrowed,
               COALESCE(SUM(l.fineAmount), 0) as totalFines,
               MAX(l.loanDate) as lastBorrowDate
        FROM Loan l
        GROUP BY l.userId
        HAVING COUNT(l.id) > 0
        ORDER BY COUNT(l.id) DESC
        LIMIT 10
        """)
    List<Object[]> getTopBorrowers();
    
    // Top Customers - Using only order data, user details fetched separately
    @Query("""
        SELECT o.userId,
               COALESCE(SUM(o.totalAmount), 0) as totalSpent,
               COUNT(o.id) as totalOrders,
               MAX(o.orderDate) as lastOrderDate
        FROM Order o
        WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')
        GROUP BY o.userId
        HAVING COUNT(o.id) > 0
        ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC
        LIMIT 10
        """)
    List<Object[]> getTopCustomers();
    
    // Overdue Books - User details fetched separately from Authentication Service
    @Query("""
        SELECT l.id, b.title, l.userId, l.dueDate, l.fineAmount, l.status
        FROM Loan l
        JOIN l.book b
        WHERE l.status = 'OVERDUE' OR (l.status = 'BORROWED' AND l.dueDate < CURRENT_DATE)
        ORDER BY l.dueDate ASC
        """)
    List<Object[]> getOverdueBooks();
    
    // Low Stock Books
    @Query("""
        SELECT b.id, b.title,
               COALESCE((SELECT STRING_AGG(a.name, ', ') FROM BookAuthor ba JOIN Author a ON ba.authorId = a.id WHERE ba.bookId = b.id), 'Unknown') as author,
               b.stockForSale, c.name as category
        FROM Book b
        LEFT JOIN Category c ON b.categoryId = c.id
        WHERE b.isSellable = true AND b.stockForSale <= 5
        ORDER BY b.stockForSale ASC
        """)
    List<Object[]> getLowStockBooks();
    
    // Monthly Statistics - User registrations will be fetched from Authentication Service
    // This method will be replaced with service call in DashboardService
    @Query("""
        SELECT YEAR(l.createdAt), MONTH(l.createdAt), COUNT(DISTINCT l.userId)
        FROM Loan l
        WHERE l.createdAt >= :startDate
        GROUP BY YEAR(l.createdAt), MONTH(l.createdAt)
        ORDER BY YEAR(l.createdAt), MONTH(l.createdAt)
        """)
    List<Object[]> getUniqueUsersByMonth(@Param("startDate") LocalDateTime startDate);
    
    @Query("""
        SELECT YEAR(l.loanDate), MONTH(l.loanDate), COUNT(l.id)
        FROM Loan l
        WHERE l.loanDate >= :startDate
        GROUP BY YEAR(l.loanDate), MONTH(l.loanDate)
        ORDER BY YEAR(l.loanDate), MONTH(l.loanDate)
        """)
    List<Object[]> getLoansByMonth(@Param("startDate") LocalDateTime startDate);
    
    @Query("""
        SELECT YEAR(o.orderDate), MONTH(o.orderDate), COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.orderDate >= :startDate AND o.status NOT IN ('CANCELLED', 'REFUNDED')
        GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)
        ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)
        """)
    List<Object[]> getRevenueByMonth(@Param("startDate") LocalDateTime startDate);
    
    // Category-based statistics
    @Query("""
        SELECT c.name, COUNT(l.id)
        FROM Category c
        LEFT JOIN Book b ON c.id = b.categoryId
        LEFT JOIN Loan l ON b.id = l.bookId
        GROUP BY c.id, c.name
        ORDER BY COUNT(l.id) DESC
        """)
    List<Object[]> getBooksBorrowedByCategory();
    
    @Query("""
        SELECT c.name, COALESCE(SUM(oi.quantity), 0)
        FROM Category c
        LEFT JOIN Book b ON c.id = b.categoryId
        LEFT JOIN OrderItem oi ON b.id = oi.bookId
        LEFT JOIN Order o ON oi.orderId = o.id AND o.status NOT IN ('CANCELLED', 'REFUNDED')
        GROUP BY c.id, c.name
        ORDER BY COALESCE(SUM(oi.quantity), 0) DESC
        """)
    List<Object[]> getBooksSoldByCategory();
}