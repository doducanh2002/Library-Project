package com.library.service;

import com.library.dto.admin.UserManagementDTO;
import com.library.entity.User;
import com.library.entity.Role;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import com.library.repository.RoleRepository;
import com.library.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminUserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getAllUsers(UserManagementDTO.UserSearchCriteria criteria, Pageable pageable) {
        log.info("Fetching users with criteria: {}", criteria);
        
        Specification<User> spec = createUserSpecification(criteria);
        Page<User> users = userRepository.findAll(spec, pageable);
        
        return users.map(this::convertToUserManagementDTO);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public UserManagementDTO getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return convertToUserManagementDTO(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public UserManagementDTO updateUserStatus(Long userId, UserManagementDTO.UserStatusUpdateRequest request) {
        log.info("Updating status for user ID: {} to active: {}", userId, request.getIsActive());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(request.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        log.info("User status updated successfully for user ID: {}. Reason: {}", userId, request.getReason());
        
        return convertToUserManagementDTO(updatedUser);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public UserManagementDTO updateUserRoles(Long userId, UserManagementDTO.UserRoleUpdateRequest request) {
        log.info("Updating roles for user ID: {} to roles: {}", userId, request.getRoles());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Validate and fetch roles
        Set<Role> newRoles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        
        user.setRoles(newRoles);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        log.info("User roles updated successfully for user ID: {}. Reason: {}", userId, request.getReason());
        
        return convertToUserManagementDTO(updatedUser);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public UserManagementDTO updateUser(Long userId, UserManagementDTO userUpdateRequest) {
        log.info("Updating user with ID: {}", userId);
        
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Check if email is being changed and if it's already taken
        if (!existingUser.getEmail().equals(userUpdateRequest.getEmail()) && 
            userRepository.existsByEmail(userUpdateRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken: " + userUpdateRequest.getEmail());
        }
        
        // Check if username is being changed and if it's already taken
        if (!existingUser.getUsername().equals(userUpdateRequest.getUsername()) && 
            userRepository.existsByUsername(userUpdateRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken: " + userUpdateRequest.getUsername());
        }
        
        // Update user fields
        existingUser.setEmail(userUpdateRequest.getEmail());
        existingUser.setFullName(userUpdateRequest.getFullName());
        existingUser.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        existingUser.setAddress(userUpdateRequest.getAddress());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        // Update roles if provided
        if (userUpdateRequest.getRoles() != null && !userUpdateRequest.getRoles().isEmpty()) {
            Set<Role> newRoles = userUpdateRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            existingUser.setRoles(newRoles);
        }
        
        User updatedUser = userRepository.save(existingUser);
        
        log.info("User updated successfully with ID: {}", userId);
        
        return convertToUserManagementDTO(updatedUser);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> searchUsers(String searchTerm, Pageable pageable) {
        log.info("Searching users with term: {}", searchTerm);
        
        Page<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, pageable);
        
        return users.map(this::convertToUserManagementDTO);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Check if user has active loans or pending orders
        boolean hasActiveLoans = user.getLoans().stream()
                .anyMatch(loan -> "BORROWED".equals(loan.getStatus()) || "OVERDUE".equals(loan.getStatus()));
        
        boolean hasPendingOrders = user.getOrders().stream()
                .anyMatch(order -> "PENDING_PAYMENT".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus()));
        
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete user with active loans");
        }
        
        if (hasPendingOrders) {
            throw new IllegalStateException("Cannot delete user with pending orders");
        }
        
        userRepository.delete(user);
        
        log.info("User deleted successfully with ID: {}", userId);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getActiveUsers(Pageable pageable) {
        log.info("Fetching active users");
        
        Page<User> users = userRepository.findByIsActiveTrue(pageable);
        return users.map(this::convertToUserManagementDTO);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getUsersByRole(String roleName, Pageable pageable) {
        log.info("Fetching users with role: {}", roleName);
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        Page<User> users = userRepository.findByRolesContaining(role, pageable);
        return users.map(this::convertToUserManagementDTO);
    }
    
    private Specification<User> createUserSpecification(UserManagementDTO.UserSearchCriteria criteria) {
        if (criteria == null) {
            return null;
        }
        
        Specification<User> spec = Specification.where(null);
        
        if (criteria.getUsername() != null && !criteria.getUsername().trim().isEmpty()) {
            spec = spec.and(UserSpecification.hasUsernameContaining(criteria.getUsername()));
        }
        
        if (criteria.getEmail() != null && !criteria.getEmail().trim().isEmpty()) {
            spec = spec.and(UserSpecification.hasEmailContaining(criteria.getEmail()));
        }
        
        if (criteria.getFullName() != null && !criteria.getFullName().trim().isEmpty()) {
            spec = spec.and(UserSpecification.hasFullNameContaining(criteria.getFullName()));
        }
        
        if (criteria.getIsActive() != null) {
            spec = spec.and(UserSpecification.hasActiveStatus(criteria.getIsActive()));
        }
        
        if (criteria.getRoles() != null && !criteria.getRoles().isEmpty()) {
            spec = spec.and(UserSpecification.hasAnyRole(criteria.getRoles()));
        }
        
        if (criteria.getCreatedAfter() != null) {
            spec = spec.and(UserSpecification.createdAfter(criteria.getCreatedAfter()));
        }
        
        if (criteria.getCreatedBefore() != null) {
            spec = spec.and(UserSpecification.createdBefore(criteria.getCreatedBefore()));
        }
        
        return spec;
    }
    
    private UserManagementDTO convertToUserManagementDTO(User user) {
        UserManagementDTO dto = userMapper.toUserManagementDTO(user);
        
        // Add additional statistics
        dto.setTotalLoans(user.getLoans() != null ? (long) user.getLoans().size() : 0L);
        dto.setActiveLoans(user.getLoans() != null ? 
                user.getLoans().stream()
                        .filter(loan -> "BORROWED".equals(loan.getStatus()) || "OVERDUE".equals(loan.getStatus()))
                        .count() : 0L);
        
        dto.setTotalOrders(user.getOrders() != null ? (long) user.getOrders().size() : 0L);
        
        if (user.getOrders() != null) {
            dto.setTotalSpent(user.getOrders().stream()
                    .filter(order -> !"CANCELLED".equals(order.getStatus()) && !"REFUNDED".equals(order.getStatus()))
                    .map(order -> order.getTotalAmount())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        }
        
        if (user.getLoans() != null) {
            dto.setUnpaidFines(user.getLoans().stream()
                    .filter(loan -> !loan.getFinePaid() && loan.getFineAmount() != null)
                    .map(loan -> loan.getFineAmount())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        }
        
        // Calculate account status and risk level
        dto.setAccountStatus(calculateAccountStatus(user));
        dto.setRiskLevel(calculateRiskLevel(user));
        dto.setLastActivity(user.getLastLogin());
        
        return dto;
    }
    
    private String calculateAccountStatus(User user) {
        if (!user.getIsActive()) {
            return "INACTIVE";
        }
        
        boolean hasOverdueLoans = user.getLoans() != null && 
                user.getLoans().stream().anyMatch(loan -> "OVERDUE".equals(loan.getStatus()));
        
        boolean hasUnpaidFines = user.getLoans() != null &&
                user.getLoans().stream().anyMatch(loan -> !loan.getFinePaid() && 
                        loan.getFineAmount() != null && loan.getFineAmount().compareTo(java.math.BigDecimal.ZERO) > 0);
        
        if (hasOverdueLoans && hasUnpaidFines) {
            return "SUSPENDED";
        }
        
        return "ACTIVE";
    }
    
    private String calculateRiskLevel(User user) {
        int riskScore = 0;
        
        if (user.getLoans() != null) {
            long overdueCount = user.getLoans().stream()
                    .filter(loan -> "OVERDUE".equals(loan.getStatus()))
                    .count();
            
            if (overdueCount > 3) {
                riskScore += 3;
            } else if (overdueCount > 1) {
                riskScore += 2;
            } else if (overdueCount > 0) {
                riskScore += 1;
            }
            
            java.math.BigDecimal totalFines = user.getLoans().stream()
                    .filter(loan -> !loan.getFinePaid() && loan.getFineAmount() != null)
                    .map(loan -> loan.getFineAmount())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            if (totalFines.compareTo(java.math.BigDecimal.valueOf(100000)) > 0) { // > 100k VND
                riskScore += 2;
            } else if (totalFines.compareTo(java.math.BigDecimal.valueOf(50000)) > 0) { // > 50k VND
                riskScore += 1;
            }
        }
        
        if (riskScore >= 4) {
            return "HIGH";
        } else if (riskScore >= 2) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}