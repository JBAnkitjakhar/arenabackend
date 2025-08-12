// src/main/java/com/algoarena/model/UserProgress.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "userprogress")
@CompoundIndex(def = "{'user': 1, 'question': 1}", unique = true)
public class UserProgress {

    @Id
    private String id;

    @DBRef
    private User user;

    @DBRef
    private Question question;

    private boolean solved;
    private QuestionLevel level;
    private LocalDateTime solvedAt;

    // Constructors
    public UserProgress() {}

    public UserProgress(User user, Question question, boolean solved, QuestionLevel level) {
        this.user = user;
        this.question = question;
        this.solved = solved;
        this.level = level;
        if (solved) {
            this.solvedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
        if (solved && this.solvedAt == null) {
            this.solvedAt = LocalDateTime.now();
        } else if (!solved) {
            this.solvedAt = null;
        }
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

    @Override
    public String toString() {
        return "UserProgress{" +
                "id='" + id + '\'' +
                ", user=" + (user != null ? user.getName() : "null") +
                ", question=" + (question != null ? question.getTitle() : "null") +
                ", solved=" + solved +
                ", level=" + level +
                '}';
    }
}