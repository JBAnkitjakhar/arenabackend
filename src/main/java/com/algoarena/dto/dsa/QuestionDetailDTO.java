// src/main/java/com/algoarena/dto/dsa/QuestionDetailDTO.java
package com.algoarena.dto.dsa;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionDetailDTO {

    private QuestionDTO question;
    private List<SolutionDTO> solutions;
    private boolean solved;
    private LocalDateTime solvedAt;

    // Constructors
    public QuestionDetailDTO() {}

    public QuestionDetailDTO(QuestionDTO question, List<SolutionDTO> solutions, boolean solved, LocalDateTime solvedAt) {
        this.question = question;
        this.solutions = solutions;
        this.solved = solved;
        this.solvedAt = solvedAt;
    }

    // Getters and Setters
    public QuestionDTO getQuestion() {
        return question;
    }

    public void setQuestion(QuestionDTO question) {
        this.question = question;
    }

    public List<SolutionDTO> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<SolutionDTO> solutions) {
        this.solutions = solutions;
    }

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
}