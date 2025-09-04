// we will handle that file use case later

// src/main/java/com/algoarena/config/CacheConfig.java

// package com.algoarena.config;

// import com.fasterxml.jackson.annotation.JsonAutoDetect;
// import com.fasterxml.jackson.annotation.JsonTypeInfo;
// import com.fasterxml.jackson.annotation.PropertyAccessor;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import org.springframework.cache.CacheManager;
// import org.springframework.cache.annotation.EnableCaching;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.cache.RedisCacheConfiguration;
// import org.springframework.data.redis.cache.RedisCacheManager;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
// import org.springframework.data.redis.serializer.RedisSerializationContext;
// import org.springframework.data.redis.serializer.StringRedisSerializer;

// import java.time.Duration;
// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// @EnableCaching
// public class CacheConfig {

//     /**
//      * Custom ObjectMapper for Redis with JSR310 support
//      */
//     @Bean
//     public ObjectMapper redisObjectMapper() {
//         ObjectMapper mapper = new ObjectMapper();
        
//         // Enable Java Time module for LocalDateTime support
//         mapper.registerModule(new JavaTimeModule());
        
//         // Configure visibility
//         mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
//         // Enable default typing for polymorphic deserialization
//         mapper.activateDefaultTyping(
//             LaissezFaireSubTypeValidator.instance,
//             ObjectMapper.DefaultTyping.NON_FINAL,
//             JsonTypeInfo.As.PROPERTY
//         );
        
//         // Disable writing dates as timestamps
//         mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
//         return mapper;
//     }

//     @Bean
//     public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//         // Create custom JSON serializer with proper ObjectMapper
//         GenericJackson2JsonRedisSerializer jsonSerializer = 
//             new GenericJackson2JsonRedisSerializer(redisObjectMapper());

//         // Default cache configuration
//         RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
//                 .entryTtl(Duration.ofHours(1)) // Default 1 hour TTL
//                 .serializeKeysWith(RedisSerializationContext.SerializationPair
//                         .fromSerializer(new StringRedisSerializer()))
//                 .serializeValuesWith(RedisSerializationContext.SerializationPair
//                         .fromSerializer(jsonSerializer))
//                 .disableCachingNullValues(); // Don't cache null values

//         // Custom cache configurations for different cache names
//         Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
//         // User progress cache - 1 hour TTL
//         cacheConfigurations.put("user-progress", defaultCacheConfig
//                 .entryTtl(Duration.ofHours(1)));
        
//         // Category progress cache - 30 minutes TTL  
//         cacheConfigurations.put("category-progress", defaultCacheConfig
//                 .entryTtl(Duration.ofMinutes(30)));
        
//         // User progress stats - 30 minutes TTL
//         cacheConfigurations.put("user-progress-stats", defaultCacheConfig
//                 .entryTtl(Duration.ofMinutes(30)));
        
//         // Recent progress cache - 10 minutes TTL (changes frequently)
//         cacheConfigurations.put("recent-progress", defaultCacheConfig
//                 .entryTtl(Duration.ofMinutes(10)));

//         // Test cache - 5 minutes TTL
//         cacheConfigurations.put("test-cache", defaultCacheConfig
//                 .entryTtl(Duration.ofMinutes(5)));

//         return RedisCacheManager.builder(redisConnectionFactory)
//                 .cacheDefaults(defaultCacheConfig)
//                 .withInitialCacheConfigurations(cacheConfigurations)
//                 .build();
//     }

//     @Bean
//     public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//         RedisTemplate<String, Object> template = new RedisTemplate<>();
//         template.setConnectionFactory(redisConnectionFactory);
        
//         // Use String serializer for keys
//         StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//         template.setKeySerializer(stringRedisSerializer);
//         template.setHashKeySerializer(stringRedisSerializer);
        
//         // Use custom JSON serializer for values
//         GenericJackson2JsonRedisSerializer jsonSerializer = 
//             new GenericJackson2JsonRedisSerializer(redisObjectMapper());
//         template.setValueSerializer(jsonSerializer);
//         template.setHashValueSerializer(jsonSerializer);
        
//         template.afterPropertiesSet();
//         return template;
//     }
// }