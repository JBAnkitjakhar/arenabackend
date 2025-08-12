// src/main/java/com/algoarena/repository/SolutionRepository.java
package com.algoarena.repository;

import com.algoarena.model.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends MongoRepository<Solution, String> {

    // Find solutions by question
    List<Solution> findByQuestion_Id(String questionId);
    
    // Find solutions by question with sorting
    List<Solution> findByQuestion_IdOrderByCreatedAtAsc(String questionId);

    // Find solutions by creator
    List<Solution> findByCreatedBy_Id(String createdById);

    // Count solutions for a question
    long countByQuestion_Id(String questionId);

    // Find all solutions with pagination
    Page<Solution> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Find solutions by question with pagination
    Page<Solution> findByQuestion_IdOrderByCreatedAtDesc(String questionId, Pageable pageable);

    // Find solutions with visualizers
    @Query("{ 'visualizerFileIds': { $exists: true, $not: { $size: 0 } } }")
    List<Solution> findSolutionsWithVisualizers();

    // Find solutions with images
    @Query("{ 'imageUrls': { $exists: true, $not: { $size: 0 } } }")
    List<Solution> findSolutionsWithImages();

    // NEW: Find solutions with YouTube videos
    @Query("{ 'youtubeLink': { $exists: true, $ne: null, $ne: '' } }")
    List<Solution> findSolutionsWithYoutubeVideos();

    // NEW: Find solutions with Google Drive links
    @Query("{ 'driveLink': { $exists: true, $ne: null, $ne: '' } }")
    List<Solution> findSolutionsWithDriveLinks();

    // NEW: Find solutions with both YouTube and Drive links
    @Query("{ $and: [ " +
           "{ 'youtubeLink': { $exists: true, $ne: null, $ne: '' } }, " +
           "{ 'driveLink': { $exists: true, $ne: null, $ne: '' } } " +
           "] }")
    List<Solution> findSolutionsWithBothLinks();

    // NEW: Find solutions by link type
    @Query("{ $or: [ " +
           "{ 'youtubeLink': { $regex: ?0, $options: 'i' } }, " +
           "{ 'driveLink': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<Solution> findSolutionsByLinkContent(String linkPattern);

    // Delete all solutions for a question
    void deleteByQuestion_Id(String questionId);

    // Custom query to get solution summary
    @Query(value = "{ 'question': ?0 }", 
           fields = "{ 'content': 1, 'createdBy': 1, 'createdAt': 1, 'driveLink': 1, 'youtubeLink': 1 }")
    List<Solution> findSolutionSummaryByQuestion(String questionId);

    // Find solutions by creator with pagination
    Page<Solution> findByCreatedBy_IdOrderByCreatedAtDesc(String createdById, Pageable pageable);

    // NEW: Get solutions with complete media (images + visualizers + videos)
    @Query("{ $and: [ " +
           "{ 'imageUrls': { $exists: true, $not: { $size: 0 } } }, " +
           "{ 'visualizerFileIds': { $exists: true, $not: { $size: 0 } } }, " +
           "{ 'youtubeLink': { $exists: true, $ne: null, $ne: '' } } " +
           "] }")
    List<Solution> findSolutionsWithCompleteMedia();

    // NEW: Count solutions by media type
    @Query(value = "{ 'youtubeLink': { $exists: true, $ne: null, $ne: '' } }", count = true)
    long countSolutionsWithYoutubeVideos();

    @Query(value = "{ 'driveLink': { $exists: true, $ne: null, $ne: '' } }", count = true)
    long countSolutionsWithDriveLinks();

    @Query(value = "{ 'imageUrls': { $exists: true, $not: { $size: 0 } } }", count = true)
    long countSolutionsWithImages();

    @Query(value = "{ 'visualizerFileIds': { $exists: true, $not: { $size: 0 } } }", count = true)
    long countSolutionsWithVisualizers();
}