// src/main/java/com/algoarena/dto/dsa/UserProgressDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.UserProgress;
import com.algoarena.model.QuestionLevel;

import java.time.LocalDateTime;

public class UserProgressDTO {

    private String id;

    private String userId;
    private String userName;

    private String questionId;
    private String questionTitle;

    private boolean solved;
    private QuestionLevel level;
    private LocalDateTime solvedAt;

    // Constructors
    public UserProgressDTO() {}

    public UserProgressDTO(UserProgress userProgress) {
        this.id = userProgress.getId();
        this.userId = userProgress.getUser() != null ? userProgress.getUser().getId() : null;
        this.userName = userProgress.getUser() != null ? userProgress.getUser().getName() : null;
        this.questionId = userProgress.getQuestion() != null ? userProgress.getQuestion().getId() : null;
        this.questionTitle = userProgress.getQuestion() != null ? userProgress.getQuestion().getTitle() : null;
        this.solved = userProgress.isSolved();
        this.level = userProgress.getLevel();
        this.solvedAt = userProgress.getSolvedAt();
    }

    // Static factory method
    public static UserProgressDTO fromEntity(UserProgress userProgress) {
        return new UserProgressDTO(userProgress);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public QuestionLevel getLevel() {
        return level;
    }

    public void setLevel(QuestionLevel level) {
        this.level = level;
    }

    public LocalDateTime getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
    }
}