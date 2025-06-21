package com.library.service;

import com.library.client.AuthServiceClient;
import com.library.dto.response.UserSummaryDTO;
import com.library.repository.LoanRepository;
import com.library.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final RestTemplate restTemplate;
    private final LoanRepository loanRepository;
    private final OrderRepository orderRepository;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public Page<UserSummaryDTO> getAllUsers(Pageable pageable, String search, String role, Boolean isActive) {
        try {
            String url = String.format("%s/api/admin/users?page=%d&size=%d&sortBy=%s&sortDir=%s",
                    authServiceUrl, pageable.getPageNumber(), pageable.getPageSize(),
                    pageable.getSort().iterator().next().getProperty(),
                    pageable.getSort().iterator().next().getDirection().name());
            
            if (search != null && !search.trim().isEmpty()) {
                url += "&search=" + search.trim();
            }
            if (role != null && !role.trim().isEmpty()) {
                url += "&role=" + role.trim();
            }
            if (isActive != null) {
                url += "&isActive=" + isActive;
            }
            
            log.info("Calling auth service to get users: {}", url);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                
                List<Map<String, Object>> userMaps = (List<Map<String, Object>>) data.get("content");
                List<UserSummaryDTO> users = new ArrayList<>();
                
                for (Map<String, Object> userMap : userMaps) {
                    UserSummaryDTO user = mapToUserSummaryDTO(userMap);
                    enrichUserWithStatistics(user);
                    users.add(user);
                }
                
                int totalElements = ((Number) data.get("totalElements")).intValue();
                return new PageImpl<>(users, pageable, totalElements);
            }
            
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("Error calling auth service to get users", e);
            return Page.empty(pageable);
        }
    }

    public UserSummaryDTO getUserById(Long userId) {
        try {
            String url = String.format("%s/api/admin/users/%d", authServiceUrl, userId);
            log.info("Calling auth service to get user by ID: {}", url);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> userData = (Map<String, Object>) responseBody.get("data");
                
                UserSummaryDTO user = mapToUserSummaryDTO(userData);
                enrichUserWithStatistics(user);
                return user;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error calling auth service to get user by ID: {}", userId, e);
            return null;
        }
    }

    public boolean updateUserStatus(Long userId, Boolean isActive) {
        try {
            String url = String.format("%s/api/admin/users/%d/status?isActive=%s", authServiceUrl, userId, isActive);
            log.info("Calling auth service to update user status: {}", url);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.PUT, null, new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error calling auth service to update user status: {}", userId, e);
            return false;
        }
    }

    public boolean updateUserRole(Long userId, String role) {
        try {
            String url = String.format("%s/api/admin/users/%d/role?role=%s", authServiceUrl, userId, role);
            log.info("Calling auth service to update user role: {}", url);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.PUT, null, new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error calling auth service to update user role: {}", userId, e);
            return false;
        }
    }

    public Page<Object> getUserLoans(Long userId, Pageable pageable) {
        try {
            return loanRepository.findByUserId(userId, pageable)
                    .map(loan -> (Object) loan);
        } catch (Exception e) {
            log.error("Error retrieving user loans for userId: {}", userId, e);
            return Page.empty(pageable);
        }
    }

    public Page<Object> getUserOrders(Long userId, Pageable pageable) {
        try {
            return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                    .map(order -> (Object) order);
        } catch (Exception e) {
            log.error("Error retrieving user orders for userId: {}", userId, e);
            return Page.empty(pageable);
        }
    }

    public boolean deleteUser(Long userId) {
        try {
            String url = String.format("%s/api/admin/users/%d", authServiceUrl, userId);
            log.info("Calling auth service to delete user: {}", url);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, null, new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error calling auth service to delete user: {}", userId, e);
            return false;
        }
    }

    private UserSummaryDTO mapToUserSummaryDTO(Map<String, Object> userMap) {
        return UserSummaryDTO.builder()
                .userId(((Number) userMap.get("id")).longValue())
                .username((String) userMap.get("username"))
                .email((String) userMap.get("email"))
                .fullName((String) userMap.get("fullName"))
                .phoneNumber((String) userMap.get("phoneNumber"))
                .role((String) userMap.get("role"))
                .isActive((Boolean) userMap.get("isActive"))
                .createdAt(java.time.LocalDateTime.parse((String) userMap.get("createdAt")))
                .lastLoginAt(userMap.get("lastLoginAt") != null ? 
                        java.time.LocalDateTime.parse((String) userMap.get("lastLoginAt")) : null)
                .totalLoans(0L)
                .activeLoans(0L)
                .overdueLoans(0L)
                .totalOrders(0L)
                .totalSpent(BigDecimal.ZERO)
                .averageRating(0.0)
                .build();
    }

    private void enrichUserWithStatistics(UserSummaryDTO user) {
        try {
            Long userId = user.getUserId();
            
            // Get loan statistics
            long totalLoans = loanRepository.countByUserIdAndStatusIn(userId, 
                    List.of(com.library.entity.enums.LoanStatus.BORROWED, 
                           com.library.entity.enums.LoanStatus.RETURNED,
                           com.library.entity.enums.LoanStatus.OVERDUE));
            
            long activeLoans = loanRepository.countByUserIdAndStatusIn(userId,
                    List.of(com.library.entity.enums.LoanStatus.BORROWED));
            
            long overdueLoans = loanRepository.countByUserIdAndStatusIn(userId,
                    List.of(com.library.entity.enums.LoanStatus.OVERDUE));
            
            // Get order statistics
            long totalOrders = orderRepository.countByUserId(userId);
            BigDecimal totalSpent = orderRepository.getTotalRevenueByDateRange(
                    java.time.LocalDateTime.of(2020, 1, 1, 0, 0),
                    java.time.LocalDateTime.now());
            
            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }
            
            user.setTotalLoans(totalLoans);
            user.setActiveLoans(activeLoans);
            user.setOverdueLoans(overdueLoans);
            user.setTotalOrders(totalOrders);
            user.setTotalSpent(totalSpent);
            user.setAverageRating(0.0); // TODO: Implement rating system
            
        } catch (Exception e) {
            log.error("Error enriching user statistics for userId: {}", user.getUserId(), e);
        }
    }
}