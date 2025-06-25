package com.library.controller;

import com.library.dto.admin.ReportDTO;
import com.library.dto.BaseResponse;
import com.library.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Admin Reports", description = "Report generation and management APIs")
@CrossOrigin(origins = "*")
public class AdminReportController {
    
    private final ReportService reportService;
    
    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    @PostMapping("/admin/reports/generate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Generate report",
        description = "Generate a report based on specified criteria and format"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.ReportResponse> generateReport(
            @Valid @RequestBody ReportDTO.ReportRequest request) {
        
        log.info("Admin requesting report generation: {}", request.getReportType());
        
        ReportDTO.ReportResponse response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }
    
    @GetMapping("/admin/reports/loans")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get loan report data",
        description = "Retrieve loan statistics and analytics data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan report data retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.LoanReportData> getLoanReport(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Admin requesting loan report data from {} to {}", startDate, endDate);
        
        ReportDTO.LoanReportData reportData = reportService.generateLoanReport(startDate, endDate);
        return BaseResponse.success(reportData);
    }
    
    @GetMapping("/admin/reports/sales")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get sales report data",
        description = "Retrieve sales statistics and analytics data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sales report data retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.SalesReportData> getSalesReport(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Admin requesting sales report data from {} to {}", startDate, endDate);
        
        ReportDTO.SalesReportData reportData = reportService.generateSalesReport(startDate, endDate);
        return BaseResponse.success(reportData);
    }
    
    @GetMapping("/admin/reports/users")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get user activity report data",
        description = "Retrieve user activity statistics and analytics data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User activity report data retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.UserActivityReportData> getUserActivityReport(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Admin requesting user activity report data from {} to {}", startDate, endDate);
        
        ReportDTO.UserActivityReportData reportData = reportService.generateUserActivityReport(startDate, endDate);
        return BaseResponse.success(reportData);
    }
    
    @GetMapping("/admin/reports/overdue")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get overdue report data",
        description = "Retrieve overdue books and fine statistics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue report data retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.OverdueReportData> getOverdueReport() {
        log.info("Admin requesting overdue report data");
        
        ReportDTO.OverdueReportData reportData = reportService.generateOverdueReport();
        return BaseResponse.success(reportData);
    }
    
    @PostMapping("/admin/reports/export")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Export report to file",
        description = "Export report data to PDF, Excel, or CSV format"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report exported successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.ReportResponse> exportReport(
            @Valid @RequestBody ReportDTO.ReportRequest request) {
        
        log.info("Admin requesting report export: {} in format: {}", 
                request.getReportType(), request.getExportFormat());
        
        // Set export format if not specified
        if (request.getExportFormat() == null) {
            request.setExportFormat(ReportDTO.ReportRequest.ExportFormat.PDF);
        }
        
        ReportDTO.ReportResponse response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }
    
    @GetMapping("/admin/reports/download/{reportId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Download generated report",
        description = "Download a previously generated report file"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "Report not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<String> downloadReport(
            @Parameter(description = "Report ID") @PathVariable String reportId) {
        
        log.info("Admin requesting report download for ID: {}", reportId);
        
        // In real implementation, you would:
        // 1. Validate report ID
        // 2. Check if file exists
        // 3. Return file stream or redirect to file URL
        
        return BaseResponse.success("Report download would start here for ID: " + reportId);
    }
    
    @GetMapping("/admin/reports/financial")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get financial report data",
        description = "Retrieve financial summary and analytics (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Financial report data retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.FinancialSummaryData> getFinancialReport(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Admin requesting financial report data from {} to {}", startDate, endDate);
        
        // In real implementation, you would call reportService.generateFinancialReport()
        // For now, return mock data
        ReportDTO.FinancialSummaryData reportData = ReportDTO.FinancialSummaryData.builder()
                .totalRevenue(java.math.BigDecimal.valueOf(1000000))
                .totalExpenses(java.math.BigDecimal.valueOf(700000))
                .netProfit(java.math.BigDecimal.valueOf(300000))
                .profitMargin(30.0)
                .finesCollected(java.math.BigDecimal.valueOf(50000))
                .refundsIssued(java.math.BigDecimal.valueOf(10000))
                .revenueBySource(java.util.Collections.emptyList())
                .expensesByCategory(java.util.Collections.emptyList())
                .monthlyBreakdown(java.util.Collections.emptyList())
                .build();
        
        return BaseResponse.success(reportData);
    }
    
    @GetMapping("/admin/reports/templates")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get available report templates",
        description = "Retrieve list of available report templates and their configurations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report templates retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<List<String>> getReportTemplates() {
        log.info("Admin requesting available report templates");
        
        List<String> templates = java.util.Arrays.asList(
                "LOAN_REPORT",
                "SALES_REPORT", 
                "USER_ACTIVITY_REPORT",
                "BOOK_POPULARITY_REPORT",
                "REVENUE_REPORT",
                "OVERDUE_REPORT",
                "INVENTORY_REPORT",
                "FINANCIAL_SUMMARY"
        );
        
        return BaseResponse.success(templates);
    }
    
    @GetMapping("/admin/reports/book-popularity")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get book popularity report",
        description = "Retrieve book popularity statistics and rankings"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book popularity report retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.BookPopularityReportData> getBookPopularityReport(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Limit results") @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Admin requesting book popularity report from {} to {} (limit: {})", startDate, endDate, limit);
        
        ReportDTO.BookPopularityReportData reportData = reportService.generateBookPopularityReport(startDate, endDate, limit);
        return BaseResponse.success(reportData);
    }
    
    @GetMapping("/admin/reports/inventory")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get inventory report",
        description = "Retrieve inventory status and stock analytics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory report retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<ReportDTO.InventoryReportData> getInventoryReport() {
        log.info("Admin requesting inventory report");
        
        ReportDTO.InventoryReportData reportData = reportService.generateInventoryReport();
        return BaseResponse.success(reportData);
    }
}