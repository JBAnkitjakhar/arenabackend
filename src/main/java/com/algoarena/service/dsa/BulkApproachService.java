// NEW: src/main/java/com/algoarena/service/dsa/BulkApproachService.java
package com.algoarena.service.dsa;

import com.algoarena.repository.ApproachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for bulk approach operations to avoid N+1 queries
 * Uses MongoDB aggregation pipeline for efficient bulk counting
 */
@Service
public class BulkApproachService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired 
    private ApproachRepository approachRepository;

    /**
     * BULK: Get approach counts for multiple questions in a single database query
     * This replaces N individual queries with 1 aggregation pipeline
     * 
     * @param userId User ID to count approaches for
     * @param questionIds List of question IDs to get counts for
     * @return Map of questionId -> count
     */
    public Map<String, Integer> getBulkApproachCounts(String userId, List<String> questionIds) {
        if (questionIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            // Convert string IDs to ObjectId for MongoDB matching
            List<ObjectId> questionObjectIds = questionIds.stream()
                    .map(ObjectId::new)
                    .toList();
            ObjectId userObjectId = new ObjectId(userId);

            // Build aggregation pipeline
            MatchOperation matchStage = Aggregation.match(
                Criteria.where("user.$id").is(userObjectId)
                       .and("question.$id").in(questionObjectIds)
            );

            GroupOperation groupStage = Aggregation.group("question.$id")
                    .count().as("count");

            Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                groupStage
            );

            // Execute aggregation
            AggregationResults<ApproachCountResult> results = mongoTemplate.aggregate(
                aggregation, 
                "approaches",  // Collection name
                ApproachCountResult.class
            );

            // Convert results to map
            Map<String, Integer> countMap = new HashMap<>();
            
            // Initialize all questions with 0 count
            for (String questionId : questionIds) {
                countMap.put(questionId, 0);
            }
            
            // Update with actual counts
            for (ApproachCountResult result : results.getMappedResults()) {
                String questionId = result.getId().toString();
                countMap.put(questionId, result.getCount());
            }

            // System.out.println("BULK SUCCESS: Counted approaches for " + questionIds.size() + 
            //                  " questions in single aggregation query. Found " + 
            //                  results.getMappedResults().size() + " questions with approaches.");

            return countMap;

        } catch (Exception e) {
            // System.out.println("BULK FAILED: Aggregation failed, falling back to individual queries: " + e.getMessage());
            
            // Fallback to individual queries
            return getFallbackApproachCounts(userId, questionIds);
        }
    }

    /**
     * Fallback method using individual queries if bulk aggregation fails
     */
    private Map<String, Integer> getFallbackApproachCounts(String userId, List<String> questionIds) {
        Map<String, Integer> countMap = new HashMap<>();
        
        for (String questionId : questionIds) {
            try {
                long count = approachRepository.countByQuestion_IdAndUser_Id(questionId, userId);
                countMap.put(questionId, (int) count);
            } catch (Exception e) {
                // System.out.println("ERROR counting approaches for question " + questionId + ": " + e.getMessage());
                countMap.put(questionId, 0);
            }
        }
        
        // System.out.println("FALLBACK SUCCESS: Used individual queries for " + questionIds.size() + " questions");
        return countMap;
    }

    /**
     * Result class for aggregation pipeline
     */
    public static class ApproachCountResult {
        private ObjectId id;  // This will be the question._id
        private int count;

        public ObjectId getId() { return id; }
        public void setId(ObjectId id) { this.id = id; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}