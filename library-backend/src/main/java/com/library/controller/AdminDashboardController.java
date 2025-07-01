package com.library.controller;

import com.library.dto.*;
import com.library.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "APIs for admin dashboard and analytics")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get dashboard overview", 
        description = "Get comprehensive dashboard overview with all key metrics and statistics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard overview retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<DashboardOverviewDTO> getDashboardOverview() {
        log.info("Getting dashboard overview");
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        return BaseResponse.success(overview);
    }

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get system statistics", 
        description = "Get detailed system statistics including performance metrics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<SystemStatsDTO> getSystemStatistics() {
        log.info("Getting system statistics");
        SystemStatsDTO stats = dashboardService.getSystemStatistics();
        return BaseResponse.success(stats);
    }

    @GetMapping("/activities")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get recent activities", 
        description = "Get list of recent system activities"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<DashboardActivityDTO>> getRecentActivities(
            @Parameter(description = "Number of activities to return") 
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Getting recent activities, limit: {}", limit);
        List<DashboardActivityDTO> activities = dashboardService.getRecentActivities(limit);
        return BaseResponse.success(activities);
    }

    @GetMapping("/trends/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get user registration trend", 
        description = "Get user registration trend for specified number of days"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registration trend retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Long>> getUserRegistrationTrend(
            @Parameter(description = "Number of days") 
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("Getting user registration trend for {} days", days);
        Map<String, Long> trend = dashboardService.getUserRegistrationTrend(days);
        return BaseResponse.success(trend);
    }

    @GetMapping("/trends/loans")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get loan trend", 
        description = "Get loan trend for specified number of days"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan trend retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Long>> getLoanTrend(
            @Parameter(description = "Number of days") 
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("Getting loan trend for {} days", days);
        Map<String, Long> trend = dashboardService.getLoanTrend(days);
        return BaseResponse.success(trend);
    }

    @GetMapping("/trends/revenue")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get revenue trend", 
        description = "Get revenue trend for specified number of days"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue trend retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Object>> getRevenueTrend(
            @Parameter(description = "Number of days") 
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("Getting revenue trend for {} days", days);
        Map<String, Object> trend = dashboardService.getRevenueTrend(days);
        return BaseResponse.success(trend);
    }

    @GetMapping("/distributions/categories")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get book category distribution", 
        description = "Get distribution of books by category"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category distribution retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Long>> getBookCategoryDistribution() {
        log.info("Getting book category distribution");
        Map<String, Long> distribution = dashboardService.getBookCategoryDistribution();
        return BaseResponse.success(distribution);
    }

    @GetMapping("/distributions/order-status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get order status distribution", 
        description = "Get distribution of orders by status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status distribution retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Long>> getOrderStatusDistribution() {
        log.info("Getting order status distribution");
        Map<String, Long> distribution = dashboardService.getOrderStatusDistribution();
        return BaseResponse.success(distribution);
    }

    @GetMapping("/distributions/loan-status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get loan status distribution", 
        description = "Get distribution of loans by status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan status distribution retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Long>> getLoanStatusDistribution() {
        log.info("Getting loan status distribution");
        Map<String, Long> distribution = dashboardService.getLoanStatusDistribution();
        return BaseResponse.success(distribution);
    }

    @GetMapping("/top/books")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get top performing books", 
        description = "Get list of top performing books based on borrows and purchases"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Top performing books retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<Map<String, Object>>> getTopPerformingBooks(
            @Parameter(description = "Number of books to return") 
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting top {} performing books", limit);
        List<Map<String, Object>> books = dashboardService.getTopPerformingBooks(limit);
        return BaseResponse.success(books);
    }

    @GetMapping("/top/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get most active users", 
        description = "Get list of most active users based on loans and orders"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Most active users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<Map<String, Object>>> getMostActiveUsers(
            @Parameter(description = "Number of users to return") 
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting top {} active users", limit);
        List<Map<String, Object>> users = dashboardService.getMostActiveUsers(limit);
        return BaseResponse.success(users);
    }

    @GetMapping("/alerts")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get system alerts", 
        description = "Get current system alerts and warnings"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System alerts retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Object>> getSystemAlerts() {
        log.info("Getting system alerts");
        Map<String, Object> alerts = dashboardService.getSystemAlerts();
        return BaseResponse.success(alerts);
    }

    @GetMapping("/performance")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get performance metrics", 
        description = "Get system performance metrics for specified time range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Performance metrics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Object>> getPerformanceMetrics(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        log.info("Getting performance metrics from {} to {}", startDate, endDate);
        Map<String, Object> metrics = dashboardService.getPerformanceMetrics(startDate, endDate);
        return BaseResponse.success(metrics);
    }

    @GetMapping("/financial")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get financial summary", 
        description = "Get financial summary for specified time range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Financial summary retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Object>> getFinancialSummary(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        log.info("Getting financial summary from {} to {}", startDate, endDate);
        Map<String, Object> summary = dashboardService.getFinancialSummary(startDate, endDate);
        return BaseResponse.success(summary);
    }

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Check system health", 
        description = "Check overall system health status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System health status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Object>> checkSystemHealth() {
        log.info("Checking system health");
        Boolean healthy = dashboardService.checkSystemHealth();
        
        Map<String, Object> healthStatus = Map.of(
            "healthy", healthy,
            "status", healthy ? "UP" : "DOWN",
            "timestamp", LocalDateTime.now()
        );
        
        return BaseResponse.success(healthStatus);
    }

    @GetMapping("/cache")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get cache statistics", 
        description = "Get cache performance statistics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Object>> getCacheStatistics() {
        log.info("Getting cache statistics");
        Map<String, Object> stats = dashboardService.getCacheStatistics();
        return BaseResponse.success(stats);
    }
}