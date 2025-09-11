// src/main/java/com/algoarena/controller/dsa/QuestionController.java
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.QuestionDTO;
import com.algoarena.dto.dsa.QuestionDetailDTO;
import com.algoarena.dto.dsa.QuestionSummaryDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<QuestionDTO>> getAllQuestions(
            Pageable pageable,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search) {
        Page<QuestionDTO> questions = questionService.getAllQuestions(pageable, categoryId, level, search);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionDetailDTO> getQuestionDetails(
            @PathVariable String id,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        QuestionDetailDTO questionDetail = questionService.getQuestionDetails(id, currentUser.getId());
        if (questionDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questionDetail);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody QuestionDTO questionDTO,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        QuestionDTO createdQuestion = questionService.createQuestion(questionDTO, currentUser);
        return ResponseEntity.status(201).body(createdQuestion);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO updatedQuestion = questionService.updateQuestion(id, questionDTO);
            return ResponseEntity.ok(updatedQuestion);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, String>> deleteQuestion(@PathVariable String id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionDTO>> searchQuestions(@RequestParam String q) {
        List<QuestionDTO> questions = questionService.searchQuestions(q);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getQuestionStats() {
        Map<String, Object> stats = questionService.getQuestionCounts();
        return ResponseEntity.ok(stats);
    }

    /**
     * NEW OPTIMIZED ENDPOINT: Get questions summary with user progress
     * This endpoint eliminates N+1 queries by fetching user progress in bulk
     * GET /api/questions/summary
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<QuestionSummaryDTO>> getQuestionsSummaryWithProgress(
            Pageable pageable,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Page<QuestionSummaryDTO> questionsSummary = questionService.getQuestionsWithProgress(
                pageable, categoryId, level, search, currentUser.getId());

        return ResponseEntity.ok(questionsSummary);
    }
}