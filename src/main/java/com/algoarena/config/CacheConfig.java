// src/main/java/com/algoarena/config/CacheConfig.java - HYBRID CACHING WITH EVICTION

package com.algoarena.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.interceptor.KeyGenerator;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);

        // Configure cache names for different data types
        cacheManager.setCacheNames(Arrays.asList(
                // User-specific caches (evicted when user data changes)
                "questionsSummary",     // Questions with user progress
                "categoriesProgress",   // Categories with user progress  
                "userProgressStats",    // User progress statistics
                
                // Global caches (evicted when admin changes data)
                "questionsList",        // Basic questions without user data
                "categoriesList",       // Basic categories without user data
                "adminStats",          // Admin statistics
                
                // Short-lived caches
                "categoryStats"        // Category statistics
        ));

        return cacheManager;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            key.append(method.getName());
            for (Object param : params) {
                if (param != null) {
                    key.append("_").append(param.toString());
                }
            }
            return key.toString();
        };
    }
}