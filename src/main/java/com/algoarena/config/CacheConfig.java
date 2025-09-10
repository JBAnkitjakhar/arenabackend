// src/main/java/com/algoarena/config/CacheConfig.java
package com.algoarena.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);

        // Pre-configure cache names for better performance
        cacheManager.setCacheNames(Arrays.asList(
                "questionsSummary",
                "categoriesProgress",
                "userProgress",
                "categoryStats"));

        return cacheManager;
    }
}