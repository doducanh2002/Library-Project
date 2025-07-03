package org.aibles.authenservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aibles.authenservice.dto.request.AdminUserSearchRequest;
import org.aibles.authenservice.dto.request.UpdateUserRolesRequest;
import org.aibles.authenservice.dto.request.UpdateUserStatusRequest;
import org.aibles.authenservice.dto.response.AdminUserDTO;
import org.aibles.authenservice.dto.response.BaseResponse;
import org.aibles.authenservice.dto.response.UserStatsDTO;
import org.aibles.authenservice.service.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@CrossOrigin(origins = "*")
@Tag(name = "Admin User Management", description = "Admin operations for user management")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get all users with pagination and filtering")
    public BaseResponse<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Boolean isActivated,
            @RequestParam(required = false) Boolean isLocked,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("Received get all users request with keyword: {}, role: {}, page: {}, size: {}", keyword, role, page, size);
        
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
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), users);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get user details by ID")
    public BaseResponse<AdminUserDTO> getUserById(@PathVariable String userId) {
        log.info("Received get user by ID request for userId: {}", userId);
        AdminUserDTO user = adminUserService.getUserById(userId);
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), user);
    }

    @PutMapping("/{userId}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user account status (activate/deactivate, lock/unlock)")
    public BaseResponse<AdminUserDTO> updateUserStatus(
            @PathVariable String userId,
            @RequestBody @Validated UpdateUserStatusRequest request) {
        
        log.info("Received update user status request for userId: {}", userId);
        request.setUserId(userId);
        AdminUserDTO updatedUser = adminUserService.updateUserStatus(request);
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), updatedUser);
    }

    @PutMapping("/{userId}/roles")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user roles")
    public BaseResponse<AdminUserDTO> updateUserRoles(
            @PathVariable String userId,
            @RequestBody @Validated UpdateUserRolesRequest request) {
        
        log.info("Received update user roles request for userId: {}", userId);
        request.setUserId(userId);
        AdminUserDTO updatedUser = adminUserService.updateUserRoles(request);
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), updatedUser);
    }

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get user statistics for admin dashboard")
    public BaseResponse<UserStatsDTO> getUserStatistics() {
        log.info("Received get user statistics request");
        UserStatsDTO stats = adminUserService.getUserStatistics();
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), stats);
    }

    @GetMapping("/roles")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get all available roles")
    public BaseResponse<List<String>> getAllRoles() {
        log.info("Received get all roles request");
        List<String> roles = adminUserService.getAllRoles();
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), roles);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user account")
    public BaseResponse<String> deleteUser(@PathVariable String userId) {
        log.info("Received delete user request for userId: {}", userId);
        adminUserService.deleteUser(userId);
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), null);
    }

    @PostMapping("/{userId}/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset user password")
    public BaseResponse<String> resetUserPassword(@PathVariable String userId) {
        log.info("Received reset user password request for userId: {}", userId);
        adminUserService.resetUserPassword(userId);
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), null);
    }

    @GetMapping("/inactive")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Get users who haven't logged in for specified days")
    public BaseResponse<List<AdminUserDTO>> getInactiveUsers(
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Received get inactive users request for days: {}", days);
        List<AdminUserDTO> inactiveUsers = adminUserService.getInactiveUsers(days);
        
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), inactiveUsers);
    }
}