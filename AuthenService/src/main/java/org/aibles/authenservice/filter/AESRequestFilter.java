package org.aibles.authenservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

public class AESRequestFilter extends OncePerRequestFilter {

    private final String aesKey;

    public AESRequestFilter(String aesKey) {
        this.aesKey = aesKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!"POST".equals(request.getMethod()) && !"PUT".equals(request.getMethod()) && !"DELETE".equals(request.getMethod())&& !"GET".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        if (requestBody == null || requestBody.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty request body");
            return;
        }
        try {
            String decryptedBody = AESUtils.decrypt(requestBody, aesKey);
            WrappedHttpServletRequest wrappedRequest = new WrappedHttpServletRequest(request, decryptedBody);
            filterChain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid encrypted request body");
        }
    }
}
