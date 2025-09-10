// src/main/java/com/algoarena/dto/dsa/QuestionSummaryDTO.java

package com.algoarena.dto.dsa;

import com.algoarena.model.QuestionLevel;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Lightweight Question DTO with user progress included
 * Used for questions listing page to avoid N+1 queries
 */
public class QuestionSummaryDTO {
    private String id;
    private String title;
    private String categoryId;
    private String categoryName;
    private QuestionLevel level;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    
    // User progress embedded to avoid N+1 queries
    private UserProgressSummary userProgress;

    // Constructors
    public QuestionSummaryDTO() {}

    public QuestionSummaryDTO(String id, String title, String categoryId, String categoryName, 
                             QuestionLevel level, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.level = level;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public QuestionLevel getLevel() {
        return level;
    }

    public void setLevel(QuestionLevel level) {
        this.level = level;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserProgressSummary getUserProgress() {
        return userProgress;
    }

    public void setUserProgress(UserProgressSummary userProgress) {
        this.userProgress = userProgress;
    }

    /**
     * Inner class for user progress summary
     * Contains only essential user progress data
     */
    public static class UserProgressSummary {
        private boolean solved;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime solvedAt;
        
        private int approachCount;

        // Constructors
        public UserProgressSummary() {}

        public UserProgressSummary(boolean solved, LocalDateTime solvedAt, int approachCount) {
            this.solved = solved;
            this.solvedAt = solvedAt;
            this.approachCount = approachCount;
        }

        // Getters and Setters
        public boolean isSolved() {
            return solved;
        }

        public void setSolved(boolean solved) {
            this.solved = solved;
        }

        public LocalDateTime getSolvedAt() {
            return solvedAt;
        }

        public void setSolvedAt(LocalDateTime solvedAt) {
            this.solvedAt = solvedAt;
        }

        public int getApproachCount() {
            return approachCount;
        }

        public void setApproachCount(int approachCount) {
            this.approachCount = approachCount;
        }
    }
}