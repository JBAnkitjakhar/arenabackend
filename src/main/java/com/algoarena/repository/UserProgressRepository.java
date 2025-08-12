// src/main/java/com/algoarena/repository/UserProgressRepository.java
package com.algoarena.repository;

import com.algoarena.model.UserProgress;
import com.algoarena.model.QuestionLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends MongoRepository<UserProgress, String> {

    // Find progress for a specific user and question
    Optional<UserProgress> findByUser_IdAndQuestion_Id(String userId, String questionId);

    // Find all progress for a user
    List<UserProgress> findByUser_Id(String userId);

    // Find solved questions for a user
    List<UserProgress> findByUser_IdAndSolvedTrue(String userId);

    // Find unsolved questions for a user
    List<UserProgress> findByUser_IdAndSolvedFalse(String userId);

    // Count solved questions by user
    long countByUser_IdAndSolvedTrue(String userId);

    // Count solved questions by user and level
    long countByUser_IdAndSolvedTrueAndLevel(String userId, QuestionLevel level);

    // Find progress by question (all users)
    List<UserProgress> findByQuestion_Id(String questionId);

    // Count how many users solved a specific question
    long countByQuestion_IdAndSolvedTrue(String questionId);

    // Delete progress for a question (when question is deleted)
    void deleteByQuestion_Id(String questionId);

    // Get user statistics
    @Query("{ 'user': ?0, 'solved': true }")
    List<UserProgress> findSolvedQuestionsByUser(String userId);

    // Get solved questions by level for a user
    List<UserProgress> findByUser_IdAndSolvedTrueAndLevel(String userId, QuestionLevel level);

    // Check if user has solved a question
    boolean existsByUser_IdAndQuestion_IdAndSolvedTrue(String userId, String questionId);

    // Get recent activity for a user
    List<UserProgress> findTop10ByUser_IdAndSolvedTrueOrderBySolvedAtDesc(String userId);

    // Get overall statistics
    @Query(value = "{ 'solved': true }", count = true)
    long countTotalSolvedQuestions();

    // Get level-wise statistics for a user
    @Query(value = "{ 'user': ?0, 'solved': true }", 
           fields = "{ 'level': 1 }")
    List<UserProgress> findSolvedQuestionLevelsByUser(String userId);
}