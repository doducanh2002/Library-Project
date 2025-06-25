package com.library.service;

import com.library.dto.dashboard.DashboardOverviewDTO;
import com.library.dto.dashboard.DashboardStatisticsDTO;
import com.library.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {
    
    private final DashboardRepository dashboardRepository;
    
    @Cacheable(value = "dashboard-overview", unless = "#result == null")
    public DashboardOverviewDTO getDashboardOverview() {
        log.info("Generating dashboard overview");
        
        try {
            return DashboardOverviewDTO.builder()
                    .userStatistics(getUserStatistics())
                    .bookStatistics(getBookStatistics())
                    .loanStatistics(getLoanStatistics())
                    .revenueStatistics(getRevenueStatistics())
                    .recentActivities(getRecentActivities())
                    .systemHealth(getSystemHealth())
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error generating dashboard overview", e);
            throw new RuntimeException("Failed to generate dashboard overview", e);
        }
    }
    
    @Cacheable(value = "dashboard-statistics", unless = "#result == null")
    public DashboardStatisticsDTO getDashboardStatistics() {
        log.info("Generating dashboard statistics");
        
        try {
            LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
            
            return DashboardStatisticsDTO.builder()
                    .userRegistrationsByMonth(getUserRegistrationsByMonth(twelveMonthsAgo))
                    .loansByMonth(getLoansByMonth(twelveMonthsAgo))
                    .revenueByMonth(getRevenueByMonth(twelveMonthsAgo))
                    .booksBorrowedByCategory(getBooksBorrowedByCategory())
                    .booksSoldByCategory(getBooksSoldByCategory())
                    .topBorrowers(getTopBorrowers())
                    .topCustomers(getTopCustomers())
                    .overdueBooks(getOverdueBooks())
                    .lowStockBooks(getLowStockBooks())
                    .performanceMetrics(getPerformanceMetrics())
                    .build();
        } catch (Exception e) {
            log.error("Error generating dashboard statistics", e);
            throw new RuntimeException("Failed to generate dashboard statistics", e);
        }
    }
    
    private DashboardOverviewDTO.UserStatistics getUserStatistics() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        LocalDate today = LocalDate.now();
        
        // Note: In a real microservices setup, these would be fetched from Authentication Service
        // For now, using approximations based on loan and order data
        Long totalUsers = dashboardRepository.countActiveUsersFromLoans(); // Approximation
        Long activeUsers = dashboardRepository.countActiveUsersFromLoans();
        Long newUsersThisMonth = dashboardRepository.countNewUsersFromOrders(oneMonthAgo);
        Long newUsersLastMonth = dashboardRepository.countNewUsersFromOrders(twoMonthsAgo) - newUsersThisMonth;
        Long newUsersToday = dashboardRepository.countNewUsersFromLoansToday(today);
        
        Double userGrowthRate = calculateGrowthRate(newUsersThisMonth, newUsersLastMonth);
        
        return DashboardOverviewDTO.UserStatistics.builder()
                .totalUsers(totalUsers != null ? totalUsers : 0L)
                .activeUsers(activeUsers != null ? activeUsers : 0L)
                .newUsersThisMonth(newUsersThisMonth != null ? newUsersThisMonth : 0L)
                .newUsersToday(newUsersToday != null ? newUsersToday : 0L)
                .userGrowthRate(userGrowthRate != null ? userGrowthRate : 0.0)
                .build();
    }
    
    private DashboardOverviewDTO.BookStatistics getBookStatistics() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        Long totalBooks = dashboardRepository.countTotalBooks();
        Long availableBooks = dashboardRepository.countAvailableBooks();
        Long borrowedBooks = dashboardRepository.countBorrowedBooks();
        Long outOfStockBooks = dashboardRepository.countOutOfStockBooks();
        Long newBooksThisMonth = dashboardRepository.countNewBooks(oneMonthAgo);
        
        List<DashboardOverviewDTO.PopularBookDTO> popularBooks = getPopularBooks();
        
        return DashboardOverviewDTO.BookStatistics.builder()
                .totalBooks(totalBooks)
                .availableBooks(availableBooks)
                .borrowedBooks(borrowedBooks)
                .outOfStockBooks(outOfStockBooks)
                .newBooksThisMonth(newBooksThisMonth)
                .popularBooks(popularBooks)
                .build();
    }
    
    private DashboardOverviewDTO.LoanStatistics getLoanStatistics() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        Long totalLoans = dashboardRepository.countTotalLoans();
        Long activeLoans = dashboardRepository.countActiveLoans();
        Long overdueLoans = dashboardRepository.countOverdueLoans();
        Long loansThisMonth = dashboardRepository.countLoansThisMonth(oneMonthAgo);
        Long returnedThisMonth = dashboardRepository.countReturnsThisMonth(oneMonthAgo);
        BigDecimal totalFinesCollected = dashboardRepository.getTotalFinesCollected();
        BigDecimal pendingFines = dashboardRepository.getPendingFines();
        Double averageLoanDuration = dashboardRepository.getAverageLoanDuration();
        
        return DashboardOverviewDTO.LoanStatistics.builder()
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .overdueLoans(overdueLoans)
                .loansThisMonth(loansThisMonth)
                .returnedThisMonth(returnedThisMonth)
                .totalFinesCollected(totalFinesCollected)
                .pendingFines(pendingFines)
                .averageLoanDuration(averageLoanDuration)
                .build();
    }
    
    private DashboardOverviewDTO.RevenueStatistics getRevenueStatistics() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        LocalDate today = LocalDate.now();
        
        BigDecimal totalRevenue = dashboardRepository.getTotalRevenue();
        BigDecimal revenueThisMonth = dashboardRepository.getRevenueThisMonth(oneMonthAgo);
        BigDecimal revenueLastMonth = dashboardRepository.getRevenueThisMonth(twoMonthsAgo).subtract(revenueThisMonth);
        BigDecimal revenueToday = dashboardRepository.getRevenueToday(today);
        BigDecimal averageOrderValue = dashboardRepository.getAverageOrderValue();
        Long totalOrders = dashboardRepository.countTotalOrders();
        Long ordersThisMonth = dashboardRepository.countOrdersThisMonth(oneMonthAgo);
        Long pendingOrders = dashboardRepository.countPendingOrders();
        
        Double revenueGrowthRate = calculateGrowthRate(revenueThisMonth, revenueLastMonth);
        
        return DashboardOverviewDTO.RevenueStatistics.builder()
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .revenueToday(revenueToday)
                .averageOrderValue(averageOrderValue)
                .totalOrders(totalOrders)
                .ordersThisMonth(ordersThisMonth)
                .pendingOrders(pendingOrders)
                .revenueGrowthRate(revenueGrowthRate)
                .build();
    }
    
    private List<DashboardOverviewDTO.PopularBookDTO> getPopularBooks() {
        List<Object[]> results = dashboardRepository.getPopularBooks();
        
        return results.stream()
                .map(row -> DashboardOverviewDTO.PopularBookDTO.builder()
                        .bookId((Long) row[0])
                        .title((String) row[1])
                        .author((String) row[2])
                        .borrowCount((Long) row[3])
                        .purchaseCount(((Number) row[4]).longValue())
                        .coverImageUrl((String) row[5])
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<DashboardOverviewDTO.RecentActivityDTO> getRecentActivities() {
        // This would typically come from an audit log table
        // For now, return sample data
        return Arrays.asList(
                DashboardOverviewDTO.RecentActivityDTO.builder()
                        .type("LOAN")
                        .description("Book borrowed: Spring Boot in Action")
                        .userName("John Doe")
                        .timestamp(LocalDateTime.now().minusHours(1))
                        .status("APPROVED")
                        .build(),
                DashboardOverviewDTO.RecentActivityDTO.builder()
                        .type("ORDER")
                        .description("Order placed: #ORD-2024-001234")
                        .userName("Jane Smith")
                        .timestamp(LocalDateTime.now().minusHours(2))
                        .status("PAID")
                        .build()
        );
    }
    
    private DashboardOverviewDTO.SystemHealthDTO getSystemHealth() {
        // This would typically integrate with monitoring systems
        return DashboardOverviewDTO.SystemHealthDTO.builder()
                .status("HEALTHY")
                .databaseConnections(10L)
                .redisConnections(5L)
                .systemLoad(0.65)
                .freeMemory(2048L)
                .totalMemory(8192L)
                .alerts(Collections.emptyList())
                .build();
    }
    
    private Map<String, Long> getUserRegistrationsByMonth(LocalDateTime startDate) {
        // Note: In real microservices setup, this would call Authentication Service
        // For now, using unique users from loans as approximation
        List<Object[]> results = dashboardRepository.getUniqueUsersByMonth(startDate);
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> String.format("%d-%02d", (Integer) row[0], (Integer) row[1]),
                        row -> ((Number) row[2]).longValue(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
    
    private Map<String, Long> getLoansByMonth(LocalDateTime startDate) {
        List<Object[]> results = dashboardRepository.getLoansByMonth(startDate);
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> String.format("%d-%02d", (Integer) row[0], (Integer) row[1]),
                        row -> ((Number) row[2]).longValue(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
    
    private Map<String, BigDecimal> getRevenueByMonth(LocalDateTime startDate) {
        List<Object[]> results = dashboardRepository.getRevenueByMonth(startDate);
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> String.format("%d-%02d", (Integer) row[0], (Integer) row[1]),
                        row -> (BigDecimal) row[2],
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
    
    private Map<String, Long> getBooksBorrowedByCategory() {
        List<Object[]> results = dashboardRepository.getBooksBorrowedByCategory();
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
    
    private Map<String, Long> getBooksSoldByCategory() {
        List<Object[]> results = dashboardRepository.getBooksSoldByCategory();
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
    
    private List<DashboardStatisticsDTO.TopBorrowerDTO> getTopBorrowers() {
        List<Object[]> results = dashboardRepository.getTopBorrowers();
        
        return results.stream()
                .map(row -> {
                    Long userId = (Long) row[0];
                    // Note: In real microservices setup, user details would be fetched from Authentication Service
                    String fullName = "User " + userId; // Placeholder
                    String email = "user" + userId + "@example.com"; // Placeholder
                    
                    return DashboardStatisticsDTO.TopBorrowerDTO.builder()
                            .userId(userId)
                            .fullName(fullName)
                            .email(email)
                            .totalBorrowedBooks(((Number) row[1]).longValue())
                            .currentBorrowedBooks(((Number) row[2]).longValue())
                            .totalFines((BigDecimal) row[3])
                            .lastBorrowDate(row[4] != null ? ((java.sql.Timestamp) row[4]).toLocalDateTime().toLocalDate() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private List<DashboardStatisticsDTO.TopCustomerDTO> getTopCustomers() {
        List<Object[]> results = dashboardRepository.getTopCustomers();
        
        return results.stream()
                .map(row -> {
                    Long userId = (Long) row[0];
                    BigDecimal totalSpent = (BigDecimal) row[1];
                    String customerTier = calculateCustomerTier(totalSpent);
                    
                    // Note: In real microservices setup, user details would be fetched from Authentication Service
                    String fullName = "Customer " + userId; // Placeholder
                    String email = "customer" + userId + "@example.com"; // Placeholder
                    
                    return DashboardStatisticsDTO.TopCustomerDTO.builder()
                            .userId(userId)
                            .fullName(fullName)
                            .email(email)
                            .totalSpent(totalSpent)
                            .totalOrders(((Number) row[2]).longValue())
                            .lastOrderDate(row[3] != null ? ((java.sql.Timestamp) row[3]).toLocalDateTime().toLocalDate() : null)
                            .customerTier(customerTier)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private List<DashboardStatisticsDTO.OverdueBookDTO> getOverdueBooks() {
        List<Object[]> results = dashboardRepository.getOverdueBooks();
        
        return results.stream()
                .map(row -> {
                    Long userId = (Long) row[2];
                    LocalDate dueDate = ((java.sql.Timestamp) row[3]).toLocalDateTime().toLocalDate();
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                    
                    // Note: In real microservices setup, user details would be fetched from Authentication Service
                    String borrowerName = "Borrower " + userId; // Placeholder
                    String borrowerEmail = "borrower" + userId + "@example.com"; // Placeholder
                    
                    return DashboardStatisticsDTO.OverdueBookDTO.builder()
                            .loanId((Long) row[0])
                            .bookTitle((String) row[1])
                            .borrowerName(borrowerName)
                            .borrowerEmail(borrowerEmail)
                            .dueDate(dueDate)
                            .daysOverdue(daysOverdue)
                            .fineAmount((BigDecimal) row[4])
                            .status((String) row[5])
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private List<DashboardStatisticsDTO.LowStockBookDTO> getLowStockBooks() {
        List<Object[]> results = dashboardRepository.getLowStockBooks();
        
        return results.stream()
                .map(row -> DashboardStatisticsDTO.LowStockBookDTO.builder()
                        .bookId((Long) row[0])
                        .title((String) row[1])
                        .author((String) row[2])
                        .currentStock((Integer) row[3])
                        .minimumStock(5) // Default minimum stock
                        .category((String) row[4])
                        .isPopular(((Integer) row[3]) < 3) // Consider popular if stock < 3
                        .build())
                .collect(Collectors.toList());
    }
    
    private DashboardStatisticsDTO.PerformanceMetricsDTO getPerformanceMetrics() {
        // This would typically integrate with monitoring systems
        return DashboardStatisticsDTO.PerformanceMetricsDTO.builder()
                .averageResponseTime(250.0)
                .totalApiRequests(10000L)
                .failedApiRequests(25L)
                .successRate(99.75)
                .peakConcurrentUsers(150L)
                .busyHours("9-11 AM, 2-4 PM")
                .apiEndpointUsage(Map.of(
                        "/api/v1/books", 3500L,
                        "/api/v1/loans", 2000L,
                        "/api/v1/orders", 1500L,
                        "/api/v1/auth", 3000L
                ))
                .build();
    }
    
    private Double calculateGrowthRate(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return current != null && current.doubleValue() > 0 ? 100.0 : 0.0;
        }
        
        double growth = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
        return Math.round(growth * 100.0) / 100.0;
    }
    
    private Double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        
        BigDecimal growth = current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return growth.doubleValue();
    }
    
    private String calculateCustomerTier(BigDecimal totalSpent) {
        if (totalSpent.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            return "PLATINUM";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            return "GOLD";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return "SILVER";
        } else {
            return "BRONZE";
        }
    }
}