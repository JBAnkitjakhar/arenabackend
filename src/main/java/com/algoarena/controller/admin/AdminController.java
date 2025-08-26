// src/main/java/com/algoarena/controller/admin/AdminController.java

package com.algoarena.controller.admin;

import com.algoarena.service.admin.AdminService;  
import com.algoarena.service.dsa.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;   

    @Autowired
    private UserProgressService userProgressService;

    /**
     * Get admin dashboard statistics
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = adminService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get system settings
     * GET /api/admin/settings
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        Map<String, Object> settings = adminService.getSystemSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * Update system settings
     * PUT /api/admin/settings
     */
    @PutMapping("/settings")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(@RequestBody Map<String, Object> settings) {
        Map<String, Object> updatedSettings = adminService.updateSystemSettings(settings);
        return ResponseEntity.ok(updatedSettings);
    }

    /**
     * Get global progress statistics
     * GET /api/admin/progress
     */
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getGlobalProgress() {
        Map<String, Object> globalProgress = userProgressService.getGlobalStats();
        return ResponseEntity.ok(globalProgress);
    }

    /**
     * Get system health information
     * GET /api/admin/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = adminService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * Get application metrics
     * GET /api/admin/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getApplicationMetrics() {
        Map<String, Object> metrics = adminService.getApplicationMetrics();
        return ResponseEntity.ok(metrics);
    }
}