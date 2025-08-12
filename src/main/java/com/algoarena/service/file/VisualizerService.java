// src/main/java/com/algoarena/service/file/VisualizerService.java
package com.algoarena.service.file;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
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
import java.util.Map;
import java.util.UUID;

@Service
public class VisualizerService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    /**
     * Upload HTML visualizer file to GridFS
     * @param file The HTML file to upload
     * @param solutionId The ID of the solution this visualizer belongs to
     * @return Map containing file metadata
     */
    public Map<String, Object> uploadVisualizerFile(MultipartFile file, String solutionId) throws IOException {
        // Validate file
        validateHtmlFile(file);
        
        // Read and sanitize HTML content
        String htmlContent = new String(file.getBytes());
        String sanitizedHtml = sanitizeHtmlContent(htmlContent);
        
        // Generate metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("solutionId", solutionId);
        metadata.put("originalFileName", file.getOriginalFilename());
        metadata.put("contentType", "text/html");
        metadata.put("uploadedAt", System.currentTimeMillis());
        metadata.put("fileSize", sanitizedHtml.getBytes().length);
        
        // Generate unique filename
        String filename = "visualizer_" + solutionId + "_" + UUID.randomUUID().toString() + ".html";
        
        try {
            // FIXED: Store in GridFS with proper ByteArrayInputStream
            ObjectId fileId = gridFsTemplate.store(
                new ByteArrayInputStream(sanitizedHtml.getBytes()),
                filename,
                "text/html",
                metadata
            );
            
            // Return result
            Map<String, Object> result = new HashMap<>();
            result.put("fileId", fileId.toString());
            result.put("filename", filename);
            result.put("originalFileName", file.getOriginalFilename());
            result.put("size", sanitizedHtml.getBytes().length);
            result.put("solutionId", solutionId);
            result.put("uploadedAt", metadata.get("uploadedAt"));
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload HTML file to GridFS: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve HTML visualizer file from GridFS
     * @param fileId The GridFS file ID
     * @return GridFsResource containing the file
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
     * Get HTML content as string
     * @param fileId The GridFS file ID
     * @return HTML content as string
     */
    public String getVisualizerContent(String fileId) throws IOException {
        GridFsResource resource = getVisualizerFile(fileId);
        
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes());
        }
    }

    /**
     * Delete visualizer file from GridFS
     * @param fileId The GridFS file ID
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
     * @param fileId The GridFS file ID
     * @return File metadata
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
            metadata.put("contentType", gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("contentType") : "text/html");
            metadata.put("solutionId", gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("solutionId") : null);
            metadata.put("originalFileName", gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("originalFileName") : null);
            
            return metadata;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid file ID format: " + fileId);
        }
    }

    /**
     * List all visualizer files for a solution
     * @param solutionId The solution ID
     * @return List of file metadata
     */
    public java.util.List<Map<String, Object>> getVisualizerFilesBySolution(String solutionId) {
        Query query = Query.query(Criteria.where("metadata.solutionId").is(solutionId));
        
        return gridFsTemplate.find(query)
                .map(gridFSFile -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("fileId", gridFSFile.getObjectId().toString());
                    metadata.put("filename", gridFSFile.getFilename());
                    metadata.put("size", gridFSFile.getLength());
                    metadata.put("uploadDate", gridFSFile.getUploadDate());
                    metadata.put("originalFileName", gridFSFile.getMetadata() != null ? 
                                 gridFSFile.getMetadata().get("originalFileName") : null);
                    return metadata;
                })
                .into(new java.util.ArrayList<>());
    }

    /**
     * Delete all visualizer files for a solution
     * @param solutionId The solution ID
     */
    public void deleteAllVisualizerFilesForSolution(String solutionId) {
        Query query = Query.query(Criteria.where("metadata.solutionId").is(solutionId));
        gridFsTemplate.delete(query);
    }

    /**
     * Validate HTML file
     */
    private void validateHtmlFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (500KB limit)
        long maxSize = 500 * 1024; // 500KB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 500KB limit");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/html")) {
            throw new IllegalArgumentException("Invalid file type. Only HTML files are allowed");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".html")) {
            throw new IllegalArgumentException("Invalid file extension. Only .html files are allowed");
        }
    }

    /**
     * Sanitize HTML content to remove potentially dangerous elements
     * while preserving educational visualization functionality
     */
    private String sanitizeHtmlContent(String htmlContent) {
        // Create a relaxed whitelist for educational visualizations
        Safelist safelist = Safelist.relaxed()
                // Allow common HTML elements
                .addTags("canvas", "svg", "path", "circle", "rect", "line", "text", "g", "defs", "pattern")
                // Allow style attributes for visualization
                .addAttributes(":all", "style", "class", "id")
                // Allow canvas attributes
                .addAttributes("canvas", "width", "height")
                // Allow SVG attributes
                .addAttributes("svg", "width", "height", "viewBox", "xmlns")
                .addAttributes("path", "d", "fill", "stroke", "stroke-width")
                .addAttributes("circle", "cx", "cy", "r", "fill", "stroke")
                .addAttributes("rect", "x", "y", "width", "height", "fill", "stroke")
                .addAttributes("line", "x1", "y1", "x2", "y2", "stroke")
                .addAttributes("text", "x", "y", "font-size", "fill")
                // Allow data attributes for interactive elements
                .addAttributes(":all", "data-*");

        // Clean the HTML but preserve educational content
        String cleanHtml = Jsoup.clean(htmlContent, safelist);
        
        // Add basic security headers
        String secureHtml = "<!DOCTYPE html>\n<html>\n<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>Algorithm Visualizer</title>\n" +
                "<style>\n" +
                "body { font-family: Arial, sans-serif; margin: 20px; }\n" +
                ".container { max-width: 100%; overflow: hidden; }\n" +
                "</style>\n" +
                "</head>\n<body>\n" +
                "<div class=\"container\">\n" +
                cleanHtml +
                "\n</div>\n</body>\n</html>";
        
        return secureHtml;
    }

    /**
     * Test GridFS connection
     */
    public boolean testConnection() {
        try {
            // Try to perform a simple operation
            gridFsTemplate.find(Query.query(Criteria.where("filename").is("test")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}