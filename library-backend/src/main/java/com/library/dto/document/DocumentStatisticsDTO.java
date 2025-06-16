package com.library.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentStatisticsDTO {
    private Long totalDocuments;
    private Long totalDownloads;
    private Long totalSize;
    private String totalSizeFormatted;
    private Map<String, Long> documentsByAccessLevel;
    private Map<String, Long> documentsByFileType;
    private Long documentsUploadedToday;
    private Long documentsUploadedThisWeek;
    private Long documentsUploadedThisMonth;
}