// src/main/java/com/algoarena/service/RedisTestService.java

package com.algoarena.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisTestService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Test basic Redis connectivity
     */
    public Map<String, Object> testRedisConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test basic set/get operations
            String testKey = "redis-test:" + System.currentTimeMillis();
            String testValue = "Redis is working! " + LocalDateTime.now();
            
            // Set value
            redisTemplate.opsForValue().set(testKey, testValue);
            
            // Get value back
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            // Clean up
            redisTemplate.delete(testKey);
            
            result.put("success", true);
            result.put("message", "Redis connection successful");
            result.put("testValue", testValue);
            result.put("retrievedValue", retrievedValue);
            result.put("valuesMatch", testValue.equals(retrievedValue));
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Redis connection failed");
            result.put("message", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * Test Spring Cache abstraction
     */
    @Cacheable(value = "test-cache", key = "#input")
    public Map<String, Object> testCacheFunction(String input) {
        // This method will be called only once for the same input
        // Subsequent calls will return cached result
        
        Map<String, Object> result = new HashMap<>();
        result.put("input", input);
        result.put("processedAt", LocalDateTime.now());
        result.put("randomValue", Math.random());
        result.put("message", "This result should be cached");
        
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return result;
    }

    /**
     * Get Redis info
     */
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Test connection
            String ping = redisTemplate.getConnectionFactory().getConnection().ping();
            info.put("ping", ping);
            info.put("connected", "PONG".equals(ping));
            
            // Get some basic info
            info.put("timestamp", LocalDateTime.now());
            info.put("redisTemplate", redisTemplate.getClass().getSimpleName());
            
        } catch (Exception e) {
            info.put("error", e.getMessage());
            info.put("connected", false);
        }
        
        return info;
    }
}