package com.library.service;

import com.library.dto.admin.SystemConfigDTO;
import com.library.entity.SystemConfig;
import com.library.entity.User;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.SystemConfigMapper;
import com.library.repository.SystemConfigRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemConfigService {
    
    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;
    private final SystemConfigMapper systemConfigMapper;
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public Page<SystemConfigDTO> getAllConfigs(Pageable pageable) {
        log.info("Fetching all system configurations");
        
        Page<SystemConfig> configs = systemConfigRepository.findAll(pageable);
        return configs.map(systemConfigMapper::toDTO);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public Page<SystemConfigDTO> getConfigsByCategory(String category, Pageable pageable) {
        log.info("Fetching system configurations for category: {}", category);
        
        Page<SystemConfig> configs = systemConfigRepository.findByCategory(category, pageable);
        return configs.map(systemConfigMapper::toDTO);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public SystemConfigDTO getConfigByKey(String configKey) {
        log.info("Fetching system configuration for key: {}", configKey);
        
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found: " + configKey));
        
        return systemConfigMapper.toDTO(config);
    }
    
    @Cacheable(value = "system-config", key = "#configKey")
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey) {
        log.debug("Getting config value for key: {}", configKey);
        
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }
    
    @Cacheable(value = "system-config", key = "#configKey + '-default'")
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey, String defaultValue) {
        log.debug("Getting config value for key: {} with default: {}", configKey, defaultValue);
        
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "system-config", key = "#configKey")
    public SystemConfigDTO updateConfig(String configKey, SystemConfigDTO.SystemConfigUpdateRequest request) {
        log.info("Updating system configuration: {} to value: {}", configKey, request.getConfigValue());
        
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found: " + configKey));
        
        if (!config.getIsEditable()) {
            throw new IllegalStateException("Configuration '" + configKey + "' is not editable");
        }
        
        // Validate the new value based on config type
        validateConfigValue(config.getConfigType(), request.getConfigValue(), config.getValidationRule());
        
        String oldValue = config.getConfigValue();
        config.setConfigValue(request.getConfigValue());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(getCurrentUserId());
        
        SystemConfig updatedConfig = systemConfigRepository.save(config);
        
        log.info("Configuration '{}' updated from '{}' to '{}'. Reason: {}", 
                configKey, oldValue, request.getConfigValue(), request.getReason());
        
        return systemConfigMapper.toDTO(updatedConfig);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "system-config", allEntries = true)
    public Map<String, SystemConfigDTO> batchUpdateConfigs(SystemConfigDTO.SystemConfigBatchUpdateRequest request) {
        log.info("Batch updating {} system configurations", request.getConfigurations().size());
        
        Map<String, SystemConfigDTO> results = request.getConfigurations().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                SystemConfigDTO.SystemConfigUpdateRequest updateRequest = 
                                        SystemConfigDTO.SystemConfigUpdateRequest.builder()
                                                .configValue(entry.getValue())
                                                .reason(request.getReason())
                                                .build();
                                return updateConfig(entry.getKey(), updateRequest);
                            } catch (Exception e) {
                                log.error("Failed to update config: {}", entry.getKey(), e);
                                throw new RuntimeException("Failed to update config '" + entry.getKey() + "': " + e.getMessage());
                            }
                        }
                ));
        
        log.info("Batch update completed successfully. Reason: {}", request.getReason());
        
        return results;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public SystemConfigDTO createConfig(SystemConfigDTO configDTO) {
        log.info("Creating new system configuration: {}", configDTO.getConfigKey());
        
        if (systemConfigRepository.existsByConfigKey(configDTO.getConfigKey())) {
            throw new IllegalArgumentException("Configuration already exists: " + configDTO.getConfigKey());
        }
        
        SystemConfig config = systemConfigMapper.toEntity(configDTO);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(getCurrentUserId());
        
        SystemConfig savedConfig = systemConfigRepository.save(config);
        
        log.info("System configuration created successfully: {}", configDTO.getConfigKey());
        
        return systemConfigMapper.toDTO(savedConfig);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "system-config", key = "#configKey")
    public void deleteConfig(String configKey) {
        log.info("Deleting system configuration: {}", configKey);
        
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found: " + configKey));
        
        if (!config.getIsEditable()) {
            throw new IllegalStateException("Configuration '" + configKey + "' cannot be deleted");
        }
        
        systemConfigRepository.delete(config);
        
        log.info("System configuration deleted successfully: {}", configKey);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public SystemConfigDTO.LoanPolicyDTO getLoanPolicies() {
        log.info("Fetching loan policy configurations");
        
        return SystemConfigDTO.LoanPolicyDTO.builder()
                .maxBooksPerUser(getIntegerConfig("MAX_BOOKS_PER_USER", 5))
                .defaultLoanPeriodDays(getIntegerConfig("DEFAULT_LOAN_PERIOD_DAYS", 14))
                .finePerDay(getDecimalConfig("FINE_PER_DAY", BigDecimal.valueOf(5000)))
                .maxRenewalTimes(getIntegerConfig("MAX_RENEWAL_TIMES", 2))
                .gracePeriodDays(getIntegerConfig("GRACE_PERIOD_DAYS", 1))
                .maxFineAmount(getDecimalConfig("MAX_FINE_AMOUNT", BigDecimal.valueOf(500000)))
                .autoCalculateFines(getBooleanConfig("AUTO_CALCULATE_FINES", true))
                .allowRenewalWithFines(getBooleanConfig("ALLOW_RENEWAL_WITH_FINES", false))
                .reminderDaysBeforeDue(getIntegerConfig("REMINDER_DAYS_BEFORE_DUE", 3))
                .overdueNotificationFrequencyDays(getIntegerConfig("OVERDUE_NOTIFICATION_FREQUENCY_DAYS", 7))
                .build();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "system-config", allEntries = true)
    public SystemConfigDTO.LoanPolicyDTO updateLoanPolicies(SystemConfigDTO.LoanPolicyDTO loanPolicy) {
        log.info("Updating loan policy configurations");
        
        Map<String, String> configs = Map.of(
                "MAX_BOOKS_PER_USER", loanPolicy.getMaxBooksPerUser().toString(),
                "DEFAULT_LOAN_PERIOD_DAYS", loanPolicy.getDefaultLoanPeriodDays().toString(),
                "FINE_PER_DAY", loanPolicy.getFinePerDay().toString(),
                "MAX_RENEWAL_TIMES", loanPolicy.getMaxRenewalTimes().toString(),
                "GRACE_PERIOD_DAYS", loanPolicy.getGracePeriodDays().toString(),
                "MAX_FINE_AMOUNT", loanPolicy.getMaxFineAmount().toString(),
                "AUTO_CALCULATE_FINES", loanPolicy.getAutoCalculateFines().toString(),
                "ALLOW_RENEWAL_WITH_FINES", loanPolicy.getAllowRenewalWithFines().toString(),
                "REMINDER_DAYS_BEFORE_DUE", loanPolicy.getReminderDaysBeforeDue().toString(),
                "OVERDUE_NOTIFICATION_FREQUENCY_DAYS", loanPolicy.getOverdueNotificationFrequencyDays().toString()
        );
        
        SystemConfigDTO.SystemConfigBatchUpdateRequest request = 
                SystemConfigDTO.SystemConfigBatchUpdateRequest.builder()
                        .configurations(configs)
                        .reason("Loan policy update")
                        .build();
        
        batchUpdateConfigs(request);
        
        log.info("Loan policy configurations updated successfully");
        
        return loanPolicy;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<String> getConfigCategories() {
        log.info("Fetching all configuration categories");
        
        return systemConfigRepository.findDistinctCategories();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStatus() {
        log.info("Fetching system status information");
        
        return Map.of(
                "maintenanceMode", getBooleanConfig("SYSTEM_MAINTENANCE_MODE", false),
                "allowRegistration", getBooleanConfig("ALLOW_USER_REGISTRATION", true),
                "requireEmailVerification", getBooleanConfig("REQUIRE_EMAIL_VERIFICATION", true),
                "maxFileUploadSize", getIntegerConfig("MAX_FILE_UPLOAD_SIZE_MB", 100),
                "sessionTimeout", getIntegerConfig("SESSION_TIMEOUT_MINUTES", 30),
                "backupEnabled", getBooleanConfig("AUTO_BACKUP_ENABLED", true),
                "notificationsEnabled", getBooleanConfig("NOTIFICATION_EMAIL_ENABLED", true)
        );
    }
    
    @CacheEvict(value = "system-config", allEntries = true)
    public void clearConfigCache() {
        log.info("Clearing system configuration cache");
    }
    
    @Transactional(readOnly = true)
    public List<SystemConfigDTO> getPublicConfigs() {
        log.info("Fetching public system configurations");
        
        List<SystemConfig> configs = systemConfigRepository.findByIsPublicTrue();
        return configs.stream()
                .map(systemConfigMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    private void validateConfigValue(String configType, String value, String validationRule) {
        try {
            switch (SystemConfigDTO.ConfigType.valueOf(configType)) {
                case INTEGER:
                    Integer.valueOf(value);
                    break;
                case DECIMAL:
                    new BigDecimal(value);
                    break;
                case BOOLEAN:
                    Boolean.valueOf(value);
                    break;
                case EMAIL:
                    if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        throw new IllegalArgumentException("Invalid email format");
                    }
                    break;
                case URL:
                    if (!value.matches("^https?://.*")) {
                        throw new IllegalArgumentException("Invalid URL format");
                    }
                    break;
                case JSON:
                    // Basic JSON validation (can be enhanced)
                    if (!value.trim().startsWith("{") && !value.trim().startsWith("[")) {
                        throw new IllegalArgumentException("Invalid JSON format");
                    }
                    break;
            }
            
            // Apply custom validation rule if provided
            if (validationRule != null && !validationRule.trim().isEmpty()) {
                if (!value.matches(validationRule)) {
                    throw new IllegalArgumentException("Value does not match validation rule: " + validationRule);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration value: " + e.getMessage());
        }
    }
    
    private Integer getIntegerConfig(String key, Integer defaultValue) {
        String value = getConfigValue(key);
        return value != null ? Integer.valueOf(value) : defaultValue;
    }
    
    private BigDecimal getDecimalConfig(String key, BigDecimal defaultValue) {
        String value = getConfigValue(key);
        return value != null ? new BigDecimal(value) : defaultValue;
    }
    
    private Boolean getBooleanConfig(String key, Boolean defaultValue) {
        String value = getConfigValue(key);
        return value != null ? Boolean.valueOf(value) : defaultValue;
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            Optional<User> user = userRepository.findByUsername(authentication.getName());
            return user.map(User::getId).orElse(null);
        }
        return null;
    }
}