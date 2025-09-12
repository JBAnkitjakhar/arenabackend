// TEMPORARY DEBUG VERSION - src/main/java/com/algoarena/controller/dsa/UserProgressController.java

package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.UserProgressDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.UserProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@PreAuthorize("isAuthenticated()")
public class UserProgressController {

    private static final Logger logger = LoggerFactory.getLogger(UserProgressController.class);

    @Autowired
    private UserProgressService userProgressService;

    /**
     * ENHANCED DEBUG VERSION - Get current user's progress statistics
     * GET /api/users/progress
     */
    @GetMapping("/users/progress")
    public ResponseEntity<Map<String, Object>> getCurrentUserProgressStats(
            Authentication authentication, 
            HttpServletRequest request) {
        
        // ENHANCED DEBUG LOGGING
        logger.info("=== USER PROGRESS REQUEST DEBUG ===");
        logger.info("Request URL: {}", request.getRequestURL());
        logger.info("Request Method: {}", request.getMethod());
        logger.info("Authorization Header: {}", request.getHeader("Authorization"));
        logger.info("Authentication object: {}", authentication);
        logger.info("Authentication class: {}", authentication != null ? authentication.getClass().getName() : "null");
        logger.info("Is Authenticated: {}", authentication != null ? authentication.isAuthenticated() : false);
        logger.info("Principal class: {}", authentication != null && authentication.getPrincipal() != null ? 
            authentication.getPrincipal().getClass().getName() : "null");
        
        if (authentication == null) {
            logger.error("Authentication is null");
            return ResponseEntity.status(401).body(Map.of("error", "Authentication is null"));
        }
        
        if (!authentication.isAuthenticated()) {
            logger.error("User is not authenticated");
            return ResponseEntity.status(401).body(Map.of("error", "User is not authenticated"));
        }
        
        if (authentication.getPrincipal() == null) {
            logger.error("Authentication principal is null");
            return ResponseEntity.status(401).body(Map.of("error", "Authentication principal is null"));
        }
        
        try {
            // SAFE CASTING with detailed error handling
            Object principal = authentication.getPrincipal();
            logger.info("Principal object: {}", principal);
            
            if (!(principal instanceof User)) {
                logger.error("Principal is not a User instance. Actual class: {}", principal.getClass().getName());
                logger.error("Principal toString: {}", principal.toString());
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Invalid authentication principal type",
                    "actualType", principal.getClass().getName(),
                    "expectedType", User.class.getName()
                ));
            }
            
            User currentUser = (User) principal;
            logger.info("User authenticated successfully: {}", currentUser.getEmail());
            logger.info("User ID: {}", currentUser.getId());
            logger.info("User Role: {}", currentUser.getRole());
            
            Map<String, Object> stats = userProgressService.getUserProgressStats(currentUser.getId());
            logger.info("Successfully retrieved user progress stats");
            return ResponseEntity.ok(stats);
            
        } catch (ClassCastException e) {
            logger.error("ClassCastException when casting principal to User", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to cast authentication principal to User",
                "exception", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error in getCurrentUserProgressStats", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal server error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * SIMPLE TEST ENDPOINT for debugging
     * GET /api/debug/auth-test
     */
    @GetMapping("/debug/auth-test")
    public ResponseEntity<Map<String, Object>> testAuthentication(
            Authentication authentication, 
            HttpServletRequest request) {
        
        Map<String, Object> debugInfo = new HashMap<>();
        
        debugInfo.put("timestamp", System.currentTimeMillis());
        debugInfo.put("requestUrl", request.getRequestURL().toString());
        debugInfo.put("authorizationHeader", request.getHeader("Authorization"));
        debugInfo.put("hasAuthentication", authentication != null);
        
        if (authentication != null) {
            debugInfo.put("isAuthenticated", authentication.isAuthenticated());
            debugInfo.put("principalClass", authentication.getPrincipal().getClass().getName());
            debugInfo.put("hasPrincipal", authentication.getPrincipal() != null);
            
            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                debugInfo.put("userEmail", user.getEmail());
                debugInfo.put("userId", user.getId());
                debugInfo.put("userRole", user.getRole());
                debugInfo.put("status", "SUCCESS - User authenticated properly");
            } else {
                debugInfo.put("principalType", authentication.getPrincipal().toString());
                debugInfo.put("status", "ERROR - Principal is not User type");
            }
        } else {
            debugInfo.put("status", "ERROR - No authentication object");
        }
        
        logger.info("Auth test result: {}", debugInfo);
        return ResponseEntity.ok(debugInfo);
    }

    // Keep all other existing methods unchanged...
    
    @GetMapping("/debug/user-progress")
    public ResponseEntity<String> debugUserProgress(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        userProgressService.debugUserProgress(currentUser.getId());
        return ResponseEntity.ok("Debug output printed to console - check server logs");
    }

    @GetMapping("/users/progress/recent")
    public ResponseEntity<List<UserProgressDTO>> getCurrentUserRecentProgress(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<UserProgressDTO> recentProgress = userProgressService.getRecentProgress(currentUser.getId());
        return ResponseEntity.ok(recentProgress);
    }

    @GetMapping("/questions/{questionId}/progress")
    public ResponseEntity<UserProgressDTO> getQuestionProgress(
            @PathVariable String questionId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserProgressDTO progress = userProgressService.getProgressByQuestionAndUser(questionId, currentUser.getId());

        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(progress);
    }

    @PostMapping("/questions/{questionId}/progress")
    public ResponseEntity<UserProgressDTO> updateQuestionProgress(
            @PathVariable String questionId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        boolean solved = request.getOrDefault("solved", false);

        try {
            UserProgressDTO updatedProgress = userProgressService.updateProgress(questionId, currentUser.getId(), solved);
            return ResponseEntity.ok(updatedProgress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categories/{categoryId}/progress")
    public ResponseEntity<Map<String, Object>> getCategoryProgress(
            @PathVariable String categoryId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> progress = userProgressService.getUserCategoryProgress(currentUser.getId(), categoryId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/users/{userId}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getUserProgressStats(@PathVariable String userId) {
        Map<String, Object> stats = userProgressService.getUserProgressStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users/{userId}/progress/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserProgressDTO>> getAllUserProgress(@PathVariable String userId) {
        List<UserProgressDTO> allProgress = userProgressService.getAllProgressByUser(userId);
        return ResponseEntity.ok(allProgress);
    }

    @GetMapping("/admin/progress/global")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getGlobalProgressStats() {
        Map<String, Object> globalStats = userProgressService.getGlobalStats();
        return ResponseEntity.ok(globalStats);
    }

    @PostMapping("/users/progress/bulk")
    public ResponseEntity<Map<String, Boolean>> getBulkQuestionProgress(
            @RequestBody Map<String, List<String>> request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<String> questionIds = request.get("questionIds");

        if (questionIds == null || questionIds.isEmpty()) {
            return ResponseEntity.ok(new HashMap<>());
        }

        Map<String, Boolean> progressMap = userProgressService.getBulkProgressStatus(
                currentUser.getId(),
                questionIds);

        return ResponseEntity.ok(progressMap);
    }
}