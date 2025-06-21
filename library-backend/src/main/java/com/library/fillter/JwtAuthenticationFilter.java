package com.library.fillter;

import com.library.util.JwtVerifier;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

//      private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

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
                          // Create authentication object
                          UsernamePasswordAuthenticationToken authentication =
                              new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                          SecurityContextHolder.getContext().setAuthentication(authentication);

                      }
                  } else {
                  }
              } catch (Exception e) {
              }
          }

          filterChain.doFilter(request, response);
      }

      @Override
      protected boolean shouldNotFilter(HttpServletRequest request) {
          String path = request.getRequestURI();
          // Skip JWT validation for public endpoints
          return path.startsWith("/api/v1/books/public") ||
                 path.startsWith("/actuator/");
      }
  }
