package com.library.config;

import com.library.fillter.JwtAuthenticationFilter;
import com.library.util.JwtVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtVerifier jwtVerifier;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtVerifier jwtVerifier, CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtVerifier = jwtVerifier;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtVerifier);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health check endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/v1/health").permitAll()
                        
                        // Public Book endpoints
                        .requestMatchers("/api/v1/books", "/api/v1/books/{id}", "/api/v1/books/isbn/{isbn}",
                                "/api/v1/books/popular", "/api/v1/books/category/{categoryId}", 
                                "/api/v1/books/author/{authorId}", "/api/v1/books/publisher/{publisherId}").permitAll()
                        .requestMatchers("/api/v1/books/search").permitAll()
                        
                        // Public Category endpoints
                        .requestMatchers("/api/v1/categories", "/api/v1/categories/{categoryId}",
                                "/api/v1/categories/active", "/api/v1/categories/root",
                                "/api/v1/categories/slug/{slug}", "/api/v1/categories/{categoryId}/subcategories",
                                "/api/v1/categories/{categoryId}/hierarchy", "/api/v1/categories/check-name",
                                "/api/v1/categories/check-slug").permitAll()
                        
                        // Public Author endpoints
                        .requestMatchers("/api/v1/authors", "/api/v1/authors/{authorId}", "/api/v1/authors/search",
                                "/api/v1/authors/{authorId}/books", "/api/v1/authors/name/{name}",
                                "/api/v1/authors/nationality/{nationality}", "/api/v1/authors/prolific",
                                "/api/v1/authors/nationalities").permitAll()
                        
                        // Public Publisher endpoints
                        .requestMatchers("/api/v1/publishers", "/api/v1/publishers/{publisherId}",
                                "/api/v1/publishers/{publisherId}/books", "/api/v1/publishers/search",
                                "/api/v1/publishers/established", "/api/v1/publishers/active",
                                "/api/v1/publishers/check-name").permitAll()
                        
                        // Public Search endpoints
                        .requestMatchers("/api/v1/search/fulltext", "/api/v1/search/advanced",
                                "/api/v1/search/categories", "/api/v1/search/authors",
                                "/api/v1/search/available-for-loan", "/api/v1/search/available-for-sale",
                                "/api/v1/search/recent", "/api/v1/search/suggestions",
                                "/api/v1/search/popular-terms", "/api/v1/search/filters/performance").permitAll()
                        
                        // Public Document endpoints
                        .requestMatchers("/api/v1/documents", "/api/v1/documents/{id}", "/api/v1/documents/search",
                                "/api/v1/documents/public", "/api/v1/documents/book/{bookId}",
                                "/api/v1/documents/{id}/download", "/api/v1/documents/{id}/download-url",
                                "/api/v1/documents/{id}/view", "/api/v1/documents/{id}/view-url",
                                "/api/v1/documents/popular").permitAll()
                        
                        // Admin endpoints
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
