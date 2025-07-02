package com.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {
    
    // User Statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    
    // Book Statistics
    private Long totalBooks;
    private Long availableBooks;
    private Long lentBooks;
    private Long booksAddedThisMonth;
    
    // Loan Statistics
    private Long totalLoans;
    private Long activeLoans;
    private Long overdueLoans;
    private Long loansToday;
    
    // Order Statistics
    private Long totalOrders;
    private Long pendingOrders;
    private Long completedOrdersToday;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    
    // Document Statistics
    private Long totalDocuments;
    private Long documentsUploadedThisMonth;
    private Long totalDownloads;
    private Long downloadsToday;
    
    // System Statistics
    private Long totalNotifications;
    private Long unreadNotifications;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
}