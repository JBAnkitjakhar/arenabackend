// src/main/java/com/algoarena/dto/dsa/ApproachDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.Approach;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ApproachDTO {

    private String id;

    private String questionId;
    private String questionTitle;

    private String userId;
    private String userName;

    @NotBlank(message = "Text content is required")
    @Size(min = 10, message = "Text content must be at least 10 characters")
    private String textContent;

    private String codeContent;

    @Size(max = 50, message = "Code language must not exceed 50 characters")
    private String codeLanguage;

    private int contentSize;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ApproachDTO() {}

    public ApproachDTO(Approach approach) {
        this.id = approach.getId();
        this.questionId = approach.getQuestion() != null ? approach.getQuestion().getId() : null;
        this.questionTitle = approach.getQuestion() != null ? approach.getQuestion().getTitle() : null;
        this.userId = approach.getUser() != null ? approach.getUser().getId() : null;
        this.userName = approach.getUser() != null ? approach.getUser().getName() : null;
        this.textContent = approach.getTextContent();
        this.codeContent = approach.getCodeContent();
        this.codeLanguage = approach.getCodeLanguage();
        this.contentSize = approach.getContentSize();
        this.createdAt = approach.getCreatedAt();
        this.updatedAt = approach.getUpdatedAt();
    }

    // Static factory method
    public static ApproachDTO fromEntity(Approach approach) {
        return new ApproachDTO(approach);
    }

    // Helper method to calculate content size (for validation)
    public int calculateContentSize() {
        int size = 0;
        if (textContent != null) {
            size += textContent.getBytes().length;
        }
        if (codeContent != null) {
            size += codeContent.getBytes().length;
        }
        return size;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
        this.contentSize = calculateContentSize(); // Recalculate size
    }

    public String getCodeContent() {
        return codeContent;
    }

    public void setCodeContent(String codeContent) {
        this.codeContent = codeContent;
        this.contentSize = calculateContentSize(); // Recalculate size
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    public int getContentSize() {
        return contentSize;
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
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
}