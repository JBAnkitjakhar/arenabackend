// src/main/java/com/algoarena/controller/admin/AdminController.java

package com.algoarena.controller.admin;

import com.algoarena.dto.admin.UserDTO;
import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import com.algoarena.service.admin.AdminService;  
import com.algoarena.service.admin.UserService;
import com.algoarena.service.dsa.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;   

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private UserService userService;

    /**
     * TEMPORARY DEBUG ENDPOINT
     * GET /api/admin/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Admin endpoint is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Get admin Home statistics (Enhanced with user stats)
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        try {
            Map<String, Object> stats = adminService.getAdminHomeStats();
            
            // Add user role statistics
            UserService.UserStatsDTO userStats = userService.getUserStats();
            Map<String, Object> roleStats = new HashMap<>();
            roleStats.put("totalUsers", userStats.getTotalUsers());
            roleStats.put("users", userStats.getUsers());
            roleStats.put("admins", userStats.getAdmins());
            roleStats.put("superAdmins", userStats.getSuperAdmins());
            
            stats.put("userRoles", roleStats);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "AdminService error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("cause", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get all users with pagination (Admin/SuperAdmin only)
     * GET /api/admin/users?page=0&size=20
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        try {
            Page<UserDTO> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get specific user details
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            UserDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "User not found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    /**
     * Get users by role with pagination
     * GET /api/admin/users/role/{role}?page=0&size=20
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @PathVariable String role,
            Pageable pageable) {
        try {
            UserRole userRole = UserRole.fromString(role);
            Page<UserDTO> users = userService.getUsersByRole(userRole, pageable);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Update user role (SuperAdmin can create admins, Primary SuperAdmin can do all)
     * PUT /api/admin/users/{userId}/role
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String userId,
            @RequestBody RoleUpdateRequest request,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            UserDTO updatedUser = userService.updateUserRole(userId, request.getRole(), currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User role updated successfully");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Role update failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        }
    }

    /**
     * Get user statistics
     * GET /api/admin/users/stats
     */
    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            UserService.UserStatsDTO stats = userService.getUserStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch user statistics");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get system settings
     * GET /api/admin/settings
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        try {
            Map<String, Object> settings = adminService.getSystemSettings();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Settings error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Update system settings
     * PUT /api/admin/settings
     */
    @PutMapping("/settings")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(@RequestBody Map<String, Object> settings) {
        try {
            Map<String, Object> updatedSettings = adminService.updateSystemSettings(settings);
            return ResponseEntity.ok(updatedSettings);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Update settings error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get global progress statistics  
     * GET /api/admin/progress
     */
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getGlobalProgress() {
        try {
            Map<String, Object> globalProgress = userProgressService.getGlobalStats();
            return ResponseEntity.ok(globalProgress);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "UserProgressService error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("cause", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get system health information
     * GET /api/admin/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = adminService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Health check error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get application metrics
     * GET /api/admin/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getApplicationMetrics() {
        try {
            Map<String, Object> metrics = adminService.getApplicationMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Metrics error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get role permissions matrix
     * GET /api/admin/users/permissions
     */
    @GetMapping("/users/permissions")
    public ResponseEntity<Map<String, Object>> getRolePermissions() {
        Map<String, Object> permissions = new HashMap<>();
        
        // Define what each role can do
        Map<String, Object> userPermissions = new HashMap<>();
        userPermissions.put("canCreateQuestions", false);
        userPermissions.put("canEditQuestions", false);
        userPermissions.put("canDeleteQuestions", false);
        userPermissions.put("canManageUsers", false);
        userPermissions.put("canChangeRoles", false);
        userPermissions.put("canAccessAdminPanel", false);
        
        Map<String, Object> adminPermissions = new HashMap<>();
        adminPermissions.put("canCreateQuestions", true);
        adminPermissions.put("canEditQuestions", true);
        adminPermissions.put("canDeleteQuestions", true);
        adminPermissions.put("canManageUsers", false);
        adminPermissions.put("canChangeRoles", false); // ZERO role management
        adminPermissions.put("canAccessAdminPanel", true);
        
        Map<String, Object> superAdminPermissions = new HashMap<>();
        superAdminPermissions.put("canCreateQuestions", true);
        superAdminPermissions.put("canEditQuestions", true);
        superAdminPermissions.put("canDeleteQuestions", true);
        superAdminPermissions.put("canManageUsers", true);
        superAdminPermissions.put("canChangeRoles", true); // Can create ADMIN
        superAdminPermissions.put("canAccessAdminPanel", true);
        superAdminPermissions.put("canManageSystemSettings", true);
        
        permissions.put("USER", userPermissions);
        permissions.put("ADMIN", adminPermissions);
        permissions.put("SUPERADMIN", superAdminPermissions);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("permissions", permissions);
        response.put("hierarchy", new String[]{"USER", "ADMIN", "SUPERADMIN", "PRIMARY_SUPERADMIN"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * Request DTO for role updates
     */
    public static class RoleUpdateRequest {
        private UserRole role;

        public RoleUpdateRequest() {}

        public RoleUpdateRequest(UserRole role) {
            this.role = role;
        }

        public UserRole getRole() {
            return role;
        }

        public void setRole(UserRole role) {
            this.role = role;
        }
    }
}