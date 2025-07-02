package org.aibles.authenservice.service;

import org.aibles.authenservice.dto.request.AdminUserSearchRequest;
import org.aibles.authenservice.dto.request.UpdateUserRolesRequest;
import org.aibles.authenservice.dto.request.UpdateUserStatusRequest;
import org.aibles.authenservice.dto.response.AdminUserDTO;
import org.aibles.authenservice.dto.response.UserStatsDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminUserService {
    
    /**
     * Get paginated list of users with search and filter
     */
    Page<AdminUserDTO> getAllUsers(AdminUserSearchRequest request);
    
    /**
     * Get user details by ID for admin view
     */
    AdminUserDTO getUserById(String userId);
    
    /**
     * Update user account status (activated/locked)
     */
    AdminUserDTO updateUserStatus(UpdateUserStatusRequest request);
    
    /**
     * Update user roles
     */
    AdminUserDTO updateUserRoles(UpdateUserRolesRequest request);
    
    /**
     * Get user statistics for dashboard
     */
    UserStatsDTO getUserStatistics();
    
    /**
     * Get all available roles
     */
    List<String> getAllRoles();
    
    /**
     * Delete user account (soft delete or hard delete based on business rules)
     */
    void deleteUser(String userId);
    
    /**
     * Reset user password and send email
     */
    void resetUserPassword(String userId);
    
    /**
     * Get users who haven't logged in for specified days
     */
    List<AdminUserDTO> getInactiveUsers(int days);
}