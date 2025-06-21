package com.library.repository;

import com.library.entity.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    boolean existsByConfigKey(String configKey);
    
    List<SystemConfig> findByIsPublicTrue();
    
    List<SystemConfig> findByIsActiveTrue();
    
    List<SystemConfig> findByCategory(String category);
    
    List<SystemConfig> findByCategoryAndIsActiveTrue(String category);
    
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isPublic = true AND sc.isActive = true")
    List<SystemConfig> findPublicActiveConfigs();
    
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "(:category IS NULL OR sc.category = :category) AND " +
           "(:isPublic IS NULL OR sc.isPublic = :isPublic) AND " +
           "(:isActive IS NULL OR sc.isActive = :isActive) AND " +
           "(:configType IS NULL OR sc.configType = :configType)")
    Page<SystemConfig> findWithFilters(
            @Param("category") String category,
            @Param("isPublic") Boolean isPublic,
            @Param("isActive") Boolean isActive,
            @Param("configType") SystemConfig.ConfigType configType,
            Pageable pageable);
    
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "LOWER(sc.configKey) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sc.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<SystemConfig> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT sc.category FROM SystemConfig sc WHERE sc.category IS NOT NULL ORDER BY sc.category")
    List<String> findAllCategories();
}