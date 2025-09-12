// src/main/java/com/algoarena/security/JwtAuthenticationFilter.java
package com.algoarena.security;

import com.algoarena.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    // ✅ FIXED: Define specific endpoints to skip JWT processing
    private static final List<String> SKIP_JWT_PATHS = List.of(
            "/oauth2/",
            "/login",
            "/error",
            "/actuator/",

            // KEEP-ALIVE STATUS ENDPOINTS (CRITICAL FOR RENDER)
            "/status",      
            "/ping",
            "/healthz",  

            "/auth/google",
            "/auth/github",
            "/auth/health"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getServletPath();
        
        // ✅ FIXED: Only skip JWT for specific auth endpoints, NOT all /api/auth
        if (shouldSkipJwtProcessing(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if Authorization header is present and valid
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtService.extractUsername(jwt);

            // If user email is present and no authentication is set in SecurityContext
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                // Validate token and set authentication
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log the error but continue with the filter chain
            logger.error("JWT Authentication failed", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ✅ FIXED: Check if JWT processing should be skipped for this path
     */
    private boolean shouldSkipJwtProcessing(String path) {
        return SKIP_JWT_PATHS.stream().anyMatch(path::contains);
    }
}