package com.library.service;

import com.library.dto.DashboardOverviewDTO;
import com.library.dto.SystemStatsDTO;
import com.library.dto.DashboardActivityDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    
    /**
     * Get comprehensive dashboard overview with all key metrics
     */
    DashboardOverviewDTO getDashboardOverview();
    
    /**
     * Get system statistics and health metrics
     */
    SystemStatsDTO getSystemStatistics();
    
    /**
     * Get recent system activities
     */
    List<DashboardActivityDTO> getRecentActivities(int limit);
    
    /**
     * Get user registration trend for specified period
     */
    Map<String, Long> getUserRegistrationTrend(int days);
    
    /**
     * Get loan trend for specified period
     */
    Map<String, Long> getLoanTrend(int days);
    
    /**
     * Get revenue trend for specified period
     */
    Map<String, Object> getRevenueTrend(int days);
    
    /**
     * Get book category distribution
     */
    Map<String, Long> getBookCategoryDistribution();
    
    /**
     * Get order status distribution
     */
    Map<String, Long> getOrderStatusDistribution();
    
    /**
     * Get loan status distribution
     */
    Map<String, Long> getLoanStatusDistribution();
    
    /**
     * Get top performing books
     */
    List<Map<String, Object>> getTopPerformingBooks(int limit);
    
    /**
     * Get most active users
     */
    List<Map<String, Object>> getMostActiveUsers(int limit);
    
    /**
     * Get system alerts and warnings
     */
    Map<String, Object> getSystemAlerts();
    
    /**
     * Get performance metrics for specified time range
     */
    Map<String, Object> getPerformanceMetrics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get financial summary
     */
    Map<String, Object> getFinancialSummary(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Check system health
     */
    Boolean checkSystemHealth();
    
    /**
     * Get cache statistics
     */
    Map<String, Object> getCacheStatistics();
}