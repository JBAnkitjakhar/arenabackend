// src/main/java/com/algoarena/repository/UserRepository.java
package com.algoarena.repository;

import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by Google ID
    Optional<User> findByGoogleId(String googleId);

    // Find user by GitHub ID
    Optional<User> findByGithubId(String githubId);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRole(UserRole role);

    // Count users by role
    long countByRole(UserRole role);

    // Find all admin users
    @Query("{ 'role': { $in: ['ADMIN', 'SUPERADMIN'] } }")
    List<User> findAllAdmins();

    // Count total users
    @Query(value = "{}", count = true)
    long countAllUsers();

    // Find users with pagination (handled by MongoRepository)
    // List<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Custom query to find user by OAuth provider info
    @Query("{ $or: [ { 'googleId': ?0 }, { 'githubId': ?0 }, { 'email': ?1 } ] }")
    Optional<User> findByOAuthInfo(String providerId, String email);
}