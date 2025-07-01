package com.library.service;

import com.library.dto.ReportRequestDTO;
import com.library.dto.ReportResponseDTO;

import java.util.List;

public interface ReportService {
    
    /**
     * Generate a report based on request parameters
     */
    ReportResponseDTO generateReport(ReportRequestDTO request);
    
    /**
     * Get report status by ID
     */
    ReportResponseDTO getReportStatus(String reportId);
    
    /**
     * Get list of available reports for current user
     */
    List<ReportResponseDTO> getAvailableReports();
    
    /**
     * Get report download URL
     */
    String getReportDownloadUrl(String reportId);
    
    /**
     * Delete a report
     */
    void deleteReport(String reportId);
    
    /**
     * Get supported report types
     */
    List<String> getSupportedReportTypes();
    
    /**
     * Get supported export formats
     */
    List<String> getSupportedFormats();
}