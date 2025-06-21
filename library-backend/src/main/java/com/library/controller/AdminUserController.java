package com.library.controller;

import com.library.client.AuthServiceClient;
import com.library.dto.response.BaseResponse;
import com.library.dto.response.UserSummaryDTO;
import com.library.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "Admin APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(
        summary = "Get all users with pagination", 
        description = "Retrieve paginated list of users with their statistics"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<Page<UserSummaryDTO>>> getAllUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Search by username, email, or full name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive) {
        
        log.info("Admin requesting users list - page: {}, size: {}, search: {}, role: {}, isActive: {}", 
                page, size, search, role, isActive);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<UserSummaryDTO> users = adminUserService.getAllUsers(pageable, search, role, isActive);
            return ResponseEntity.ok(BaseResponse.success(users, "Users retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving users list", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/{userId}")
    @Operation(
        summary = "Get user details", 
        description = "Retrieve detailed information about a specific user"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<UserSummaryDTO>> getUserById(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        
        log.info("Admin requesting user details for userId: {}", userId);
        
        try {
            UserSummaryDTO user = adminUserService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(BaseResponse.success(user, "User details retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving user details for userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @PutMapping("/{userId}/status")
    @Operation(
        summary = "Update user status", 
        description = "Activate or deactivate a user account"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<String>> updateUserStatus(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "New active status")
            @RequestParam Boolean isActive) {
        
        log.info("Admin updating user status - userId: {}, isActive: {}", userId, isActive);
        
        try {
            boolean updated = adminUserService.updateUserStatus(userId, isActive);
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            
            String action = isActive ? "activated" : "deactivated";
            return ResponseEntity.ok(BaseResponse.success("SUCCESS", 
                    String.format("User has been %s successfully", action)));
        } catch (Exception e) {
            log.error("Error updating user status for userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @PutMapping("/{userId}/role")
    @Operation(
        summary = "Update user role", 
        description = "Change user role (USER, LIBRARIAN, ADMIN)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<String>> updateUserRole(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "New role")
            @RequestParam String role) {
        
        log.info("Admin updating user role - userId: {}, role: {}", userId, role);
        
        if (!isValidRole(role)) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_ROLE"));
        }
        
        try {
            boolean updated = adminUserService.updateUserRole(userId, role);
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(BaseResponse.success("SUCCESS", 
                    "User role updated successfully"));
        } catch (Exception e) {
            log.error("Error updating user role for userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/{userId}/loans")
    @Operation(
        summary = "Get user loan history", 
        description = "Retrieve user's loan history with pagination"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<Page<Object>>> getUserLoans(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin requesting loan history for userId: {}", userId);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Object> loans = adminUserService.getUserLoans(userId, pageable);
            return ResponseEntity.ok(BaseResponse.success(loans, "User loans retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving user loans for userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/{userId}/orders")
    @Operation(
        summary = "Get user order history", 
        description = "Retrieve user's order history with pagination"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BaseResponse<Page<Object>>> getUserOrders(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin requesting order history for userId: {}", userId);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Object> orders = adminUserService.getUserOrders(userId, pageable);
            return ResponseEntity.ok(BaseResponse.success(orders, "User orders retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving user orders for userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Delete user account", 
        description = "Permanently delete a user account (use with caution)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<String>> deleteUser(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        
        log.warn("Admin requesting to delete user account - userId: {}", userId);
        
        try {
            boolean deleted = adminUserService.deleteUser(userId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(BaseResponse.success("SUCCESS", 
                    "User account deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting user account for userId: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR"));
        }
    }

    private boolean isValidRole(String role) {
        return role != null && (role.equals("USER") || role.equals("LIBRARIAN") || role.equals("ADMIN"));
    }
}