package com.library.gateway.config;

import com.library.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class RateLimitConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String userId = jwtUtil.extractUserIdForRateLimit(authHeader);
                    log.debug("Rate limiting key for user: {}", userId);
                    return Mono.just(userId);
                } catch (Exception e) {
                    log.debug("Could not extract user ID for rate limiting: {}", e.getMessage());
                }
            }
            
            // Fallback to IP-based rate limiting for unauthenticated requests
            String clientIP = getClientIP(exchange);
            log.debug("Rate limiting key for IP: {}", clientIP);
            return Mono.just(clientIP);
        };
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIP = getClientIP(exchange);
            log.debug("IP-based rate limiting key: {}", clientIP);
            return Mono.just(clientIP);
        };
    }

    private String getClientIP(org.springframework.web.server.ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return exchange.getRequest().getRemoteAddress() != null ? 
               exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : 
               "unknown";
    }
}