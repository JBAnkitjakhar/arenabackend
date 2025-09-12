// src/main/java/com/algoarena/service/dsa/CategoryService.java - FIXED CACHE EVICTION

package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.CategoryDTO;
import com.algoarena.dto.dsa.CategorySummaryDTO;
import com.algoarena.model.Category;
import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import com.algoarena.model.User;
import com.algoarena.model.UserProgress;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.ApproachRepository;
import com.algoarena.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private ApproachRepository approachRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    // ==================== HYBRID CACHING METHODS ====================

    /**
     * HYBRID: Get all categories with user progress - CACHED with smart eviction
     * Cache key is user-specific to avoid conflicts between different users
     */
    @Cacheable(value = "categoriesProgress", key = "#userId")
    public List<CategorySummaryDTO> getCategoriesWithProgress(String userId) {
        // System.out.println("CACHE MISS: Fetching fresh categories with progress for user: " + userId);

        // Step 1: Get all categories
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();

        // Step 2: Get all questions for calculating stats
        List<Question> allQuestions = questionRepository.findAll();

        // Step 3: Get all user progress for this user
        List<UserProgress> allUserProgress = userProgressRepository.findByUser_IdAndSolvedTrue(userId);

        // Create a map for quick lookup of solved questions
        Set<String> solvedQuestionIds = allUserProgress.stream()
                .map(progress -> progress.getQuestion().getId())
                .collect(Collectors.toSet());

        // System.out.println("DEBUG: User " + userId + " has solved " + solvedQuestionIds.size() + " questions");

        // Step 4: Process each category
        return categories.stream()
                .map(category -> {
                    // Create basic category summary
                    CategorySummaryDTO summary = new CategorySummaryDTO(
                            category.getId(),
                            category.getName(),
                            category.getCreatedBy().getName(),
                            category.getCreatedBy().getId(),
                            category.getCreatedAt(),
                            category.getUpdatedAt());

                    // Calculate question statistics for this category
                    List<Question> categoryQuestions = allQuestions.stream()
                            .filter(q -> q.getCategory().getId().equals(category.getId()))
                            .collect(Collectors.toList());

                    // Count questions by level
                    long easyCount = categoryQuestions.stream()
                            .filter(q -> q.getLevel() == QuestionLevel.EASY).count();
                    long mediumCount = categoryQuestions.stream()
                            .filter(q -> q.getLevel() == QuestionLevel.MEDIUM).count();
                    long hardCount = categoryQuestions.stream()
                            .filter(q -> q.getLevel() == QuestionLevel.HARD).count();

                    CategorySummaryDTO.QuestionStats.ByLevel questionsByLevel = new CategorySummaryDTO.QuestionStats.ByLevel(
                            (int) easyCount, (int) mediumCount, (int) hardCount);

                    CategorySummaryDTO.QuestionStats questionStats = new CategorySummaryDTO.QuestionStats(
                            categoryQuestions.size(), questionsByLevel);

                    // Count solved questions by level in this category
                    long solvedEasy = categoryQuestions.stream()
                            .filter(q -> q.getLevel() == QuestionLevel.EASY)
                            .filter(q -> solvedQuestionIds.contains(q.getId()))
                            .count();

                    long solvedMedium = categoryQuestions.stream()
                            .filter(q -> q.getLevel() == QuestionLevel.MEDIUM)
                            .filter(q -> solvedQuestionIds.contains(q.getId()))
                            .count();

                    long solvedHard = categoryQuestions.stream()
                            .filter(q -> q.getLevel() == QuestionLevel.HARD)
                            .filter(q -> solvedQuestionIds.contains(q.getId()))
                            .count();

                    int totalSolvedInCategory = (int) (solvedEasy + solvedMedium + solvedHard);

                    CategorySummaryDTO.UserProgressStats.ByLevel solvedByLevel = new CategorySummaryDTO.UserProgressStats.ByLevel(
                            (int) solvedEasy, (int) solvedMedium, (int) solvedHard);

                    double progressPercentage = categoryQuestions.size() > 0
                            ? (totalSolvedInCategory * 100.0) / categoryQuestions.size()
                            : 0.0;

                    CategorySummaryDTO.UserProgressStats userProgressStats = new CategorySummaryDTO.UserProgressStats(
                            totalSolvedInCategory,
                            solvedByLevel,
                            Math.round(progressPercentage * 100.0) / 100.0);

                    // Set the stats on the summary
                    summary.setQuestionStats(questionStats);
                    summary.setUserProgress(userProgressStats);

                    return summary;
                })
                .collect(Collectors.toList());
    }

    /**
     * HYBRID: Get all categories - CACHED (no user-specific data)
     */
    @Cacheable(value = "categoriesList", key = "'all'")
    public List<CategoryDTO> getAllCategories() {
        // System.out.println("CACHE MISS: Fetching all categories from database");
        
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .toList();
    }

    // ==================== CRUD OPERATIONS WITH PROPER CACHE EVICTION ====================

    /**
     * Create category with PROPER cache eviction using @CacheEvict
     */
    @CacheEvict(value = {"categoriesProgress", "categoriesList", "questionsSummary", "adminStats"}, allEntries = true)
    public CategoryDTO createCategory(CategoryDTO categoryDTO, User createdBy) {
        Category category = new Category();
        category.setName(categoryDTO.getName().trim());
        category.setCreatedBy(createdBy);

        Category savedCategory = categoryRepository.save(category);
        
        // System.out.println("Category created and ALL relevant caches evicted");
        
        return CategoryDTO.fromEntity(savedCategory);
    }

    /**
     * Update category with PROPER cache eviction using @CacheEvict
     */
    @CacheEvict(value = {"categoriesProgress", "categoriesList", "questionsSummary", "categoryStats", "adminStats"}, allEntries = true)
    public CategoryDTO updateCategory(String id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(categoryDTO.getName().trim());

        Category updatedCategory = categoryRepository.save(category);
        
        // System.out.println("Category updated and ALL relevant caches evicted");

        return CategoryDTO.fromEntity(updatedCategory);
    }

    /**
     * Delete category with PROPER cache eviction using @CacheEvict
     */
    @CacheEvict(value = {"categoriesProgress", "categoriesList", "questionsSummary", "questionsList", "categoryStats", "adminStats"}, allEntries = true)
    @Transactional
    public int deleteCategory(String id) {
        // First, get all questions in this category
        var questions = questionRepository.findByCategory_Id(id);
        int deletedQuestionsCount = questions.size();

        // Extract question IDs for bulk deletion
        List<String> questionIds = questions.stream()
                .map(question -> question.getId())
                .toList();

        if (!questionIds.isEmpty()) {
            // Delete all related data for each question
            for (String questionId : questionIds) {
                solutionRepository.deleteByQuestion_Id(questionId);
                approachRepository.deleteByQuestion_Id(questionId);
                userProgressRepository.deleteByQuestion_Id(questionId);
            }

            // Delete all questions in this category
            questionRepository.deleteAll(questions);
        }

        // Finally, delete the category
        categoryRepository.deleteById(id);

        // System.out.println("Category and " + deletedQuestionsCount + " questions deleted, ALL caches evicted");

        return deletedQuestionsCount;
    }

    // ==================== EXISTING METHODS ====================

    public CategoryDTO getCategoryById(String id) {
        Category category = categoryRepository.findById(id).orElse(null);
        return category != null ? CategoryDTO.fromEntity(category) : null;
    }

    public boolean existsById(String id) {
        return categoryRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    public boolean existsByNameAndNotId(String name, String excludeId) {
        var existing = categoryRepository.findByNameIgnoreCase(name);
        return existing.isPresent() && !existing.get().getId().equals(excludeId);
    }

    /**
     * Get category statistics - CACHED with shorter TTL
     */
    @Cacheable(value = "categoryStats", key = "#categoryId")
    public Map<String, Object> getCategoryStats(String categoryId) {
        // System.out.println("CACHE MISS: Fetching category stats for ID: " + categoryId);
        
        Map<String, Object> stats = new HashMap<>();

        // Get total questions in this category
        long questionCount = questionRepository.countByCategory_Id(categoryId);
        stats.put("totalQuestions", questionCount);

        // Get questions by difficulty level
        var questions = questionRepository.findByCategory_Id(categoryId);
        long easyCount = questions.stream().mapToLong(q -> q.getLevel().name().equals("EASY") ? 1 : 0).sum();
        long mediumCount = questions.stream().mapToLong(q -> q.getLevel().name().equals("MEDIUM") ? 1 : 0).sum();
        long hardCount = questions.stream().mapToLong(q -> q.getLevel().name().equals("HARD") ? 1 : 0).sum();

        Map<String, Long> levelStats = new HashMap<>();
        levelStats.put("easy", easyCount);
        levelStats.put("medium", mediumCount);
        levelStats.put("hard", hardCount);
        stats.put("questionsByLevel", levelStats);

        // Get total solutions count
        List<String> questionIds = questions.stream()
                .map(q -> q.getId())
                .toList();
        long totalSolutions = 0;
        for (String questionId : questionIds) {
            totalSolutions += solutionRepository.countByQuestion_Id(questionId);
        }
        stats.put("totalSolutions", totalSolutions);

        return stats;
    }

    public List<CategoryDTO> getCategoriesByCreator(String creatorId) {
        List<Category> categories = categoryRepository.findByCreatedBy_Id(creatorId);
        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .toList();
    }

    public long getTotalCategoriesCount() {
        return categoryRepository.countAllCategories();
    }
}