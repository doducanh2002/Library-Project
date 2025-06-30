package com.library.gateway.filter;

import com.library.gateway.util.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);
    private final JwtVerifier jwtVerifier;

    public JwtAuthenticationGatewayFilterFactory(JwtVerifier jwtVerifier) {
        super(Config.class);
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public String name() {
        return "JwtAuthentication";
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod().name();

            log.debug("JWT Authentication filter processing: {} {}", method, path);
            
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Check if Authorization header is present
            if (authHeader == null || authHeader.trim().isEmpty()) {
                log.warn("Missing Authorization header for {} {}", method, path);
                return onError(exchange, "Authorization header is required", HttpStatus.UNAUTHORIZED);
            }

            // Check if header has Bearer prefix
            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for {} {}", method, path);
                return onError(exchange, "Authorization header must start with 'Bearer '", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            
            // Check if token is not empty
            if (token.trim().isEmpty()) {
                log.warn("Empty JWT token for {} {}", method, path);
                return onError(exchange, "JWT token is empty", HttpStatus.UNAUTHORIZED);
            }

            try {
                log.debug("Validating JWT token for {} {}", method, path);
                
                // Validate the token
                boolean isValid = jwtVerifier.validateToken(token);
                
                if (!isValid) {
                    log.warn("JWT token validation failed for {} {}", method, path);
                    return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
                }

                // Extract username from token
                String username = jwtVerifier.extractUsername(token);
                
                if (username == null || username.trim().isEmpty()) {
                    log.warn("Cannot extract username from JWT token for {} {}", method, path);
                    return onError(exchange, "Invalid JWT token - missing username", HttpStatus.UNAUTHORIZED);
                }

                log.debug("JWT authentication successful for user: {} on {} {}", username, method, path);

                // Add user information to headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-ID", username)
                        .header("X-User-Username", username)
                        .header("X-Auth-Token", token)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT validation error for {} {}: {}", method, path, e.getMessage(), e);
                return onError(exchange, "JWT validation failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
        
        Instant timestamp = Instant.now();
        String body = String.format(
            "{\"success\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"%s\",\"status\":%d,\"timestamp\":\"%s\",\"path\":\"%s\"}",
            message,
            status.value(),
            timestamp.toString(),
            exchange.getRequest().getPath().value()
        );
        
        log.warn("JWT Authentication failed: {} - {} {}", message, 
                 exchange.getRequest().getMethod().name(), 
                 exchange.getRequest().getPath().value());
        
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties can be added here if needed
        private boolean strictMode = true;
        
        public boolean isStrictMode() {
            return strictMode;
        }
        
        public void setStrictMode(boolean strictMode) {
            this.strictMode = strictMode;
        }
    }
}