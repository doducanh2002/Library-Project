package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {
    
    // User statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersThisMonth;
    private Long newUsersToday;
    
    // Book statistics
    private Long totalBooks;
    private Long availableBooks;
    private Long borrowedBooks;
    private Long booksAddedThisMonth;
    
    // Loan statistics
    private Long totalLoans;
    private Long activeLoans;
    private Long overdueLoans;
    private Long loansThisMonth;
    private BigDecimal totalFinesCollected;
    private BigDecimal pendingFines;
    
    // Order statistics
    private Long totalOrders;
    private Long pendingOrders;
    private Long completedOrders;
    private Long ordersThisMonth;
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    
    // Document statistics
    private Long totalDocuments;
    private Long publicDocuments;
    private Long documentsUploadedThisMonth;
    private Long totalDownloads;
    
    // Notification statistics
    private Long totalNotifications;
    private Long unreadNotifications;
    private Long notificationsThisWeek;
    
    // System statistics
    private Double systemLoad;
    private Long totalStorageUsed;
    private String storageUsedFormatted;
    private LocalDateTime lastBackupDate;
    private Boolean systemHealthy;
    
    // Recent activity
    private List<DashboardActivityDTO> recentActivities;
    
    // Charts data
    private Map<String, Long> userRegistrationTrend; // Last 7 days
    private Map<String, Long> loanTrend; // Last 7 days  
    private Map<String, BigDecimal> revenueTrend; // Last 7 days
    private Map<String, Long> bookCategoryDistribution;
    private Map<String, Long> orderStatusDistribution;
    private Map<String, Long> loanStatusDistribution;
    
    // Performance metrics
    private Double averageResponseTime;
    private Long totalApiCalls;
    private Long errorRate;
    
    private LocalDateTime generatedAt;
}