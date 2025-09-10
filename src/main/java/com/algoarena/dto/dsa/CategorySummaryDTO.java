// src/main/java/com/algoarena/dto/dsa/CategorySummaryDTO.java
package com.algoarena.dto.dsa;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Category DTO with user progress statistics included
 * Used for categories listing page to avoid N+1 queries
 */
public class CategorySummaryDTO {
    private String id;
    private String name;
    private String createdByName;
    private String createdById;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    // Question statistics for this category
    private QuestionStats questionStats;
    
    // User progress statistics for this category
    private UserProgressStats userProgress;

    // Constructors
    public CategorySummaryDTO() {}

    public CategorySummaryDTO(String id, String name, String createdByName, String createdById,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdByName = createdByName;
        this.createdById = createdById;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public QuestionStats getQuestionStats() {
        return questionStats;
    }

    public void setQuestionStats(QuestionStats questionStats) {
        this.questionStats = questionStats;
    }

    public UserProgressStats getUserProgress() {
        return userProgress;
    }

    public void setUserProgress(UserProgressStats userProgress) {
        this.userProgress = userProgress;
    }

    /**
     * Inner class for question statistics per category
     */
    public static class QuestionStats {
        private int total;
        private ByLevel byLevel;

        public QuestionStats() {}

        public QuestionStats(int total, ByLevel byLevel) {
            this.total = total;
            this.byLevel = byLevel;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public ByLevel getByLevel() {
            return byLevel;
        }

        public void setByLevel(ByLevel byLevel) {
            this.byLevel = byLevel;
        }

        public static class ByLevel {
            private int easy;
            private int medium;
            private int hard;

            public ByLevel() {}

            public ByLevel(int easy, int medium, int hard) {
                this.easy = easy;
                this.medium = medium;
                this.hard = hard;
            }

            public int getEasy() {
                return easy;
            }

            public void setEasy(int easy) {
                this.easy = easy;
            }

            public int getMedium() {
                return medium;
            }

            public void setMedium(int medium) {
                this.medium = medium;
            }

            public int getHard() {
                return hard;
            }

            public void setHard(int hard) {
                this.hard = hard;
            }
        }
    }

    /**
     * Inner class for user progress statistics per category
     */
    public static class UserProgressStats {
        private int solved;
        private ByLevel solvedByLevel;
        private double progressPercentage;

        public UserProgressStats() {}

        public UserProgressStats(int solved, ByLevel solvedByLevel, double progressPercentage) {
            this.solved = solved;
            this.solvedByLevel = solvedByLevel;
            this.progressPercentage = progressPercentage;
        }

        public int getSolved() {
            return solved;
        }

        public void setSolved(int solved) {
            this.solved = solved;
        }

        public ByLevel getSolvedByLevel() {
            return solvedByLevel;
        }

        public void setSolvedByLevel(ByLevel solvedByLevel) {
            this.solvedByLevel = solvedByLevel;
        }

        public double getProgressPercentage() {
            return progressPercentage;
        }

        public void setProgressPercentage(double progressPercentage) {
            this.progressPercentage = progressPercentage;
        }

        public static class ByLevel {
            private int easy;
            private int medium;
            private int hard;

            public ByLevel() {}

            public ByLevel(int easy, int medium, int hard) {
                this.easy = easy;
                this.medium = medium;
                this.hard = hard;
            }

            public int getEasy() {
                return easy;
            }

            public void setEasy(int easy) {
                this.easy = easy;
            }

            public int getMedium() {
                return medium;
            }

            public void setMedium(int medium) {
                this.medium = medium;
            }

            public int getHard() {
                return hard;
            }

            public void setHard(int hard) {
                this.hard = hard;
            }
        }
    }
}