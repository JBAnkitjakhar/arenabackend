// src/main/java/com/algoarena/service/file/VisualizerService.java
package com.algoarena.service.file;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
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

@Service
public class VisualizerService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    /**
     * FIXED: Upload HTML visualizer file with minimal sanitization that preserves
     * educational JavaScript
     */
    public Map<String, Object> uploadVisualizerFile(MultipartFile file, String solutionId) throws IOException {
        // Validate file
        validateHtmlFile(file);

        // Read HTML content
        String htmlContent = new String(file.getBytes());

        // FIXED: Use minimal processing that preserves educational JavaScript
        String processedHtml = processEducationalHtml(htmlContent);

        // Generate metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("solutionId", solutionId);
        metadata.put("originalFileName", file.getOriginalFilename());
        metadata.put("contentType", "text/html");
        metadata.put("uploadedAt", System.currentTimeMillis());
        metadata.put("fileSize", processedHtml.getBytes().length);
        metadata.put("isInteractive", containsJavaScript(processedHtml));

        // Log what we're storing
        // System.out.println("Uploading HTML file:");
        // System.out.println("Original size: " + htmlContent.length() + " chars");
        // System.out.println("Processed size: " + processedHtml.length() + " chars");
        // System.out.println("Has <style>: " +
        // processedHtml.toLowerCase().contains("<style"));
        // System.out.println("Has <script>: " +
        // processedHtml.toLowerCase().contains("<script"));

        // Generate unique filename
        String filename = "visualizer_" + solutionId + "_" + UUID.randomUUID().toString() + ".html";

        try {
            // Store in GridFS
            ObjectId fileId = gridFsTemplate.store(
                    new ByteArrayInputStream(processedHtml.getBytes()),
                    filename,
                    "text/html",
                    metadata);

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
     * FIXED: Minimal processing that preserves educational JavaScript and CSS
     */
    private String processEducationalHtml(String htmlContent) {
        try {
            // FOR EDUCATIONAL VISUALIZERS: Only block truly dangerous content
            // Preserve all CSS and educational JavaScript

            String processedContent = htmlContent;

            // Only remove extremely dangerous patterns while keeping educational scripts
            processedContent = processedContent
                    // Block cookie access
                    .replaceAll("(?i)document\\.cookie\\s*=", "/* BLOCKED: document.cookie = */")
                    // Block location changes
                    .replaceAll("(?i)(window\\.location|location\\.href)\\s*=", "/* BLOCKED: location change */")
                    // Block external fetch/XHR (but allow educational demos)
                    .replaceAll("(?i)fetch\\s*\\([\"']https?://(?!localhost)",
                            "/* BLOCKED: external fetch */ fetch('data:")
                    // Remove external script sources for security
                    .replaceAll("(?i)<script[^>]*src=[\"']https?://[^\"']*[\"']",
                            "<script /* BLOCKED: external source */");

            // Ensure proper HTML structure
            if (!processedContent.toLowerCase().contains("<!doctype") &&
                    !processedContent.toLowerCase().contains("<html")) {
                processedContent = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n</head>\n<body>\n"
                        + processedContent + "\n</body>\n</html>";
            }

            // System.out.println("Educational HTML processing complete:");
            // System.out.println("Original had <script>: " +
            // htmlContent.toLowerCase().contains("<script"));
            // System.out.println("Processed has <script>: " +
            // processedContent.toLowerCase().contains("<script"));
            // System.out.println("Original had <style>: " +
            // htmlContent.toLowerCase().contains("<style"));
            // System.out.println("Processed has <style>: " +
            // processedContent.toLowerCase().contains("<style"));

            return processedContent;

        } catch (Exception e) {
            System.err.println("HTML processing failed, using original: " + e.getMessage());
            return htmlContent; // Return original if processing fails
        }
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
     * Get raw HTML content without any sanitization processing
     */
    public String getRawVisualizerContent(String fileId) throws IOException {
        GridFsResource resource = getVisualizerFile(fileId);

        try (InputStream inputStream = resource.getInputStream()) {
            String rawContent = new String(inputStream.readAllBytes());
            // System.out.println("Retrieved raw content: " + rawContent.length() + "
            // chars");
            return rawContent;
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
            metadata.put("contentType",
                    gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("contentType") : "text/html");
            metadata.put("solutionId",
                    gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("solutionId") : null);
            metadata.put("originalFileName",
                    gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("originalFileName") : null);
            metadata.put("isInteractive",
                    gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("isInteractive") : false);

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
            fileInfo.put("originalFileName",
                    gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("originalFileName") : null);
            fileInfo.put("isInteractive",
                    gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().get("isInteractive") : false);
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
     * Delete all visualizer files for a solution in one operation
     */
    public void deleteAllVisualizersForSolution(String solutionId) {
        try {
            Query query = Query.query(Criteria.where("metadata.solutionId").is(solutionId));

            // Get count for logging
            var files = gridFsTemplate.find(query);
            var fileList = files.into(new java.util.ArrayList<>());
            int fileCount = fileList.size();

            if (fileCount == 0) {
                // System.out.println("No visualizer files found for solution: " + solutionId);
                return;
            }

            // System.out.println("Deleting " + fileCount + " visualizer files for solution: " + solutionId);

            // Delete all files in one operation
            gridFsTemplate.delete(query);

            System.out
                    .println("Successfully deleted all " + fileCount + " visualizer files for solution: " + solutionId);

        } catch (Exception e) {
            System.err.println("Failed to delete visualizer files for solution " + solutionId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete visualizer files for solution: " + solutionId, e);
        }
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