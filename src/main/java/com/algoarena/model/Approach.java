// src/main/java/com/algoarena/model/Approach.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "approaches")
@CompoundIndex(def = "{'question': 1, 'user': 1}")
public class Approach {

    @Id
    private String id;

    @DBRef
    private Question question;

    @DBRef
    private User user;

    private String textContent;
    private String codeContent;
    private String codeLanguage;
    
    // Content size tracking for 15KB limit
    private int contentSize;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Approach() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.codeLanguage = "javascript"; // Default
    }

    public Approach(Question question, User user, String textContent) {
        this();
        this.question = question;
        this.user = user;
        this.textContent = textContent;
        this.contentSize = calculateContentSize();
    }

    // Helper method to calculate content size
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

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
        this.contentSize = calculateContentSize();
        this.updatedAt = LocalDateTime.now();
    }

    public String getCodeContent() {
        return codeContent;
    }

    public void setCodeContent(String codeContent) {
        this.codeContent = codeContent;
        this.contentSize = calculateContentSize();
        this.updatedAt = LocalDateTime.now();
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
        this.updatedAt = LocalDateTime.now();
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

    @Override
    public String toString() {
        return "Approach{" +
                "id='" + id + '\'' +
                ", question=" + (question != null ? question.getTitle() : "null") +
                ", user=" + (user != null ? user.getName() : "null") +
                ", contentSize=" + contentSize +
                '}';
    }
}