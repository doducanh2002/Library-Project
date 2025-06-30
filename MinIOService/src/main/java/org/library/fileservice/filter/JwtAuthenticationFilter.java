package org.library.fileservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.library.fileservice.utill.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

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

                    if (username != null) {
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Successfully authenticated user: {}", username);
                    }
                } else {
                    log.warn("Invalid JWT token provided");
                    handleAuthenticationFailure(response, "Invalid or expired token");
                    return;
                }
            } catch (Exception e) {
                log.error("JWT authentication failed: {}", e.getMessage());
                handleAuthenticationFailure(response, "Authentication failed: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
            java.time.Instant.now().toString(),
            message,
            "MinIO File Service"
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
