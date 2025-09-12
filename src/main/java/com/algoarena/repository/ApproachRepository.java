// src/main/java/com/algoarena/repository/ApproachRepository.java - FINAL CLEAN VERSION
package com.algoarena.repository;

import com.algoarena.model.Approach;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApproachRepository extends MongoRepository<Approach, String> {

    // Find approaches by question and user
    List<Approach> findByQuestion_IdAndUser_Id(String questionId, String userId);

    // Find approaches by question and user with sorting
    List<Approach> findByQuestion_IdAndUser_IdOrderByCreatedAtAsc(String questionId, String userId);

    // Find approaches by user
    List<Approach> findByUser_Id(String userId);

    // Find approaches by question
    List<Approach> findByQuestion_Id(String questionId);

    // Count approaches for a user on a specific question (THIS WORKS PERFECTLY)
    long countByQuestion_IdAndUser_Id(String questionId, String userId);

    // Count total approaches by user
    long countByUser_Id(String userId);

    // Calculate total content size for user on a specific question
    @Query(value = "{ 'question': ?0, 'user': ?1 }", fields = "{ 'contentSize': 1 }")
    List<Approach> findApproachSizesByQuestionAndUser(String questionId, String userId);

    // Delete all approaches for a question
    void deleteByQuestion_Id(String questionId);

    // Delete all approaches by user for a specific question
    void deleteByQuestion_IdAndUser_Id(String questionId, String userId);

    // Find approaches by user with total content size
    @Query("{ 'user': ?0 }")
    List<Approach> findByUserWithContentSize(String userId);

    // Get total content size for a user across all questions
    @Query(value = "{ 'user': ?0 }", fields = "{ 'contentSize': 1 }")
    List<Approach> findContentSizesByUser(String userId);

    // Find recent approaches by user
    List<Approach> findTop10ByUser_IdOrderByUpdatedAtDesc(String userId);

    // Find approaches by user and multiple questions (for potential bulk operations)
    List<Approach> findByUser_IdAndQuestion_IdIn(String userId, List<String> questionIds);
}