package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {
    
    // Loan Configuration
    private Integer maxLoansPerUser;
    private Integer loanDurationDays;
    private BigDecimal dailyFineAmount;
    private BigDecimal maxFineAmount;
    private Integer gracePeriodDays;
    
    // Order Configuration
    private BigDecimal shippingFee;
    private BigDecimal freeShippingThreshold;
    private BigDecimal taxRate;
    private Integer orderTimeoutMinutes;
    
    // Document Configuration
    private Long maxFileSize; // in bytes
    private String allowedFileTypes;
    private Integer documentsPerPage;
    
    // Notification Configuration
    private Boolean emailNotificationsEnabled;
    private Boolean smsNotificationsEnabled;
    private Integer notificationRetentionDays;
    private Integer dueSoonReminderDays;
    
    // System Configuration
    private String systemName;
    private String systemVersion;
    private String adminEmail;
    private String supportEmail;
    private Boolean maintenanceMode;
    private String maintenanceMessage;
}