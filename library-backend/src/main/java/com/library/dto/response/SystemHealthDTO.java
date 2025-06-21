package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  
@AllArgsConstructor
@Builder
public class SystemHealthDTO {
    private String databaseStatus;
    private String redisStatus;
    private String minioStatus;
    private String authServiceStatus;
    private Double cpuUsage;
    private Double memoryUsage;
    private Long diskUsage;
    private Long diskTotal;
    private String overallStatus; // HEALTHY, WARNING, CRITICAL
}