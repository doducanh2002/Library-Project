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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing book categories")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    // Public endpoints
    
    @GetMapping("/categories")
    @Operation(summary = "Get all categories with pagination", description = "Retrieve paginated list of all categories")
    public ResponseEntity<BaseResponse<Page<CategoryDTO>>> getAllCategories(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("GET /api/v1/categories - Fetching categories with pagination");
        
        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        
        return ResponseEntity.ok(BaseResponse.<Page<CategoryDTO>>builder()
                .success(true)
                .message("Categories retrieved successfully")
                .data(categories)
                .build());
    }
    
    @GetMapping("/categories/active")
    @Operation(summary = "Get active categories", description = "Retrieve list of active categories")
    public ResponseEntity<BaseResponse<List<CategoryDTO>>> getActiveCategories() {
        log.info("GET /api/v1/categories/active - Fetching active categories");
        
        List<CategoryDTO> categories = categoryService.getActiveCategories();
        
        return ResponseEntity.ok(BaseResponse.<List<CategoryDTO>>builder()
                .success(true)
                .message("Active categories retrieved successfully")
                .data(categories)
                .build());
    }
    
    @GetMapping("/categories/root")
    @Operation(summary = "Get root categories", description = "Retrieve list of root categories (categories without parent)")
    public ResponseEntity<BaseResponse<List<CategoryDTO>>> getRootCategories() {
        log.info("GET /api/v1/categories/root - Fetching root categories");
        
        List<CategoryDTO> categories = categoryService.getRootCategories();
        
        return ResponseEntity.ok(BaseResponse.<List<CategoryDTO>>builder()
                .success(true)
                .message("Root categories retrieved successfully")
                .data(categories)
                .build());
    }
    
    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieve detailed information about a specific category")
    public ResponseEntity<BaseResponse<CategoryDetailDTO>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("GET /api/v1/categories/{} - Fetching category details", categoryId);
        
        CategoryDetailDTO category = categoryService.getCategoryById(categoryId);
        
        return ResponseEntity.ok(BaseResponse.<CategoryDetailDTO>builder()
                .success(true)
                .message("Category retrieved successfully")
                .data(category)
                .build());
    }
    
    @GetMapping("/categories/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieve detailed information about a category by its slug")
    public ResponseEntity<BaseResponse<CategoryDetailDTO>> getCategoryBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        log.info("GET /api/v1/categories/slug/{} - Fetching category details by slug", slug);
        
        CategoryDetailDTO category = categoryService.getCategoryBySlug(slug);
        
        return ResponseEntity.ok(BaseResponse.<CategoryDetailDTO>builder()
                .success(true)
                .message("Category retrieved successfully")
                .data(category)
                .build());
    }
    
    @GetMapping("/categories/{categoryId}/subcategories")
    @Operation(summary = "Get subcategories", description = "Retrieve list of subcategories for a specific category")
    public ResponseEntity<BaseResponse<List<CategoryDTO>>> getSubcategories(
            @Parameter(description = "Parent category ID") @PathVariable Long categoryId) {
        log.info("GET /api/v1/categories/{}/subcategories - Fetching subcategories", categoryId);
        
        List<CategoryDTO> subcategories = categoryService.getSubcategories(categoryId);
        
        return ResponseEntity.ok(BaseResponse.<List<CategoryDTO>>builder()
                .success(true)
                .message("Subcategories retrieved successfully")
                .data(subcategories)
                .build());
    }
    
    @GetMapping("/categories/{categoryId}/hierarchy")
    @Operation(summary = "Get category hierarchy", description = "Retrieve the full hierarchy path from root to the specified category")
    public ResponseEntity<BaseResponse<List<CategoryDTO>>> getCategoryHierarchy(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("GET /api/v1/categories/{}/hierarchy - Fetching category hierarchy", categoryId);
        
        List<CategoryDTO> hierarchy = categoryService.getCategoryHierarchy(categoryId);
        
        return ResponseEntity.ok(BaseResponse.<List<CategoryDTO>>builder()
                .success(true)
                .message("Category hierarchy retrieved successfully")
                .data(hierarchy)
                .build());
    }
    
    // Admin endpoints
    
    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create new category", description = "Create a new category (Admin/Librarian only)")
    public ResponseEntity<BaseResponse<CategoryDetailDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO createRequest) {
        log.info("POST /api/v1/admin/categories - Creating new category: {}", createRequest.getName());
        
        CategoryDetailDTO category = categoryService.createCategory(createRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.<CategoryDetailDTO>builder()
                        .success(true)
                        .message("Category created successfully")
                        .data(category)
                        .build());
    }
    
    @PutMapping("/admin/categories/{categoryId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Update an existing category (Admin/Librarian only)")
    public ResponseEntity<BaseResponse<CategoryDetailDTO>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequestDTO updateRequest) {
        log.info("PUT /api/v1/admin/categories/{} - Updating category", categoryId);
        
        CategoryDetailDTO category = categoryService.updateCategory(categoryId, updateRequest);
        
        return ResponseEntity.ok(BaseResponse.<CategoryDetailDTO>builder()
                .success(true)
                .message("Category updated successfully")
                .data(category)
                .build());
    }
    
    @DeleteMapping("/admin/categories/{categoryId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Delete a category (Admin/Librarian only)")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("DELETE /api/v1/admin/categories/{} - Deleting category", categoryId);
        
        categoryService.deleteCategory(categoryId);
        
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .success(true)
                .message("Category deleted successfully")
                .build());
    }
    
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Get all categories for admin", description = "Retrieve all categories with admin details (Admin/Librarian only)")
    public ResponseEntity<BaseResponse<Page<CategoryDTO>>> getAllCategoriesForAdmin(
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        log.info("GET /api/v1/admin/categories - Fetching categories for admin");
        
        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        
        return ResponseEntity.ok(BaseResponse.<Page<CategoryDTO>>builder()
                .success(true)
                .message("Categories retrieved successfully")
                .data(categories)
                .build());
    }
    
    // Utility endpoints
    
    @GetMapping("/categories/check-name")
    @Operation(summary = "Check category name availability", description = "Check if a category name is available")
    public ResponseEntity<BaseResponse<Boolean>> checkNameAvailability(
            @Parameter(description = "Category name to check") @RequestParam String name) {
        log.debug("GET /api/v1/categories/check-name - Checking availability for name: {}", name);
        
        boolean exists = categoryService.existsByName(name);
        
        return ResponseEntity.ok(BaseResponse.<Boolean>builder()
                .success(true)
                .message(exists ? "Category name already exists" : "Category name is available")
                .data(!exists)
                .build());
    }
    
    @GetMapping("/categories/check-slug")
    @Operation(summary = "Check category slug availability", description = "Check if a category slug is available")
    public ResponseEntity<BaseResponse<Boolean>> checkSlugAvailability(
            @Parameter(description = "Category slug to check") @RequestParam String slug) {
        log.debug("GET /api/v1/categories/check-slug - Checking availability for slug: {}", slug);
        
        boolean exists = categoryService.existsBySlug(slug);
        
        return ResponseEntity.ok(BaseResponse.<Boolean>builder()
                .success(true)
                .message(exists ? "Category slug already exists" : "Category slug is available")
                .data(!exists)
                .build());
    }
}