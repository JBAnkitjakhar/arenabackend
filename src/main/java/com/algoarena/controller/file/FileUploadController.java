// src/main/java/com/algoarena/controller/file/FileUploadController.java
package com.algoarena.controller.file;

import com.algoarena.service.file.CloudinaryService;
import com.algoarena.service.file.VisualizerService;
import com.algoarena.service.dsa.SolutionService; // ADD THIS IMPORT
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;

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
    private final SolutionService solutionService; // ADD THIS LINE

    // UPDATE CONSTRUCTOR TO INCLUDE SolutionService
    public FileUploadController(CloudinaryService cloudinaryService, 
                               VisualizerService visualizerService,
                               SolutionService solutionService) {
        this.cloudinaryService = cloudinaryService;
        this.visualizerService = visualizerService;
        this.solutionService = solutionService; // ADD THIS LINE
    }

    // ==================== IMAGE UPLOAD ENDPOINTS ====================
    // ... keep all existing image methods unchanged ...

    /**
     * Upload image for question (Admin only)
     */
    @PostMapping("/images/questions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> uploadQuestionImage(
            @RequestParam("image") MultipartFile file) {
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
            @RequestParam("image") MultipartFile file) {
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
            @RequestParam String publicId) {
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
            @RequestParam(defaultValue = "200") int height) {
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
     * UPDATED: Upload HTML visualizer file and link to solution (Admin only)
     */
    @PostMapping("/visualizers/{solutionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> uploadVisualizerFile(
            @PathVariable String solutionId,
            @RequestParam("visualizer") MultipartFile file) {
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

            // STEP 1: Upload file to GridFS
            Map<String, Object> result = visualizerService.uploadVisualizerFile(file, solutionId);
            String fileId = (String) result.get("fileId");

            // STEP 2: CRITICAL FIX - Link the file to the solution
            try {
                solutionService.addVisualizerToSolution(solutionId, fileId);
                System.out.println("Successfully linked visualizer " + fileId + " to solution " + solutionId);
            } catch (Exception e) {
                // If solution linking fails, clean up the uploaded file
                System.err.println("Failed to link visualizer to solution, cleaning up file: " + e.getMessage());
                try {
                    visualizerService.deleteVisualizerFile(fileId);
                } catch (Exception cleanupError) {
                    System.err.println("Failed to cleanup file after linking failure: " + cleanupError.getMessage());
                }
                throw new RuntimeException("Failed to link visualizer to solution: " + e.getMessage());
            }

            response.put("success", true);
            response.put("data", result);
            response.put("message", "Visualizer file uploaded successfully and linked to solution");

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
     * ALTERNATIVE APPROACH: Serve raw HTML content without any processing
     * This bypasses all sanitization issues for trusted educational content
     */
    @GetMapping("/visualizers/{fileId}")
    public ResponseEntity<String> getVisualizerFile(
            @PathVariable String fileId,
            HttpServletRequest request) {

        try {
            // System.out.println("Accessing visualizer: " + fileId);
            // System.out.println("User authenticated: " + (request.getUserPrincipal() != null));

            if (fileId == null || fileId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Invalid file ID");
            }

            // GET RAW CONTENT WITHOUT ANY PROCESSING
            String htmlContent = visualizerService.getRawVisualizerContent(fileId);

            // System.out.println("Raw HTML size: " + htmlContent.length() + " chars");
            // System.out.println("Contains <style>: " + htmlContent.toLowerCase().contains("<style"));
            // System.out.println("Contains <script>: " + htmlContent.toLowerCase().contains("<script"));

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    // MINIMAL SECURITY HEADERS - DON'T BLOCK INLINE CONTENT
                    .header("X-Frame-Options", "SAMEORIGIN")
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Cache-Control", "public, max-age=3600")
                    // RELAXED CSP FOR EDUCATIONAL CONTENT
                    .header("Content-Security-Policy",
                            "default-src 'self' 'unsafe-inline' 'unsafe-eval' data: blob:; " +
                                    "img-src 'self' data: https: blob:; " +
                                    "connect-src 'self'; " + // Allow some connections for educational demos
                                    "form-action 'self'; " +
                                    "frame-ancestors 'self'")
                    .body(htmlContent);

        } catch (Exception e) {
            System.err.println("Error serving visualizer: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Visualizer file not found: " + e.getMessage());
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
     * ENHANCED: List all visualizer files for a solution
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

            // ENHANCED: Use the new method that returns proper structure
            Map<String, Object> result = visualizerService.listVisualizersBySolution(solutionId);

            response.put("success", true);
            response.put("data", (List<?>) result.get("files"));
            response.put("count", result.get("count"));
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
     * UPDATED: Delete visualizer file and unlink from solution (Admin only)
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

            // STEP 1: Get file metadata to find the solution ID
            Map<String, Object> metadata = visualizerService.getVisualizerMetadata(fileId);
            String solutionId = (String) metadata.get("solutionId");

            // STEP 2: Remove from solution's visualizerFileIds array
            if (solutionId != null) {
                try {
                    solutionService.removeVisualizerFromSolution(solutionId, fileId);
                    System.out.println("Successfully unlinked visualizer " + fileId + " from solution " + solutionId);
                } catch (Exception e) {
                    System.err.println("Failed to unlink visualizer from solution: " + e.getMessage());
                    // Continue with file deletion even if solution update fails
                }
            }

            // STEP 3: Delete the file from GridFS
            visualizerService.deleteVisualizerFile(fileId);

            response.put("success", true);
            response.put("message", "Visualizer file deleted successfully and unlinked from solution");
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
    // ... keep all existing health check methods unchanged ...

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
     * ENHANCED: Get file upload limits and configuration with interactive HTML info
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

        // ENHANCED: HTML configuration with interactive features info
        Map<String, Object> htmlConfig = new HashMap<>();
        htmlConfig.put("maxSize", "500KB");
        htmlConfig.put("maxSizeBytes", 500 * 1024);
        htmlConfig.put("allowedTypes", List.of("text/html"));
        htmlConfig.put("allowedExtensions", List.of(".html"));
        htmlConfig.put("maxPerSolution", 2);
        htmlConfig.put("interactiveSupport", true);
        htmlConfig.put("supportedFeatures", List.of(
                "JavaScript animations",
                "Canvas rendering",
                "SVG graphics",
                "Interactive buttons",
                "Educational visualizations"));
        htmlConfig.put("securityMeasures", List.of(
                "Content Security Policy",
                "XSS Protection",
                "Network isolation",
                "Safe sandboxing"));

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