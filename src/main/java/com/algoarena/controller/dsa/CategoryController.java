// src/main/java/com/algoarena/controller/dsa/CategoryController.java
package com.algoarena.controller.dsa;

import com.algoarena.dto.dsa.CategoryDTO;
import com.algoarena.dto.dsa.CategorySummaryDTO;
import com.algoarena.model.User;
import com.algoarena.service.dsa.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO, currentUser);
        return ResponseEntity.status(201).body(createdCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable String id) {
        try {
            int deletedQuestions = categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "deletedQuestions", deletedQuestions));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCategoryStats(@PathVariable String id) {
        Map<String, Object> stats = categoryService.getCategoryStats(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * NEW OPTIMIZED ENDPOINT: Get all categories with user progress included
     * This eliminates N+1 queries by including progress stats in single API call
     * GET /api/categories/with-progress
     */
    @GetMapping("/with-progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategorySummaryDTO>> getCategoriesWithProgress(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        List<CategorySummaryDTO> categoriesWithProgress = categoryService
                .getCategoriesWithProgress(currentUser.getId());

        return ResponseEntity.ok(categoriesWithProgress);
    }
}