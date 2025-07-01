package com.library.service.impl;

import com.library.dto.SystemConfigDTO;
import com.library.service.SystemConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {
    
    private final ObjectMapper objectMapper;
    
    // In-memory configuration store (in real implementation, use database)
    private final Map<String, SystemConfigDTO> configurations = initializeDefaultConfigurations();
    
    @Override
    @Cacheable(value = "system-configurations")
    public List<SystemConfigDTO> getAllConfigurations() {
        log.info("Getting all system configurations");
        return new ArrayList<>(configurations.values());
    }
    
    @Override
    @Cacheable(value = "system-configurations", key = "#category")
    public List<SystemConfigDTO> getConfigurationsByCategory(String category) {
        log.info("Getting configurations for category: {}", category);
        return configurations.values().stream()
                .filter(config -> category.equals(config.getCategory()))
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "system-configuration", key = "#key")
    public SystemConfigDTO getConfiguration(String key) {
        SystemConfigDTO config = configurations.get(key);
        if (config == null) {
            throw new IllegalArgumentException("Configuration not found: " + key);
        }
        return config;
    }
    
    @Override
    @CacheEvict(value = {"system-configurations", "system-configuration"}, allEntries = true)
    public SystemConfigDTO updateConfiguration(String key, String value) {
        log.info("Updating configuration: {} = {}", key, value);
        
        SystemConfigDTO config = getConfiguration(key);
        
        if (!config.getEditable()) {
            throw new IllegalArgumentException("Configuration is not editable: " + key);
        }
        
        // Validate the value
        validateConfigurationValue(config, value);
        
        // Update the configuration
        config.setValue(value);
        config.setLastModified(LocalDateTime.now());
        config.setLastModifiedBy(getCurrentUserId());
        
        configurations.put(key, config);
        
        log.info("Configuration updated successfully: {}", key);
        return config;
    }
    
    @Override
    @CacheEvict(value = {"system-configurations", "system-configuration"}, allEntries = true)
    public List<SystemConfigDTO> updateConfigurations(Map<String, String> configUpdates) {
        log.info("Updating {} configurations", configUpdates.size());
        
        List<SystemConfigDTO> updatedConfigs = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : configUpdates.entrySet()) {
            try {
                SystemConfigDTO updated = updateConfiguration(entry.getKey(), entry.getValue());
                updatedConfigs.add(updated);
            } catch (Exception e) {
                log.error("Failed to update configuration {}: {}", entry.getKey(), e.getMessage());
                // Continue with other configurations
            }
        }
        
        return updatedConfigs;
    }
    
    @Override
    @CacheEvict(value = {"system-configurations", "system-configuration"}, allEntries = true)
    public SystemConfigDTO resetConfiguration(String key) {
        log.info("Resetting configuration to default: {}", key);
        
        SystemConfigDTO config = getConfiguration(key);
        
        if (!config.getEditable()) {
            throw new IllegalArgumentException("Configuration is not editable: " + key);
        }
        
        config.setValue(config.getDefaultValue());
        config.setLastModified(LocalDateTime.now());
        config.setLastModifiedBy(getCurrentUserId());
        
        configurations.put(key, config);
        
        log.info("Configuration reset successfully: {}", key);
        return config;
    }
    
    @Override
    public String getConfigValue(String key) {
        return getConfiguration(key).getValue();
    }
    
    @Override
    public Integer getConfigValueAsInt(String key) {
        String value = getConfigValue(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Configuration value is not a valid integer: {} = {}", key, value);
            throw new IllegalArgumentException("Configuration value is not a valid integer: " + key);
        }
    }
    
    @Override
    public Boolean getConfigValueAsBoolean(String key) {
        String value = getConfigValue(key);
        return Boolean.parseBoolean(value);
    }
    
    @Override
    public List<String> getAvailableCategories() {
        return configurations.values().stream()
                .map(SystemConfigDTO::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Override
    public String exportConfigurations() {
        log.info("Exporting system configurations");
        
        try {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("timestamp", LocalDateTime.now());
            exportData.put("version", "1.0");
            exportData.put("configurations", configurations);
            
            return objectMapper.writeValueAsString(exportData);
        } catch (JsonProcessingException e) {
            log.error("Failed to export configurations", e);
            throw new RuntimeException("Failed to export configurations", e);
        }
    }
    
    @Override
    @CacheEvict(value = {"system-configurations", "system-configuration"}, allEntries = true)
    public void importConfigurations(String jsonData) {
        log.info("Importing system configurations");
        
        try {
            Map<String, Object> importData = objectMapper.readValue(jsonData, Map.class);
            Map<String, Object> importedConfigs = (Map<String, Object>) importData.get("configurations");
            
            for (Map.Entry<String, Object> entry : importedConfigs.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> configData = (Map<String, Object>) entry.getValue();
                String value = (String) configData.get("value");
                
                if (configurations.containsKey(key)) {
                    updateConfiguration(key, value);
                }
            }
            
            log.info("Configurations imported successfully");
        } catch (Exception e) {
            log.error("Failed to import configurations", e);
            throw new RuntimeException("Failed to import configurations", e);
        }
    }
    
    // Helper methods
    
    private void validateConfigurationValue(SystemConfigDTO config, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration value cannot be null or empty");
        }
        
        switch (config.getDataType().toUpperCase()) {
            case "INTEGER":
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid integer value: " + value);
                }
                break;
            case "BOOLEAN":
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("Invalid boolean value: " + value);
                }
                break;
            case "JSON":
                try {
                    objectMapper.readTree(value);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Invalid JSON value: " + value);
                }
                break;
        }
        
        // Apply additional validation rules if present
        if (config.getValidationRules() != null) {
            applyValidationRules(config, value);
        }
    }
    
    private void applyValidationRules(SystemConfigDTO config, String value) {
        Map<String, Object> rules = config.getValidationRules();
        
        if (rules.containsKey("minLength")) {
            int minLength = (Integer) rules.get("minLength");
            if (value.length() < minLength) {
                throw new IllegalArgumentException("Value must be at least " + minLength + " characters long");
            }
        }
        
        if (rules.containsKey("maxLength")) {
            int maxLength = (Integer) rules.get("maxLength");
            if (value.length() > maxLength) {
                throw new IllegalArgumentException("Value must be no more than " + maxLength + " characters long");
            }
        }
        
        if (rules.containsKey("pattern")) {
            String pattern = (String) rules.get("pattern");
            if (!value.matches(pattern)) {
                throw new IllegalArgumentException("Value does not match required pattern");
            }
        }
        
        if (rules.containsKey("allowedValues")) {
            List<String> allowedValues = (List<String>) rules.get("allowedValues");
            if (!allowedValues.contains(value)) {
                throw new IllegalArgumentException("Value must be one of: " + allowedValues);
            }
        }
    }
    
    private String getCurrentUserId() {
        return "admin"; // Placeholder
    }
    
    private static Map<String, SystemConfigDTO> initializeDefaultConfigurations() {
        Map<String, SystemConfigDTO> configs = new HashMap<>();
        
        // System configurations
        configs.put("system.name", SystemConfigDTO.builder()
                .key("system.name")
                .value("Library Management System")
                .dataType("STRING")
                .category("SYSTEM")
                .description("Name of the library system")
                .editable(true)
                .sensitive(false)
                .defaultValue("Library Management System")
                .build());
        
        configs.put("system.version", SystemConfigDTO.builder()
                .key("system.version")
                .value("1.0.0")
                .dataType("STRING")
                .category("SYSTEM")
                .description("Current system version")
                .editable(false)
                .sensitive(false)
                .defaultValue("1.0.0")
                .build());
        
        configs.put("system.maintenance.mode", SystemConfigDTO.builder()
                .key("system.maintenance.mode")
                .value("false")
                .dataType("BOOLEAN")
                .category("SYSTEM")
                .description("Enable maintenance mode")
                .editable(true)
                .sensitive(false)
                .defaultValue("false")
                .build());
        
        // Library configurations
        configs.put("library.max.loans.per.user", SystemConfigDTO.builder()
                .key("library.max.loans.per.user")
                .value("5")
                .dataType("INTEGER")
                .category("LIBRARY")
                .description("Maximum number of books a user can borrow")
                .editable(true)
                .sensitive(false)
                .defaultValue("5")
                .validationRules(Map.of("min", 1, "max", 20))
                .build());
        
        configs.put("library.loan.duration.days", SystemConfigDTO.builder()
                .key("library.loan.duration.days")
                .value("14")
                .dataType("INTEGER")
                .category("LIBRARY")
                .description("Default loan duration in days")
                .editable(true)
                .sensitive(false)
                .defaultValue("14")
                .validationRules(Map.of("min", 1, "max", 90))
                .build());
        
        configs.put("library.fine.per.day", SystemConfigDTO.builder()
                .key("library.fine.per.day")
                .value("5000")
                .dataType("INTEGER")
                .category("LIBRARY")
                .description("Fine amount per day for overdue books (VND)")
                .editable(true)
                .sensitive(false)
                .defaultValue("5000")
                .validationRules(Map.of("min", 1000, "max", 50000))
                .build());
        
        configs.put("library.max.fine.amount", SystemConfigDTO.builder()
                .key("library.max.fine.amount")
                .value("50000")
                .dataType("INTEGER")
                .category("LIBRARY")
                .description("Maximum fine amount per book (VND)")
                .editable(true)
                .sensitive(false)
                .defaultValue("50000")
                .validationRules(Map.of("min", 10000, "max", 500000))
                .build());
        
        // Notification configurations
        configs.put("notification.email.enabled", SystemConfigDTO.builder()
                .key("notification.email.enabled")
                .value("true")
                .dataType("BOOLEAN")
                .category("NOTIFICATION")
                .description("Enable email notifications")
                .editable(true)
                .sensitive(false)
                .defaultValue("true")
                .build());
        
        configs.put("notification.sms.enabled", SystemConfigDTO.builder()
                .key("notification.sms.enabled")
                .value("false")
                .dataType("BOOLEAN")
                .category("NOTIFICATION")
                .description("Enable SMS notifications")
                .editable(true)
                .sensitive(false)
                .defaultValue("false")
                .build());
        
        configs.put("notification.due.reminder.days", SystemConfigDTO.builder()
                .key("notification.due.reminder.days")
                .value("3")
                .dataType("INTEGER")
                .category("NOTIFICATION")
                .description("Days before due date to send reminder")
                .editable(true)
                .sensitive(false)
                .defaultValue("3")
                .validationRules(Map.of("min", 1, "max", 14))
                .build());
        
        // Payment configurations
        configs.put("payment.vnpay.enabled", SystemConfigDTO.builder()
                .key("payment.vnpay.enabled")
                .value("true")
                .dataType("BOOLEAN")
                .category("PAYMENT")
                .description("Enable VNPay payment gateway")
                .editable(true)
                .sensitive(false)
                .defaultValue("true")
                .build());
        
        configs.put("payment.cash.enabled", SystemConfigDTO.builder()
                .key("payment.cash.enabled")
                .value("true")
                .dataType("BOOLEAN")
                .category("PAYMENT")
                .description("Enable cash payment option")
                .editable(true)
                .sensitive(false)
                .defaultValue("true")
                .build());
        
        // Security configurations
        configs.put("security.password.min.length", SystemConfigDTO.builder()
                .key("security.password.min.length")
                .value("8")
                .dataType("INTEGER")
                .category("SECURITY")
                .description("Minimum password length")
                .editable(true)
                .sensitive(false)
                .defaultValue("8")
                .validationRules(Map.of("min", 6, "max", 32))
                .build());
        
        configs.put("security.session.timeout.minutes", SystemConfigDTO.builder()
                .key("security.session.timeout.minutes")
                .value("120")
                .dataType("INTEGER")
                .category("SECURITY")
                .description("Session timeout in minutes")
                .editable(true)
                .sensitive(false)
                .defaultValue("120")
                .validationRules(Map.of("min", 15, "max", 480))
                .build());
        
        return configs;
    }
}