// src/main/java/com/algoarena/service/KeepAliveService.java

package com.algoarena.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class KeepAliveService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${app.render.url:}")
    private String renderUrl;
    
    @Value("${app.keep-alive.enabled:true}")
    private boolean keepAliveEnabled;
    
    public KeepAliveService() {
        // Use simple RestTemplate without timeout configuration to avoid deprecated APIs
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Self-ping every 14 minutes to prevent Render sleep (15 min timeout)
     * Fixed rate means it runs every 14 minutes regardless of execution time
     */
    @Scheduled(fixedRate = 14 * 60 * 1000) // 14 minutes in milliseconds
    public void keepAlive() {
        if (!keepAliveEnabled) {
            logger.debug("Keep-alive service is disabled");
            return;
        }
        
        if (renderUrl == null || renderUrl.isEmpty()) {
            logger.debug("Render URL not configured, skipping keep-alive ping");
            return;
        }
        
        try {
            String statusUrl = renderUrl + "/api/status";  // CHANGED: /status instead of /health
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            logger.info("Sending keep-alive ping at {} to: {}", timestamp, statusUrl);
            
            String response = restTemplate.getForObject(statusUrl, String.class);
            
            logger.info("Keep-alive ping successful! Response: {}", response);
            
        } catch (Exception e) {
            logger.warn("Keep-alive ping failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            // Don't throw exception - let it continue to try next time
        }
    }
    
    /**
     * Log service status on startup
     */
    @Scheduled(initialDelay = 30000, fixedRate = Long.MAX_VALUE) // Run once after 30 seconds
    public void logKeepAliveStatus() {
        if (keepAliveEnabled && renderUrl != null && !renderUrl.isEmpty()) {
            logger.info("Keep-Alive Service ACTIVE - App will stay awake on Render!");
            logger.info("Target URL: {}/api/status", renderUrl);  // CHANGED: /status instead of /health
            logger.info("Ping interval: Every 14 minutes");
        } else {
            logger.info("Keep-Alive Service DISABLED or URL not configured");
        }
    }
}