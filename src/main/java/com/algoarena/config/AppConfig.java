// src/main/java/com/algoarena/config/AppConfig.java
package com.algoarena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    
    private Piston piston = new Piston();
    private FileUpload file = new FileUpload();
    private Cors cors = new Cors();
    
    // Nested Classes
    public static class Piston {
        private String apiUrl;
        private int timeout;
        
        // Getters and Setters
        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
    
    public static class FileUpload {
        private String maxFileSize;
        private String maxRequestSize;
        private Images images = new Images();
        private Html html = new Html();
        
        public static class Images {
            private String maxSize;
            private String allowedTypes;
            private int maxPerQuestion;
            private int maxPerSolution;
            
            // Getters and Setters
            public String getMaxSize() { return maxSize; }
            public void setMaxSize(String maxSize) { this.maxSize = maxSize; }
            public String getAllowedTypes() { return allowedTypes; }
            public void setAllowedTypes(String allowedTypes) { this.allowedTypes = allowedTypes; }
            public int getMaxPerQuestion() { return maxPerQuestion; }
            public void setMaxPerQuestion(int maxPerQuestion) { this.maxPerQuestion = maxPerQuestion; }
            public int getMaxPerSolution() { return maxPerSolution; }
            public void setMaxPerSolution(int maxPerSolution) { this.maxPerSolution = maxPerSolution; }
        }
        
        public static class Html {
            private String maxSize;
            private int maxPerSolution;
            
            // Getters and Setters
            public String getMaxSize() { return maxSize; }
            public void setMaxSize(String maxSize) { this.maxSize = maxSize; }
            public int getMaxPerSolution() { return maxPerSolution; }
            public void setMaxPerSolution(int maxPerSolution) { this.maxPerSolution = maxPerSolution; }
        }
        
        // Getters and Setters
        public String getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(String maxFileSize) { this.maxFileSize = maxFileSize; }
        public String getMaxRequestSize() { return maxRequestSize; }
        public void setMaxRequestSize(String maxRequestSize) { this.maxRequestSize = maxRequestSize; }
        public Images getImages() { return images; }
        public void setImages(Images images) { this.images = images; }
        public Html getHtml() { return html; }
        public void setHtml(Html html) { this.html = html; }
    }
    
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
        
        // Getters and Setters
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public String getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(String allowedMethods) { this.allowedMethods = allowedMethods; }
        public String getAllowedHeaders() { return allowedHeaders; }
        public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }
        public boolean isAllowCredentials() { return allowCredentials; }
        public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
    }
    
    // Main Getters and Setters
    public Piston getPiston() { return piston; }
    public void setPiston(Piston piston) { this.piston = piston; }
    public FileUpload getFile() { return file; }
    public void setFile(FileUpload file) { this.file = file; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
}




// # src/main/resources/application-local.properties
// # Local Development Configuration

// # MongoDB Atlas Configuration
// spring.data.mongodb.uri=mongodb+srv://ankitalgo:ankitalgo98@cluster0.zaz9iwg.mongodb.net/algoarena?retryWrites=true&w=majority&appName=Cluster0

// # OAuth2 Configuration - Replace with your actual values
// spring.security.oauth2.client.registration.google.client-id=239280589443-0pdoefpvjffetoc8a0b0vd9dt3suvbdk.apps.googleusercontent.com
// spring.security.oauth2.client.registration.google.client-secret=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

// spring.security.oauth2.client.registration.github.client-id=Ov23liWPiHmd3cahVDCS
// spring.security.oauth2.client.registration.github.client-secret=b66590dc2e3ca401f2c3a6f97bae7b1ffa423df2

// # JWT Configuration
// app.jwt.secret=de93cf7dff2e469fd3569ae38c093dc7d4ba51877fb866d79aa371a8bdcc795b84da70068af23e0b5f6f3a9af661cdfdfebe5a2af3788292ccb284accd2ca734

// # Cloudinary Configuration - Replace with your actual values
// app.cloudinary.cloud-name=dknqdokha
// app.cloudinary.api-key=854872614664444
// app.cloudinary.api-secret=kQIB-LORSXnomB5CgAWZimVF-xg

// # Redis Configuration (local fallback)
// spring.data.redis.url="rediss://default:AdVCAAIjcDEzZTE3MmIzZGVmMDA0YmIxYTEyZTg5OTNmMGZhYTJjY3AxMA@humble-cockatoo-54594.upstash.io:6379"

// # CORS for local development
// app.cors.allowed-origins=http://localhost:3000

// # Piston API
// app.piston.api-url=https://emkc.org/api/v2/piston