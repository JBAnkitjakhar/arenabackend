// src/main/java/com/algoarena/controller/dsa/SolutionController.java
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.SolutionDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.SolutionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/solutions")
public class SolutionController {

    @Autowired
    private SolutionService solutionService;

    @GetMapping("/{id}")
    public ResponseEntity<SolutionDTO> getSolutionById(@PathVariable String id) {
        SolutionDTO solution = solutionService.getSolutionById(id);
        if (solution == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(solution);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<SolutionDTO>> getAllSolutions(Pageable pageable) {
        Page<SolutionDTO> solutions = solutionService.getAllSolutions(pageable);
        return ResponseEntity.ok(solutions);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<SolutionDTO>> getSolutionsByQuestion(@PathVariable String questionId) {
        List<SolutionDTO> solutions = solutionService.getSolutionsByQuestion(questionId);
        return ResponseEntity.ok(solutions);
    }

    @PostMapping("/question/{questionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> createSolution(
            @PathVariable String questionId,
            @Valid @RequestBody SolutionDTO solutionDTO,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        SolutionDTO createdSolution = solutionService.createSolution(questionId, solutionDTO, currentUser);
        return ResponseEntity.status(201).body(createdSolution);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> updateSolution(
            @PathVariable String id,
            @Valid @RequestBody SolutionDTO solutionDTO
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.updateSolution(id, solutionDTO);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, String>> deleteSolution(@PathVariable String id) {
        try {
            solutionService.deleteSolution(id);
            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== IMAGE MANAGEMENT ENDPOINTS ====================

    /**
     * Add image to solution
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> addImageToSolution(
            @PathVariable String id,
            @RequestParam String imageUrl
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.addImageToSolution(id, imageUrl);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove image from solution
     */
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> removeImageFromSolution(
            @PathVariable String id,
            @RequestParam String imageUrl
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.removeImageFromSolution(id, imageUrl);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== VISUALIZER MANAGEMENT ENDPOINTS ====================

    /**
     * Add visualizer to solution
     */
    @PostMapping("/{id}/visualizers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> addVisualizerToSolution(
            @PathVariable String id,
            @RequestParam String visualizerFileId
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.addVisualizerToSolution(id, visualizerFileId);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove visualizer from solution
     */
    @DeleteMapping("/{id}/visualizers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<SolutionDTO> removeVisualizerFromSolution(
            @PathVariable String id,
            @RequestParam String visualizerFileId
    ) {
        try {
            SolutionDTO updatedSolution = solutionService.removeVisualizerFromSolution(id, visualizerFileId);
            return ResponseEntity.ok(updatedSolution);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== LINK VALIDATION ENDPOINTS ====================

    /**
     * Validate YouTube link and extract video info
     */
    @PostMapping("/validate-youtube")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> validateYoutubeLink(
            @RequestBody Map<String, String> request
    ) {
        String youtubeLink = request.get("youtubeLink");
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (youtubeLink == null || youtubeLink.trim().isEmpty()) {
                response.put("valid", false);
                response.put("error", "YouTube link is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create a temporary DTO to validate
            SolutionDTO tempDTO = new SolutionDTO();
            tempDTO.setYoutubeLink(youtubeLink);
            
            response.put("valid", tempDTO.hasValidYoutubeLink());
            response.put("videoId", tempDTO.getYoutubeVideoId());
            response.put("embedUrl", tempDTO.getYoutubeEmbedUrl());
            response.put("originalUrl", youtubeLink);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Validate Google Drive link
     */
    @PostMapping("/validate-drive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> validateDriveLink(
            @RequestBody Map<String, String> request
    ) {
        String driveLink = request.get("driveLink");
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (driveLink == null || driveLink.trim().isEmpty()) {
                response.put("valid", false);
                response.put("error", "Drive link is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create a temporary DTO to validate
            SolutionDTO tempDTO = new SolutionDTO();
            tempDTO.setDriveLink(driveLink);
            
            response.put("valid", tempDTO.hasValidDriveLink());
            response.put("originalUrl", driveLink);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== STATISTICS ENDPOINTS ====================

    /**
     * Get solutions with images
     */
    @GetMapping("/with-images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<SolutionDTO>> getSolutionsWithImages() {
        List<SolutionDTO> solutions = solutionService.getSolutionsWithImages();
        return ResponseEntity.ok(solutions);
    }

    /**
     * Get solutions with visualizers
     */
    @GetMapping("/with-visualizers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<SolutionDTO>> getSolutionsWithVisualizers() {
        List<SolutionDTO> solutions = solutionService.getSolutionsWithVisualizers();
        return ResponseEntity.ok(solutions);
    }

    /**
     * NEW: Get solutions with YouTube videos
     */
    @GetMapping("/with-youtube")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<SolutionDTO>> getSolutionsWithYoutubeVideos() {
        List<SolutionDTO> solutions = solutionService.getSolutionsWithYoutubeVideos();
        return ResponseEntity.ok(solutions);
    }

    /**
     * Get solutions by creator
     */
    @GetMapping("/creator/{creatorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<SolutionDTO>> getSolutionsByCreator(
            @PathVariable String creatorId,
            Pageable pageable
    ) {
        Page<SolutionDTO> solutions = solutionService.getSolutionsByCreator(creatorId, pageable);
        return ResponseEntity.ok(solutions);
    }

    /**
     * NEW: Get solution statistics including media counts
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getSolutionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get all solutions for counting
        List<SolutionDTO> allSolutions = solutionService.getAllSolutions(Pageable.unpaged()).getContent();
        
        stats.put("totalSolutions", allSolutions.size());
        stats.put("solutionsWithImages", solutionService.getSolutionsWithImages().size());
        stats.put("solutionsWithVisualizers", solutionService.getSolutionsWithVisualizers().size());
        stats.put("solutionsWithYoutubeVideos", solutionService.getSolutionsWithYoutubeVideos().size());
        
        // Count solutions with different combinations
        long solutionsWithDriveLinks = allSolutions.stream()
                .mapToLong(s -> s.hasValidDriveLink() ? 1 : 0)
                .sum();
        
        long solutionsWithBothLinks = allSolutions.stream()
                .mapToLong(s -> (s.hasValidDriveLink() && s.hasValidYoutubeLink()) ? 1 : 0)
                .sum();
        
        stats.put("solutionsWithDriveLinks", solutionsWithDriveLinks);
        stats.put("solutionsWithBothLinks", solutionsWithBothLinks);
        
        return ResponseEntity.ok(stats);
    }
}