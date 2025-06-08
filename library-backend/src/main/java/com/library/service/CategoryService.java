package com.library.service;

import com.library.dto.CategoryDTO;
import com.library.dto.CategoryDetailDTO;
import com.library.dto.CreateCategoryRequestDTO;
import com.library.dto.UpdateCategoryRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    
    // CRUD Operations
    CategoryDetailDTO createCategory(CreateCategoryRequestDTO createRequest);
    
    CategoryDetailDTO getCategoryById(Long id);
    
    CategoryDetailDTO getCategoryBySlug(String slug);
    
    CategoryDetailDTO updateCategory(Long id, UpdateCategoryRequestDTO updateRequest);
    
    void deleteCategory(Long id);
    
    // Listing methods
    Page<CategoryDTO> getAllCategories(Pageable pageable);
    
    List<CategoryDTO> getActiveCategories();
    
    List<CategoryDTO> getRootCategories();
    
    List<CategoryDTO> getSubcategories(Long parentId);
    
    // Hierarchical operations
    CategoryDetailDTO getCategoryWithSubcategories(Long id);
    
    List<CategoryDTO> getCategoryHierarchy(Long id);
    
    // Validation methods
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
    
    // Statistics
    Long getBookCount(Long categoryId);
    
    Long getTotalBooksInSubcategories(Long categoryId);
}