// src/main/java/com/algoarena/security/OAuth2SuccessHandler.java
package com.algoarena.security;

import com.algoarena.model.User;
import com.algoarena.service.auth.AuthService;
import com.algoarena.service.auth.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Value("${app.cors.allowed-origins:https://24-algofront.vercel.app}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = extractRegistrationId(request);
        
        logger.info("OAuth2 authentication successful for provider: {}", registrationId);
        logger.debug("OAuth2 user attributes: {}", oAuth2User.getAttributes());

        try {
            // Process OAuth2 user and get or create user
            User user = authService.processOAuth2User(oAuth2User, registrationId);
            logger.info("User processed successfully: {}", user.getEmail());
            
            // Generate JWT token
            String token = jwtService.generateToken(user);
            logger.debug("JWT token generated for user: {}", user.getEmail());
            
            // Get frontend URL (use first allowed origin)
            String frontendUrl = allowedOrigins.split(",")[0];
            
            // Redirect to frontend with token
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("token", token)
                    .queryParam("user", user.getId())
                    .build().toUriString();
            
            logger.info("Redirecting to frontend: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 authentication for provider: " + registrationId, e);
            
            String frontendUrl = allowedOrigins.split(",")[0];
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/login")
                    .queryParam("error", "authentication_failed")
                    .queryParam("message", e.getMessage())
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private String extractRegistrationId(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        logger.debug("Extracting registration ID from URI: {}", requestUri);
        
        // Extract from URI like /oauth2/callback/google
        String[] parts = requestUri.split("/");
        String registrationId = parts[parts.length - 1];
        
        logger.debug("Extracted registration ID: {}", registrationId);
        return registrationId;
    }
}