package com.library.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(com.library.security.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return joinPoint.proceed(); // Let security handle authentication
        }
        
        String userId = authentication.getName();
        String methodName = joinPoint.getSignature().getName();
        String rateLimitKey = String.format("rate_limit:%s:%s", userId, methodName);
        
        // Check current count
        String countStr = (String) redisTemplate.opsForValue().get(rateLimitKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        
        // Rate limiting rules based on method
        int maxRequests = getMaxRequestsForMethod(methodName);
        Duration timeWindow = getTimeWindowForMethod(methodName);
        
        if (currentCount >= maxRequests) {
            log.warn("Rate limit exceeded for user {} on method {}: {} requests", 
                    userId, methodName, currentCount);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, 
                    "Rate limit exceeded. Too many requests.");
        }
        
        // Increment counter
        redisTemplate.opsForValue().set(rateLimitKey, String.valueOf(currentCount + 1), timeWindow);
        
        log.debug("Rate limit check passed for user {} on method {}: {}/{} requests", 
                userId, methodName, currentCount + 1, maxRequests);
        
        return joinPoint.proceed();
    }

    private int getMaxRequestsForMethod(String methodName) {
        return switch (methodName) {
            case "createNotification" -> 100; // 100 notifications per hour
            case "getUserNotifications" -> 200; // 200 fetches per hour
            case "markAsRead", "markAllAsRead" -> 500; // 500 mark operations per hour
            case "deleteNotification" -> 50; // 50 deletes per hour
            case "getUnreadCount" -> 1000; // 1000 count checks per hour
            default -> 100; // Default limit
        };
    }

    private Duration getTimeWindowForMethod(String methodName) {
        return switch (methodName) {
            case "createNotification" -> Duration.ofHours(1);
            case "getUserNotifications" -> Duration.ofMinutes(30);
            case "markAsRead", "markAllAsRead" -> Duration.ofMinutes(30);
            case "deleteNotification" -> Duration.ofHours(1);
            case "getUnreadCount" -> Duration.ofMinutes(15);
            default -> Duration.ofHours(1);
        };
    }
}