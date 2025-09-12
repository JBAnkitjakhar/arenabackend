// src/main/java/com/algoarena/service/dsa/QuestionService.java - FIXED APPROACH COUNT LOGIC

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
import org.springframework.cache.annotation.CacheEvict;
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

    @Autowired
    private BulkApproachService bulkApproachService;

    // ==================== HYBRID CACHING METHODS ====================

    /**
     * HYBRID: Get questions summary with user progress - CACHED with smart eviction
     * Cache key includes all filter parameters for proper cache segmentation
     */
    @Cacheable(value = "questionsSummary", key = "#userId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_cat_' + (#categoryId ?: 'all') + '_lvl_' + (#level ?: 'all') + '_search_' + (#search ?: 'none')")
    public Page<QuestionSummaryDTO> getQuestionsWithProgress(
            Pageable pageable,
            String categoryId,
            String level,
            String search,
            String userId) {

        // System.out.println("CACHE MISS: Fetching fresh questions data for user: " + userId);

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

        // Step 3: Get user progress for these questions (optimized)
        Map<String, UserProgress> progressMap = new HashMap<>();
        for (String questionId : questionIds) {
            Optional<UserProgress> progress = userProgressRepository.findByUser_IdAndQuestion_Id(userId, questionId);
            if (progress.isPresent()) {
                progressMap.put(questionId, progress.get());
            }
        }

        // System.out.println(
        //         "DEBUG: Found " + progressMap.size() + " progress records out of " + questionIds.size() + " questions");

        // Step 4: TRULY BULK - Get approach counts using single aggregation query
        // System.out.println("DEBUG: Using bulk approach count service for " + questionIds.size() + " questions for user: " + userId);
        
        Map<String, Integer> approachCountMap = bulkApproachService.getBulkApproachCounts(userId, questionIds);
        
        // Log detailed results
        // int totalApproaches = approachCountMap.values().stream().mapToInt(Integer::intValue).sum();
        // int questionsWithApproaches = (int) approachCountMap.values().stream().filter(count -> count > 0).count();
        
        // System.out.println("BULK RESULT: Found total of " + totalApproaches + " approaches across " + 
        //                  questionsWithApproaches + " questions (out of " + questionIds.size() + " total questions)");

        // Step 5: Convert to QuestionSummaryDTO with embedded user progress and approach counts
        List<QuestionSummaryDTO> summaryList = questionsPage.getContent()
                .stream()
                .map(question -> {
                    QuestionSummaryDTO summary = new QuestionSummaryDTO(
                            question.getId(),
                            question.getTitle(),
                            question.getCategory().getId(),
                            question.getCategory().getName(),
                            question.getLevel(),
                            question.getCreatedAt());

                    // Add user progress
                    UserProgress progress = progressMap.get(question.getId());
                    int approachCount = approachCountMap.getOrDefault(question.getId(), 0);

                    if (progress != null && progress.isSolved()) {
                        summary.setUserProgress(new QuestionSummaryDTO.UserProgressSummary(
                                true,
                                progress.getSolvedAt(),
                                approachCount));
                    } else {
                        summary.setUserProgress(new QuestionSummaryDTO.UserProgressSummary(
                                false,
                                null,
                                approachCount));
                    }

                    return summary;
                })
                .collect(Collectors.toList());

        // Return paginated result
        return new PageImpl<>(summaryList, pageable, questionsPage.getTotalElements());
    }

    /**
     * Helper method to get filtered questions (can be cached separately)
     */
    @Cacheable(value = "questionsList", key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_cat_' + (#categoryId ?: 'all') + '_lvl_' + (#level ?: 'all') + '_search_' + (#search ?: 'none')")
    public Page<Question> getAllQuestionsFiltered(Pageable pageable, String categoryId, String level, String search) {
        // System.out.println("CACHE MISS: Fetching filtered questions from database");

        Page<Question> questions;

        if (search != null && !search.trim().isEmpty()) {
            List<Question> searchResults = questionRepository.searchByTitleOrStatement(search.trim());
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), searchResults.size());
            List<Question> pageContent = searchResults.subList(start, end);
            questions = new PageImpl<>(pageContent, pageable, searchResults.size());
        } else if (categoryId != null && !categoryId.isEmpty()) {
            if (level != null && !level.isEmpty()) {
                QuestionLevel questionLevel = QuestionLevel.fromString(level);
                List<Question> filteredQuestions = questionRepository.findByCategory_IdAndLevel(categoryId,
                        questionLevel);
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), filteredQuestions.size());
                List<Question> pageContent = filteredQuestions.subList(start, end);
                questions = new PageImpl<>(pageContent, pageable, filteredQuestions.size());
            } else {
                questions = questionRepository.findByCategory_Id(categoryId, pageable);
            }
        } else if (level != null && !level.isEmpty()) {
            QuestionLevel questionLevel = QuestionLevel.fromString(level);
            List<Question> filteredQuestions = questionRepository.findByLevel(questionLevel);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredQuestions.size());
            List<Question> pageContent = filteredQuestions.subList(start, end);
            questions = new PageImpl<>(pageContent, pageable, filteredQuestions.size());
        } else {
            questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return questions;
    }

    // ==================== CRUD OPERATIONS WITH PROPER CACHE EVICTION ====================

    /**
     * Create question with PROPER cache eviction using @CacheEvict
     */
    @CacheEvict(value = { "questionsSummary", "questionsList", "categoriesProgress", "adminStats" }, allEntries = true)
    public QuestionDTO createQuestion(QuestionDTO questionDTO, User createdBy) {
        // Find category
        Category category = categoryRepository.findById(questionDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Question question = new Question();
        question.setTitle(questionDTO.getTitle().trim());
        question.setStatement(questionDTO.getStatement());
        question.setImageUrls(questionDTO.getImageUrls());
        question.setCategory(category);
        question.setLevel(questionDTO.getLevel());
        question.setCreatedBy(createdBy);

        if (questionDTO.getCodeSnippets() != null) {
            List<Question.CodeSnippet> codeSnippets = questionDTO.getCodeSnippets().stream()
                    .map(dto -> new Question.CodeSnippet(dto.getLanguage(), dto.getCode(), dto.getDescription()))
                    .collect(Collectors.toList());
            question.setCodeSnippets(codeSnippets);
        }

        Question savedQuestion = questionRepository.save(question);

        // System.out.println("Question created and ALL relevant caches evicted");

        return QuestionDTO.fromEntity(savedQuestion);
    }

    /**
     * Update question with PROPER cache eviction using @CacheEvict
     */
    @CacheEvict(value = { "questionsSummary", "questionsList", "categoriesProgress", "adminStats" }, allEntries = true)
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
        question.setLevel(questionDTO.getLevel());

        if (questionDTO.getCodeSnippets() != null) {
            List<Question.CodeSnippet> codeSnippets = questionDTO.getCodeSnippets().stream()
                    .map(dto -> new Question.CodeSnippet(dto.getLanguage(), dto.getCode(), dto.getDescription()))
                    .collect(Collectors.toList());
            question.setCodeSnippets(codeSnippets);
        }

        Question updatedQuestion = questionRepository.save(question);

        // System.out.println("Question updated and ALL relevant caches evicted");

        return QuestionDTO.fromEntity(updatedQuestion);
    }

    /**
     * Delete question with PROPER cache eviction using @CacheEvict
     */
    @CacheEvict(value = { "questionsSummary", "questionsList", "categoriesProgress", "adminStats" }, allEntries = true)
    @Transactional
    public void deleteQuestion(String id) {
        // Delete all related data
        solutionRepository.deleteByQuestion_Id(id);
        approachRepository.deleteByQuestion_Id(id);
        userProgressRepository.deleteByQuestion_Id(id);

        // Delete the question
        questionRepository.deleteById(id);

        // System.out.println("Question deleted and ALL relevant caches evicted");
    }

    // ==================== EXISTING METHODS ====================

    public Page<QuestionDTO> getAllQuestions(Pageable pageable, String categoryId, String level, String search) {
        Page<Question> questions = getAllQuestionsFiltered(pageable, categoryId, level, search);
        return questions.map(QuestionDTO::fromEntity);
    }

    public QuestionDetailDTO getQuestionDetails(String questionId, String userId) {
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return null;
        }

        QuestionDTO questionDTO = QuestionDTO.fromEntity(question);
        List<SolutionDTO> solutions = solutionService.getSolutionsByQuestion(questionId);

        var userProgress = userProgressService.getProgressByQuestionAndUser(questionId, userId);
        boolean solved = userProgress != null ? userProgress.isSolved() : false;
        var solvedAt = userProgress != null ? userProgress.getSolvedAt() : null;

        return new QuestionDetailDTO(questionDTO, solutions, solved, solvedAt);
    }

    public Page<QuestionDTO> getQuestionsByCategory(String categoryId, Pageable pageable) {
        Page<Question> questions = questionRepository.findByCategory_IdOrderByCreatedAtDesc(categoryId, pageable);
        return questions.map(QuestionDTO::fromEntity);
    }

    public boolean existsById(String id) {
        return questionRepository.existsById(id);
    }

    public boolean existsByTitle(String title) {
        return questionRepository.existsByTitleIgnoreCase(title);
    }

    public boolean existsByTitleAndNotId(String title, String excludeId) {
        var questions = questionRepository.findByTitleContainingIgnoreCase(title);
        return questions.stream().anyMatch(q -> q.getTitle().equalsIgnoreCase(title) && !q.getId().equals(excludeId));
    }

    @Cacheable(value = "adminStats", key = "'questionCounts'")
    public Map<String, Object> getQuestionCounts() {
        // System.out.println("CACHE MISS: Fetching question counts from database");

        Map<String, Object> counts = new HashMap<>();

        long totalQuestions = questionRepository.count();
        counts.put("total", totalQuestions);

        Map<String, Long> levelCounts = new HashMap<>();
        levelCounts.put("easy", questionRepository.countByLevel(QuestionLevel.EASY));
        levelCounts.put("medium", questionRepository.countByLevel(QuestionLevel.MEDIUM));
        levelCounts.put("hard", questionRepository.countByLevel(QuestionLevel.HARD));
        counts.put("byLevel", levelCounts);

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

    public List<QuestionDTO> searchQuestions(String searchTerm) {
        List<Question> questions = questionRepository.searchByTitleOrStatement(searchTerm);
        return questions.stream()
                .map(QuestionDTO::fromEntity)
                .toList();
    }

    public List<QuestionDTO> getQuestionsByCreator(String creatorId) {
        List<Question> questions = questionRepository.findByCreatedBy_Id(creatorId);
        return questions.stream()
                .map(QuestionDTO::fromEntity)
                .toList();
    }
}