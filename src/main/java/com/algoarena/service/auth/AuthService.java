// src/main/java/com/algoarena/service/auth/AuthService.java
package com.algoarena.service.auth;

import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import com.algoarena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User processOAuth2User(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = extractEmail(attributes, registrationId);
        String name = extractName(attributes, registrationId);
        String image = extractImage(attributes, registrationId);
        String providerId = extractProviderId(attributes, registrationId);
        
        // Check if user already exists
        User existingUser = userRepository.findByEmail(email).orElse(null);
        
        if (existingUser != null) {
            // Update existing user with latest info
            return updateExistingUser(existingUser, name, image, providerId, registrationId);
        } else {
            // Create new user
            return createNewUser(email, name, image, providerId, registrationId);
        }
    }

    private User updateExistingUser(User user, String name, String image, String providerId, String registrationId) {
        boolean updated = false;
        
        // Update name if different
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            updated = true;
        }
        
        // Update image if different
        if (image != null && !image.equals(user.getImage())) {
            user.setImage(image);
            updated = true;
        }
        
        // Update provider ID
        if ("google".equals(registrationId) && !providerId.equals(user.getGoogleId())) {
            user.setGoogleId(providerId);
            updated = true;
        } else if ("github".equals(registrationId) && !providerId.equals(user.getGithubId())) {
            user.setGithubId(providerId);
            updated = true;
        }
        
        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        
        return user;
    }

    private User createNewUser(String email, String name, String image, String providerId, String registrationId) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setImage(image);
        
        if ("google".equals(registrationId)) {
            newUser.setGoogleId(providerId);
        } else if ("github".equals(registrationId)) {
            newUser.setGithubId(providerId);
        }
        
        // Set role - check if this is the first user (make them superadmin)
        newUser.setRole(determineUserRole(email));
        
        return userRepository.save(newUser);
    }

    private UserRole determineUserRole(String email) {
        // If this is the first user or specific email, make them superadmin
        long userCount = userRepository.countAllUsers();
        
        if (userCount == 0 || "ankitjakharabc@gmail.com".equals(email)) {
            return UserRole.SUPERADMIN;
        }
        
        return UserRole.USER;
    }

    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("email");
            case "github":
                return (String) attributes.get("email");
            default:
                throw new IllegalArgumentException("Unsupported registration ID: " + registrationId);
        }
    }

    private String extractName(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                return (String) attributes.get("name");
            default:
                return "Unknown User";
        }
    }

    private String extractImage(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("picture");
            case "github":
                return (String) attributes.get("avatar_url");
            default:
                return null;
        }
    }

    private String extractProviderId(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("sub");
            case "github":
                return String.valueOf(attributes.get("id"));
            default:
                throw new IllegalArgumentException("Unsupported registration ID: " + registrationId);
        }
    }

    // Helper method to get current authenticated user
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}