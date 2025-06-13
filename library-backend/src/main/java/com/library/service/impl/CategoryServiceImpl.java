package com.library.service.impl;

import com.library.dto.CategoryDTO;
import com.library.dto.CategoryDetailDTO;
import com.library.dto.CreateCategoryRequestDTO;
import com.library.dto.UpdateCategoryRequestDTO;
import com.library.entity.Category;
import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateBookException;
import com.library.mapper.CategoryMapper;
import com.library.repository.CategoryRepository;
import com.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    @Override
    @Transactional
    public CategoryDetailDTO createCategory(CreateCategoryRequestDTO createRequest) {
        log.info("Creating new category with name: {}", createRequest.getName());
        
        // Validate unique name
        if (categoryRepository.existsByName(createRequest.getName())) {
            throw new DuplicateBookException("Category with name '" + createRequest.getName() + "' already exists");
        }
        
        // Validate unique slug if provided
        if (createRequest.getSlug() != null && categoryRepository.existsBySlug(createRequest.getSlug())) {
            throw new DuplicateBookException("Category with slug '" + createRequest.getSlug() + "' already exists");
        }
        
        Category category = categoryMapper.toEntity(createRequest);
        
        // Set parent category if provided
        if (createRequest.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(createRequest.getParentCategoryId())
                    .orElseThrow(() -> new BookNotFoundException("Parent category not found with id: " + createRequest.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        }
        
        category = categoryRepository.save(category);
        log.info("Successfully created category with id: {}", category.getId());
        
        return enrichCategoryDetailDTO(categoryMapper.toDetailDTO(category));
    }
    
    @Override
    public CategoryDetailDTO getCategoryById(Long id) {
        log.debug("Fetching category by id: {}", id);
        Category category = categoryRepository.findByIdWithSubcategories(id)
                .orElseThrow(() -> new BookNotFoundException("Category not found with id: " + id));
        
        return enrichCategoryDetailDTO(categoryMapper.toDetailDTO(category));
    }
    
    @Override
    public CategoryDetailDTO getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BookNotFoundException("Category not found with slug: " + slug));
        
        category = categoryRepository.findByIdWithSubcategories(category.getId()).orElse(category);
        return enrichCategoryDetailDTO(categoryMapper.toDetailDTO(category));
    }
    
    @Override
    @Transactional
    public CategoryDetailDTO updateCategory(Long id, UpdateCategoryRequestDTO updateRequest) {
        log.info("Updating category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Category not found with id: " + id));
        
        // Validate unique name if changed
        if (updateRequest.getName() != null && 
            !updateRequest.getName().equals(category.getName()) &&
            categoryRepository.existsByName(updateRequest.getName())) {
            throw new DuplicateBookException("Category with name '" + updateRequest.getName() + "' already exists");
        }
        
        // Validate unique slug if changed
        if (updateRequest.getSlug() != null && 
            !updateRequest.getSlug().equals(category.getSlug()) &&
            categoryRepository.existsBySlug(updateRequest.getSlug())) {
            throw new DuplicateBookException("Category with slug '" + updateRequest.getSlug() + "' already exists");
        }
        
        // Validate parent category if changed
        if (updateRequest.getParentCategoryId() != null) {
            if (updateRequest.getParentCategoryId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            
            Category parentCategory = categoryRepository.findById(updateRequest.getParentCategoryId())
                    .orElseThrow(() -> new BookNotFoundException("Parent category not found with id: " + updateRequest.getParentCategoryId()));
            
            // Check for circular reference
            if (isDescendantOf(parentCategory, category)) {
                throw new IllegalArgumentException("Cannot set parent category: would create circular reference");
            }
        }
        
        categoryMapper.updateEntityFromDTO(updateRequest, category);
        
        // Set parent category if provided
        if (updateRequest.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(updateRequest.getParentCategoryId()).orElse(null);
            category.setParentCategory(parentCategory);
        }
        
        category = categoryRepository.save(category);
        log.info("Successfully updated category with id: {}", id);
        
        return enrichCategoryDetailDTO(categoryMapper.toDetailDTO(category));
    }
    
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Category not found with id: " + id));
        
        // Check if category has books
        Long bookCount = categoryRepository.countBooksByCategoryId(id);
        if (bookCount > 0) {
            throw new IllegalStateException("Cannot delete category: " + bookCount + " books are assigned to this category");
        }
        
        // Check if category has subcategories
        List<Category> subcategories = categoryRepository.findByParentCategoryId(id);
        if (!subcategories.isEmpty()) {
            throw new IllegalStateException("Cannot delete category: category has " + subcategories.size() + " subcategories");
        }
        
        categoryRepository.delete(category);
        log.info("Successfully deleted category with id: {}", id);
    }
    
    @Override
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        log.debug("Fetching all categories with pagination");
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(this::enrichCategoryDTO);
    }
    
    @Override
    public List<CategoryDTO> getActiveCategories() {
        log.debug("Fetching active categories");
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categories.stream()
                .map(this::enrichCategoryDTO)
                .toList();
    }
    
    @Override
    public List<CategoryDTO> getRootCategories() {
        log.debug("Fetching root categories");
        List<Category> categories = categoryRepository.findByParentCategoryIsNull();
        return categories.stream()
                .map(this::enrichCategoryDTO)
                .toList();
    }
    
    @Override
    public List<CategoryDTO> getSubcategories(Long parentId) {
        log.debug("Fetching subcategories for parent id: {}", parentId);
        
        // Validate parent exists
        if (!categoryRepository.existsById(parentId)) {
            throw new BookNotFoundException("Parent category not found with id: " + parentId);
        }
        
        List<Category> categories = categoryRepository.findByParentCategoryId(parentId);
        return categories.stream()
                .map(this::enrichCategoryDTO)
                .toList();
    }
    
    @Override
    public CategoryDetailDTO getCategoryWithSubcategories(Long id) {
        log.debug("Fetching category with subcategories for id: {}", id);
        return getCategoryById(id);
    }
    
    @Override
    public List<CategoryDTO> getCategoryHierarchy(Long id) {
        log.debug("Fetching category hierarchy for id: {}", id);
        
        Category category = categoryRepository.findByIdWithParent(id)
                .orElseThrow(() -> new BookNotFoundException("Category not found with id: " + id));
        
        List<CategoryDTO> hierarchy = new ArrayList<>();
        Category current = category;
        
        // Build hierarchy from current to root
        while (current != null) {
            hierarchy.add(0, enrichCategoryDTO(current)); // Add at beginning to reverse order
            current = current.getParentCategory();
        }
        
        return hierarchy;
    }
    
    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    @Override
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }
    
    @Override
    public Long getBookCount(Long categoryId) {
        return categoryRepository.countBooksByCategoryId(categoryId);
    }
    
    @Override
    public Long getTotalBooksInSubcategories(Long categoryId) {
        log.debug("Calculating total books in subcategories for category id: {}", categoryId);
        
        Category category = categoryRepository.findByIdWithSubcategories(categoryId)
                .orElseThrow(() -> new BookNotFoundException("Category not found with id: " + categoryId));
        
        return calculateTotalBooksRecursive(category);
    }
    
    // Helper methods
    
    private CategoryDTO enrichCategoryDTO(Category category) {
        CategoryDTO dto = categoryMapper.toDTO(category);
        dto.setBookCount(categoryRepository.countBooksByCategoryId(category.getId()));
        return dto;
    }
    
    private CategoryDetailDTO enrichCategoryDetailDTO(CategoryDetailDTO dto) {
        dto.setBookCount(categoryRepository.countBooksByCategoryId(dto.getId()));
        dto.setTotalBooksInSubcategories(getTotalBooksInSubcategories(dto.getId()));
        
        // Enrich subcategories
        if (dto.getSubcategories() != null) {
            dto.setSubcategories(
                dto.getSubcategories().stream()
                    .map(subcat -> {
                        subcat.setBookCount(categoryRepository.countBooksByCategoryId(subcat.getId()));
                        return subcat;
                    })
                    .toList()
            );
        }
        
        return dto;
    }
    
    private Long calculateTotalBooksRecursive(Category category) {
        Long total = categoryRepository.countBooksByCategoryId(category.getId());
        
        for (Category subcategory : category.getSubcategories()) {
            total += calculateTotalBooksRecursive(subcategory);
        }
        
        return total;
    }
    
    private boolean isDescendantOf(Category potentialAncestor, Category category) {
        Category current = potentialAncestor.getParentCategory();
        
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
            current = current.getParentCategory();
        }
        
        return false;
    }
}