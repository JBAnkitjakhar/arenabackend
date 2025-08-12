// src/main/java/com/algoarena/config/CloudinaryConfig.java
package com.algoarena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cloudinary")
public class CloudinaryConfig {
    
    private String cloudName;
    private String apiKey;
    private String apiSecret;
    
    // Constructors
    public CloudinaryConfig() {}
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "CloudinaryConfig{" +
                "cloudName='" + cloudName + '\'' +
                ", apiKey='[HIDDEN]'" +
                ", apiSecret='[HIDDEN]'" +
                '}';
    }
}