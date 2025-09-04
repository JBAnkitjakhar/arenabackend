// src/main/java/com/algoarena/service/dsa/UserProgressService.java - COMPLETE with Redis Caching
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.UserProgressDTO;
import com.algoarena.dto.dsa.UserProgressBulkDTO;
import com.algoarena.model.UserProgress;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.model.QuestionLevel;
import com.algoarena.repository.UserProgressRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== CACHED BULK OPERATIONS ====================

    /**
     * Get ALL user progress as bulk data (REDIS CACHED)
     * Cache key: user-progress::userId
     * TTL: 1 hour
     */
    @Cacheable(value = "user-progress", key = "#userId")
    public UserProgressBulkDTO getAllUserProgressBulk(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all progress for user
        List<UserProgress> progressList = userProgressRepository.findByUser_Id(userId);
        
        // Build progress map
        Map<String, UserProgressBulkDTO.QuestionProgressSummary> progressMap = new HashMap<>();
        for (UserProgress progress : progressList) {
            if (progress.getQuestion() != null) {
                UserProgressBulkDTO.QuestionProgressSummary summary = 
                    new UserProgressBulkDTO.QuestionProgressSummary(
                        progress.isSolved(),
                        progress.getLevel(),
                        progress.getSolvedAt()
                    );
                progressMap.put(progress.getQuestion().getId(), summary);
            }
        }

        // Calculate statistics
        UserProgressBulkDTO.UserProgressStatsSummary stats = calculateProgressStats(userId);

        // Create bulk DTO
        UserProgressBulkDTO bulkDTO = new UserProgressBulkDTO(userId, user.getName());
        bulkDTO.setProgressMap(progressMap);
        bulkDTO.setStats(stats);
        bulkDTO.setLastUpdated(LocalDateTime.now());

        return bulkDTO;
    }

    /**
     * Quick check if user solved a specific question (REDIS CACHED)
     */
    @Cacheable(value = "user-progress", key = "#userId + ':' + #questionId")
    public boolean hasUserSolvedQuestionCached(String userId, String questionId) {
        return userProgressRepository.existsByUser_IdAndQuestion_IdAndSolvedTrue(userId, questionId);
    }

    /**
     * Get user progress statistics with caching (REDIS CACHED)
     */
    @Cacheable(value = "user-progress-stats", key = "#userId")
    public Map<String, Object> getUserProgressStats(String userId) {
        return calculateUserStats(userId);
    }

    /**
     * Get category progress for user (REDIS CACHED)
     */
    @Cacheable(value = "category-progress", key = "#userId + ':' + #categoryId")
    public Map<String, Object> getUserCategoryProgress(String userId, String categoryId) {
        return calculateCategoryProgress(userId, categoryId);
    }

    /**
     * Get recent progress with caching (REDIS CACHED)
     */
    @Cacheable(value = "recent-progress", key = "#userId")
    public List<UserProgressDTO> getRecentProgress(String userId) {
        List<UserProgress> recentProgress = userProgressRepository.findTop10ByUser_IdAndSolvedTrueOrderBySolvedAtDesc(userId);
        return recentProgress.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    // ==================== CACHE INVALIDATION OPERATIONS ====================

    /**
     * Update progress with cache eviction
     */
    @CacheEvict(value = {"user-progress", "user-progress-stats", "category-progress", "recent-progress"}, 
                key = "#userId", 
                condition = "#userId != null")
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
        return UserProgressDTO.fromEntity(savedProgress);
    }

    // ==================== NON-CACHED OPERATIONS ====================

    /**
     * Get progress by question and user (non-cached for real-time accuracy)
     */
    public UserProgressDTO getProgressByQuestionAndUser(String questionId, String userId) {
        UserProgress progress = userProgressRepository.findByUser_IdAndQuestion_Id(userId, questionId)
                .orElse(null);
        return progress != null ? UserProgressDTO.fromEntity(progress) : null;
    }

    /**
     * Get all progress for a user (non-cached, used by admin)
     */
    public List<UserProgressDTO> getAllProgressByUser(String userId) {
        List<UserProgress> progressList = userProgressRepository.findByUser_Id(userId);
        return progressList.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    /**
     * Get solved questions by user
     */
    public List<UserProgressDTO> getSolvedQuestionsByUser(String userId) {
        List<UserProgress> solvedQuestions = userProgressRepository.findByUser_IdAndSolvedTrue(userId);
        return solvedQuestions.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    /**
     * Check if user has solved a question (non-cached)
     */
    public boolean hasUserSolvedQuestion(String userId, String questionId) {
        return userProgressRepository.existsByUser_IdAndQuestion_IdAndSolvedTrue(userId, questionId);
    }

    /**
     * Get progress by question (all users)
     */
    public List<UserProgressDTO> getProgressByQuestion(String questionId) {
        List<UserProgress> progressList = userProgressRepository.findByQuestion_Id(questionId);
        return progressList.stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }

    /**
     * Count how many users solved a specific question
     */
    public long countUsersSolvedQuestion(String questionId) {
        return userProgressRepository.countByQuestion_IdAndSolvedTrue(questionId);
    }

    /**
     * Get global statistics
     */
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

    /**
     * Get user's rank/leaderboard position
     */
    public Map<String, Object> getUserRank(String userId) {
        Map<String, Object> rankInfo = new HashMap<>();
        
        long userSolvedCount = userProgressRepository.countByUser_IdAndSolvedTrue(userId);
        rankInfo.put("userSolvedCount", userSolvedCount);
        rankInfo.put("rank", "Calculation needed"); // Placeholder
        
        return rankInfo;
    }

    /**
     * Delete all progress for a question (used when question is deleted)
     */
    public void deleteAllProgressForQuestion(String questionId) {
        userProgressRepository.deleteByQuestion_Id(questionId);
    }

    // ==================== HELPER METHODS FOR CACHED OPERATIONS ====================

    /**
     * Calculate progress stats for bulk DTO
     */
    private UserProgressBulkDTO.UserProgressStatsSummary calculateProgressStats(String userId) {
        UserProgressBulkDTO.UserProgressStatsSummary stats = new UserProgressBulkDTO.UserProgressStatsSummary();
        
        // Count solved questions
        int totalSolved = (int) userProgressRepository.countByUser_IdAndSolvedTrue(userId);
        stats.setTotalSolved(totalSolved);
        
        // Total questions available
        int totalQuestions = (int) questionRepository.count();
        stats.setTotalQuestions(totalQuestions);
        
        // Progress percentage
        double progressPercentage = totalQuestions > 0 ? (totalSolved * 100.0) / totalQuestions : 0.0;
        stats.setProgressPercentage(Math.round(progressPercentage * 100.0) / 100.0);
        
        // Solved by level
        Map<String, Integer> solvedByLevel = new HashMap<>();
        solvedByLevel.put("easy", (int) userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.EASY));
        solvedByLevel.put("medium", (int) userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.MEDIUM));
        solvedByLevel.put("hard", (int) userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.HARD));
        stats.setSolvedByLevel(solvedByLevel);
        
        return stats;
    }

    /**
     * Calculate detailed user statistics
     */
    private Map<String, Object> calculateUserStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Total solved questions
        long totalSolved = userProgressRepository.countByUser_IdAndSolvedTrue(userId);
        stats.put("totalSolved", totalSolved);
        
        // Solved questions by level
        Map<String, Long> solvedByLevel = new HashMap<>();
        solvedByLevel.put("easy", userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.EASY));
        solvedByLevel.put("medium", userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.MEDIUM));
        solvedByLevel.put("hard", userProgressRepository.countByUser_IdAndSolvedTrueAndLevel(userId, QuestionLevel.HARD));
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
        
        // Calculate streak and recent solved
        int streak = calculateUserStreak(userId);
        stats.put("streak", streak);
        
        int recentSolved = getRecentSolvedCount(userId, 7);
        stats.put("recentSolved", recentSolved);
        
        return stats;
    }

    /**
     * Calculate category progress
     */
    private Map<String, Object> calculateCategoryProgress(String userId, String categoryId) {
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
        
        double categoryProgress = totalQuestionsInCategory > 0 ? 
            (solvedInCategory * 100.0) / totalQuestionsInCategory : 0.0;
        progress.put("categoryProgressPercentage", Math.round(categoryProgress * 100.0) / 100.0);
        
        return progress;
    }

    /**
     * Calculate user's current streak
     */
    private int calculateUserStreak(String userId) {
        try {
            List<UserProgress> recentProgress = userProgressRepository.findTop30ByUser_IdAndSolvedTrueOrderBySolvedAtDesc(userId);
            
            if (recentProgress.isEmpty()) {
                return 0;
            }
            
            LocalDate today = LocalDate.now();
            LocalDate currentDate = today;
            int streak = 0;
            
            Set<LocalDate> solvedDates = new HashSet<>();
            for (UserProgress progress : recentProgress) {
                if (progress.getSolvedAt() != null) {
                    LocalDate solveDate = progress.getSolvedAt().toLocalDate();
                    solvedDates.add(solveDate);
                }
            }
            
            // Count consecutive days from today backwards
            while (solvedDates.contains(currentDate)) {
                streak++;
                currentDate = currentDate.minusDays(1);
            }
            
            return streak;
        } catch (Exception e) {
            return 0;
        }
    }

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
            return 0;
        }
    }

    /**
     * Count distinct users who have solved at least one question
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
            return 0;
        }
    }
}