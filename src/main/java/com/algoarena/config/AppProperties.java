// src/main/java/com/algoarena/config/AppProperties.java

package com.algoarena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private Render render = new Render();
    private KeepAlive keepAlive = new KeepAlive();
    private Jwt jwt = new Jwt();
    private Cloudinary cloudinary = new Cloudinary();
    private Piston piston = new Piston();
    private File file = new File();
    private Cors cors = new Cors();
    
    // Main getters and setters
    public Render getRender() {
        return render;
    }
    
    public void setRender(Render render) {
        this.render = render;
    }
    
    public KeepAlive getKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(KeepAlive keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    public Jwt getJwt() {
        return jwt;
    }
    
    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }
    
    public Cloudinary getCloudinary() {
        return cloudinary;
    }
    
    public void setCloudinary(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    
    public Piston getPiston() {
        return piston;
    }
    
    public void setPiston(Piston piston) {
        this.piston = piston;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    public Cors getCors() {
        return cors;
    }
    
    public void setCors(Cors cors) {
        this.cors = cors;
    }
    
    // Inner classes for nested properties
    public static class Render {
        private String url;
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
    }
    
    public static class KeepAlive {
        private boolean enabled = true;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Jwt {
        private String secret;
        private long expiration = 86400000L; // 24 hours
        private long refreshExpiration = 604800000L; // 7 days
        
        public String getSecret() {
            return secret;
        }
        
        public void setSecret(String secret) {
            this.secret = secret;
        }
        
        public long getExpiration() {
            return expiration;
        }
        
        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
        
        public long getRefreshExpiration() {
            return refreshExpiration;
        }
        
        public void setRefreshExpiration(long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }
    
    public static class Cloudinary {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
        
        public String getCloudName() {
            return cloudName;
        }
        
        public void setCloudName(String cloudName) {
            this.cloudName = cloudName;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getApiSecret() {
            return apiSecret;
        }
        
        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }
    }
    
    public static class Piston {
        private String apiUrl = "https://emkc.org/api/v2/piston";
        private long timeout = 30000L;
        
        public String getApiUrl() {
            return apiUrl;
        }
        
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        
        public long getTimeout() {
            return timeout;
        }
        
        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }
    
    public static class File {
        private String maxFileSize = "10MB";
        private String maxRequestSize = "50MB";
        private Images images = new Images();
        private Html html = new Html();
        
        public String getMaxFileSize() {
            return maxFileSize;
        }
        
        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }
        
        public String getMaxRequestSize() {
            return maxRequestSize;
        }
        
        public void setMaxRequestSize(String maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }
        
        public Images getImages() {
            return images;
        }
        
        public void setImages(Images images) {
            this.images = images;
        }
        
        public Html getHtml() {
            return html;
        }
        
        public void setHtml(Html html) {
            this.html = html;
        }
        
        public static class Images {
            private String maxSize = "2MB";
            private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp";
            private int maxPerQuestion = 5;
            private int maxPerSolution = 10;
            
            public String getMaxSize() {
                return maxSize;
            }
            
            public void setMaxSize(String maxSize) {
                this.maxSize = maxSize;
            }
            
            public String getAllowedTypes() {
                return allowedTypes;
            }
            
            public void setAllowedTypes(String allowedTypes) {
                this.allowedTypes = allowedTypes;
            }
            
            public int getMaxPerQuestion() {
                return maxPerQuestion;
            }
            
            public void setMaxPerQuestion(int maxPerQuestion) {
                this.maxPerQuestion = maxPerQuestion;
            }
            
            public int getMaxPerSolution() {
                return maxPerSolution;
            }
            
            public void setMaxPerSolution(int maxPerSolution) {
                this.maxPerSolution = maxPerSolution;
            }
        }
        
        public static class Html {
            private String maxSize = "500KB";
            private int maxPerSolution = 2;
            
            public String getMaxSize() {
                return maxSize;
            }
            
            public void setMaxSize(String maxSize) {
                this.maxSize = maxSize;
            }
            
            public int getMaxPerSolution() {
                return maxPerSolution;
            }
            
            public void setMaxPerSolution(int maxPerSolution) {
                this.maxPerSolution = maxPerSolution;
            }
        }
    }
    
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000";
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
        
        public String getAllowedOrigins() {
            return allowedOrigins;
        }
        
        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
        
        public String getAllowedMethods() {
            return allowedMethods;
        }
        
        public void setAllowedMethods(String allowedMethods) {
            this.allowedMethods = allowedMethods;
        }
        
        public String getAllowedHeaders() {
            return allowedHeaders;
        }
        
        public void setAllowedHeaders(String allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }
        
        public boolean isAllowCredentials() {
            return allowCredentials;
        }
        
        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }
    }
}