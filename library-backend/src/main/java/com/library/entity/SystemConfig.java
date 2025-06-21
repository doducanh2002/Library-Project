package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", unique = true, nullable = false)
    @NotBlank(message = "Config key is required")
    @Size(max = 100, message = "Config key must not exceed 100 characters")
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Config value must not exceed 5000 characters")
    private String configValue;
    
    @Column(name = "config_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConfigType configType;
    
    @Column(name = "description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Column(name = "category")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;
    
    @Column(name = "validation_rules")
    @Size(max = 1000, message = "Validation rules must not exceed 1000 characters")
    private String validationRules;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    public enum ConfigType {
        STRING,
        INTEGER,
        DECIMAL,
        BOOLEAN,
        JSON,
        URL,
        EMAIL,
        PASSWORD
    }
}