package org.aibles.authenservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.authenservice.dto.request.AdminUserSearchRequest;
import org.aibles.authenservice.dto.request.UpdateUserRolesRequest;
import org.aibles.authenservice.dto.request.UpdateUserStatusRequest;
import org.aibles.authenservice.dto.response.AdminUserDTO;
import org.aibles.authenservice.dto.response.UserStatsDTO;
import org.aibles.authenservice.entity.Account;
import org.aibles.authenservice.entity.AccountRole;
import org.aibles.authenservice.entity.Role;
import org.aibles.authenservice.entity.User;
import org.aibles.authenservice.exception.UserNotFoundException;
import org.aibles.authenservice.repository.AccountRepository;
import org.aibles.authenservice.repository.AccountRoleRepository;
import org.aibles.authenservice.repository.RoleRepository;
import org.aibles.authenservice.repository.UserRepository;
import org.aibles.authenservice.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(UserRepository userRepository, AccountRepository accountRepository, AccountRoleRepository accountRoleRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(AdminUserSearchRequest request) {
        Specification<User> spec = createUserSpecification(request);
        
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            request.getSortBy()
        );
        
        PageRequest pageRequest = PageRequest.of(
            request.getPage(), 
            request.getSize(), 
            sort
        );
        
        Page<User> users = userRepository.findAll(spec, pageRequest);
        return users.map(this::mapToAdminUserDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return mapToAdminUserDTO(user);
    }

    @Override
    @Transactional
    public AdminUserDTO updateUserStatus(UpdateUserStatusRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));
        
        Account account = accountRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Account not found for user: " + user.getId()));
        
        if (request.getActivated() != null) {
            account.setActivated(request.getActivated());
        }
        
        if (request.getLocked() != null) {
            account.setLocked(request.getLocked());
        }
        
        accountRepository.save(account);
        
//        log.info("Updated user status for user: {} by admin. Activated: {}, Locked: {}, Reason: {}",
//                user.getEmail(), request.getActivated(), request.getLocked(), request.getReason());
        
        return mapToAdminUserDTO(user);
    }

    @Override
    @Transactional
    public AdminUserDTO updateUserRoles(UpdateUserRolesRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));
        
        Account account = accountRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Account not found for user: " + user.getId()));
        
        // Remove existing roles
        accountRoleRepository.deleteByAccountId(account.getId());
        
        // Add new roles
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            
            AccountRole accountRole = AccountRole.builder()
                .id(UUID.randomUUID().toString())
                .accountId(account.getId())
                .roleId(role.getId())
                .build();
            
            accountRoleRepository.save(accountRole);
        }
        
//        log.info("Updated roles for user: {} to: {}", user.getEmail(), request.getRoles());
        
        return mapToAdminUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsDTO getUserStatistics() {
        Long totalUsers = userRepository.countTotalUsers();
        Long activeUsers = userRepository.countActiveUsers();
        Long inactiveUsers = userRepository.countInactiveUsers();
        Long lockedUsers = userRepository.countLockedUsers();
        Long usersWithMultipleRoles = userRepository.countUsersWithMultipleRoles();
        
        // Calculate new users this month/week - simplified for now
        Long newUsersThisMonth = totalUsers; // TODO: Implement date-based counting
        Long newUsersThisWeek = totalUsers; // TODO: Implement date-based counting
        Double averageUsersPerDay = totalUsers.doubleValue() / 30; // Simplified calculation
        
        return UserStatsDTO.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .inactiveUsers(inactiveUsers)
            .lockedUsers(lockedUsers)
            .newUsersThisMonth(newUsersThisMonth)
            .newUsersThisWeek(newUsersThisWeek)
            .usersWithMultipleRoles(usersWithMultipleRoles)
            .averageUsersPerDay(averageUsersPerDay)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllRoles() {
        return roleRepository.findAll().stream()
            .map(Role::getName)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        Account account = accountRepository.findByUserId(userId).orElse(null);
        if (account != null) {
            // Remove roles first
            accountRoleRepository.deleteByAccountId(account.getId());
            // Delete account
            accountRepository.delete(account);
        }
        
        // Delete user
        userRepository.delete(user);
        
//        log.info("Deleted user: {} by admin", user.getEmail());
    }

    @Override
    @Transactional
    public void resetUserPassword(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        Account account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Account not found for user: " + userId));
        
        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        account.setPassword(passwordEncoder.encode(tempPassword));
        accountRepository.save(account);
        
        // TODO: Send email with temporary password
        
//        log.info("Reset password for user: {} by admin", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getInactiveUsers(int days) {
        // TODO: Implement based on last login tracking
        // For now, return empty list
        return new ArrayList<>();
    }

    private Specification<User> createUserSpecification(AdminUserSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Keyword search
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), keyword);
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), keyword);
                predicates.add(criteriaBuilder.or(emailPredicate, namePredicate));
            }
            
            // Gender filter
            if (request.getGender() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), request.getGender()));
            }
            
            // Account status filters would need joins - simplified for now
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AdminUserDTO mapToAdminUserDTO(User user) {
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        List<String> roles = new ArrayList<>();
        
        if (account != null) {
            List<AccountRole> accountRoles = accountRoleRepository.findByAccountId(account.getId());
            roles = accountRoles.stream()
                .map(ar -> roleRepository.findById(ar.getRoleId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get().getName())
                .collect(Collectors.toList());
        }
        
        return AdminUserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .gender(user.getGender())
            .address(user.getAddress())
            .username(account != null ? account.getUsername() : null)
            .isActivated(account != null ? account.getActivated() : null)
            .isLocked(account != null ? account.getIsLocked() : null)
            .roles(roles)
            .createdAt(null) // TODO: Add created timestamp to entities
            .lastLoginAt(null) // TODO: Add last login tracking
            .totalLogins(0L) // TODO: Add login count tracking
            .build();
    }
}