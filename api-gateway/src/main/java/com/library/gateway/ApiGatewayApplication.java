package com.library.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service Routes
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("http://localhost:8081"))
                
                // Book Catalog Service Routes
                .route("catalog-books", r -> r
                        .path("/api/v1/books/**")
                        .uri("http://localhost:8082"))
                        
                .route("catalog-categories", r -> r
                        .path("/api/v1/categories/**")
                        .uri("http://localhost:8082"))
                        
                .route("catalog-authors", r -> r
                        .path("/api/v1/authors/**")
                        .uri("http://localhost:8082"))
                        
                .route("catalog-publishers", r -> r
                        .path("/api/v1/publishers/**")
                        .uri("http://localhost:8082"))
                
                // File Storage Service Routes
                .route("file-service", r -> r
                        .path("/api/v1/files/**")
                        .uri("http://localhost:8083"))
                        
                // Health Check Routes
                .route("auth-health", r -> r
                        .path("/health/auth")
                        .uri("http://localhost:8081/actuator/health"))
                        
                .route("catalog-health", r -> r
                        .path("/health/catalog")
                        .uri("http://localhost:8082/actuator/health"))
                        
                .route("file-health", r -> r
                        .path("/health/files")
                        .uri("http://localhost:8083/actuator/health"))
                
                .build();
    }
}