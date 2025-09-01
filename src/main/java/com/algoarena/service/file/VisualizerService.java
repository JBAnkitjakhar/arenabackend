// src/main/java/com/algoarena/service/file/VisualizerService.java
package com.algoarena.service.file;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class VisualizerService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // SECURITY: Patterns for potentially dangerous content
    private static final Pattern DANGEROUS_JS_PATTERNS = Pattern.compile(
        "(?i)(fetch|xhr|xmlhttprequest|websocket|eval|function\\s*\\(\\s*\\)\\s*\\{.*location|document\\.cookie|" +
        "window\\.location|top\\.location|parent\\.location|localStorage|sessionStorage|indexedDB|" +
        "document\\.write|innerHTML.*<script|outerHTML.*<script|alert\\s*\\(|confirm\\s*\\(|prompt\\s*\\()"
    );

    /**
     * Upload HTML visualizer file to GridFS with controlled security
     */
    public Map<String, Object> uploadVisualizerFile(MultipartFile file, String solutionId) throws IOException {
        // Validate file
        validateHtmlFile(file);
        
        // Read HTML content
        String htmlContent = new String(file.getBytes());
        
        // ENHANCED: Apply controlled sanitization that preserves educational functionality
        String processedHtml = processHtmlForEducationalUse(htmlContent);
        
        // Generate metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("solutionId", solutionId);
        metadata.put("originalFileName", file.getOriginalFilename());
        metadata.put("contentType", "text/html");
        metadata.put("uploadedAt", System.currentTimeMillis());
        metadata.put("fileSize", processedHtml.getBytes().length);
        metadata.put("isInteractive", containsJavaScript(processedHtml));
        
        // Generate unique filename
        String filename = "visualizer_" + solutionId + "_" + UUID.randomUUID().toString() + ".html";
        
        try {
            // Store in GridFS
            ObjectId fileId = gridFsTemplate.store(
                new ByteArrayInputStream(processedHtml.getBytes()),
                filename,
                "text/html",
                metadata
            );
            
            // Return result
            Map<String, Object> result = new HashMap<>();
            result.put("fileId", fileId.toString());
            result.put("filename", filename);
            result.put("originalFileName", file.getOriginalFilename());
            result.put("size", processedHtml.getBytes().length);
            result.put("solutionId", solutionId);
            result.put("uploadedAt", metadata.get("uploadedAt"));
            result.put("isInteractive", metadata.get("isInteractive"));
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload HTML file to GridFS: " + e.getMessage(), e);
        }
    }

    /**
     * ENHANCED: Process HTML for educational use while maintaining interactivity
     * This method applies controlled security without breaking educational visualizations
     */
    private String processHtmlForEducationalUse(String htmlContent) {
        try {
            // Parse the HTML document
            Document doc = Jsoup.parse(htmlContent);
            
            // Remove potentially dangerous script content while preserving educational functionality
            doc.select("script").forEach(script -> {
                String scriptContent = script.html();
                
                // Check for dangerous patterns
                if (DANGEROUS_JS_PATTERNS.matcher(scriptContent).find()) {
                    // Log the removal (in production, you'd use proper logging)
                    // System.out.println("SECURITY: Removed potentially dangerous script content");
                    script.remove();
                } else {
                    // Keep educational scripts but add safety measures
                    String saferScript = addSecurityMeasures(scriptContent);
                    script.html(saferScript);
                }
            });
            
            // Add security headers and educational context
            addSecurityHeaders(doc);
            
            return doc.html();
            
        } catch (Exception e) {
            // Fallback to basic sanitization if parsing fails
            System.err.println("HTML processing failed, applying basic sanitization: " + e.getMessage());
            return basicSanitization(htmlContent);
        }
    }

    /**
     * Add basic security measures to JavaScript while preserving functionality
     */
    private String addSecurityMeasures(String scriptContent) {
        // Add try-catch wrapper for error handling
        return "try {\n" + scriptContent + "\n} catch (error) {\n" +
               "    console.warn('Visualizer script error:', error);\n" +
               "}";
    }

    /**
     * Add security headers to the HTML document
     */
    private void addSecurityHeaders(Document doc) {
        // Add or update meta tags for security
        if (doc.select("meta[http-equiv=Content-Security-Policy]").isEmpty()) {
            doc.head().appendElement("meta")
                .attr("http-equiv", "Content-Security-Policy")
                .attr("content", "default-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                "img-src 'self' data: https:; " +
                                "connect-src 'none'; " +
                                "form-action 'none'; " +
                                "frame-ancestors 'self'");
        }
        
        // Add educational context comment
        doc.body().prepend("<!-- Educational Algorithm Visualizer - Rendered securely within AlgoArena -->");
        
        // Ensure viewport meta tag exists
        if (doc.select("meta[name=viewport]").isEmpty()) {
            doc.head().appendElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=device-width, initial-scale=1.0");
        }
    }

    /**
     * Basic sanitization fallback (more restrictive)
     */
    private String basicSanitization(String htmlContent) {
        Safelist safelist = Safelist.relaxed()
                .addTags("canvas", "svg", "path", "circle", "rect", "line", "text", "g", "defs", "pattern")
                .addAttributes(":all", "style", "class", "id")
                .addAttributes("canvas", "width", "height")
                .addAttributes("svg", "width", "height", "viewBox", "xmlns")
                .addAttributes("path", "d", "fill", "stroke", "stroke-width")
                .addAttributes(":all", "data-*");

        return Jsoup.clean(htmlContent, safelist);
    }

    /**
     * Check if HTML contains JavaScript
     */
    private boolean containsJavaScript(String htmlContent) {
        return htmlContent.toLowerCase().contains("<script") || 
               htmlContent.toLowerCase().contains("javascript:");
    }

    /**
     * Validate HTML file constraints
     */
    private void validateHtmlFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size (500KB limit)
        long maxSize = 500 * 1024; // 500KB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 500KB limit. Current size: " + 
                                             (file.getSize() / 1024) + "KB");
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/html")) {
            throw new IllegalArgumentException("Invalid content type. Only HTML files are allowed");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".html")) {
            throw new IllegalArgumentException("Invalid file extension. Only .html files are allowed");
        }
    }

    /**
     * Retrieve HTML visualizer file from GridFS
     */
    public GridFsResource getVisualizerFile(String fileId) throws IOException {
        try {
            ObjectId objectId = new ObjectId(fileId);
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
            
            if (gridFSFile == null) {
                throw new RuntimeException("Visualizer file not found with ID: " + fileId);
            }
            
            return gridFsTemplate.getResource(gridFSFile);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid file ID format: " + fileId);
        }
    }

    /**
     * Get HTML content as string - ENHANCED with security context
     */
    public String getVisualizerContent(String fileId) throws IOException {
        GridFsResource resource = getVisualizerFile(fileId);
        
        try (InputStream inputStream = resource.getInputStream()) {
            String htmlContent = new String(inputStream.readAllBytes());
            
            // Add runtime security context if needed
            return addRuntimeSecurityContext(htmlContent);
        }
    }

    /**
     * Add runtime security context to HTML before serving
     */
    private String addRuntimeSecurityContext(String htmlContent) {
        // You can add additional runtime security measures here if needed
        // For now, return as-is since security is handled during upload
        return htmlContent;
    }

    /**
     * Delete visualizer file from GridFS
     */
    public void deleteVisualizerFile(String fileId) {
        try {
            ObjectId objectId = new ObjectId(fileId);
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(objectId)));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid file ID format: " + fileId);
        }
    }

    /**
     * Get visualizer file metadata
     */
    public Map<String, Object> getVisualizerMetadata(String fileId) {
        try {
            ObjectId objectId = new ObjectId(fileId);
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
            
            if (gridFSFile == null) {
                throw new RuntimeException("Visualizer file not found with ID: " + fileId);
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileId", gridFSFile.getObjectId().toString());
            metadata.put("filename", gridFSFile.getFilename());
            metadata.put("size", gridFSFile.getLength());
            metadata.put("uploadDate", gridFSFile.getUploadDate());
            metadata.put("contentType", gridFSFile.getMetadata() != null ? 
                        gridFSFile.getMetadata().get("contentType") : "text/html");
            metadata.put("solutionId", gridFSFile.getMetadata() != null ? 
                        gridFSFile.getMetadata().get("solutionId") : null);
            metadata.put("originalFileName", gridFSFile.getMetadata() != null ? 
                        gridFSFile.getMetadata().get("originalFileName") : null);
            metadata.put("isInteractive", gridFSFile.getMetadata() != null ? 
                        gridFSFile.getMetadata().get("isInteractive") : false);
            
            return metadata;
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid file ID format: " + fileId);
        }
    }

    /**
     * List visualizers by solution ID
     */
    public Map<String, Object> listVisualizersBySolution(String solutionId) {
        Query query = Query.query(Criteria.where("metadata.solutionId").is(solutionId));
        
        var files = gridFsTemplate.find(query);
        var fileList = files.map(gridFSFile -> {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", gridFSFile.getObjectId().toString());
            fileInfo.put("filename", gridFSFile.getFilename());
            fileInfo.put("size", gridFSFile.getLength());
            fileInfo.put("uploadDate", gridFSFile.getUploadDate());
            fileInfo.put("originalFileName", gridFSFile.getMetadata() != null ? 
                        gridFSFile.getMetadata().get("originalFileName") : null);
            fileInfo.put("isInteractive", gridFSFile.getMetadata() != null ? 
                        gridFSFile.getMetadata().get("isInteractive") : false);
            return fileInfo;
        }).into(new java.util.ArrayList<>());
        
        Map<String, Object> result = new HashMap<>();
        result.put("solutionId", solutionId);
        result.put("files", fileList);
        result.put("count", fileList.size());
        
        return result;
    }

    /**
     * FIXED: Get visualizer files by solution ID (used by controller)
     */
    public List<Map<String, Object>> getVisualizerFilesBySolution(String solutionId) {
        Map<String, Object> result = listVisualizersBySolution(solutionId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("files");
        return files;
    }

    /**
     * Test GridFS connection
     */
    public boolean testConnection() {
        try {
            gridFsTemplate.find(Query.query(Criteria.where("filename").is("test")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}