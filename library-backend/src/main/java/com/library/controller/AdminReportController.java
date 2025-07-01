package com.library.controller;

import com.library.dto.*;
import com.library.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Reports", description = "APIs for generating and managing reports")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Generate report", 
        description = "Generate a new report based on specified criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Report generation started"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<ReportResponseDTO> generateReport(
            @Valid @RequestBody ReportRequestDTO request) {
        
        log.info("Generating report: {} in format: {}", request.getReportType(), request.getFormat());
        ReportResponseDTO response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }

    @GetMapping("/{reportId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get report status", 
        description = "Get the status and details of a specific report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public BaseResponse<ReportResponseDTO> getReportStatus(
            @Parameter(description = "Report ID") @PathVariable String reportId) {
        
        log.info("Getting report status: {}", reportId);
        ReportResponseDTO response = reportService.getReportStatus(reportId);
        return BaseResponse.success(response);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get available reports", 
        description = "Get list of available reports for current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available reports retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<ReportResponseDTO>> getAvailableReports() {
        log.info("Getting available reports");
        List<ReportResponseDTO> reports = reportService.getAvailableReports();
        return BaseResponse.success(reports);
    }

    @GetMapping("/{reportId}/download")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get report download URL", 
        description = "Get download URL for a completed report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Download URL retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Report not found"),
        @ApiResponse(responseCode = "409", description = "Report not ready for download")
    })
    public BaseResponse<String> getReportDownloadUrl(
            @Parameter(description = "Report ID") @PathVariable String reportId) {
        
        log.info("Getting download URL for report: {}", reportId);
        String downloadUrl = reportService.getReportDownloadUrl(reportId);
        return BaseResponse.success(downloadUrl);
    }

    @DeleteMapping("/{reportId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Delete report", 
        description = "Delete a report and its associated files"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public BaseResponse<String> deleteReport(
            @Parameter(description = "Report ID") @PathVariable String reportId) {
        
        log.info("Deleting report: {}", reportId);
        reportService.deleteReport(reportId);
        return BaseResponse.success("Report deleted successfully");
    }

    @GetMapping("/types")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get supported report types", 
        description = "Get list of supported report types"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report types retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<String>> getSupportedReportTypes() {
        log.info("Getting supported report types");
        List<String> types = reportService.getSupportedReportTypes();
        return BaseResponse.success(types);
    }

    @GetMapping("/formats")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get supported formats", 
        description = "Get list of supported export formats"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export formats retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<String>> getSupportedFormats() {
        log.info("Getting supported export formats");
        List<String> formats = reportService.getSupportedFormats();
        return BaseResponse.success(formats);
    }

    // Predefined report endpoints for common use cases

    @PostMapping("/loan-summary")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Generate loan summary report", 
        description = "Generate a comprehensive loan summary report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Loan report generation started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<ReportResponseDTO> generateLoanSummaryReport(
            @Parameter(description = "Export format") 
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        log.info("Generating loan summary report in format: {}", format);
        
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("LOAN_REPORT")
                .format(format)
                .title("Loan Summary Report")
                .includeCharts(true)
                .includeDetails(true)
                .includeSummary(true)
                .build();
        
        ReportResponseDTO response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }

    @PostMapping("/financial-summary")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Generate financial summary report", 
        description = "Generate a comprehensive financial summary report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Financial report generation started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<ReportResponseDTO> generateFinancialSummaryReport(
            @Parameter(description = "Export format") 
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        log.info("Generating financial summary report in format: {}", format);
        
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("FINANCIAL_REPORT")
                .format(format)
                .title("Financial Summary Report")
                .includeCharts(true)
                .includeDetails(true)
                .includeSummary(true)
                .build();
        
        ReportResponseDTO response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }

    @PostMapping("/user-activity")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Generate user activity report", 
        description = "Generate a comprehensive user activity report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "User activity report generation started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<ReportResponseDTO> generateUserActivityReport(
            @Parameter(description = "Export format") 
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        log.info("Generating user activity report in format: {}", format);
        
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("USER_REPORT")
                .format(format)
                .title("User Activity Report")
                .includeCharts(true)
                .includeDetails(true)
                .includeSummary(true)
                .build();
        
        ReportResponseDTO response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }

    @PostMapping("/book-inventory")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Generate book inventory report", 
        description = "Generate a comprehensive book inventory report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Book inventory report generation started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<ReportResponseDTO> generateBookInventoryReport(
            @Parameter(description = "Export format") 
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        log.info("Generating book inventory report in format: {}", format);
        
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("BOOK_REPORT")
                .format(format)
                .title("Book Inventory Report")
                .includeCharts(true)
                .includeDetails(true)
                .includeSummary(true)
                .build();
        
        ReportResponseDTO response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }

    @PostMapping("/order-analysis")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Generate order analysis report", 
        description = "Generate a comprehensive order analysis report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Order analysis report generation started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<ReportResponseDTO> generateOrderAnalysisReport(
            @Parameter(description = "Export format") 
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        log.info("Generating order analysis report in format: {}", format);
        
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("ORDER_REPORT")
                .format(format)
                .title("Order Analysis Report")
                .includeCharts(true)
                .includeDetails(true)
                .includeSummary(true)
                .build();
        
        ReportResponseDTO response = reportService.generateReport(request);
        return BaseResponse.success(response);
    }
}