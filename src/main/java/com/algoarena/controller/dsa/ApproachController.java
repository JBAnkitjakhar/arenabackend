// src/main/java/com/algoarena/controller/dsa/ApproachController.java
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.ApproachDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.ApproachService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/approaches")
@PreAuthorize("isAuthenticated()")
public class ApproachController {

    @Autowired
    private ApproachService approachService;

    // ==================== APPROACH CRUD OPERATIONS ====================

    /**
     * Get approach by ID (only if user owns it)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApproachDTO> getApproachById(
            @PathVariable String id,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        ApproachDTO approach = approachService.getApproachByIdAndUser(id, currentUser.getId());
        
        if (approach == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(approach);
    }

    /**
     * Get all approaches for a question by current user
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<ApproachDTO>> getApproachesByQuestion(
            @PathVariable String questionId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        List<ApproachDTO> approaches = approachService.getApproachesByQuestionAndUser(questionId, currentUser.getId());
        return ResponseEntity.ok(approaches);
    }

    /**
     * Create new approach for a question
     */
    @PostMapping("/question/{questionId}")
    public ResponseEntity<Map<String, Object>> createApproach(
            @PathVariable String questionId,
            @Valid @RequestBody ApproachDTO approachDTO,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        try {
            ApproachDTO createdApproach = approachService.createApproach(questionId, approachDTO, currentUser);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", createdApproach,
                "message", "Approach created successfully"
            );
            
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update approach (only if user owns it)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateApproach(
            @PathVariable String id,
            @Valid @RequestBody ApproachDTO approachDTO,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if user owns this approach
        ApproachDTO existingApproach = approachService.getApproachByIdAndUser(id, currentUser.getId());
        if (existingApproach == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            ApproachDTO updatedApproach = approachService.updateApproach(id, approachDTO);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", updatedApproach,
                "message", "Approach updated successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete approach (only if user owns it)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteApproach(
            @PathVariable String id,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if user owns this approach
        ApproachDTO existingApproach = approachService.getApproachByIdAndUser(id, currentUser.getId());
        if (existingApproach == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            approachService.deleteApproach(id);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Approach deleted successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== USER APPROACH STATISTICS ====================

    /**
     * Get all approaches by current user
     */
    @GetMapping("/my-approaches")
    public ResponseEntity<List<ApproachDTO>> getMyApproaches(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<ApproachDTO> approaches = approachService.getApproachesByUser(currentUser.getId());
        return ResponseEntity.ok(approaches);
    }

    /**
     * Get recent approaches by current user
     */
    @GetMapping("/my-approaches/recent")
    public ResponseEntity<List<ApproachDTO>> getMyRecentApproaches(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<ApproachDTO> approaches = approachService.getRecentApproachesByUser(currentUser.getId());
        return ResponseEntity.ok(approaches);
    }

    /**
     * Get approach statistics for current user
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyApproachStats(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> stats = approachService.getUserApproachStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * Get size usage for current user on a specific question
     */
    @GetMapping("/question/{questionId}/size-usage")
    public ResponseEntity<Map<String, Object>> getQuestionSizeUsage(
            @PathVariable String questionId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Map<String, Object> usage = approachService.getUserQuestionSizeUsage(currentUser.getId(), questionId);
        return ResponseEntity.ok(usage);
    }

    /**
     * UPDATED: Check both count and size limits before creating/updating approach
     */
    @PostMapping("/question/{questionId}/check-limits")
    public ResponseEntity<Map<String, Object>> checkApproachLimits(
            @PathVariable String questionId,
            @RequestBody Map<String, String> content,
            @RequestParam(required = false) String excludeApproachId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        String textContent = content.get("textContent");
        String codeContent = content.get("codeContent");
        
        Map<String, Object> limits = approachService.checkApproachLimits(
            currentUser.getId(), 
            questionId, 
            textContent, 
            codeContent, 
            excludeApproachId
        );
        
        return ResponseEntity.ok(limits);
    }

    /**
     * DEPRECATED: Use check-limits instead (kept for backward compatibility)
     */
    @PostMapping("/question/{questionId}/check-size")
    public ResponseEntity<Map<String, Object>> checkSizeLimits(
            @PathVariable String questionId,
            @RequestBody Map<String, String> content,
            @RequestParam(required = false) String excludeApproachId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        
        String textContent = content.get("textContent");
        String codeContent = content.get("codeContent");
        
        Map<String, Object> limits = approachService.checkSizeLimits(
            currentUser.getId(), 
            questionId, 
            textContent, 
            codeContent, 
            excludeApproachId
        );
        
        return ResponseEntity.ok(limits);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Delete all approaches by user for a specific question (Admin only)
     */
    @DeleteMapping("/question/{questionId}/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUserApproachesForQuestion(
            @PathVariable String questionId,
            @PathVariable String userId
    ) {
        try {
            approachService.deleteAllApproachesByUserForQuestion(userId, questionId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "All user approaches for question deleted successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete all approaches for a question (Admin only - used when deleting question)
     */
    @DeleteMapping("/question/{questionId}/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAllApproachesForQuestion(@PathVariable String questionId) {
        try {
            approachService.deleteAllApproachesForQuestion(questionId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "All approaches for question deleted successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }
}