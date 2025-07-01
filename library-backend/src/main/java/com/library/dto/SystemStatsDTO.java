package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsDTO {
    private String systemVersion;
    private LocalDateTime systemStartTime;
    private Long systemUptime; // in seconds
    private String systemUptimeFormatted;
    
    // Performance metrics
    private Double cpuUsage;
    private Double memoryUsage;
    private Long totalMemory;
    private Long usedMemory;
    private String memoryUsageFormatted;
    
    // Database metrics
    private Long databaseConnections;
    private Long maxDatabaseConnections;
    private Double databaseResponseTime;
    private Long databaseSize;
    private String databaseSizeFormatted;
    
    // Storage metrics
    private Long totalStorageSpace;
    private Long usedStorageSpace;
    private Long freeStorageSpace;
    private Double storageUsagePercentage;
    private String totalStorageFormatted;
    private String usedStorageFormatted;
    private String freeStorageFormatted;
    
    // Application metrics
    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
    private Double successRate;
    private Double errorRate;
    private Double averageResponseTime;
    
    // Background tasks
    private Long scheduledTasksCount;
    private Long completedTasksCount;
    private Long failedTasksCount;
    private LocalDateTime lastTaskExecution;
    
    // Cache metrics
    private Long cacheHits;
    private Long cacheMisses;
    private Double cacheHitRate;
    private Long cacheSize;
    
    // MinIO metrics
    private Boolean minioHealthy;
    private Long minioTotalObjects;
    private Long minioTotalSize;
    private String minioTotalSizeFormatted;
    
    // Redis metrics
    private Boolean redisHealthy;
    private Long redisConnections;
    private Long redisMemoryUsage;
    private String redisMemoryUsageFormatted;
    
    // Alerts and warnings
    private Map<String, Object> alerts;
    private Map<String, Object> warnings;
    
    private LocalDateTime generatedAt;
}