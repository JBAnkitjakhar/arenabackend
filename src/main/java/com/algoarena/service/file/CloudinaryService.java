// src/main/java/com/algoarena/service/file/CloudinaryService.java
// 68a0cc009bfc40d04af794d0
package com.algoarena.service.file;

import com.algoarena.config.CloudinaryConfig;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(CloudinaryConfig cloudinaryConfig) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudinaryConfig.getCloudName());
        config.put("api_key", cloudinaryConfig.getApiKey());
        config.put("api_secret", cloudinaryConfig.getApiSecret());
        config.put("secure", "true");
        
        this.cloudinary = new Cloudinary(config);
    }

    /**
     * Upload image to Cloudinary
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        validateImageFile(file);
        
        String publicId = folder + "/" + UUID.randomUUID().toString();
        
        Map<String, Object> uploadOptions = new HashMap<>();
        uploadOptions.put("folder", "algoarena/" + folder);
        uploadOptions.put("public_id", publicId);
        uploadOptions.put("resource_type", "image");
        // uploadOptions.put("format", "auto");
        // uploadOptions.put("quality", "auto:good");
        
        // FIXED: Suppress the generic type warning
        @SuppressWarnings("rawtypes")
        Transformation transformation = new Transformation()
                .width(1200)
                .height(800)
                .crop("limit");
                // .quality("auto");
        
        uploadOptions.put("transformation", transformation);
        
        try {
            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("url", result.get("secure_url"));
            uploadResult.put("secure_url", result.get("secure_url"));
            uploadResult.put("public_id", result.get("public_id"));
            uploadResult.put("width", result.get("width"));
            uploadResult.put("height", result.get("height"));
            uploadResult.put("format", result.get("format"));
            uploadResult.put("size", result.get("bytes"));
            uploadResult.put("created_at", result.get("created_at"));
            
            return uploadResult;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> uploadQuestionImage(MultipartFile file) throws IOException {
        return uploadImage(file, "questions");
    }

    public Map<String, Object> uploadSolutionImage(MultipartFile file) throws IOException {
        return uploadImage(file, "solutions");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        try {
            return (Map<String, Object>) cloudinary.uploader().destroy(publicId, new HashMap<>());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getImageInfo(String publicId) throws IOException {
        try {
            return (Map<String, Object>) cloudinary.api().resource(publicId, new HashMap<>());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get image info from Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Generate transformation URL with warning suppression
     */
    public String generateThumbnailUrl(String originalUrl, int width, int height) {
        if (originalUrl == null || !originalUrl.contains("cloudinary.com")) {
            return originalUrl;
        }

        try {
            String[] parts = originalUrl.split("/");
            if (parts.length < 2) {
                return originalUrl;
            }
            
            String publicIdWithExtension = parts[parts.length - 1];
            String publicId;
            
            if (publicIdWithExtension.contains(".")) {
                publicId = publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf('.'));
            } else {
                publicId = publicIdWithExtension;
            }
            
            // FIXED: Suppress generic type warning for Transformation
            @SuppressWarnings("rawtypes")
            Transformation transformation = new Transformation()
                    .width(width)
                    .height(height)
                    .crop("fill");
                    // .quality("auto:good");
            
            String thumbnailUrl = cloudinary.url()
                    .transformation(transformation)
                    .generate(publicId);
                    
            return thumbnailUrl;
        } catch (Exception e) {
            return originalUrl;
        }
    }

    /**
     * Alternative method using string-based transformation (NO WARNINGS)
     */
    public String generateThumbnailUrlSimple(String originalUrl, int width, int height) {
        if (originalUrl == null || !originalUrl.contains("cloudinary.com")) {
            return originalUrl;
        }

        try {
            // Simple string replacement approach - no warnings
            String transformationString = "w_" + width + ",h_" + height + ",c_fill,q_auto:good";
            
            if (originalUrl.contains("/upload/")) {
                return originalUrl.replace("/upload/", "/upload/" + transformationString + "/");
            } else {
                return originalUrl;
            }
        } catch (Exception e) {
            return originalUrl;
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        long maxSize = 2 * 1024 * 1024; // 2MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid file extension");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    private boolean hasValidImageExtension(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        return lowercaseFilename.endsWith(".jpg") ||
               lowercaseFilename.endsWith(".jpeg") ||
               lowercaseFilename.endsWith(".png") ||
               lowercaseFilename.endsWith(".gif") ||
               lowercaseFilename.endsWith(".webp");
    }

    public boolean testConnection() {
        try {
            cloudinary.api().ping(new HashMap<>());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getConnectionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("cloud_name", cloudinary.config.cloudName);
        info.put("api_key", cloudinary.config.apiKey != null ? "configured" : "missing");
        info.put("api_secret", cloudinary.config.apiSecret != null ? "configured" : "missing");
        info.put("secure", cloudinary.config.secure);
        return info;
    }
}