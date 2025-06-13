package com.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management System API")
                        .version("2.0")
                        .description("""
                                RESTful API for Library Management System
                                
                                ## Features
                                - **Authentication & Authorization**: JWT-based security with role management
                                - **Book Catalog Management**: Comprehensive book, author, category, and publisher management
                                - **Loan Management**: Complete book borrowing system with overdue tracking
                                - **E-commerce**: Shopping cart and order management with payment integration
                                - **File Storage**: Document and image management with MinIO
                                
                                ## Modules
                                1. **Books & Catalog** - Browse, search, and manage books
                                2. **Loans** - Request, approve, and track book loans
                                3. **Cart & Orders** - Shopping cart and order processing
                                4. **Admin** - Administrative functions for librarians
                                
                                ## Sprint 4 - Order Management
                                This version includes complete order management functionality:
                                - Shopping cart operations
                                - Order creation and checkout
                                - Order tracking and history
                                - Admin order management
                                - Financial calculations (shipping, tax, discounts)
                                """)
                        .contact(new Contact()
                                .name("Library Development Team")
                                .email("dev@library.com")
                                .url("https://github.com/library-project"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(
                        new Components()
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}