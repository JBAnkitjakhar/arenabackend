// src/main/java/com/algoarena/service/dsa/CategoryService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.CategoryDTO;
import com.algoarena.model.Category;
import com.algoarena.model.User;
import com.algoarena.repository.CategoryRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.ApproachRepository;
import com.algoarena.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

    // Get all categories
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .toList();
    }

    // Get category by ID
    public CategoryDTO getCategoryById(String id) {
        Category category = categoryRepository.findById(id).orElse(null);
        return category != null ? CategoryDTO.fromEntity(category) : null;
    }

    // Create new category
    public CategoryDTO createCategory(CategoryDTO categoryDTO, User createdBy) {
        Category category = new Category();
        category.setName(categoryDTO.getName().trim());
        category.setCreatedBy(createdBy);
        
        Category savedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(savedCategory);
    }

    // Update category
    public CategoryDTO updateCategory(String id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setName(categoryDTO.getName().trim());
        
        Category updatedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(updatedCategory);
    }

    // Delete category (cascade delete related data)
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
            // Delete all related data in parallel for better performance
            solutionRepository.deleteByQuestion_Id(id); // This should be updated to handle multiple questions
            approachRepository.deleteByQuestion_Id(id); // This should be updated to handle multiple questions
            userProgressRepository.deleteByQuestion_Id(id); // This should be updated to handle multiple questions
            
            // Delete all questions in this category
            questionRepository.deleteAll(questions);
        }
        
        // Finally, delete the category
        categoryRepository.deleteById(id);
        
        return deletedQuestionsCount;
    }

    // Check if category exists by ID
    public boolean existsById(String id) {
        return categoryRepository.existsById(id);
    }

    // Check if category name exists
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    // Check if category name exists excluding current category
    public boolean existsByNameAndNotId(String name, String excludeId) {
        var existing = categoryRepository.findByNameIgnoreCase(name);
        return existing.isPresent() && !existing.get().getId().equals(excludeId);
    }

    // Get category statistics
    public Map<String, Object> getCategoryStats(String categoryId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get total questions in this category
        long questionCount = questionRepository.countByCategory_Id(categoryId);
        stats.put("totalQuestions", questionCount);
        
        // Get questions by difficulty level
        var questions = questionRepository.findByCategory_Id(categoryId);
        long easyCount = questions.stream().mapToLong(q -> 
                q.getLevel().name().equals("EASY") ? 1 : 0).sum();
        long mediumCount = questions.stream().mapToLong(q -> 
                q.getLevel().name().equals("MEDIUM") ? 1 : 0).sum();
        long hardCount = questions.stream().mapToLong(q -> 
                q.getLevel().name().equals("HARD") ? 1 : 0).sum();
        
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

    // Get all categories created by a user
    public List<CategoryDTO> getCategoriesByCreator(String creatorId) {
        List<Category> categories = categoryRepository.findByCreatedBy_Id(creatorId);
        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .toList();
    }

    // Get total categories count
    public long getTotalCategoriesCount() {
        return categoryRepository.countAllCategories();
    }
}