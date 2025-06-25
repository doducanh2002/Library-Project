package com.library.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDTO {
    
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLogin;
    
    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;
    
    // Statistics
    private Long totalLoans;
    private Long activeLoans;
    private Long totalOrders;
    private java.math.BigDecimal totalSpent;
    private java.math.BigDecimal unpaidFines;
    
    // User Activity
    private String accountStatus; // ACTIVE, SUSPENDED, BANNED, INACTIVE
    private String riskLevel; // LOW, MEDIUM, HIGH
    private LocalDateTime lastActivity;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatusUpdateRequest {
        private Boolean isActive;
        private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleUpdateRequest {
        @NotEmpty(message = "At least one role is required")
        private Set<String> roles;
        private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSearchCriteria {
        private String username;
        private String email;
        private String fullName;
        private Boolean isActive;
        private Set<String> roles;
        private LocalDateTime createdAfter;
        private LocalDateTime createdBefore;
        private String accountStatus;
        private String riskLevel;
    }
}