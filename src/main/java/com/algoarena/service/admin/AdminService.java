// src/main/java/com/algoarena/service/admin/AdminService.java
//at 92 line 
package com.algoarena.service.admin;

import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.ApproachRepository;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private ApproachRepository approachRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    /**
     * Get admin Home statistics
     */
    public Map<String, Object> getAdminHomeStats() {
        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        long totalUsers = userRepository.count();
        long totalQuestions = questionRepository.count();
        long totalSolutions = solutionRepository.count();
        long totalApproaches = approachRepository.count();
        long totalCategories = categoryRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalQuestions", totalQuestions);
        stats.put("totalSolutions", totalSolutions);
        stats.put("totalApproaches", totalApproaches);
        stats.put("totalCategories", totalCategories);

        // Progress statistics
        long totalProgress = userProgressRepository.count();
        long totalSolved = userProgressRepository.countTotalSolvedQuestions();
        
        stats.put("totalProgress", totalProgress);
        stats.put("totalSolved", totalSolved);
        
        // Calculate engagement metrics
        double avgProgressPerUser = totalUsers > 0 ? (double) totalProgress / totalUsers : 0.0;
        double avgSolvedPerUser = totalUsers > 0 ? (double) totalSolved / totalUsers : 0.0;
        
        stats.put("avgProgressPerUser", Math.round(avgProgressPerUser * 100.0) / 100.0);
        stats.put("avgSolvedPerUser", Math.round(avgSolvedPerUser * 100.0) / 100.0);

        // Recent activity (last 7 days) - placeholder implementation
        stats.put("recentUsers", "0"); // Would implement with date queries
        stats.put("recentQuestions", "0");
        stats.put("recentSolutions", "0");

        // System health
        stats.put("systemStatus", "healthy");
        stats.put("lastUpdated", System.currentTimeMillis());

        return stats;
    }

    /**
     * Get system settings (placeholder implementation)
     */
    public Map<String, Object> getSystemSettings() {
        Map<String, Object> settings = new HashMap<>();

        // Site settings
        Map<String, Object> siteSettings = new HashMap<>();
        siteSettings.put("siteName", "AlgoArena");
        siteSettings.put("siteDescription", "Master Data Structures & Algorithms");
        siteSettings.put("contactEmail", "admin@algoarena.com");
        siteSettings.put("maintenanceMode", false);

        // User settings
        Map<String, Object> userSettings = new HashMap<>();
        userSettings.put("allowRegistration", true);
        userSettings.put("requireEmailVerification", true);
        userSettings.put("defaultUserRole", "USER");
        userSettings.put("maxUsersPerDay", 100);

        // Security settings
        Map<String, Object> securitySettings = new HashMap<>();
        securitySettings.put("sessionTimeout", 24);
        securitySettings.put("passwordMinLength", 8);
        securitySettings.put("requireTwoFactor", false);
        securitySettings.put("allowOAuth", true);
        securitySettings.put("maxLoginAttempts", 5);

        // API settings
        Map<String, Object> apiSettings = new HashMap<>();
        apiSettings.put("rateLimitPerHour", 1000);
        apiSettings.put("enableApiDocs", true);
        apiSettings.put("apiTimeout", 30);
        apiSettings.put("maxFileSize", 10);

        // Notification settings
        Map<String, Object> notificationSettings = new HashMap<>();
        notificationSettings.put("emailNotifications", true);
        notificationSettings.put("systemAlerts", true);
        notificationSettings.put("userWelcomeEmail", true);
        notificationSettings.put("adminNotifications", true);

        settings.put("site", siteSettings);
        settings.put("users", userSettings);
        settings.put("security", securitySettings);
        settings.put("api", apiSettings);
        settings.put("notifications", notificationSettings);
        settings.put("lastUpdated", System.currentTimeMillis());

        return settings;
    }

    /**
     * Update system settings (placeholder implementation)
     */
    public Map<String, Object> updateSystemSettings(Map<String, Object> newSettings) {
        // In a real implementation, you would:
        // 1. Validate the settings
        // 2. Save them to database or configuration file
        // 3. Apply them to the application
        
        // For now, just return the settings with timestamp
        Map<String, Object> updatedSettings = new HashMap<>(newSettings);
        updatedSettings.put("lastUpdated", System.currentTimeMillis());
        updatedSettings.put("updatedBy", "system"); // Would get from authentication
        
        return updatedSettings;
    }

    /**
     * Get system health information
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Test database connectivity
            long userCount = userRepository.count();
            health.put("database", "healthy");
            health.put("userCount", userCount);
        } catch (Exception e) {
            health.put("database", "unhealthy");
            health.put("databaseError", e.getMessage());
        }

        // Add more health checks as needed
        health.put("timestamp", System.currentTimeMillis());
        health.put("uptime", "unknown"); // Would implement with application start time
        
        return health;
    }

    /**
     * Get application metrics for monitoring
     */
    public Map<String, Object> getApplicationMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Database metrics
        Map<String, Object> dbMetrics = new HashMap<>();
        dbMetrics.put("totalUsers", userRepository.count());
        dbMetrics.put("totalQuestions", questionRepository.count());
        dbMetrics.put("totalSolutions", solutionRepository.count());
        dbMetrics.put("totalApproaches", approachRepository.count());
        dbMetrics.put("totalCategories", categoryRepository.count());

        // Performance metrics (placeholder)
        Map<String, Object> perfMetrics = new HashMap<>();
        perfMetrics.put("avgResponseTime", "unknown");
        perfMetrics.put("requestsPerMinute", "unknown");
        perfMetrics.put("errorRate", "unknown");

        metrics.put("database", dbMetrics);
        metrics.put("performance", perfMetrics);
        metrics.put("timestamp", System.currentTimeMillis());

        return metrics;
    }
}