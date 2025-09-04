// only for test case

// src/main/java/com/algoarena/controller/RedisTestController.java

// package com.algoarena.controller;

// import com.algoarena.service.RedisTestService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;

// import java.util.Map;

// @RestController
// @RequestMapping("/test/redis")
// @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')") // Admin only endpoints
// public class RedisTestController {

//     @Autowired
//     private RedisTestService redisTestService;

//     /**
//      * Test Redis connectivity
//      * GET /api/test/redis/health
//      */
//     @GetMapping("/health")
//     public ResponseEntity<Map<String, Object>> testRedisHealth() {
//         Map<String, Object> result = redisTestService.testRedisConnection();
//         return ResponseEntity.ok(result);
//     }

//     /**
//      * Test Spring Cache functionality
//      * GET /api/test/redis/cache?input=test123
//      */
//     @GetMapping("/cache")
//     public ResponseEntity<Map<String, Object>> testCache(@RequestParam(defaultValue = "defaultTest") String input) {
//         Map<String, Object> result = redisTestService.testCacheFunction(input);
//         return ResponseEntity.ok(result);
//     }

//     /**
//      * Get Redis connection info
//      * GET /api/test/redis/info
//      */
//     @GetMapping("/info")
//     public ResponseEntity<Map<String, Object>> getRedisInfo() {
//         Map<String, Object> info = redisTestService.getRedisInfo();
//         return ResponseEntity.ok(info);
//     }

//     /**
//      * Clear cache for testing (Admin only)
//      * DELETE /api/test/redis/cache/clear
//      */
//     @DeleteMapping("/cache/clear")
//     public ResponseEntity<Map<String, Object>> clearCache() {
//         // This would require cache manager injection to clear specific caches
//         Map<String, Object> result = Map.of(
//             "message", "Cache clear endpoint - implementation needed",
//             "timestamp", java.time.LocalDateTime.now()
//         );
//         return ResponseEntity.ok(result);
//     }
// }