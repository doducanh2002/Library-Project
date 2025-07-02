package org.aibles.authenservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.authenservice.dto.request.AdminUserSearchRequest;
import org.aibles.authenservice.dto.request.UpdateUserRolesRequest;
import org.aibles.authenservice.dto.request.UpdateUserStatusRequest;
import org.aibles.authenservice.dto.response.AdminUserDTO;
import org.aibles.authenservice.dto.response.BaseResponse;
import org.aibles.authenservice.dto.response.UserStatsDTO;
import org.aibles.authenservice.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Admin operations for user management")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get all users with pagination and filtering")
    public ResponseEntity<BaseResponse<Page<AdminUserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Boolean isActivated,
            @RequestParam(required = false) Boolean isLocked,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        AdminUserSearchRequest request = AdminUserSearchRequest.builder()
            .keyword(keyword)
            .isActivated(isActivated)
            .isLocked(isLocked)
            .role(role)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .page(page)
            .size(size)
            .build();
        
        try {
            if (gender != null && !gender.isEmpty()) {
                // Handle gender enum conversion here if needed
            }
        } catch (Exception e) {
            log.warn("Invalid gender parameter: {}", gender);
        }
        
        Page<AdminUserDTO> users = adminUserService.getAllUsers(request);
        
        return ResponseEntity.ok(BaseResponse.<Page<AdminUserDTO>>builder()
            .success(true)
            .message("Users retrieved successfully")
            .data(users)
            .build());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<BaseResponse<AdminUserDTO>> getUserById(@PathVariable String userId) {
        AdminUserDTO user = adminUserService.getUserById(userId);
        
        return ResponseEntity.ok(BaseResponse.<AdminUserDTO>builder()
            .success(true)
            .message("User details retrieved successfully")
            .data(user)
            .build());
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user account status (activate/deactivate, lock/unlock)")
    public ResponseEntity<BaseResponse<AdminUserDTO>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        
        request.setUserId(userId);
        AdminUserDTO updatedUser = adminUserService.updateUserStatus(request);
        
        return ResponseEntity.ok(BaseResponse.<AdminUserDTO>builder()
            .success(true)
            .message("User status updated successfully")
            .data(updatedUser)
            .build());
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user roles")
    public ResponseEntity<BaseResponse<AdminUserDTO>> updateUserRoles(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        
        request.setUserId(userId);
        AdminUserDTO updatedUser = adminUserService.updateUserRoles(request);
        
        return ResponseEntity.ok(BaseResponse.<AdminUserDTO>builder()
            .success(true)
            .message("User roles updated successfully")
            .data(updatedUser)
            .build());
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get user statistics for admin dashboard")
    public ResponseEntity<BaseResponse<UserStatsDTO>> getUserStatistics() {
        UserStatsDTO stats = adminUserService.getUserStatistics();
        
        return ResponseEntity.ok(BaseResponse.<UserStatsDTO>builder()
            .success(true)
            .message("User statistics retrieved successfully")
            .data(stats)
            .build());
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get all available roles")
    public ResponseEntity<BaseResponse<List<String>>> getAllRoles() {
        List<String> roles = adminUserService.getAllRoles();
        
        return ResponseEntity.ok(BaseResponse.<List<String>>builder()
            .success(true)
            .message("Roles retrieved successfully")
            .data(roles)
            .build());
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user account")
    public ResponseEntity<BaseResponse<String>> deleteUser(@PathVariable String userId) {
        adminUserService.deleteUser(userId);
        
        return ResponseEntity.ok(BaseResponse.<String>builder()
            .success(true)
            .message("User deleted successfully")
            .build());
    }

    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset user password")
    public ResponseEntity<BaseResponse<String>> resetUserPassword(@PathVariable String userId) {
        adminUserService.resetUserPassword(userId);
        
        return ResponseEntity.ok(BaseResponse.<String>builder()
            .success(true)
            .message("Password reset successfully. Temporary password sent to user's email.")
            .build());
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get users who haven't logged in for specified days")
    public ResponseEntity<BaseResponse<List<AdminUserDTO>>> getInactiveUsers(
            @RequestParam(defaultValue = "30") int days) {
        
        List<AdminUserDTO> inactiveUsers = adminUserService.getInactiveUsers(days);
        
        return ResponseEntity.ok(BaseResponse.<List<AdminUserDTO>>builder()
            .success(true)
            .message("Inactive users retrieved successfully")
            .data(inactiveUsers)
            .build());
    }
}