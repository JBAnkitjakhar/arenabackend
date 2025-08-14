// src/main/java/com/algoarena/controller/auth/AuthController.java
package com.algoarena.controller.auth;

import com.algoarena.dto.auth.AuthResponse;
import com.algoarena.dto.auth.UserInfo;
import com.algoarena.model.User;
import com.algoarena.service.auth.AuthService;
import com.algoarena.service.auth.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    // OAuth2 login endpoints (handled by Spring Security)
    @GetMapping("/google")
    public ResponseEntity<Map<String, String>> googleLogin() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Redirect to Google OAuth");
        response.put("url", "/oauth2/authorization/google");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/github")
    public ResponseEntity<Map<String, String>> githubLogin() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Redirect to GitHub OAuth");
        response.put("url", "/oauth2/authorization/github");
        return ResponseEntity.ok(response);
    }

    // Get current user information
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            User user = (User) authentication.getPrincipal();
            UserInfo userInfo = new UserInfo(user);
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Refresh token endpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).build();
            }

            String refreshToken = authHeader.substring(7);
            String userEmail = jwtService.extractUsername(refreshToken);
            
            User user = authService.getCurrentUser(userEmail);
            String newToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            
            AuthResponse response = new AuthResponse(newToken, newRefreshToken, new UserInfo(user));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // Logout endpoint (client-side token removal)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    // Health check for auth service
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "auth");
        return ResponseEntity.ok(response);
    }
}