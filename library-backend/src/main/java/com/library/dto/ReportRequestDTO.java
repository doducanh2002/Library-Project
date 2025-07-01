package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    
    @NotNull
    private String reportType; // LOAN_REPORT, ORDER_REPORT, USER_REPORT, FINANCIAL_REPORT, etc.
    
    @NotNull
    private String format; // PDF, EXCEL, CSV
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private List<String> filters; // Additional filters
    private Map<String, Object> parameters; // Report-specific parameters
    
    private String title; // Custom report title
    private String description; // Report description
    
    private Boolean includeCharts; // Include charts in report
    private Boolean includeDetails; // Include detailed data
    private Boolean includeSummary; // Include summary section
}