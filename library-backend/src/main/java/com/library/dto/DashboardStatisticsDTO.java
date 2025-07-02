package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDTO {
    
    // Time-based statistics
    private List<DailyStatsDTO> dailyStats;
    private List<MonthlyStatsDTO> monthlyStats;
    
    // Category distributions
    private Map<String, Long> booksByCategory;
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> loansByStatus;
    private Map<String, Long> usersByRole;
    
    // Performance metrics
    private Map<String, Object> performanceMetrics;
    
    // Top items
    private List<TopBookDTO> mostBorrowedBooks;
    private List<TopBookDTO> mostSoldBooks;
    private List<TopUserDTO> mostActiveUsers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStatsDTO {
        private String date;
        private Long newUsers;
        private Long newLoans;
        private Long newOrders;
        private BigDecimal revenue;
        private Long documentDownloads;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStatsDTO {
        private String month;
        private Long totalUsers;
        private Long totalLoans;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private Long totalDocumentDownloads;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopBookDTO {
        private String bookId;
        private String title;
        private String isbn;
        private Long count;
        private BigDecimal revenue; // For sold books
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUserDTO {
        private String userId;
        private String name;
        private String email;
        private Long activityCount;
        private String activityType; // loans, orders, etc.
    }
}