// src/main/java/com/algoarena/security/OAuth2FailureHandler.java
package com.algoarena.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2FailureHandler.class);

    @Value("${app.cors.allowed-origins:https://24-algofront.vercel.app}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        logger.error("OAuth2 authentication failed", exception);

        String frontendUrl = allowedOrigins.split(",")[0];
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/login")
                .queryParam("error", "oauth_failed")
                .queryParam("message", "Authentication failed")
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}