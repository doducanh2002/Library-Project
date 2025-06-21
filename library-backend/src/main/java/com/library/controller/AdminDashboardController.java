package com.library.controller;

import com.library.dto.response.*;
import com.library.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Admin Dashboard and Statistics APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final ReportService reportService;

    @GetMapping("/stats")
    @Operation(
        summary = "Get dashboard statistics", 
        description = "Retrieve overall system statistics for admin dashboard"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<DashboardStatsDTO>> getDashboardStats() {
        log.info("Admin requesting dashboard statistics");
        
        try {
            DashboardStatsDTO stats = reportService.getDashboardStats();
            return ResponseEntity.ok(BaseResponse.success(stats, "Dashboard statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving dashboard statistics", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/reports/loans")
    @Operation(
        summary = "Generate loan report", 
        description = "Generate detailed loan report for specified date range"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<ReportDataDTO>> getLoanReport(
            @Parameter(description = "Start date (YYYY-MM-DD HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date (YYYY-MM-DD HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        log.info("Admin requesting loan report from {} to {}", fromDate, toDate);
        
        if (fromDate.isAfter(toDate)) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_DATE_RANGE"));
        }
        
        try {
            ReportDataDTO report = reportService.generateLoanReport(fromDate, toDate);
            return ResponseEntity.ok(BaseResponse.success(report, "Loan report generated successfully"));
        } catch (Exception e) {
            log.error("Error generating loan report", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/reports/orders")
    @Operation(
        summary = "Generate order report", 
        description = "Generate detailed order and revenue report for specified date range"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<ReportDataDTO>> getOrderReport(
            @Parameter(description = "Start date (YYYY-MM-DD HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date (YYYY-MM-DD HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        log.info("Admin requesting order report from {} to {}", fromDate, toDate);
        
        if (fromDate.isAfter(toDate)) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_DATE_RANGE"));
        }
        
        try {
            ReportDataDTO report = reportService.generateOrderReport(fromDate, toDate);
            return ResponseEntity.ok(BaseResponse.success(report, "Order report generated successfully"));
        } catch (Exception e) {
            log.error("Error generating order report", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/system-health")
    @Operation(
        summary = "Get system health status", 
        description = "Retrieve current system health and performance metrics"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SystemHealthDTO>> getSystemHealth() {
        log.info("Admin requesting system health status");
        
        try {
            DashboardStatsDTO stats = reportService.getDashboardStats();
            SystemHealthDTO health = stats.getSystemHealth();
            return ResponseEntity.ok(BaseResponse.success(health, "System health retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving system health", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/recent-activities")
    @Operation(
        summary = "Get recent activities", 
        description = "Retrieve recent system activities for dashboard"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<java.util.List<RecentActivityDTO>>> getRecentActivities() {
        log.info("Admin requesting recent activities");
        
        try {
            DashboardStatsDTO stats = reportService.getDashboardStats();
            java.util.List<RecentActivityDTO> activities = stats.getRecentActivities();
            return ResponseEntity.ok(BaseResponse.success(activities, "Recent activities retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving recent activities", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/popular-books")
    @Operation(
        summary = "Get popular books", 
        description = "Retrieve most popular books based on loans and orders"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<java.util.List<PopularBookDTO>>> getPopularBooks() {
        log.info("Admin requesting popular books");
        
        try {
            DashboardStatsDTO stats = reportService.getDashboardStats();
            java.util.List<PopularBookDTO> popularBooks = stats.getPopularBooks();
            return ResponseEntity.ok(BaseResponse.success(popularBooks, "Popular books retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving popular books", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/popular-categories")
    @Operation(
        summary = "Get popular categories", 
        description = "Retrieve most popular categories based on usage"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<java.util.List<PopularCategoryDTO>>> getPopularCategories() {
        log.info("Admin requesting popular categories");
        
        try {
            DashboardStatsDTO stats = reportService.getDashboardStats();
            java.util.List<PopularCategoryDTO> popularCategories = stats.getPopularCategories();
            return ResponseEntity.ok(BaseResponse.success(popularCategories, "Popular categories retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving popular categories", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }
}