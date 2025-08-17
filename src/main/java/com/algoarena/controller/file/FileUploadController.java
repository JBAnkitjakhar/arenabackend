// src/main/java/com/algoarena/controller/file/FileUploadController.java
package com.algoarena.controller.file;

import com.algoarena.service.file.CloudinaryService;
import com.algoarena.service.file.VisualizerService;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;
    private final VisualizerService visualizerService;

    public FileUploadController(CloudinaryService cloudinaryService, VisualizerService visualizerService) {
        this.cloudinaryService = cloudinaryService;
        this.visualizerService = visualizerService;
    }

    // ==================== IMAGE UPLOAD ENDPOINTS ====================

    /**
     * Upload image for question (Admin only)
     */
    @PostMapping("/images/questions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> uploadQuestionImage(
            @RequestParam("image") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file before upload
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = cloudinaryService.uploadQuestionImage(file);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Question image uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Validation failed");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Image upload failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Upload image for solution (Admin only)
     */
    @PostMapping("/images/solutions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> uploadSolutionImage(
            @RequestParam("image") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = cloudinaryService.uploadSolutionImage(file);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Solution image uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Validation failed");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Image upload failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete image from Cloudinary (Admin only)
     */
    @DeleteMapping("/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @RequestParam String publicId
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (publicId == null || publicId.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Public ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = cloudinaryService.deleteImage(publicId);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Image deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Image deletion failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Generate thumbnail URL
     */
    @GetMapping("/images/thumbnail")
    public ResponseEntity<Map<String, Object>> generateThumbnail(
            @RequestParam String imageUrl,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "200") int height
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Image URL is required");
                return ResponseEntity.badRequest().body(response);
            }

            String thumbnailUrl = cloudinaryService.generateThumbnailUrl(imageUrl, width, height);
            
            response.put("success", true);
            response.put("originalUrl", imageUrl);
            response.put("thumbnailUrl", thumbnailUrl);
            response.put("width", width);
            response.put("height", height);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Thumbnail generation failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== HTML VISUALIZER ENDPOINTS ====================

    /**
     * Upload HTML visualizer file (Admin only)
     */
    @PostMapping("/visualizers/{solutionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> uploadVisualizerFile(
            @PathVariable String solutionId,
            @RequestParam("visualizer") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (solutionId == null || solutionId.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Solution ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = visualizerService.uploadVisualizerFile(file, solutionId);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Visualizer file uploaded successfully");
            
            return ResponseEntity.status(201).body(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Validation failed");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Visualizer upload failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get HTML visualizer file content (Public - for rendering)
     */
    @GetMapping("/visualizers/{fileId}")
    public ResponseEntity<String> getVisualizerFile(@PathVariable String fileId) {
        try {
            if (fileId == null || fileId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String htmlContent = visualizerService.getVisualizerContent(fileId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .header("X-Frame-Options", "SAMEORIGIN")
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Content-Security-Policy", "default-src 'self' 'unsafe-inline' 'unsafe-eval'; img-src 'self' data: https:;")
                    .body(htmlContent);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get visualizer file as downloadable resource (Admin only)
     */
    @GetMapping("/visualizers/{fileId}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Resource> downloadVisualizerFile(@PathVariable String fileId) {
        try {
            if (fileId == null || fileId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = visualizerService.getVisualizerFile(fileId);
            Map<String, Object> metadata = visualizerService.getVisualizerMetadata(fileId);
            
            String filename = (String) metadata.get("originalFileName");
            if (filename == null || filename.trim().isEmpty()) {
                filename = "visualizer_" + fileId + ".html";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get visualizer file metadata
     */
    @GetMapping("/visualizers/{fileId}/metadata")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getVisualizerMetadata(@PathVariable String fileId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (fileId == null || fileId.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "File ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> metadata = visualizerService.getVisualizerMetadata(fileId);
            
            response.put("success", true);
            response.put("data", metadata);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get metadata");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * List all visualizer files for a solution
     */
    @GetMapping("/solutions/{solutionId}/visualizers")
    public ResponseEntity<Map<String, Object>> getVisualizersBySolution(@PathVariable String solutionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (solutionId == null || solutionId.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Solution ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            List<Map<String, Object>> visualizers = visualizerService.getVisualizerFilesBySolution(solutionId);
            
            response.put("success", true);
            response.put("data", visualizers);
            response.put("count", visualizers.size());
            response.put("solutionId", solutionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get visualizers");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete visualizer file (Admin only)
     */
    @DeleteMapping("/visualizers/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteVisualizerFile(@PathVariable String fileId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (fileId == null || fileId.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "File ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            visualizerService.deleteVisualizerFile(fileId);
            
            response.put("success", true);
            response.put("message", "Visualizer file deleted successfully");
            response.put("fileId", fileId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Visualizer deletion failed");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== HEALTH CHECK ENDPOINTS ====================

    /**
     * Test Cloudinary connection
     */
    @GetMapping("/health/cloudinary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> testCloudinaryConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isHealthy = cloudinaryService.testConnection();
            
            response.put("success", isHealthy);
            response.put("service", "cloudinary");
            response.put("status", isHealthy ? "healthy" : "unhealthy");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("service", "cloudinary");
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Test GridFS connection
     */
    @GetMapping("/health/gridfs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> testGridFSConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isHealthy = visualizerService.testConnection();
            
            response.put("success", isHealthy);
            response.put("service", "gridfs");
            response.put("status", isHealthy ? "healthy" : "unhealthy");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("service", "gridfs");
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Get file upload limits and configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getFileUploadConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // Image configuration
        Map<String, Object> imageConfig = new HashMap<>();
        imageConfig.put("maxSize", "2MB");
        imageConfig.put("maxSizeBytes", 2 * 1024 * 1024);
        imageConfig.put("allowedTypes", List.of("image/jpeg", "image/png", "image/gif", "image/webp"));
        imageConfig.put("allowedExtensions", List.of(".jpg", ".jpeg", ".png", ".gif", ".webp"));
        imageConfig.put("maxPerQuestion", 5);
        imageConfig.put("maxPerSolution", 10);
        
        // HTML configuration
        Map<String, Object> htmlConfig = new HashMap<>();
        htmlConfig.put("maxSize", "500KB");
        htmlConfig.put("maxSizeBytes", 500 * 1024);
        htmlConfig.put("allowedTypes", List.of("text/html"));
        htmlConfig.put("allowedExtensions", List.of(".html"));
        htmlConfig.put("maxPerSolution", 2);
        
        config.put("images", imageConfig);
        config.put("html", htmlConfig);
        config.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", config);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get overall file upload statistics (Admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getFileUploadStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // These would be implemented based on your needs
            stats.put("totalImagesUploaded", "Not implemented yet");
            stats.put("totalVisualizersUploaded", "Not implemented yet");
            stats.put("storageUsed", "Not implemented yet");
            stats.put("timestamp", System.currentTimeMillis());
            
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get statistics");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}