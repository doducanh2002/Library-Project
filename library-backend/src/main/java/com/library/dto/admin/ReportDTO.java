package com.library.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportRequest {
        @NotNull(message = "Report type is required")
        private ReportType reportType;
        
        private LocalDate startDate;
        private LocalDate endDate;
        private ExportFormat exportFormat;
        private Map<String, Object> parameters;
        private String reportTitle;
        private Boolean includeCharts;
        
        public enum ReportType {
            LOAN_REPORT,
            SALES_REPORT,
            USER_ACTIVITY_REPORT,
            BOOK_POPULARITY_REPORT,
            REVENUE_REPORT,
            OVERDUE_REPORT,
            INVENTORY_REPORT,
            FINANCIAL_SUMMARY,
            CUSTOM_REPORT
        }
        
        public enum ExportFormat {
            PDF, EXCEL, CSV, JSON
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportResponse {
        private String reportId;
        private String reportTitle;
        private ReportRequest.ReportType reportType;
        private ReportRequest.ExportFormat exportFormat;
        private String fileName;
        private String downloadUrl;
        private ReportStatus status;
        private LocalDateTime generatedAt;
        private String generatedBy;
        private Long fileSizeBytes;
        private String errorMessage;
        
        public enum ReportStatus {
            GENERATING, COMPLETED, FAILED, EXPIRED
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanReportData {
        private Long totalLoans;
        private Long activeLoans;
        private Long returnedLoans;
        private Long overdueLoans;
        private Double averageLoanDuration;
        private BigDecimal totalFinesCollected;
        private List<LoanSummaryItem> loansByCategory;
        private List<LoanSummaryItem> loansByMonth;
        private List<PopularBookItem> mostBorrowedBooks;
        private List<UserLoanSummary> topBorrowers;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class LoanSummaryItem {
            private String label;
            private Long count;
            private Double percentage;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PopularBookItem {
            private String bookTitle;
            private String author;
            private String category;
            private Long borrowCount;
            private Double rating;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserLoanSummary {
            private String userName;
            private String email;
            private Long totalLoans;
            private Long currentLoans;
            private BigDecimal totalFines;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesReportData {
        private BigDecimal totalRevenue;
        private BigDecimal totalProfit;
        private Long totalOrders;
        private Long totalItemsSold;
        private BigDecimal averageOrderValue;
        private List<SalesSummaryItem> salesByCategory;
        private List<SalesSummaryItem> salesByMonth;
        private List<TopSellingBookItem> topSellingBooks;
        private List<CustomerSummary> topCustomers;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SalesSummaryItem {
            private String label;
            private BigDecimal revenue;
            private Long quantity;
            private Double percentage;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TopSellingBookItem {
            private String bookTitle;
            private String author;
            private String category;
            private Long quantitySold;
            private BigDecimal revenue;
            private BigDecimal avgPrice;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CustomerSummary {
            private String customerName;
            private String email;
            private Long totalOrders;
            private BigDecimal totalSpent;
            private LocalDate lastOrderDate;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityReportData {
        private Long totalActiveUsers;
        private Long newUsersCount;
        private Long returningUsersCount;
        private Double userRetentionRate;
        private List<UserActivityItem> usersByRegistrationMonth;
        private List<UserActivityItem> usersByActivity;
        private List<UserEngagementSummary> topActiveUsers;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserActivityItem {
            private String label;
            private Long count;
            private Double percentage;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserEngagementSummary {
            private String userName;
            private String email;
            private LocalDate registrationDate;
            private LocalDateTime lastActivity;
            private Long totalLoans;
            private Long totalOrders;
            private BigDecimal totalSpent;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryReportData {
        private Long totalBooks;
        private Long uniqueTitles;
        private Long booksInStock;
        private Long booksOutOfStock;
        private BigDecimal totalInventoryValue;
        private List<CategoryInventory> inventoryByCategory;
        private List<LowStockItem> lowStockBooks;
        private List<OverstockItem> overstockBooks;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CategoryInventory {
            private String categoryName;
            private Long totalBooks;
            private Long availableBooks;
            private BigDecimal totalValue;
            private Double turnoverRate;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class LowStockItem {
            private String bookTitle;
            private String author;
            private String category;
            private Integer currentStock;
            private Integer minimumStock;
            private Integer suggestedReorder;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OverstockItem {
            private String bookTitle;
            private String author;
            private String category;
            private Integer currentStock;
            private Integer maxStock;
            private Integer excessQuantity;
            private BigDecimal tiedUpValue;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialSummaryData {
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
        private BigDecimal profitMargin;
        private BigDecimal finesCollected;
        private BigDecimal refundsIssued;
        private List<FinancialItem> revenueBySource;
        private List<FinancialItem> expensesByCategory;
        private List<MonthlyFinancialSummary> monthlyBreakdown;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FinancialItem {
            private String category;
            private BigDecimal amount;
            private Double percentage;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MonthlyFinancialSummary {
            private String month;
            private BigDecimal revenue;
            private BigDecimal expenses;
            private BigDecimal profit;
            private BigDecimal profitMargin;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverdueReportData {
        private Long totalOverdueItems;
        private BigDecimal totalOutstandingFines;
        private Long usersWithOverdueBooks;
        private Double overdueRate;
        private List<OverdueItem> overdueBooks;
        private List<OverdueUserSummary> usersWithMostOverdue;
        private List<OverdueTrendItem> overdueTrend;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OverdueItem {
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
        public static class OverdueUserSummary {
            private String userName;
            private String email;
            private Long overdueCount;
            private BigDecimal totalFines;
            private LocalDate earliestOverdueDate;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OverdueTrendItem {
            private String period;
            private Long overdueCount;
            private BigDecimal avgFineAmount;
        }
    }
}