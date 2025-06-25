package com.library.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDTO {
    
    // Time-based statistics
    private Map<String, Long> userRegistrationsByMonth;
    private Map<String, Long> loansByMonth;
    private Map<String, BigDecimal> revenueByMonth;
    private Map<String, Long> booksBorrowedByCategory;
    private Map<String, Long> booksSoldByCategory;
    
    // Detailed analytics
    private List<TopBorrowerDTO> topBorrowers;
    private List<TopCustomerDTO> topCustomers;
    private List<OverdueBookDTO> overdueBooks;
    private List<LowStockBookDTO> lowStockBooks;
    
    // Performance metrics
    private PerformanceMetricsDTO performanceMetrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopBorrowerDTO {
        private Long userId;
        private String fullName;
        private String email;
        private Long totalBorrowedBooks;
        private Long currentBorrowedBooks;
        private BigDecimal totalFines;
        private LocalDate lastBorrowDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomerDTO {
        private Long userId;
        private String fullName;
        private String email;
        private BigDecimal totalSpent;
        private Long totalOrders;
        private LocalDate lastOrderDate;
        private String customerTier; // BRONZE, SILVER, GOLD, PLATINUM
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverdueBookDTO {
        private Long loanId;
        private String bookTitle;
        private String borrowerName;
        private String borrowerEmail;
        private LocalDate dueDate;
        private Long daysOverdue;
        private BigDecimal fineAmount;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockBookDTO {
        private Long bookId;
        private String title;
        private String author;
        private Integer currentStock;
        private Integer minimumStock;
        private String category;
        private Boolean isPopular;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetricsDTO {
        private Double averageResponseTime; // in milliseconds
        private Long totalApiRequests;
        private Long failedApiRequests;
        private Double successRate; // percentage
        private Long peakConcurrentUsers;
        private String busyHours; // e.g., "9-11 AM, 2-4 PM"
        private Map<String, Long> apiEndpointUsage;
    }
}