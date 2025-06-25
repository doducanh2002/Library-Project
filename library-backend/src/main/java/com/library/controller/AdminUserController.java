package com.library.controller;

import com.library.dto.admin.UserManagementDTO;
import com.library.dto.BaseResponse;
import com.library.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Admin User Management", description = "User management APIs for administrators")
@CrossOrigin(origins = "*")
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }
    
    @GetMapping("/admin/users")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get all users",
        description = "Retrieve paginated list of all users with optional search criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BaseResponse<Page<UserManagementDTO>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Username filter") @RequestParam(required = false) String username,
            @Parameter(description = "Email filter") @RequestParam(required = false) String email,
            @Parameter(description = "Role filter") @RequestParam(required = false) String role,
            @Parameter(description = "Status filter") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Risk level filter") @RequestParam(required = false) String riskLevel) {
        
        log.info("Admin requesting users list with filters - username: {}, email: {}, role: {}", username, email, role);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        UserManagementDTO.UserSearchCriteria criteria = UserManagementDTO.UserSearchCriteria.builder()
                .username(username)
                .email(email)
                .role(role)
                .isActive(isActive)
                .riskLevel(riskLevel)
                .build();
        
        Page<UserManagementDTO> users = adminUserService.searchUsers(criteria, pageable);
        return BaseResponse.success(users);
    }
    
    @GetMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get user details",
        description = "Retrieve detailed information about a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<UserManagementDTO> getUserDetails(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        log.info("Admin requesting details for user: {}", userId);
        
        UserManagementDTO userDetails = adminUserService.getUserDetails(userId);
        return BaseResponse.success(userDetails);
    }
    
    @PutMapping("/admin/users/{userId}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user status",
        description = "Activate or deactivate a user account (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<UserManagementDTO> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserManagementDTO.UserStatusUpdateRequest request) {
        
        log.info("Admin updating status for user: {} to {}", userId, request.getIsActive());
        
        UserManagementDTO updatedUser = adminUserService.updateUserStatus(userId, request);
        return BaseResponse.success(updatedUser);
    }
    
    @PutMapping("/admin/users/{userId}/role")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user role",
        description = "Change user's role assignment (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User role updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public BaseResponse<UserManagementDTO> updateUserRole(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserManagementDTO.UserRoleUpdateRequest request) {
        
        log.info("Admin updating role for user: {} to {}", userId, request.getRole());
        
        UserManagementDTO updatedUser = adminUserService.updateUserRole(userId, request);
        return BaseResponse.success(updatedUser);
    }
    
    @GetMapping("/admin/users/statistics")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get user statistics",
        description = "Retrieve overall user statistics and analytics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<UserManagementDTO.UserStatistics> getUserStatistics() {
        log.info("Admin requesting user statistics");
        
        UserManagementDTO.UserStatistics statistics = adminUserService.getUserStatistics();
        return BaseResponse.success(statistics);
    }
    
    @GetMapping("/admin/users/risk-analysis")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get user risk analysis",
        description = "Retrieve risk analysis for users based on borrowing patterns"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Risk analysis retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<Page<UserManagementDTO.UserRiskAnalysis>> getUserRiskAnalysis(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Risk level filter") @RequestParam(required = false) String riskLevel) {
        
        log.info("Admin requesting user risk analysis");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "riskScore"));
        Page<UserManagementDTO.UserRiskAnalysis> riskAnalysis = adminUserService.getUserRiskAnalysis(riskLevel, pageable);
        
        return BaseResponse.success(riskAnalysis);
    }
    
    @PostMapping("/admin/users/{userId}/notes")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Add user note",
        description = "Add administrative note to user profile"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Note added successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<UserManagementDTO> addUserNote(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserManagementDTO.AddNoteRequest request,
            @RequestAttribute("userId") Long adminUserId) {
        
        log.info("Admin {} adding note to user: {}", adminUserId, userId);
        
        UserManagementDTO updatedUser = adminUserService.addUserNote(userId, request, adminUserId);
        return BaseResponse.success(updatedUser);
    }
    
    @GetMapping("/admin/users/activity-report")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(
        summary = "Get user activity report",
        description = "Retrieve detailed user activity and engagement report"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity report retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges")
    })
    public BaseResponse<UserManagementDTO.UserActivityReport> getUserActivityReport(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) String endDate) {
        
        log.info("Admin requesting user activity report from {} to {}", startDate, endDate);
        
        UserManagementDTO.UserActivityReport report = adminUserService.getUserActivityReport(startDate, endDate);
        return BaseResponse.success(report);
    }
}