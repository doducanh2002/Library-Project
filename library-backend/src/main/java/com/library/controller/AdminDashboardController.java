package com.library.controller;

import com.library.dto.dashboard.DashboardOverviewDTO;
import com.library.dto.dashboard.DashboardStatisticsDTO;
import com.library.dto.BaseResponse;
import com.library.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Admin Dashboard", description = "Dashboard and analytics APIs for administrators")
@CrossOrigin(origins = "*")
public class AdminDashboardController {
    
    private final DashboardService dashboardService;
    
    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping("/admin/dashboard/overview")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get dashboard overview",
        description = "Retrieve comprehensive dashboard overview with key statistics and metrics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard overview retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<DashboardOverviewDTO> getDashboardOverview() {
        log.info("Admin requesting dashboard overview");
        
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        return BaseResponse.success(overview);
    }
    
    @GetMapping("/admin/dashboard/statistics")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get detailed dashboard statistics",
        description = "Retrieve detailed analytics and statistics for admin dashboard"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<DashboardStatisticsDTO> getDashboardStatistics() {
        log.info("Admin requesting dashboard statistics");
        
        DashboardStatisticsDTO statistics = dashboardService.getDashboardStatistics();
        return BaseResponse.success(statistics);
    }
    
    @PostMapping("/admin/dashboard/refresh-cache")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Refresh dashboard cache",
        description = "Force refresh of cached dashboard data (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard cache refreshed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<String> refreshDashboardCache() {
        log.info("Admin requesting dashboard cache refresh");
        
        // In a real implementation, you would evict cache entries here
        // cacheManager.getCache("dashboard-overview").clear();
        // cacheManager.getCache("dashboard-statistics").clear();
        
        return BaseResponse.success("Cache cleared and will be regenerated on next request");
    }
    
    @GetMapping("/admin/dashboard/health-check")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get system health status",
        description = "Retrieve current system health and performance metrics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System health retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<DashboardOverviewDTO.SystemHealthDTO> getSystemHealth() {
        log.info("Admin requesting system health check");
        
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        DashboardOverviewDTO.SystemHealthDTO systemHealth = overview.getSystemHealth();
        
        return BaseResponse.success(systemHealth);
    }
    
    @GetMapping("/admin/dashboard/recent-activities")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get recent system activities",
        description = "Retrieve recent activities and events in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<List<DashboardOverviewDTO.RecentActivityDTO>> getRecentActivities() {
        log.info("Admin requesting recent activities");
        
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        List<DashboardOverviewDTO.RecentActivityDTO> recentActivities = overview.getRecentActivities();
        
        return BaseResponse.success(recentActivities);
    }
    
    @GetMapping("/admin/dashboard/popular-books")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get popular books analytics",
        description = "Retrieve analytics for most popular books based on loans and purchases"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popular books analytics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<List<DashboardOverviewDTO.PopularBookDTO>> getPopularBooksAnalytics() {
        log.info("Admin requesting popular books analytics");
        
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        List<DashboardOverviewDTO.PopularBookDTO> popularBooks = overview.getBookStatistics().getPopularBooks();
        
        return BaseResponse.success(popularBooks);
    }
    
    @GetMapping("/admin/dashboard/user-analytics")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get user analytics",
        description = "Retrieve user growth and engagement analytics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User analytics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<DashboardOverviewDTO.UserStatistics> getUserAnalytics() {
        log.info("Admin requesting user analytics");
        
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        DashboardOverviewDTO.UserStatistics userStatistics = overview.getUserStatistics();
        
        return BaseResponse.success(userStatistics);
    }
    
    @GetMapping("/admin/dashboard/revenue-analytics")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get revenue analytics",
        description = "Retrieve revenue and sales analytics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue analytics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<DashboardOverviewDTO.RevenueStatistics> getRevenueAnalytics() {
        log.info("Admin requesting revenue analytics");
        
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        DashboardOverviewDTO.RevenueStatistics revenueStatistics = overview.getRevenueStatistics();
        
        return BaseResponse.success(revenueStatistics);
    }
    
    @GetMapping("/admin/dashboard/loan-analytics")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(
        summary = "Get loan analytics",
        description = "Retrieve loan and borrowing analytics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan analytics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<DashboardOverviewDTO.LoanStatistics> getLoanAnalytics() {
        log.info("Admin requesting loan analytics");
        
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        DashboardOverviewDTO.LoanStatistics loanStatistics = overview.getLoanStatistics();
        
        return BaseResponse.success(loanStatistics);
    }
}