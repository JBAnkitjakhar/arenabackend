// src/main/java/com/algoarena/model/Question.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "questions")
public class Question {

    @Id
    private String id;

    private String title;
    private String statement;
    
    // Image URLs for question explanation
    private List<String> imageUrls;
    
    // Keep for backward compatibility (will be migrated to imageUrls)
    private String imageFolderUrl;
    
    private List<CodeSnippet> codeSnippets;

    @DBRef
    private Category category;

    private QuestionLevel level;

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
    public Question() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Question(String title, String statement, Category category, QuestionLevel level, User createdBy) {
        this();
        this.title = title;
        this.statement = statement;
        this.category = category;
        this.level = level;
        this.createdBy = createdBy;
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
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        this.updatedAt = LocalDateTime.now();
    }

    public String getImageFolderUrl() {
        return imageFolderUrl;
    }

    public void setImageFolderUrl(String imageFolderUrl) {
        this.imageFolderUrl = imageFolderUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public List<CodeSnippet> getCodeSnippets() {
        return codeSnippets;
    }

    public void setCodeSnippets(List<CodeSnippet> codeSnippets) {
        this.codeSnippets = codeSnippets;
        this.updatedAt = LocalDateTime.now();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public QuestionLevel getLevel() {
        return level;
    }

    public void setLevel(QuestionLevel level) {
        this.level = level;
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

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", level=" + level +
                ", category=" + (category != null ? category.getName() : "null") +
                '}';
    }
}