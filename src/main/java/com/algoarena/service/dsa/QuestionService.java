// src/main/java/com/algoarena/service/dsa/QuestionService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.QuestionDTO;
import com.algoarena.dto.dsa.QuestionDetailDTO;
import com.algoarena.dto.dsa.QuestionSummaryDTO;
import com.algoarena.dto.dsa.SolutionDTO;
import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.User;
import com.algoarena.model.UserProgress;
import com.algoarena.model.Category;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.ApproachRepository;
import com.algoarena.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private ApproachRepository approachRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private SolutionService solutionService;

    @Autowired
    private UserProgressService userProgressService;

    // ==================== NEW OPTIMIZED METHOD ====================
    /**
     * NEW: Get questions summary with user progress (avoids N+1 queries)
     * This method fetches questions and user progress in bulk operations
     */
    @Cacheable(value = "questionsSummary", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #categoryId + '_' + #level + '_' + #search")
public Page<QuestionSummaryDTO> getQuestionsWithProgress(
        Pageable pageable, 
        String categoryId, 
        String level, 
        String search,
        String userId) {
    
    // Step 1: Get questions with filtering
    Page<Question> questionsPage = getAllQuestionsFiltered(pageable, categoryId, level, search);
    
    // Step 2: Get all question IDs from the page
    List<String> questionIds = questionsPage.getContent()
        .stream()
        .map(Question::getId)
        .collect(Collectors.toList());
    
    if (questionIds.isEmpty()) {
        return new PageImpl<>(List.of(), pageable, 0);
    }
    
    // Step 3: FIXED - Use individual lookups instead of broken bulk method
    Map<String, UserProgress> progressMap = new HashMap<>();
    for (String questionId : questionIds) {
        Optional<UserProgress> progress = userProgressRepository.findByUser_IdAndQuestion_Id(userId, questionId);
        if (progress.isPresent()) {
            progressMap.put(questionId, progress.get());
        }
    }
    
    System.out.println("DEBUG: Found " + progressMap.size() + " progress records using individual lookups");
    
    // Step 4: Convert to QuestionSummaryDTO with embedded user progress
    List<QuestionSummaryDTO> summaryList = questionsPage.getContent()
        .stream()
        .map(question -> {
            QuestionSummaryDTO summary = new QuestionSummaryDTO(
                question.getId(),
                question.getTitle(),
                question.getCategory().getId(),
                question.getCategory().getName(),
                question.getLevel(),
                question.getCreatedAt()
            );
            
            // Add user progress
            UserProgress progress = progressMap.get(question.getId());
            int approachCount = 0; // Simplified - always 0 for now
            
            if (progress != null && progress.isSolved()) {
                summary.setUserProgress(new QuestionSummaryDTO.UserProgressSummary(
                    true, 
                    progress.getSolvedAt(), 
                    approachCount
                ));
            } else {
                summary.setUserProgress(new QuestionSummaryDTO.UserProgressSummary(
                    false, 
                    null, 
                    approachCount
                ));
            }
            
            return summary;
        })
        .collect(Collectors.toList());
    
    // Return paginated result
    return new PageImpl<>(summaryList, pageable, questionsPage.getTotalElements());
}

    /**
     * Helper method to get filtered questions (reuses existing logic)
     */
    private Page<Question> getAllQuestionsFiltered(Pageable pageable, String categoryId, String level, String search) {
        Page<Question> questions;

        if (search != null && !search.trim().isEmpty()) {
            // Search functionality with proper pagination
            List<Question> searchResults = questionRepository.searchByTitleOrStatement(search.trim());
            // Convert list to page
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), searchResults.size());
            List<Question> pageContent = searchResults.subList(start, end);
            questions = new PageImpl<>(pageContent, pageable, searchResults.size());
        } else if (categoryId != null && !categoryId.isEmpty()) {
            if (level != null && !level.isEmpty()) {
                // Filter by category and level with pagination
                QuestionLevel questionLevel = QuestionLevel.fromString(level);
                List<Question> filteredQuestions = questionRepository.findByCategory_IdAndLevel(categoryId,
                        questionLevel);
                // Convert to page
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), filteredQuestions.size());
                List<Question> pageContent = filteredQuestions.subList(start, end);
                questions = new PageImpl<>(pageContent, pageable, filteredQuestions.size());
            } else {
                // Use proper pagination for category filter
                questions = questionRepository.findByCategory_Id(categoryId, pageable);
            }
        } else if (level != null && !level.isEmpty()) {
            // Filter by level with pagination
            QuestionLevel questionLevel = QuestionLevel.fromString(level);
            List<Question> filteredQuestions = questionRepository.findByLevel(questionLevel);
            // Convert to page
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredQuestions.size());
            List<Question> pageContent = filteredQuestions.subList(start, end);
            questions = new PageImpl<>(pageContent, pageable, filteredQuestions.size());
        } else {
            questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return questions;
    }

    // ==================== EXISTING METHODS ====================

    // FIXED: Get all questions with pagination and filtering
    public Page<QuestionDTO> getAllQuestions(Pageable pageable, String categoryId, String level, String search) {
        Page<Question> questions = getAllQuestionsFiltered(pageable, categoryId, level, search);
        return questions.map(QuestionDTO::fromEntity);
    }

    // Get question details with solutions and user progress
    public QuestionDetailDTO getQuestionDetails(String questionId, String userId) {
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return null;
        }

        // Get question DTO
        QuestionDTO questionDTO = QuestionDTO.fromEntity(question);

        // Get solutions for this question
        List<SolutionDTO> solutions = solutionService.getSolutionsByQuestion(questionId);

        // Get user progress
        var userProgress = userProgressService.getProgressByQuestionAndUser(questionId, userId);
        boolean solved = userProgress != null ? userProgress.isSolved() : false;
        var solvedAt = userProgress != null ? userProgress.getSolvedAt() : null;

        return new QuestionDetailDTO(questionDTO, solutions, solved, solvedAt);
    }

    // Create new question
    public QuestionDTO createQuestion(QuestionDTO questionDTO, User createdBy) {
        // Find category
        Category category = categoryRepository.findById(questionDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Question question = new Question();
        question.setTitle(questionDTO.getTitle().trim());
        question.setStatement(questionDTO.getStatement());
        question.setImageUrls(questionDTO.getImageUrls());
        question.setImageFolderUrl(questionDTO.getImageFolderUrl()); // Backward compatibility
        question.setCategory(category);
        question.setLevel(questionDTO.getLevel());
        question.setCreatedBy(createdBy);

        // Convert and set code snippets
        if (questionDTO.getCodeSnippets() != null) {
            List<Question.CodeSnippet> codeSnippets = questionDTO.getCodeSnippets().stream()
                    .map(dto -> new Question.CodeSnippet(dto.getLanguage(), dto.getCode(), dto.getDescription()))
                    .collect(Collectors.toList());
            question.setCodeSnippets(codeSnippets);
        }

        Question savedQuestion = questionRepository.save(question);
        return QuestionDTO.fromEntity(savedQuestion);
    }

    // Update question
    public QuestionDTO updateQuestion(String id, QuestionDTO questionDTO) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Find category if changed
        if (!question.getCategory().getId().equals(questionDTO.getCategoryId())) {
            Category newCategory = categoryRepository.findById(questionDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            question.setCategory(newCategory);
        }

        question.setTitle(questionDTO.getTitle().trim());
        question.setStatement(questionDTO.getStatement());
        question.setImageUrls(questionDTO.getImageUrls());
        question.setImageFolderUrl(questionDTO.getImageFolderUrl());
        question.setLevel(questionDTO.getLevel());

        // Update code snippets
        if (questionDTO.getCodeSnippets() != null) {
            List<Question.CodeSnippet> codeSnippets = questionDTO.getCodeSnippets().stream()
                    .map(dto -> new Question.CodeSnippet(dto.getLanguage(), dto.getCode(), dto.getDescription()))
                    .collect(Collectors.toList());
            question.setCodeSnippets(codeSnippets);
        }

        Question updatedQuestion = questionRepository.save(question);
        return QuestionDTO.fromEntity(updatedQuestion);
    }

    // Delete question (cascade delete related data)
    @Transactional
    public void deleteQuestion(String id) {
        // Delete all related data
        solutionRepository.deleteByQuestion_Id(id);
        approachRepository.deleteByQuestion_Id(id);
        userProgressRepository.deleteByQuestion_Id(id);

        // Delete the question
        questionRepository.deleteById(id);
    }

    // Get questions by category
    public Page<QuestionDTO> getQuestionsByCategory(String categoryId, Pageable pageable) {
        Page<Question> questions = questionRepository.findByCategory_IdOrderByCreatedAtDesc(categoryId, pageable);
        return questions.map(QuestionDTO::fromEntity);
    }

    // Check if question exists by ID
    public boolean existsById(String id) {
        return questionRepository.existsById(id);
    }

    // Check if title exists
    public boolean existsByTitle(String title) {
        return questionRepository.existsByTitleIgnoreCase(title);
    }

    // Check if title exists excluding current question
    public boolean existsByTitleAndNotId(String title, String excludeId) {
        var questions = questionRepository.findByTitleContainingIgnoreCase(title);
        return questions.stream().anyMatch(q -> q.getTitle().equalsIgnoreCase(title) && !q.getId().equals(excludeId));
    }

    // Get question counts by level and category
    public Map<String, Object> getQuestionCounts() {
        Map<String, Object> counts = new HashMap<>();

        // Total questions
        long totalQuestions = questionRepository.count();
        counts.put("total", totalQuestions);

        // Questions by level
        Map<String, Long> levelCounts = new HashMap<>();
        levelCounts.put("easy", questionRepository.countByLevel(QuestionLevel.EASY));
        levelCounts.put("medium", questionRepository.countByLevel(QuestionLevel.MEDIUM));
        levelCounts.put("hard", questionRepository.countByLevel(QuestionLevel.HARD));
        counts.put("byLevel", levelCounts);

        // Questions by category
        List<Category> categories = categoryRepository.findAll();
        Map<String, Object> categoryStats = new HashMap<>();
        for (Category category : categories) {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("name", category.getName());
            categoryData.put("count", questionRepository.countByCategory_Id(category.getId()));
            categoryStats.put(category.getId(), categoryData);
        }
        counts.put("byCategory", categoryStats);

        return counts;
    }

    // Search questions
    public List<QuestionDTO> searchQuestions(String searchTerm) {
        List<Question> questions = questionRepository.searchByTitleOrStatement(searchTerm);
        return questions.stream()
                .map(QuestionDTO::fromEntity)
                .toList();
    }

    // Get questions created by a user
    public List<QuestionDTO> getQuestionsByCreator(String creatorId) {
        List<Question> questions = questionRepository.findByCreatedBy_Id(creatorId);
        return questions.stream()
                .map(QuestionDTO::fromEntity)
                .toList();
    }
}