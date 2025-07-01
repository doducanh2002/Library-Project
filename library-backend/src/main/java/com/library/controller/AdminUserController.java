package com.library.controller;

import com.library.dto.*;
import com.library.service.UserService;
import com.library.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "APIs for managing users by admin")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final DashboardService dashboardService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get all users", 
        description = "Get paginated list of all users for admin management"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Search term for username or email") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role") 
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by status") 
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Page number") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting all users for admin, search: {}, role: {}, active: {}", search, role, active);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<UserDTO> users = userService.getAllUsersForAdmin(search, role, active, pageable);
        return BaseResponse.success(users);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get user details", 
        description = "Get detailed information about a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BaseResponse<AdminUserDetailDTO> getUserDetails(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Getting user details for admin, userId: {}", userId);
        AdminUserDetailDTO userDetail = userService.getUserDetailsForAdmin(userId);
        return BaseResponse.success(userDetail);
    }

    @PutMapping("/{userId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Update user status", 
        description = "Activate or deactivate a user account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BaseResponse<UserDTO> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "New status") @RequestParam Boolean active,
            @Parameter(description = "Reason for status change") @RequestParam(required = false) String reason) {
        
        log.info("Updating user status: {} to {}, reason: {}", userId, active, reason);
        UserDTO user = userService.updateUserStatus(userId, active, reason);
        return BaseResponse.success(user);
    }

    @PutMapping("/{userId}/roles")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Update user roles", 
        description = "Update the roles assigned to a user"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User roles updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BaseResponse<UserDTO> updateUserRoles(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "New roles") @RequestBody List<String> roles) {
        
        log.info("Updating user roles: {} to {}", userId, roles);
        UserDTO user = userService.updateUserRoles(userId, roles);
        return BaseResponse.success(user);
    }

    @PostMapping("/{userId}/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Reset user password", 
        description = "Reset a user's password and send new temporary password"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BaseResponse<String> resetUserPassword(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Send email notification") @RequestParam(defaultValue = "true") Boolean sendEmail) {
        
        log.info("Resetting password for user: {}, sendEmail: {}", userId, sendEmail);
        String temporaryPassword = userService.resetUserPassword(userId, sendEmail);
        return BaseResponse.success("Password reset successfully. Temporary password: " + temporaryPassword);
    }

    @PostMapping("/{userId}/unlock")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Unlock user account", 
        description = "Unlock a locked user account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User account unlocked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BaseResponse<UserDTO> unlockUserAccount(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Unlocking user account: {}", userId);
        UserDTO user = userService.unlockUserAccount(userId);
        return BaseResponse.success(user);
    }

    @GetMapping("/{userId}/activity")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get user activity", 
        description = "Get activity history for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User activity retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BaseResponse<UserActivityDTO> getUserActivity(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Getting user activity: {}", userId);
        UserActivityDTO activity = userService.getUserActivity(userId);
        return BaseResponse.success(activity);
    }

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get user statistics", 
        description = "Get comprehensive user statistics for admin dashboard"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<UserStatisticsDTO> getUserStatistics() {
        log.info("Getting user statistics");
        UserStatisticsDTO stats = userService.getUserStatistics();
        return BaseResponse.success(stats);
    }

    @GetMapping("/most-active")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get most active users", 
        description = "Get list of most active users based on loans and orders"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Most active users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<Map<String, Object>>> getMostActiveUsers(
            @Parameter(description = "Number of users to return") 
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting top {} active users", limit);
        List<Map<String, Object>> users = dashboardService.getMostActiveUsers(limit);
        return BaseResponse.success(users);
    }

    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get recently registered users", 
        description = "Get list of recently registered users"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<UserDTO>> getRecentUsers(
            @Parameter(description = "Number of users to return") 
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting {} recent users", limit);
        List<UserDTO> users = userService.getRecentUsers(limit);
        return BaseResponse.success(users);
    }

    @PostMapping("/bulk-action")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Perform bulk action on users", 
        description = "Perform bulk actions like activate, deactivate, or delete multiple users"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bulk action completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<BulkActionResultDTO> performBulkAction(
            @RequestBody BulkUserActionRequestDTO request) {
        
        log.info("Performing bulk action: {} on {} users", request.getAction(), request.getUserIds().size());
        BulkActionResultDTO result = userService.performBulkAction(request);
        return BaseResponse.success(result);
    }

    @PostMapping("/export")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Export users", 
        description = "Export user data in specified format"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User export initiated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<String> exportUsers(
            @Parameter(description = "Export format") 
            @RequestParam(defaultValue = "CSV") String format,
            @Parameter(description = "Include sensitive data") 
            @RequestParam(defaultValue = "false") Boolean includeSensitiveData) {
        
        log.info("Exporting users in format: {}, includeSensitive: {}", format, includeSensitiveData);
        String exportResult = userService.exportUsers(format, includeSensitiveData);
        return BaseResponse.success(exportResult);
    }

    @GetMapping("/roles")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get available roles", 
        description = "Get list of available user roles"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available roles retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<List<String>> getAvailableRoles() {
        log.info("Getting available user roles");
        List<String> roles = userService.getAvailableRoles();
        return BaseResponse.success(roles);
    }

    @PostMapping("/{userId}/impersonate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Impersonate user", 
        description = "Impersonate a user for troubleshooting purposes"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User impersonation started"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public BaseResponse<Map<String, Object>> impersonateUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Impersonating user: {}", userId);
        Map<String, Object> impersonationData = userService.impersonateUser(userId);
        return BaseResponse.success(impersonationData);
    }

    @PostMapping("/stop-impersonation")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Stop impersonation", 
        description = "Stop current user impersonation session"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Impersonation stopped successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access forbidden")
    })
    public BaseResponse<String> stopImpersonation() {
        log.info("Stopping user impersonation");
        userService.stopImpersonation();
        return BaseResponse.success("Impersonation stopped successfully");
    }
}