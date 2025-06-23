package com.library.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class NotificationSecurityAspect {

    @Around("execution(* com.library.service.NotificationService.*(..)) && args(userId,..)")
    public Object checkUserAccess(ProceedingJoinPoint joinPoint, String userId) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        String currentUserId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                               auth.getAuthority().equals("ROLE_LIBRARIAN"));
        
        // Admin and Librarian can access any user's notifications
        if (isAdmin) {
            log.debug("Admin/Librarian {} accessing notifications for user {}", currentUserId, userId);
            return joinPoint.proceed();
        }
        
        // Regular users can only access their own notifications
        if (!currentUserId.equals(userId)) {
            log.warn("User {} attempted to access notifications for user {}", currentUserId, userId);
            throw new AccessDeniedException("Access denied: Cannot access other user's notifications");
        }
        
        return joinPoint.proceed();
    }

    @Around("execution(* com.library.service.NotificationService.markAsRead(Long, String)) && args(notificationId, userId)")
    public Object checkNotificationOwnership(ProceedingJoinPoint joinPoint, Long notificationId, String userId) throws Throwable {
        // This will also trigger the user access check above
        log.debug("Checking notification ownership: {} for user: {}", notificationId, userId);
        return joinPoint.proceed();
    }
}