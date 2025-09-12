// src/main/java/com/algoarena/service/dsa/UserProgressService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.UserProgressDTO;
import com.algoarena.model.UserProgress;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.model.QuestionLevel;
import com.algoarena.repository.UserProgressRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    // Get progress by question and user
    public UserProgressDTO getProgressByQuestionAndUser(String questionId, String userId) {
        UserProgress progress = userProgressRepository.findByUser_IdAndQuestion_Id(userId, questionId)
                .orElse(null);
        return progress != null ? UserProgressDTO.fromEntity(progress) : null;
    }

    /**
     * CRITICAL: Update user progress with PROPER cache eviction for
     * questions/categories
     * This method triggers cache eviction to ensure real-time updates in questions
     * and categories pages
     */
    @CacheEvict(value = { "questionsSummary", "categoriesProgress", "userProgressStats" }, allEntries = true)
    public UserProgressDTO updateProgress(String questionId, String userId, boolean solved) {
        // Find question and user
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find existing progress or create new
        UserProgress progress = userProgressRepository.findByUser_IdAndQuestion_Id(userId, questionId)
                .orElse(new UserProgress());

        progress.setUser(user);
        progress.setQuestion(question);
        progress.setLevel(question.getLevel());
        progress.setSolved(solved);

        if (solved && progress.getSolvedAt() == null) {
            progress.setSolvedAt(LocalDateTime.now());
        } else if (!solved) {
            progress.setSolvedAt(null);
        }

        UserProgress savedProgress = userProgressRepository.save(progress);

        // CRITICAL: @CacheEvict annotation above ensures all relevant caches are
        // cleared
        // System.out.println("User progress updated and ALL CACHES EVICTED for user: " + userId +
        //         " - question: " + questionId + " - solved: " + solved);

        return UserProgressDTO.fromEntity(savedProgress);
    }

    // Get all progress for a user
    public List<UserProgressDTO> getAllProgressByUser(String userId) {
        List<UserProgress> progressList = userProgressRepository.findByUser_Id(userId);
        return progressList.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    // Get solved questions by user
    public List<UserProgressDTO> getSolvedQuestionsByUser(String userId) {
        List<UserProgress> solvedQuestions = userProgressRepository.findByUser_IdAndSolvedTrue(userId);
        return solvedQuestions.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    /**
     * UPDATED: Get user progress statistics without streak, with recent solved
     * questions
     */
    public Map<String, Object> getUserProgressStats(String userId) {
        Map<String, Object> stats = new HashMap<>();

        // Total solved questions
        long totalSolved = userProgressRepository.countByUser_IdAndSolvedTrue(userId);
        stats.put("totalSolved", totalSolved);

        // Solved questions by level
        Map<String, Long> solvedByLevel = new HashMap<>();
        solvedByLevel.put("easy",
                userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.EASY));
        solvedByLevel.put("medium",
                userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.MEDIUM));
        solvedByLevel.put("hard",
                userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.HARD));
        stats.put("solvedByLevel", solvedByLevel);

        // Total questions available
        long totalQuestions = questionRepository.count();
        stats.put("totalQuestions", totalQuestions);

        // Questions by level
        Map<String, Long> totalByLevel = new HashMap<>();
        totalByLevel.put("easy", questionRepository.countByLevel(QuestionLevel.EASY));
        totalByLevel.put("medium", questionRepository.countByLevel(QuestionLevel.MEDIUM));
        totalByLevel.put("hard", questionRepository.countByLevel(QuestionLevel.HARD));
        stats.put("totalByLevel", totalByLevel);

        // Progress percentage
        double progressPercentage = totalQuestions > 0 ? (totalSolved * 100.0) / totalQuestions : 0.0;
        stats.put("progressPercentage", Math.round(progressPercentage * 100.0) / 100.0);

        // Level-wise progress percentages
        Map<String, Double> progressByLevel = new HashMap<>();
        for (String level : List.of("easy", "medium", "hard")) {
            long solved = solvedByLevel.get(level);
            long total = totalByLevel.get(level);
            double percentage = total > 0 ? (solved * 100.0) / total : 0.0;
            progressByLevel.put(level, Math.round(percentage * 100.0) / 100.0);
        }
        stats.put("progressByLevel", progressByLevel);

        // REMOVED: streak calculation (as requested)
        // stats.put("streak", 0);

        // NEW: Recent solved count (last 7 days) - but simpler implementation
        int recentSolved = getRecentSolvedCount(userId, 7);
        stats.put("recentSolved", recentSolved);

        // NEW: Add recent solved questions list (for pagination on me page)
        List<Map<String, Object>> recentSolvedQuestions = getRecentSolvedQuestions(userId);
        stats.put("recentSolvedQuestions", recentSolvedQuestions);

        return stats;
    }

    /**
     * NEW: Get recent solved questions with details for me page
     */
    private List<Map<String, Object>> getRecentSolvedQuestions(String userId) {
        List<UserProgress> recentProgress = userProgressRepository
                .findTop10ByUser_IdAndSolvedTrueOrderBySolvedAtDesc(userId);

        return recentProgress.stream()
                .map(progress -> {
                    Map<String, Object> recentQuestion = new HashMap<>();
                    recentQuestion.put("questionId", progress.getQuestion().getId());
                    recentQuestion.put("title", progress.getQuestion().getTitle());
                    recentQuestion.put("category", progress.getQuestion().getCategory().getName());
                    recentQuestion.put("level", progress.getLevel().toString());
                    recentQuestion.put("solvedAt", progress.getSolvedAt());
                    return recentQuestion;
                })
                .collect(Collectors.toList());
    }

    // Get recent progress (last 10 solved questions)
    public List<UserProgressDTO> getRecentProgress(String userId) {
        List<UserProgress> recentProgress = userProgressRepository
                .findTop10ByUser_IdAndSolvedTrueOrderBySolvedAtDesc(userId);
        return recentProgress.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    // Check if user has solved a question
    public boolean hasUserSolvedQuestion(String userId, String questionId) {
        return userProgressRepository.existsByUser_IdAndQuestion_IdAndSolvedTrue(userId, questionId);
    }

    // Get progress by question (all users)
    public List<UserProgressDTO> getProgressByQuestion(String questionId) {
        List<UserProgress> progressList = userProgressRepository.findByQuestion_Id(questionId);
        return progressList.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    // Count how many users solved a specific question
    public long countUsersSolvedQuestion(String questionId) {
        return userProgressRepository.countByQuestion_IdAndSolvedTrue(questionId);
    }

    // Get global statistics
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total solved questions across all users
        long totalSolvedGlobally = userProgressRepository.countTotalSolvedQuestions();
        stats.put("totalSolvedGlobally", totalSolvedGlobally);

        // Count distinct users who solved at least one question
        long activeUsers = getDistinctActiveUsersCount();
        stats.put("activeUsers", activeUsers);

        // Average questions solved per active user
        if (activeUsers > 0) {
            double avgQuestionsPerUser = (double) totalSolvedGlobally / activeUsers;
            stats.put("averageQuestionsPerUser", Math.round(avgQuestionsPerUser * 100.0) / 100.0);
        } else {
            stats.put("averageQuestionsPerUser", 0.0);
        }

        return stats;
    }

    // Get user's progress on a specific category
    public Map<String, Object> getUserCategoryProgress(String userId, String categoryId) {
        Map<String, Object> progress = new HashMap<>();

        // Get all questions in this category
        var questionsInCategory = questionRepository.findByCategory_Id(categoryId);
        long totalQuestionsInCategory = questionsInCategory.size();

        // Count solved questions in this category
        long solvedInCategory = 0;
        Map<String, Long> solvedByLevel = new HashMap<>();
        solvedByLevel.put("easy", 0L);
        solvedByLevel.put("medium", 0L);
        solvedByLevel.put("hard", 0L);

        for (var question : questionsInCategory) {
            if (hasUserSolvedQuestion(userId, question.getId())) {
                solvedInCategory++;
                String level = question.getLevel().toString().toLowerCase();
                solvedByLevel.put(level, solvedByLevel.get(level) + 1);
            }
        }

        progress.put("totalInCategory", totalQuestionsInCategory);
        progress.put("solvedInCategory", solvedInCategory);
        progress.put("solvedByLevel", solvedByLevel);

        double categoryProgress = totalQuestionsInCategory > 0 ? (solvedInCategory * 100.0) / totalQuestionsInCategory
                : 0.0;
        progress.put("categoryProgressPercentage", Math.round(categoryProgress * 100.0) / 100.0);

        return progress;
    }

    // Delete all progress for a question (used when question is deleted)
    public void deleteAllProgressForQuestion(String questionId) {
        userProgressRepository.deleteByQuestion_Id(questionId);
    }

    // Get user's rank/leaderboard position
    public Map<String, Object> getUserRank(String userId) {
        Map<String, Object> rankInfo = new HashMap<>();

        long userSolvedCount = userProgressRepository.countByUser_IdAndSolvedTrue(userId);

        // Count how many users have solved more questions (simplified ranking)
        // This would need a more sophisticated query in a real implementation

        rankInfo.put("userSolvedCount", userSolvedCount);
        rankInfo.put("rank", "Calculation needed"); // Placeholder

        return rankInfo;
    }

    // ==================== NEW HELPER METHODS ====================

    /**
     * Get count of questions solved in recent days
     */
    private int getRecentSolvedCount(String userId, int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

            List<UserProgress> allSolved = userProgressRepository.findByUser_IdAndSolvedTrue(userId);

            return (int) allSolved.stream()
                    .filter(progress -> progress.getSolvedAt() != null && progress.getSolvedAt().isAfter(cutoffDate))
                    .count();
        } catch (Exception e) {
            return 0; // Return 0 if calculation fails
        }
    }

    /**
     * Count distinct users who have solved at least one question
     * This method handles the distinct count logic since MongoDB aggregation can be
     * complex
     */
    private long getDistinctActiveUsersCount() {
        try {
            List<UserProgress> allSolvedProgress = userProgressRepository.findAllSolvedProgress();

            Set<String> distinctUserIds = new HashSet<>();
            for (UserProgress progress : allSolvedProgress) {
                if (progress.getUser() != null) {
                    distinctUserIds.add(progress.getUser().getId());
                }
            }

            return distinctUserIds.size();
        } catch (Exception e) {
            return 0; // Return 0 if calculation fails
        }
    }

    /**
     * Get bulk progress status for multiple questions (efficient, no 404s)
     * 
     * @param userId      User ID
     * @param questionIds List of question IDs to check
     * @return Map of questionId -> solved status (false if no progress record
     *         exists)
     */
    public Map<String, Boolean> getBulkProgressStatus(String userId, List<String> questionIds) {
        Map<String, Boolean> result = new HashMap<>();

        // Initialize all questions as not solved
        for (String questionId : questionIds) {
            result.put(questionId, false);
        }

        // Query for existing progress records
        List<UserProgress> existingProgress = userProgressRepository
                .findByUser_IdAndQuestion_IdIn(userId, questionIds);

        // Update map with actual progress status
        for (UserProgress progress : existingProgress) {
            result.put(progress.getQuestion().getId(), progress.isSolved());
        }

        return result;
    }

    /**
     * TEMPORARY DEBUG METHOD - Remove after debugging
     */
    public void debugUserProgress(String userId) {
        // System.out.println("=== DEBUG USER PROGRESS ===");
        // System.out.println("Searching for user ID: " + userId);

        // Test 1: Find all progress for user
        // List<UserProgress> allProgress = userProgressRepository.findByUser_Id(userId);
        // System.out.println("Found " + allProgress.size() + " total progress records for user");

        // for (UserProgress progress : allProgress) {
        //     // System.out.println("Progress Record:");
        //     // System.out.println("  - ID: " + progress.getId());
        //     // System.out.println("  - User ID: " + (progress.getUser() != null ? progress.getUser().getId() : "NULL"));
        //     System.out.println(
        //             "  - Question ID: " + (progress.getQuestion() != null ? progress.getQuestion().getId() : "NULL"));
        //     System.out.println("  - Solved: " + progress.isSolved());
        //     System.out.println("  - SolvedAt: " + progress.getSolvedAt());
        //     System.out.println("---");
        // }

        // Test 2: Find solved progress only
        // List<UserProgress> solvedProgress = userProgressRepository.findByUser_IdAndSolvedTrue(userId);
        // System.out.println("Found " + solvedProgress.size() + " SOLVED progress records for user");

        // Test 3: Try to find specific question
        String testQuestionId = "68a0775c16eb75603af16d58"; // Two Sum Problem
        Optional<UserProgress> specificProgress = userProgressRepository.findByUser_IdAndQuestion_Id(userId,
                testQuestionId);
        // System.out.println("Specific question lookup (" + testQuestionId + "): " + specificProgress.isPresent());
        if (specificProgress.isPresent()) {
            // System.out.println("  - Solved: " + specificProgress.get().isSolved());
        }

        // Test 4: Try bulk lookup
        // List<String> testQuestionIds = List.of("68a0775c16eb75603af16d58", "68b19033b1cd20207b378042");
        // List<UserProgress> bulkResults = userProgressRepository.findByUser_IdAndQuestion_IdIn(userId, testQuestionIds);
        // System.out.println("Bulk lookup returned " + bulkResults.size() + " records");

        // System.out.println("=== END DEBUG ===");
    }
}