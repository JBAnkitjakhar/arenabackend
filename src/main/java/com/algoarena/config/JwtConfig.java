// src/main/java/com/algoarena/config/JwtConfig.java
package com.algoarena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    
    private String secret;
    private long expiration;
    private long refreshExpiration;
    
    // Constructors
    public JwtConfig() {}
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "JwtConfig{" +
                "secret='[HIDDEN]'" +
                ", expiration=" + expiration +
                ", refreshExpiration=" + refreshExpiration +
                '}';
    }
}