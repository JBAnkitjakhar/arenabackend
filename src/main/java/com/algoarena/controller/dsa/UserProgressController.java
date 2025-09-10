// src/main/java/com/algoarena/controller/dsa/UserProgressController.java

package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.UserProgressDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@PreAuthorize("isAuthenticated()")
public class UserProgressController {

    @Autowired
    private UserProgressService userProgressService;

    // ==================== USER PROGRESS ENDPOINTS ====================

    /**
     * Get current user's progress statistics
     * GET /api/users/progress
     */
    @GetMapping("/users/progress")
    public ResponseEntity<Map<String, Object>> getCurrentUserProgressStats(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> stats = userProgressService.getUserProgressStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * TEMPORARY DEBUG ENDPOINT - Remove after debugging
     * GET /api/debug/user-progress
     */
    @GetMapping("/debug/user-progress")
    public ResponseEntity<String> debugUserProgress(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        userProgressService.debugUserProgress(currentUser.getId());
        return ResponseEntity.ok("Debug output printed to console - check server logs");
    }

    /**
     * Get current user's recent progress (last 10 solved questions)
     * GET /api/users/progress/recent
     */
    @GetMapping("/users/progress/recent")
    public ResponseEntity<List<UserProgressDTO>> getCurrentUserRecentProgress(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<UserProgressDTO> recentProgress = userProgressService.getRecentProgress(currentUser.getId());
        return ResponseEntity.ok(recentProgress);
    }

    /**
     * Get progress for specific question and current user
     * GET /api/questions/{questionId}/progress
     */
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

    /**
     * Update progress for specific question
     * POST /api/questions/{questionId}/progress
     */
    @PostMapping("/questions/{questionId}/progress")
    public ResponseEntity<UserProgressDTO> updateQuestionProgress(
            @PathVariable String questionId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        boolean solved = request.getOrDefault("solved", false);

        try {
            UserProgressDTO updatedProgress = userProgressService.updateProgress(questionId, currentUser.getId(),
                    solved);
            return ResponseEntity.ok(updatedProgress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get progress for specific category and current user
     * GET /api/categories/{categoryId}/progress
     */
    @GetMapping("/categories/{categoryId}/progress")
    public ResponseEntity<Map<String, Object>> getCategoryProgress(
            @PathVariable String categoryId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> progress = userProgressService.getUserCategoryProgress(currentUser.getId(), categoryId);
        return ResponseEntity.ok(progress);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get progress for specific user (Admin only)
     * GET /api/users/{userId}/progress
     */
    @GetMapping("/users/{userId}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getUserProgressStats(@PathVariable String userId) {
        Map<String, Object> stats = userProgressService.getUserProgressStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all progress for a user (Admin only)
     * GET /api/users/{userId}/progress/all
     */
    @GetMapping("/users/{userId}/progress/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserProgressDTO>> getAllUserProgress(@PathVariable String userId) {
        List<UserProgressDTO> allProgress = userProgressService.getAllProgressByUser(userId);
        return ResponseEntity.ok(allProgress);
    }

    /**
     * Get global progress statistics (Admin only)
     * GET /api/admin/progress/global
     */
    @GetMapping("/admin/progress/global")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getGlobalProgressStats() {
        Map<String, Object> globalStats = userProgressService.getGlobalStats();
        return ResponseEntity.ok(globalStats);
    }

    /**
     * BULK: Get progress status for multiple questions (no 404s)
     * POST /api/users/progress/bulk
     * Body: { "questionIds": ["id1", "id2", "id3"] }
     * Response: { "id1": true, "id2": false, "id3": true }
     */
    @PostMapping("/users/progress/bulk")
    public ResponseEntity<Map<String, Boolean>> getBulkQuestionProgress(
            @RequestBody Map<String, List<String>> request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<String> questionIds = request.get("questionIds");

        if (questionIds == null || questionIds.isEmpty()) {
            return ResponseEntity.ok(new HashMap<>());
        }

        // Get all progress records for these questions and current user
        Map<String, Boolean> progressMap = userProgressService.getBulkProgressStatus(
                currentUser.getId(),
                questionIds);

        return ResponseEntity.ok(progressMap);
    }
}