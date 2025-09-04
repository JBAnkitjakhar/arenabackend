// src/main/java/com/algoarena/controller/dsa/UserProgressController.java - COMPLETE with Bulk Endpoints
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.UserProgressDTO;
import com.algoarena.dto.dsa.UserProgressBulkDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping
@PreAuthorize("isAuthenticated()")
public class UserProgressController {

    @Autowired
    private UserProgressService userProgressService;

    // ==================== NEW BULK ENDPOINTS (REDIS OPTIMIZED) ====================

    /**
     * Get ALL user progress in single API call (REDIS CACHED)
     * This replaces multiple individual progress requests
     * GET /api/users/progress/all
     */
    @GetMapping("/users/progress/all")
    public ResponseEntity<UserProgressBulkDTO> getAllUserProgress(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserProgressBulkDTO bulkProgress = userProgressService.getAllUserProgressBulk(currentUser.getId());
        return ResponseEntity.ok(bulkProgress);
    }

    /**
     * Quick check if question is solved (REDIS CACHED)
     * GET /api/questions/{questionId}/solved
     */
    @GetMapping("/questions/{questionId}/solved")
    public ResponseEntity<Map<String, Boolean>> isQuestionSolved(
            @PathVariable String questionId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        boolean solved = userProgressService.hasUserSolvedQuestionCached(currentUser.getId(), questionId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("solved", solved);
        return ResponseEntity.ok(response);
    }

    // ==================== EXISTING USER PROGRESS ENDPOINTS ====================

    /**
     * Get current user's progress statistics (REDIS CACHED)
     * GET /api/users/progress
     */
    @GetMapping("/users/progress")
    public ResponseEntity<Map<String, Object>> getCurrentUserProgressStats(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> stats = userProgressService.getUserProgressStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * Get current user's recent progress (REDIS CACHED)
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
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        UserProgressDTO progress = userProgressService.getProgressByQuestionAndUser(questionId, currentUser.getId());
        
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(progress);
    }

    /**
     * Update progress for specific question (CACHE EVICTION)
     * POST /api/questions/{questionId}/progress
     */
    @PostMapping("/questions/{questionId}/progress")
    public ResponseEntity<UserProgressDTO> updateQuestionProgress(
            @PathVariable String questionId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        boolean solved = request.getOrDefault("solved", false);
        
        try {
            UserProgressDTO updatedProgress = userProgressService.updateProgress(questionId, currentUser.getId(), solved);
            return ResponseEntity.ok(updatedProgress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get progress for specific category and current user (REDIS CACHED)
     * GET /api/categories/{categoryId}/progress
     */
    @GetMapping("/categories/{categoryId}/progress")
    public ResponseEntity<Map<String, Object>> getCategoryProgress(
            @PathVariable String categoryId,
            Authentication authentication
    ) {
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
     * Get all progress for a user (Admin only) - Non-cached for admin accuracy
     * GET /api/users/{userId}/progress/all
     */
    @GetMapping("/users/{userId}/progress/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserProgressDTO>> getAllUserProgressAdmin(@PathVariable String userId) {
        List<UserProgressDTO> allProgress = userProgressService.getAllProgressByUser(userId);
        return ResponseEntity.ok(allProgress);
    }

    /**
     * Get bulk progress for specific user (Admin only) - Uses cache if available
     * GET /api/users/{userId}/progress/bulk
     */
    @GetMapping("/users/{userId}/progress/bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<UserProgressBulkDTO> getUserProgressBulk(@PathVariable String userId) {
        UserProgressBulkDTO bulkProgress = userProgressService.getAllUserProgressBulk(userId);
        return ResponseEntity.ok(bulkProgress);
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
     * Get user rank information
     * GET /api/users/rank
     */
    @GetMapping("/users/rank")
    public ResponseEntity<Map<String, Object>> getCurrentUserRank(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> rankInfo = userProgressService.getUserRank(currentUser.getId());
        return ResponseEntity.ok(rankInfo);
    }
}