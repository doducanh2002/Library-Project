package com.library.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {

    public RoleAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            String userRole = request.getHeaders().getFirst("X-User-Role");
            
            if (userRole == null) {
                return onError(exchange, "User role not found in request headers", HttpStatus.FORBIDDEN);
            }

            List<String> allowedRoles = config.getAllowedRoles();
            
            if (allowedRoles == null || allowedRoles.isEmpty()) {
                // If no roles specified, allow any authenticated user
                return chain.filter(exchange);
            }

            boolean hasPermission = allowedRoles.stream()
                    .anyMatch(role -> role.equalsIgnoreCase(userRole));

            if (!hasPermission) {
                log.warn("Access denied for user with role: {} to path: {}", userRole, request.getURI().getPath());
                return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
            }

            log.debug("Access granted for user with role: {} to path: {}", userRole, request.getURI().getPath());
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", 
                                  status.getReasonPhrase(), message);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("allowedRoles");
    }

    public static class Config {
        private List<String> allowedRoles;

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

        public void setAllowedRoles(String allowedRoles) {
            this.allowedRoles = Arrays.asList(allowedRoles.split(","));
        }
    }
}