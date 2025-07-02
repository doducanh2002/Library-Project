package com.library.controller;

import com.library.dto.DashboardOverviewDTO;
import com.library.dto.DashboardStatisticsDTO;
import com.library.dto.SystemConfigDTO;
import com.library.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin dashboard and system management")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get dashboard overview with key metrics")
    public ResponseEntity<DashboardOverviewDTO> getDashboardOverview() {
        log.info("Fetching dashboard overview");
        DashboardOverviewDTO overview = dashboardService.getDashboardOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get detailed statistics for dashboard charts")
    public ResponseEntity<DashboardStatisticsDTO> getDashboardStatistics(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        log.info("Fetching dashboard statistics from {} to {}", startDate, endDate);
        DashboardStatisticsDTO statistics = dashboardService.getDashboardStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/daily-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get daily statistics for a specific date")
    public ResponseEntity<DashboardStatisticsDTO.DailyStatsDTO> getDailyStatistics(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        log.info("Fetching daily statistics for {}", date);
        DashboardStatisticsDTO.DailyStatsDTO dailyStats = dashboardService.getDailyStatistics(date);
        return ResponseEntity.ok(dailyStats);
    }

    @GetMapping("/monthly-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get monthly statistics for a specific month")
    public ResponseEntity<DashboardStatisticsDTO.MonthlyStatsDTO> getMonthlyStatistics(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month) {
        
        log.info("Fetching monthly statistics for {}-{:02d}", year, month);
        DashboardStatisticsDTO.MonthlyStatsDTO monthlyStats = dashboardService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(monthlyStats);
    }

    @GetMapping("/system-config")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system configuration")
    public ResponseEntity<SystemConfigDTO> getSystemConfiguration() {
        log.info("Fetching system configuration");
        SystemConfigDTO config = dashboardService.getSystemConfiguration();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/system-config")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<SystemConfigDTO> updateSystemConfiguration(
            @Valid @RequestBody SystemConfigDTO config) {
        
        log.info("Updating system configuration");
        SystemConfigDTO updatedConfig = dashboardService.updateSystemConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }

    @PostMapping("/refresh-cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refresh dashboard cache")
    public ResponseEntity<String> refreshDashboardCache() {
        log.info("Refreshing dashboard cache");
        dashboardService.refreshDashboardCache();
        return ResponseEntity.ok("Dashboard cache refreshed successfully");
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get system health status")
    public ResponseEntity<String> getSystemHealth() {
        // Basic health check - can be enhanced with actual health metrics
        return ResponseEntity.ok("System is healthy");
    }
}