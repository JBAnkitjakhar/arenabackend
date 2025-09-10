// src/main/java/com/algoarena/repository/QuestionRepository.java
package com.algoarena.repository;

import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

       // Find questions by category
       List<Question> findByCategory_Id(String categoryId);

       // Find questions by category with pagination
       Page<Question> findByCategory_Id(String categoryId, Pageable pageable);

       // Find questions by difficulty level
       List<Question> findByLevel(QuestionLevel level);

       // Find questions by category and level
       List<Question> findByCategory_IdAndLevel(String categoryId, QuestionLevel level);

       // Search questions by title (case-insensitive)
       @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
       List<Question> findByTitleContainingIgnoreCase(String title);

       // Search questions by title or statement
       @Query("{ $or: [ " +
                     "{ 'title': { $regex: ?0, $options: 'i' } }, " +
                     "{ 'statement': { $regex: ?0, $options: 'i' } } " +
                     "] }")
       List<Question> searchByTitleOrStatement(String searchTerm);

       // Find questions by creator
       List<Question> findByCreatedBy_Id(String createdById);

       // Count questions by category
       long countByCategory_Id(String categoryId);

       // Count questions by level
       long countByLevel(QuestionLevel level);

       // Find all questions with pagination and sorting
       Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);

       // Find questions in a category with pagination and sorting
       Page<Question> findByCategory_IdOrderByCreatedAtDesc(String categoryId, Pageable pageable);

       // Custom aggregation to get questions with solution count
       @Query(value = "{ 'category': ?0 }", fields = "{ 'title': 1, 'level': 1, 'createdAt': 1 }")
       List<Question> findQuestionSummaryByCategory(String categoryId);

       // Check if title exists (case-insensitive)
       @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
       boolean existsByTitleIgnoreCase(String title);
}