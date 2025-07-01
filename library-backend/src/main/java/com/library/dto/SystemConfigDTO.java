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
public class SystemConfigDTO {
    private String key;
    private String value;
    private String dataType; // STRING, INTEGER, BOOLEAN, JSON
    private String category; // SYSTEM, LIBRARY, PAYMENT, NOTIFICATION, etc.
    private String description;
    private Boolean editable;
    private Boolean sensitive; // For passwords, API keys, etc.
    private String defaultValue;
    private Map<String, Object> validationRules;
    private LocalDateTime lastModified;
    private String lastModifiedBy;
}