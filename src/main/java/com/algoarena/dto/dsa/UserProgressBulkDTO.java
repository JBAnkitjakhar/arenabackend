// src/main/java/com/algoarena/dto/dsa/UserProgressBulkDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.QuestionLevel;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for bulk user progress operations
 * Contains all progress data for a user in a single response
 */
public class UserProgressBulkDTO {

    private String userId;
    private String userName;
    
    // Map of questionId -> progress summary
    private Map<String, QuestionProgressSummary> progressMap;
    
    // Overall statistics
    private UserProgressStatsSummary stats;
    
    private LocalDateTime lastUpdated;

    // Nested classes for clean data structure
    public static class QuestionProgressSummary {
        private boolean solved;
        private QuestionLevel level;
        private LocalDateTime solvedAt;

        // Constructors
        public QuestionProgressSummary() {}

        public QuestionProgressSummary(boolean solved, QuestionLevel level, LocalDateTime solvedAt) {
            this.solved = solved;
            this.level = level;
            this.solvedAt = solvedAt;
        }

        // Getters and Setters
        public boolean isSolved() { return solved; }
        public void setSolved(boolean solved) { this.solved = solved; }
        public QuestionLevel getLevel() { return level; }
        public void setLevel(QuestionLevel level) { this.level = level; }
        public LocalDateTime getSolvedAt() { return solvedAt; }
        public void setSolvedAt(LocalDateTime solvedAt) { this.solvedAt = solvedAt; }
    }

    public static class UserProgressStatsSummary {
        private int totalSolved;
        private int totalQuestions;
        private double progressPercentage;
        private Map<String, Integer> solvedByLevel; // easy, medium, hard

        // Constructors
        public UserProgressStatsSummary() {}

        // Getters and Setters
        public int getTotalSolved() { return totalSolved; }
        public void setTotalSolved(int totalSolved) { this.totalSolved = totalSolved; }
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
        public Map<String, Integer> getSolvedByLevel() { return solvedByLevel; }
        public void setSolvedByLevel(Map<String, Integer> solvedByLevel) { this.solvedByLevel = solvedByLevel; }
    }

    // Main class constructors
    public UserProgressBulkDTO() {}

    public UserProgressBulkDTO(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public Map<String, QuestionProgressSummary> getProgressMap() { return progressMap; }
    public void setProgressMap(Map<String, QuestionProgressSummary> progressMap) { this.progressMap = progressMap; }
    
    public UserProgressStatsSummary getStats() { return stats; }
    public void setStats(UserProgressStatsSummary stats) { this.stats = stats; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}