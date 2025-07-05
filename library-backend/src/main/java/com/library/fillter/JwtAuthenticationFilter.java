package com.library.fillter;

import com.library.util.JwtVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtVerifier jwtVerifier;

    public JwtAuthenticationFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtVerifier.validateToken(token)) {
                    String username = jwtVerifier.extractUsername(token);
                    List<String> roles = jwtVerifier.extractRoles(token);

                    log.info("JWT Token validation successful for user: {}", username);
                    log.info("Extracted roles from JWT: {}", roles);

                    if (username != null) {
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
                        
                        log.info("Created authorities: {}", authorities);
                        
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Successfully authenticated user: {} with roles: {}", username, roles);
                    }
                } else {
                    log.warn("Invalid JWT token provided for path: {}", request.getRequestURI());
                    handleAuthenticationFailure(response, "Invalid or expired token", request.getRequestURI());
                    return;
                }
            } catch (Exception e) {
                log.error("JWT authentication failed for path {}: {}", request.getRequestURI(), e.getMessage());
                handleAuthenticationFailure(response, "Authentication failed: " + e.getMessage(), request.getRequestURI());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT validation for public endpoints
        return path.startsWith("/api/v1/books/public") ||
               path.startsWith("/actuator/") ||
               path.equals("/api/v1/health");
    }

    private void handleAuthenticationFailure(HttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
            java.time.Instant.now().toString(),
            message,
            path
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
