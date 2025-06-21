package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDataDTO {
    private String reportType; // LOAN_REPORT, ORDER_REPORT, USER_REPORT, FINANCIAL_REPORT
    private String title;
    private String description;
    private LocalDateTime generatedAt;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    
    // Chart data
    private List<String> labels;
    private List<Object> datasets;
    
    // Table data
    private List<String> tableHeaders;
    private List<List<Object>> tableData;
    
    // Summary data
    private Map<String, Object> summary;
    
    // Export options
    private List<String> availableFormats; // PDF, EXCEL, CSV
}