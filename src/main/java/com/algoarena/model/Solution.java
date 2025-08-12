// src/main/java/com/algoarena/model/Solution.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "solutions")
public class Solution {

    @Id
    private String id;

    @DBRef
    private Question question;

    private String content;
    
    // ENHANCED: Multiple link options
    private String driveLink;        // Google Drive folder link
    private String youtubeLink;      // YouTube video explanation link
    
    // Image URLs for solution explanation
    private List<String> imageUrls;
    
    // HTML Visualizer file IDs (stored in GridFS)
    private List<String> visualizerFileIds;
    
    private CodeSnippet codeSnippet;

    @DBRef
    private User createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Inner class for code snippets
    public static class CodeSnippet {
        private String language;
        private String code;
        private String description;

        // Constructors
        public CodeSnippet() {}

        public CodeSnippet(String language, String code, String description) {
            this.language = language;
            this.code = code;
            this.description = description;
        }

        // Getters and Setters
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Constructors
    public Solution() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Solution(Question question, String content, User createdBy) {
        this();
        this.question = question;
        this.content = content;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDriveLink() {
        return driveLink;
    }

    public void setDriveLink(String driveLink) {
        this.driveLink = driveLink;
        this.updatedAt = LocalDateTime.now();
    }

    // NEW: YouTube Link
    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getVisualizerFileIds() {
        return visualizerFileIds;
    }

    public void setVisualizerFileIds(List<String> visualizerFileIds) {
        this.visualizerFileIds = visualizerFileIds;
        this.updatedAt = LocalDateTime.now();
    }

    public CodeSnippet getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(CodeSnippet codeSnippet) {
        this.codeSnippet = codeSnippet;
        this.updatedAt = LocalDateTime.now();
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    // Helper methods for link validation
    public boolean hasValidDriveLink() {
        return driveLink != null && driveLink.trim().length() > 0 && 
               driveLink.contains("drive.google.com");
    }

    public boolean hasValidYoutubeLink() {
        return youtubeLink != null && youtubeLink.trim().length() > 0 && 
               (youtubeLink.contains("youtube.com") || youtubeLink.contains("youtu.be"));
    }

    @Override
    public String toString() {
        return "Solution{" +
                "id='" + id + '\'' +
                ", question=" + (question != null ? question.getTitle() : "null") +
                ", createdBy=" + (createdBy != null ? createdBy.getName() : "null") +
                ", hasYoutubeLink=" + hasValidYoutubeLink() +
                ", hasDriveLink=" + hasValidDriveLink() +
                '}';
    }
}