// src/main/java/com/algoarena/controller/HealthController.java

package com.algoarena.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Basic health check endpoint for keep-alive pings
     * Accessible at: https://arenabackend-5bca.onrender.com/api/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("message", "AlgoArena Backend is running!");
        status.put("service", "keep-alive-ping");

        return ResponseEntity.ok(status);
    }

    /**
     * Simple ping endpoint for testing
     * Accessible at: https://arenabackend-5bca.onrender.com/api/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check with MongoDB connectivity
     * Accessible at: https://arenabackend-5bca.onrender.com/api/healthz
     */
    @GetMapping("/healthz")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("application", "AlgoArena Backend");
        health.put("version", "1.0.0");

        // Check MongoDB connectivity
        try {
            // Simple ping to MongoDB
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            health.put("mongodb", "UP");
            health.put("database", "Connected");
        } catch (Exception e) {
            health.put("mongodb", "DOWN");
            health.put("database", "Connection failed: " + e.getMessage());
            // Still return 200 OK for keep-alive purposes
        }

        // Add system info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("processors", runtime.availableProcessors());
        health.put("system", systemInfo);

        return ResponseEntity.ok(health);
    }
}