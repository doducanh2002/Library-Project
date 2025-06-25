package com.library.controller;

import com.library.dto.admin.SystemConfigDTO;
import com.library.dto.BaseResponse;
import com.library.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Admin Configuration", description = "System configuration management APIs")
@CrossOrigin(origins = "*")
public class AdminConfigController {
    
    private final SystemConfigService systemConfigService;
    
    public AdminConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }
    
    @GetMapping("/admin/config/system")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get all system configurations",
        description = "Retrieve paginated list of all system configurations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<Page<SystemConfigDTO>> getAllConfigs(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "category") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Configuration key filter") @RequestParam(required = false) String configKey) {
        
        log.info("Admin requesting system configurations - category: {}, key: {}", category, configKey);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<SystemConfigDTO> configs = systemConfigService.getAllConfigs(category, configKey, pageable);
        return BaseResponse.success(configs);
    }
    
    @GetMapping("/admin/config/system/{configKey}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get specific configuration",
        description = "Retrieve a specific system configuration by key"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Configuration not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<SystemConfigDTO> getConfig(
            @Parameter(description = "Configuration key") @PathVariable String configKey) {
        
        log.info("Admin requesting configuration: {}", configKey);
        
        SystemConfigDTO config = systemConfigService.getConfig(configKey);
        return BaseResponse.success(config);
    }
    
    @PutMapping("/admin/config/system/{configKey}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update configuration",
        description = "Update a specific system configuration (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
        @ApiResponse(responseCode = "404", description = "Configuration not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<SystemConfigDTO> updateConfig(
            @Parameter(description = "Configuration key") @PathVariable String configKey,
            @Valid @RequestBody SystemConfigDTO.ConfigUpdateRequest request,
            @RequestAttribute("userId") Long adminUserId) {
        
        log.info("Admin {} updating configuration: {} to value: {}", adminUserId, configKey, request.getValue());
        
        SystemConfigDTO updatedConfig = systemConfigService.updateConfig(configKey, request, adminUserId);
        return BaseResponse.success(updatedConfig);
    }
    
    @PostMapping("/admin/config/system")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new configuration",
        description = "Create a new system configuration (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Configuration created successfully"),
        @ApiResponse(responseCode = "409", description = "Configuration already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<SystemConfigDTO> createConfig(
            @Valid @RequestBody SystemConfigDTO.ConfigCreateRequest request,
            @RequestAttribute("userId") Long adminUserId) {
        
        log.info("Admin {} creating new configuration: {}", adminUserId, request.getConfigKey());
        
        SystemConfigDTO newConfig = systemConfigService.createConfig(request, adminUserId);
        return BaseResponse.success(newConfig);
    }
    
    @DeleteMapping("/admin/config/system/{configKey}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete configuration",
        description = "Delete a system configuration (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Configuration not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<String> deleteConfig(
            @Parameter(description = "Configuration key") @PathVariable String configKey,
            @RequestAttribute("userId") Long adminUserId) {
        
        log.info("Admin {} deleting configuration: {}", adminUserId, configKey);
        
        systemConfigService.deleteConfig(configKey, adminUserId);
        return BaseResponse.success("Configuration deleted successfully");
    }
    
    @GetMapping("/admin/config/categories")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get configuration categories",
        description = "Retrieve list of all configuration categories"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<List<String>> getConfigCategories() {
        log.info("Admin requesting configuration categories");
        
        List<String> categories = systemConfigService.getConfigCategories();
        return BaseResponse.success(categories);
    }
    
    @PostMapping("/admin/config/batch-update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Batch update configurations",
        description = "Update multiple configurations in a single request (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<List<SystemConfigDTO>> batchUpdateConfigs(
            @Valid @RequestBody List<SystemConfigDTO.ConfigUpdateRequest> requests,
            @RequestAttribute("userId") Long adminUserId) {
        
        log.info("Admin {} performing batch update of {} configurations", adminUserId, requests.size());
        
        List<SystemConfigDTO> updatedConfigs = systemConfigService.batchUpdateConfigs(requests, adminUserId);
        return BaseResponse.success(updatedConfigs);
    }
    
    @GetMapping("/admin/config/loan-policies")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get loan policy configurations",
        description = "Retrieve all loan-related policy configurations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan policies retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<SystemConfigDTO.LoanPolicyConfig> getLoanPolicies() {
        log.info("Admin requesting loan policy configurations");
        
        SystemConfigDTO.LoanPolicyConfig loanPolicies = systemConfigService.getLoanPolicies();
        return BaseResponse.success(loanPolicies);
    }
    
    @PutMapping("/admin/config/loan-policies")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update loan policy configurations",
        description = "Update loan-related policy configurations (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan policies updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<SystemConfigDTO.LoanPolicyConfig> updateLoanPolicies(
            @Valid @RequestBody SystemConfigDTO.LoanPolicyConfig request,
            @RequestAttribute("userId") Long adminUserId) {
        
        log.info("Admin {} updating loan policy configurations", adminUserId);
        
        SystemConfigDTO.LoanPolicyConfig updatedPolicies = systemConfigService.updateLoanPolicies(request, adminUserId);
        return BaseResponse.success(updatedPolicies);
    }
    
    @GetMapping("/admin/config/security-settings")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get security settings",
        description = "Retrieve security-related configurations (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Security settings retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<SystemConfigDTO.SecurityConfig> getSecuritySettings() {
        log.info("Admin requesting security settings");
        
        SystemConfigDTO.SecurityConfig securitySettings = systemConfigService.getSecuritySettings();
        return BaseResponse.success(securitySettings);
    }
    
    @PutMapping("/admin/config/security-settings")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update security settings",
        description = "Update security-related configurations (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Security settings updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<SystemConfigDTO.SecurityConfig> updateSecuritySettings(
            @Valid @RequestBody SystemConfigDTO.SecurityConfig request,
            @RequestAttribute("userId") Long adminUserId) {
        
        log.info("Admin {} updating security settings", adminUserId);
        
        SystemConfigDTO.SecurityConfig updatedSettings = systemConfigService.updateSecuritySettings(request, adminUserId);
        return BaseResponse.success(updatedSettings);
    }
    
    @PostMapping("/admin/config/reset-cache")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Reset configuration cache",
        description = "Clear and reset configuration cache (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration cache reset successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<String> resetConfigCache(@RequestAttribute("userId") Long adminUserId) {
        log.info("Admin {} resetting configuration cache", adminUserId);
        
        systemConfigService.resetCache();
        return BaseResponse.success("Configuration cache reset successfully");
    }
    
    @GetMapping("/admin/config/audit-log")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get configuration audit log",
        description = "Retrieve audit log of configuration changes (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit log retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<Page<SystemConfigDTO.ConfigAuditLog>> getConfigAuditLog(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Configuration key filter") @RequestParam(required = false) String configKey,
            @Parameter(description = "User ID filter") @RequestParam(required = false) Long userId) {
        
        log.info("Admin requesting configuration audit log");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<SystemConfigDTO.ConfigAuditLog> auditLog = systemConfigService.getConfigAuditLog(configKey, userId, pageable);
        
        return BaseResponse.success(auditLog);
    }
}