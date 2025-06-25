package com.library.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {
    
    private Long id;
    
    @NotBlank(message = "Config key is required")
    @Size(max = 100, message = "Config key must not exceed 100 characters")
    private String configKey;
    
    @NotBlank(message = "Config value is required")
    private String configValue;
    
    @NotNull(message = "Config type is required")
    private ConfigType configType;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Boolean isPublic;
    
    private String category;
    
    private Boolean isEditable;
    
    private String validationRule;
    
    private Long updatedBy;
    
    private String updatedByName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum ConfigType {
        STRING, INTEGER, DECIMAL, BOOLEAN, JSON, EMAIL, URL, PASSWORD
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemConfigUpdateRequest {
        @NotBlank(message = "Config value is required")
        private String configValue;
        
        private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemConfigBatchUpdateRequest {
        @NotNull(message = "Configurations map is required")
        private Map<String, String> configurations;
        
        private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanPolicyDTO {
        private Integer maxBooksPerUser;
        private Integer defaultLoanPeriodDays;
        private java.math.BigDecimal finePerDay;
        private Integer maxRenewalTimes;
        private Integer gracePeriodDays;
        private java.math.BigDecimal maxFineAmount;
        private Boolean autoCalculateFines;
        private Boolean allowRenewalWithFines;
        private Integer reminderDaysBeforeDue;
        private Integer overdueNotificationFrequencyDays;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessRulesDTO {
        private Boolean requireApprovalForLoans;
        private Boolean allowSelfCheckout;
        private Integer minimumStockLevel;
        private Boolean autoOrderWhenLowStock;
        private Integer orderLeadTimeDays;
        private java.math.BigDecimal minimumOrderAmount;
        private Boolean requirePaymentConfirmation;
        private Integer paymentTimeoutMinutes;
        private Boolean enableGuestCheckout;
        private java.math.BigDecimal freeShippingThreshold;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecuritySettingsDTO {
        private Integer passwordMinLength;
        private Boolean requireSpecialCharacters;
        private Boolean requireNumbers;
        private Boolean requireUppercase;
        private Integer maxLoginAttempts;
        private Integer lockoutDurationMinutes;
        private Integer sessionTimeoutMinutes;
        private Boolean enableTwoFactorAuth;
        private Boolean logSecurityEvents;
        private String allowedIpRanges;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettingsDTO {
        private Boolean emailNotificationsEnabled;
        private Boolean smsNotificationsEnabled;
        private String smtpHost;
        private Integer smtpPort;
        private String smtpUsername;
        private Boolean smtpSslEnabled;
        private String fromEmail;
        private String fromName;
        private Boolean sendLoanReminders;
        private Boolean sendOverdueNotifications;
        private Boolean sendOrderConfirmations;
        private Boolean sendPaymentNotifications;
        private Integer reminderDaysBeforeDue;
        private String overdueNotificationTemplate;
        private String loanConfirmationTemplate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceSettingsDTO {
        private Boolean maintenanceMode;
        private String maintenanceMessage;
        private LocalDateTime maintenanceStartTime;
        private LocalDateTime maintenanceEndTime;
        private String maintenanceReason;
        private Boolean allowAdminAccess;
        private Boolean enableBackgroundJobs;
        private Integer backupRetentionDays;
        private String backupSchedule;
        private Boolean autoBackupEnabled;
    }
}