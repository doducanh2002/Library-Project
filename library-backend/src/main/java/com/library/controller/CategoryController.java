package com.library.controller;

import com.library.dto.BaseResponse;
import com.library.dto.CategoryDTO;
import com.library.dto.CategoryDetailDTO;
import com.library.dto.CreateCategoryRequestDTO;
import com.library.dto.UpdateCategoryRequestDTO;
import com.library.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing book categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    // Public endpoints
    
    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all categories with pagination", description = "Retrieve paginated list of all categories")
    public BaseResponse<Page<CategoryDTO>> getAllCategories(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/v1/categories - Fetching categories with pagination");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        return BaseResponse.success(categories);
    }
    
    @GetMapping("/categories/active")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get active categories", description = "Retrieve list of active categories")
    public BaseResponse<List<CategoryDTO>> getActiveCategories() {
        log.info("GET /api/v1/categories/active - Fetching active categories");
        
        List<CategoryDTO> categories = categoryService.getActiveCategories();
        return BaseResponse.success(categories);
    }
    
    @GetMapping("/categories/root")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get root categories", description = "Retrieve list of root categories (categories without parent)")
    public BaseResponse<List<CategoryDTO>> getRootCategories() {
        log.info("GET /api/v1/categories/root - Fetching root categories");
        
        List<CategoryDTO> categories = categoryService.getRootCategories();
        return BaseResponse.success(categories);
    }
    
    @GetMapping("/categories/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get category by ID", description = "Retrieve detailed information about a specific category")
    public BaseResponse<CategoryDetailDTO> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("GET /api/v1/categories/{} - Fetching category details", categoryId);
        
        CategoryDetailDTO category = categoryService.getCategoryById(categoryId);
        return BaseResponse.success(category);
    }
    
    @GetMapping("/categories/slug/{slug}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get category by slug", description = "Retrieve detailed information about a category by its slug")
    public BaseResponse<CategoryDetailDTO> getCategoryBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        log.info("GET /api/v1/categories/slug/{} - Fetching category details by slug", slug);
        
        CategoryDetailDTO category = categoryService.getCategoryBySlug(slug);
        return BaseResponse.success(category);
    }
    
    @GetMapping("/categories/{categoryId}/subcategories")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get subcategories", description = "Retrieve list of subcategories for a specific category")
    public BaseResponse<List<CategoryDTO>> getSubcategories(
            @Parameter(description = "Parent category ID") @PathVariable Long categoryId) {
        log.info("GET /api/v1/categories/{}/subcategories - Fetching subcategories", categoryId);
        
        List<CategoryDTO> subcategories = categoryService.getSubcategories(categoryId);
        return BaseResponse.success(subcategories);
    }
    
    @GetMapping("/categories/{categoryId}/hierarchy")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get category hierarchy", description = "Retrieve the full hierarchy path from root to the specified category")
    public BaseResponse<List<CategoryDTO>> getCategoryHierarchy(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("GET /api/v1/categories/{}/hierarchy - Fetching category hierarchy", categoryId);
        
        List<CategoryDTO> hierarchy = categoryService.getCategoryHierarchy(categoryId);
        return BaseResponse.success(hierarchy);
    }
    
    // Admin endpoints
    
    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create new category", description = "Create a new category (Admin/Librarian only)")
    public BaseResponse<CategoryDetailDTO> createCategory(
            @RequestBody @Validated CreateCategoryRequestDTO createRequest) {
        log.info("POST /api/v1/admin/categories - Creating new category: {}", createRequest.getName());
        
        CategoryDetailDTO category = categoryService.createCategory(createRequest);
        return BaseResponse.success(category);
    }
    
    @PutMapping("/admin/categories/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Update an existing category (Admin/Librarian only)")
    public BaseResponse<CategoryDetailDTO> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @RequestBody @Validated UpdateCategoryRequestDTO updateRequest) {
        log.info("PUT /api/v1/admin/categories/{} - Updating category", categoryId);
        
        CategoryDetailDTO category = categoryService.updateCategory(categoryId, updateRequest);
        return BaseResponse.success(category);
    }
    
    @DeleteMapping("/admin/categories/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Delete a category (Admin/Librarian only)")
    public BaseResponse<String> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("DELETE /api/v1/admin/categories/{} - Deleting category", categoryId);
        
        categoryService.deleteCategory(categoryId);
        return BaseResponse.success("Category deleted successfully");
    }
    
    @GetMapping("/admin/categories")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all categories for admin", description = "Retrieve all categories with admin details (Admin/Librarian only)")
    public BaseResponse<Page<CategoryDTO>> getAllCategoriesForAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/v1/admin/categories - Fetching categories for admin");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        return BaseResponse.success(categories);
    }
    
    // Utility endpoints
    
    @GetMapping("/categories/check-name")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check category name availability", description = "Check if a category name is available")
    public BaseResponse<Boolean> checkNameAvailability(
            @Parameter(description = "Category name to check") @RequestParam String name) {
        log.debug("GET /api/v1/categories/check-name - Checking availability for name: {}", name);
        
        boolean exists = categoryService.existsByName(name);
        return BaseResponse.success(!exists);
    }
    
    @GetMapping("/categories/check-slug")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check category slug availability", description = "Check if a category slug is available")
    public BaseResponse<Boolean> checkSlugAvailability(
            @Parameter(description = "Category slug to check") @RequestParam String slug) {
        log.debug("GET /api/v1/categories/check-slug - Checking availability for slug: {}", slug);
        
        boolean exists = categoryService.existsBySlug(slug);
        return BaseResponse.success(!exists);
    }
}