package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private String reportId;
    private String reportType;
    private String format;
    private String title;
    private String fileName;
    private String downloadUrl;
    private Long fileSize;
    private String fileSizeFormatted;
    private String status; // GENERATING, COMPLETED, FAILED
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    private String generatedBy;
    private Integer recordCount;
    private String errorMessage;
}