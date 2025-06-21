package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    
    // Overall statistics
    private Long totalBooks;
    private Long totalActiveLoans;
    private Long totalOverdueLoans;
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long totalDocuments;
    
    // Today's activities
    private Long todayLoans;
    private Long todayReturns;
    private Long todayNewUsers;
    private Long todayOrders;
    private BigDecimal todayRevenue;
    
    // This month statistics
    private Long monthlyLoans;
    private Long monthlyReturns;
    private Long monthlyNewUsers;
    private Long monthlyOrders;
    private BigDecimal monthlyRevenue;
    
    // Popular items
    private List<PopularBookDTO> popularBooks;
    private List<PopularCategoryDTO> popularCategories;
    
    // Recent activities
    private List<RecentActivityDTO> recentActivities;
    
    // System health
    private SystemHealthDTO systemHealth;
    
    private LocalDateTime lastUpdated;
}