package com.library.service;

import com.library.dto.DashboardOverviewDTO;
import com.library.dto.DashboardStatisticsDTO;
import com.library.dto.SystemConfigDTO;

import java.time.LocalDate;

public interface DashboardService {
    
    /**
     * Get dashboard overview with key metrics
     */
    DashboardOverviewDTO getDashboardOverview();
    
    /**
     * Get detailed statistics for dashboard charts and graphs
     */
    DashboardStatisticsDTO getDashboardStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get system configuration
     */
    SystemConfigDTO getSystemConfiguration();
    
    /**
     * Update system configuration
     */
    SystemConfigDTO updateSystemConfiguration(SystemConfigDTO config);
    
    /**
     * Get daily statistics for a specific date
     */
    DashboardStatisticsDTO.DailyStatsDTO getDailyStatistics(LocalDate date);
    
    /**
     * Get monthly statistics for a specific month
     */
    DashboardStatisticsDTO.MonthlyStatsDTO getMonthlyStatistics(int year, int month);
    
    /**
     * Refresh dashboard cache
     */
    void refreshDashboardCache();
}