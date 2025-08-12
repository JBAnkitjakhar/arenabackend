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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    // Update user progress for a question
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

    // Get user progress statistics
    public Map<String, Object> getUserProgressStats(String userId) {
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
        
        return stats;
    }

    // Get recent progress (last 10 solved questions)
    public List<UserProgressDTO> getRecentProgress(String userId) {
        List<UserProgress> recentProgress = userProgressRepository.findTop10ByUser_IdAndSolvedTrueOrderBySolvedAtDesc(userId);
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
        
        // Total unique users who solved at least one question
        long activeUsers = userProgressRepository.findSolvedQuestionsByUser("").size(); // This needs a proper query
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
        
        double categoryProgress = totalQuestionsInCategory > 0 ? 
            (solvedInCategory * 100.0) / totalQuestionsInCategory : 0.0;
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
}