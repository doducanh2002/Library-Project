package com.library.controller;

import com.library.dto.*;
import com.library.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/system")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin System Management", description = "APIs for system configuration and management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/config")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get all system configurations", 
        description = "Get all system configurations for admin management"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<SystemConfigDTO>> getAllConfigurations() {
        log.info("Getting all system configurations");
        List<SystemConfigDTO> configs = systemConfigService.getAllConfigurations();
        return BaseResponse.success(configs);
    }

    @GetMapping("/config/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get configurations by category", 
        description = "Get system configurations filtered by category"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<SystemConfigDTO>> getConfigurationsByCategory(
            @Parameter(description = "Configuration category") @PathVariable String category) {
        
        log.info("Getting configurations for category: {}", category);
        List<SystemConfigDTO> configs = systemConfigService.getConfigurationsByCategory(category);
        return BaseResponse.success(configs);
    }

    @GetMapping("/config/{key}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get configuration by key", 
        description = "Get specific system configuration by key"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public BaseResponse<SystemConfigDTO> getConfiguration(
            @Parameter(description = "Configuration key") @PathVariable String key) {
        
        log.info("Getting configuration: {}", key);
        SystemConfigDTO config = systemConfigService.getConfiguration(key);
        return BaseResponse.success(config);
    }

    @PutMapping("/config/{key}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Update configuration", 
        description = "Update a system configuration value"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid configuration value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public BaseResponse<SystemConfigDTO> updateConfiguration(
            @Parameter(description = "Configuration key") @PathVariable String key,
            @Parameter(description = "New configuration value") @RequestBody String value) {
        
        log.info("Updating configuration: {} = {}", key, value);
        SystemConfigDTO config = systemConfigService.updateConfiguration(key, value);
        return BaseResponse.success(config);
    }

    @PutMapping("/config")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Update multiple configurations", 
        description = "Update multiple system configurations at once"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid configuration values"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<SystemConfigDTO>> updateConfigurations(
            @RequestBody Map<String, String> configurations) {
        
        log.info("Updating {} configurations", configurations.size());
        List<SystemConfigDTO> updated = systemConfigService.updateConfigurations(configurations);
        return BaseResponse.success(updated);
    }

    @PostMapping("/config/{key}/reset")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Reset configuration to default", 
        description = "Reset a system configuration to its default value"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration reset successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public BaseResponse<SystemConfigDTO> resetConfiguration(
            @Parameter(description = "Configuration key") @PathVariable String key) {
        
        log.info("Resetting configuration to default: {}", key);
        SystemConfigDTO config = systemConfigService.resetConfiguration(key);
        return BaseResponse.success(config);
    }

    @GetMapping("/config/categories")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get available categories", 
        description = "Get list of available configuration categories"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<String>> getAvailableCategories() {
        log.info("Getting available configuration categories");
        List<String> categories = systemConfigService.getAvailableCategories();
        return BaseResponse.success(categories);
    }

    @GetMapping("/config/export")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Export configurations", 
        description = "Export all system configurations as JSON"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations exported successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<String> exportConfigurations() {
        log.info("Exporting system configurations");
        String exported = systemConfigService.exportConfigurations();
        return BaseResponse.success(exported);
    }

    @PostMapping("/config/import")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Import configurations", 
        description = "Import system configurations from JSON data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations imported successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid JSON data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<String> importConfigurations(
            @Parameter(description = "JSON configuration data") @RequestBody String jsonData) {
        
        log.info("Importing system configurations");
        systemConfigService.importConfigurations(jsonData);
        return BaseResponse.success("Configurations imported successfully");
    }

    // Predefined configuration endpoints for common settings

    @GetMapping("/config/library")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get library settings", 
        description = "Get all library-related configuration settings"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Library settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<SystemConfigDTO>> getLibrarySettings() {
        log.info("Getting library configuration settings");
        List<SystemConfigDTO> configs = systemConfigService.getConfigurationsByCategory("LIBRARY");
        return BaseResponse.success(configs);
    }

    @GetMapping("/config/notification")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get notification settings", 
        description = "Get all notification-related configuration settings"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<SystemConfigDTO>> getNotificationSettings() {
        log.info("Getting notification configuration settings");
        List<SystemConfigDTO> configs = systemConfigService.getConfigurationsByCategory("NOTIFICATION");
        return BaseResponse.success(configs);
    }

    @GetMapping("/config/payment")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get payment settings", 
        description = "Get all payment-related configuration settings"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<SystemConfigDTO>> getPaymentSettings() {
        log.info("Getting payment configuration settings");
        List<SystemConfigDTO> configs = systemConfigService.getConfigurationsByCategory("PAYMENT");
        return BaseResponse.success(configs);
    }

    @GetMapping("/config/security")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get security settings", 
        description = "Get all security-related configuration settings"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Security settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<SystemConfigDTO>> getSecuritySettings() {
        log.info("Getting security configuration settings");
        List<SystemConfigDTO> configs = systemConfigService.getConfigurationsByCategory("SECURITY");
        return BaseResponse.success(configs);
    }

    // System management endpoints

    @PostMapping("/maintenance/enable")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Enable maintenance mode", 
        description = "Enable system maintenance mode"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Maintenance mode enabled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<String> enableMaintenanceMode() {
        log.info("Enabling maintenance mode");
        systemConfigService.updateConfiguration("system.maintenance.mode", "true");
        return BaseResponse.success("Maintenance mode enabled");
    }

    @PostMapping("/maintenance/disable")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Disable maintenance mode", 
        description = "Disable system maintenance mode"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Maintenance mode disabled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<String> disableMaintenanceMode() {
        log.info("Disabling maintenance mode");
        systemConfigService.updateConfiguration("system.maintenance.mode", "false");
        return BaseResponse.success("Maintenance mode disabled");
    }

    @GetMapping("/maintenance/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get maintenance status", 
        description = "Get current maintenance mode status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Maintenance status retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Map<String, Object>> getMaintenanceStatus() {
        log.info("Getting maintenance mode status");
        Boolean maintenanceMode = systemConfigService.getConfigValueAsBoolean("system.maintenance.mode");
        
        Map<String, Object> status = Map.of(
            "maintenanceMode", maintenanceMode,
            "status", maintenanceMode ? "MAINTENANCE" : "ACTIVE"
        );
        
        return BaseResponse.success(status);
    }
}