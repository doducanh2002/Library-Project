package com.library.service;

import com.library.dto.SystemConfigDTO;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {
    
    /**
     * Get all system configurations
     */
    List<SystemConfigDTO> getAllConfigurations();
    
    /**
     * Get configurations by category
     */
    List<SystemConfigDTO> getConfigurationsByCategory(String category);
    
    /**
     * Get configuration by key
     */
    SystemConfigDTO getConfiguration(String key);
    
    /**
     * Update configuration value
     */
    SystemConfigDTO updateConfiguration(String key, String value);
    
    /**
     * Update multiple configurations
     */
    List<SystemConfigDTO> updateConfigurations(Map<String, String> configurations);
    
    /**
     * Reset configuration to default value
     */
    SystemConfigDTO resetConfiguration(String key);
    
    /**
     * Get configuration value as string
     */
    String getConfigValue(String key);
    
    /**
     * Get configuration value as integer
     */
    Integer getConfigValueAsInt(String key);
    
    /**
     * Get configuration value as boolean
     */
    Boolean getConfigValueAsBoolean(String key);
    
    /**
     * Get available configuration categories
     */
    List<String> getAvailableCategories();
    
    /**
     * Export configurations as JSON
     */
    String exportConfigurations();
    
    /**
     * Import configurations from JSON
     */
    void importConfigurations(String jsonData);
}