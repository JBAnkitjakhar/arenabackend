// src/main/java/com/algoarena/service/admin/UserService.java
package com.algoarena.service.admin;

import com.algoarena.dto.admin.UserDTO;
import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import com.algoarena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Primary Super Admin email
    private static final String PRIMARY_SUPER_ADMIN_EMAIL = "ankitjakharabc@gmail.com";

    /**
     * Get all users with pagination
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        List<UserDTO> userDTOs = users.getContent()
                .stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(userDTOs, pageable, users.getTotalElements());
    }

    /**
     * Get users by role with pagination
     */
    public Page<UserDTO> getUsersByRole(UserRole role, Pageable pageable) {
        List<User> allUsers = userRepository.findByRole(role);
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUsers.size());
        
        List<User> pageUsers = allUsers.subList(start, end);
        List<UserDTO> userDTOs = pageUsers.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(userDTOs, pageable, allUsers.size());
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToUserDTO(user);
    }

    /**
     * Update user role with proper authorization checks
     */
    public UserDTO updateUserRole(String userId, UserRole newRole, User currentUser) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Validation checks
        validateRoleUpdatePermission(currentUser, targetUser, newRole);

        // Update the role
        targetUser.setRole(newRole);
        targetUser.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(targetUser);
        return convertToUserDTO(updatedUser);
    }

    /**
     * Get user statistics
     */
    public UserStatsDTO getUserStats() {
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        long superAdminCount = userRepository.countByRole(UserRole.SUPERADMIN);
        long userCount = userRepository.countByRole(UserRole.USER);

        return new UserStatsDTO(totalUsers, userCount, adminCount, superAdminCount);
    }

    /**
     * Validate role update permissions
     */
    private void validateRoleUpdatePermission(User currentUser, User targetUser, UserRole newRole) {
        boolean isPrimarySuperAdmin = PRIMARY_SUPER_ADMIN_EMAIL.equals(currentUser.getEmail());
        boolean isTargetPrimarySuperAdmin = PRIMARY_SUPER_ADMIN_EMAIL.equals(targetUser.getEmail());

        // Rule 1: No one can change primary super admin's role
        if (isTargetPrimarySuperAdmin) {
            throw new RuntimeException("Primary Super Admin role cannot be modified");
        }

        // Rule 2: Only primary super admin can create/modify super admins
        if (newRole == UserRole.SUPERADMIN && !isPrimarySuperAdmin) {
            throw new RuntimeException("Only Primary Super Admin can create Super Admins");
        }

        // Rule 3: Super admins cannot modify other super admins (except primary)
        if (targetUser.getRole() == UserRole.SUPERADMIN && 
            currentUser.getRole() == UserRole.SUPERADMIN && 
            !isPrimarySuperAdmin) {
            throw new RuntimeException("Super Admins cannot modify other Super Admins");
        }

        // Rule 4: Admins have NO role management permissions at all
        if (currentUser.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Admins do not have permission to modify user roles");
        }

        // Rule 5: Users cannot modify any roles
        if (currentUser.getRole() == UserRole.USER) {
            throw new RuntimeException("Users cannot modify roles");
        }

        // Rule 6: Cannot modify self (prevent accidental role changes)
        if (currentUser.getId().equals(targetUser.getId()) && !isPrimarySuperAdmin) {
            throw new RuntimeException("Cannot modify your own role");
        }
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getImage(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            PRIMARY_SUPER_ADMIN_EMAIL.equals(user.getEmail())
        );
    }

    /**
     * Inner class for user statistics
     */
    public static class UserStatsDTO {
        private final long totalUsers;
        private final long users;
        private final long admins;
        private final long superAdmins;

        public UserStatsDTO(long totalUsers, long users, long admins, long superAdmins) {
            this.totalUsers = totalUsers;
            this.users = users;
            this.admins = admins;
            this.superAdmins = superAdmins;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getUsers() { return users; }
        public long getAdmins() { return admins; }
        public long getSuperAdmins() { return superAdmins; }
    }
}