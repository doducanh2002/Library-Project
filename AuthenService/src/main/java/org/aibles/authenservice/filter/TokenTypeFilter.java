package org.aibles.authenservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TokenTypeFilter extends OncePerRequestFilter {

    private final AntPathRequestMatcher loginMatcher = new AntPathRequestMatcher("/api/v1/auth/login");
    private final AntPathRequestMatcher googleCallbackMatcher = new AntPathRequestMatcher("/api/v1/auth/google/callback");
    private final AntPathRequestMatcher oauth2CodeMatcher = new AntPathRequestMatcher("/login/oauth2/code/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tokenType = request.getHeader("X-Token-Type");

        if (tokenType != null) {
            if (tokenType.equals("LOCAL")) {
                // Allow only local login endpoint
                if (!loginMatcher.matches(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Local login only allowed for /api/v1/auth/login\"}");
                    return;
                }
            } else if (tokenType.equals("GOOGLE")) {
                // Allow only Google OAuth2 endpoints
                if (!googleCallbackMatcher.matches(request) && !oauth2CodeMatcher.matches(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Google login only allowed for OAuth2 endpoints\"}");
                    return;
                }
            } else {
                // Invalid X-Token-Type value
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid X-Token-Type value\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}