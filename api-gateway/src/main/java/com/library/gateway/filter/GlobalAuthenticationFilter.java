package com.library.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GlobalAuthenticationFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        // Authentication Service - Public endpoints
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/sendotp",
        "/api/v1/auth/active",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/api/v1/auth/jwk/token",
        
        // Library Backend - Books (all public endpoints)
        "/api/v1/books",
        
        // Library Backend - Categories (all public endpoints)
        "/api/v1/categories",
        
        // Library Backend - Authors (all public endpoints)
        "/api/v1/authors",
        
        // Library Backend - Publishers (all public endpoints)
        "/api/v1/publishers",
        
        // Library Backend - Search (all public endpoints)
        "/api/v1/search",
        
        // Library Backend - Documents (public endpoints only)
        "/api/v1/documents",
        
        // Health check endpoints
        "/api/v1/health",
        "/api/v1/files/health",
        "/actuator",
        "/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // Check if the path is public
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        
        // Let the request continue to JWT filter for validation
        return chain.filter(exchange);
    }
    
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(publicPath -> path.startsWith(publicPath));
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", "Unauthorized");
        errorResponse.put("message", "Unauthorized");
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", Instant.now().toString());
        
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100; // Execute before other filters
    }
}