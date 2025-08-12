// src/main/java/com/algoarena/security/OAuth2SuccessHandler.java
package com.algoarena.security;

import com.algoarena.model.User;
import com.algoarena.service.auth.AuthService;
import com.algoarena.service.auth.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = extractRegistrationId(request);
        
        try {
            // Process OAuth2 user and get or create user
            User user = authService.processOAuth2User(oAuth2User, registrationId);
            
            // Generate JWT token
            String token = jwtService.generateToken(user);
            
            // Redirect to frontend with token
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/auth/callback")
                    .queryParam("token", token)
                    .queryParam("user", user.getId())
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 authentication", e);
            
            String errorUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/auth/error")
                    .queryParam("error", "authentication_failed")
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private String extractRegistrationId(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // Extract from URI like /api/auth/oauth2/callback/google
        String[] parts = requestUri.split("/");
        return parts[parts.length - 1]; // Last part is registration ID
    }
}