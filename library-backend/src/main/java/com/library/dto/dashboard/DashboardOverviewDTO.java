package com.library.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {
    
    // User Statistics
    private UserStatistics userStatistics;
    
    // Book Statistics
    private BookStatistics bookStatistics;
    
    // Loan Statistics
    private LoanStatistics loanStatistics;
    
    // Revenue Statistics
    private RevenueStatistics revenueStatistics;
    
    // Recent Activities
    private List<RecentActivityDTO> recentActivities;
    
    // System Health
    private SystemHealthDTO systemHealth;
    
    private LocalDateTime generatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatistics {
        private Long totalUsers;
        private Long activeUsers;
        private Long newUsersThisMonth;
        private Long newUsersToday;
        private Double userGrowthRate; // percentage
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookStatistics {
        private Long totalBooks;
        private Long availableBooks;
        private Long borrowedBooks;
        private Long outOfStockBooks;
        private Long newBooksThisMonth;
        private List<PopularBookDTO> popularBooks;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanStatistics {
        private Long totalLoans;
        private Long activeLoans;
        private Long overdueLoans;
        private Long loansThisMonth;
        private Long returnedThisMonth;
        private BigDecimal totalFinesCollected;
        private BigDecimal pendingFines;
        private Double averageLoanDuration; // in days
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueStatistics {
        private BigDecimal totalRevenue;
        private BigDecimal revenueThisMonth;
        private BigDecimal revenueToday;
        private BigDecimal averageOrderValue;
        private Long totalOrders;
        private Long ordersThisMonth;
        private Long pendingOrders;
        private Double revenueGrowthRate; // percentage
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularBookDTO {
        private Long bookId;
        private String title;
        private String author;
        private Long borrowCount;
        private Long purchaseCount;
        private String coverImageUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDTO {
        private String type; // LOAN, ORDER, USER_REGISTRATION, BOOK_ADDED, etc.
        private String description;
        private String userName;
        private LocalDateTime timestamp;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealthDTO {
        private String status; // HEALTHY, WARNING, CRITICAL
        private Long databaseConnections;
        private Long redisConnections;
        private Double systemLoad;
        private Long freeMemory;
        private Long totalMemory;
        private List<String> alerts;
    }
}