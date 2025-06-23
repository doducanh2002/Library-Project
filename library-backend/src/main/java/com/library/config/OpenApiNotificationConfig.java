package com.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiNotificationConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management System API")
                        .version("1.0.0")
                        .description("Complete Library Management System with Notification Support")
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Development Server"),
                        new Server().url("https://api.library.com").description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme")))
                .tags(List.of(
                        new Tag().name("Notifications").description("User notification management"),
                        new Tag().name("Admin Notifications").description("Administrative notification operations"),
                        new Tag().name("Books").description("Book catalog management"),
                        new Tag().name("Authors").description("Author management"),
                        new Tag().name("Categories").description("Category management"),
                        new Tag().name("Publishers").description("Publisher management"),
                        new Tag().name("Loans").description("Book loan management"),
                        new Tag().name("Orders").description("Book order management"),
                        new Tag().name("Cart").description("Shopping cart management"),
                        new Tag().name("Documents").description("Document management"),
                        new Tag().name("Admin Documents").description("Administrative document operations"),
                        new Tag().name("Search").description("Search operations"),
                        new Tag().name("Health").description("System health checks")
                ));
    }
}