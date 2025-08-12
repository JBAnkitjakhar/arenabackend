// src/main/java/com/algoarena/service/dsa/ApproachService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.ApproachDTO;
import com.algoarena.model.Approach;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.repository.ApproachRepository;
import com.algoarena.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class ApproachService {

    @Autowired
    private ApproachRepository approachRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // Constants for limits
    private static final int MAX_APPROACHES_PER_QUESTION = 3;
    private static final int MAX_TOTAL_SIZE_PER_USER_PER_QUESTION = 15 * 1024; // 15KB

    // Get approach by ID and user (security check)
    public ApproachDTO getApproachByIdAndUser(String id, String userId) {
        List<Approach> approaches = approachRepository.findByQuestion_IdAndUser_Id("", userId);
        Approach approach = approaches.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        return approach != null ? ApproachDTO.fromEntity(approach) : null;
    }

    // Get approaches by question and user
    public List<ApproachDTO> getApproachesByQuestionAndUser(String questionId, String userId) {
        List<Approach> approaches = approachRepository.findByQuestion_IdAndUser_IdOrderByCreatedAtAsc(questionId, userId);
        return approaches.stream()
                .map(ApproachDTO::fromEntity)
                .toList();
    }

    // Create new approach
    public ApproachDTO createApproach(String questionId, ApproachDTO approachDTO, User user) {
        // Find the question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Approach approach = new Approach();
        approach.setQuestion(question);
        approach.setUser(user);
        approach.setTextContent(approachDTO.getTextContent());
        approach.setCodeContent(approachDTO.getCodeContent());
        approach.setCodeLanguage(approachDTO.getCodeLanguage() != null ? 
                approachDTO.getCodeLanguage() : "javascript");

        // Calculate and set content size
        int contentSize = calculateContentSize(approachDTO.getTextContent(), approachDTO.getCodeContent());
        approach.setContentSize(contentSize);

        Approach savedApproach = approachRepository.save(approach);
        return ApproachDTO.fromEntity(savedApproach);
    }

    // Update approach
    public ApproachDTO updateApproach(String id, ApproachDTO approachDTO) {
        Approach approach = approachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approach not found"));

        approach.setTextContent(approachDTO.getTextContent());
        approach.setCodeContent(approachDTO.getCodeContent());
        if (approachDTO.getCodeLanguage() != null) {
            approach.setCodeLanguage(approachDTO.getCodeLanguage());
        }

        // Recalculate content size
        int contentSize = calculateContentSize(approachDTO.getTextContent(), approachDTO.getCodeContent());
        approach.setContentSize(contentSize);

        Approach updatedApproach = approachRepository.save(approach);
        return ApproachDTO.fromEntity(updatedApproach);
    }

    // Delete approach
    public void deleteApproach(String id) {
        if (!approachRepository.existsById(id)) {
            throw new RuntimeException("Approach not found");
        }
        approachRepository.deleteById(id);
    }

    // Count approaches by question and user
    public long countApproachesByQuestionAndUser(String questionId, String userId) {
        return approachRepository.countByQuestion_IdAndUser_Id(questionId, userId);
    }

    // Check size limits (15KB per user per question)
    public Map<String, Object> checkSizeLimits(String userId, String questionId, 
                                               String textContent, String codeContent, String excludeApproachId) {
        Map<String, Object> result = new HashMap<>();
        
        // Calculate size of new/updated content
        int newContentSize = calculateContentSize(textContent, codeContent);
        
        // Get existing approaches for this user and question
        List<Approach> existingApproaches = approachRepository.findByQuestion_IdAndUser_Id(questionId, userId);
        
        // Calculate total size of existing approaches (excluding the one being updated)
        int existingTotalSize = existingApproaches.stream()
                .filter(approach -> excludeApproachId == null || !approach.getId().equals(excludeApproachId))
                .mapToInt(Approach::getContentSize)
                .sum();
        
        int totalSizeAfterUpdate = existingTotalSize + newContentSize;
        int remainingBytes = MAX_TOTAL_SIZE_PER_USER_PER_QUESTION - totalSizeAfterUpdate;
        
        result.put("canAdd", totalSizeAfterUpdate <= MAX_TOTAL_SIZE_PER_USER_PER_QUESTION);
        result.put("currentSize", existingTotalSize);
        result.put("newSize", newContentSize);
        result.put("totalSizeAfterUpdate", totalSizeAfterUpdate);
        result.put("maxAllowedSize", MAX_TOTAL_SIZE_PER_USER_PER_QUESTION);
        result.put("remainingBytes", Math.max(0, remainingBytes));
        
        return result;
    }

    // Calculate content size in bytes
    private int calculateContentSize(String textContent, String codeContent) {
        int size = 0;
        if (textContent != null) {
            size += textContent.getBytes().length;
        }
        if (codeContent != null) {
            size += codeContent.getBytes().length;
        }
        return size;
    }

    // Get approaches by user
    public List<ApproachDTO> getApproachesByUser(String userId) {
        List<Approach> approaches = approachRepository.findByUser_Id(userId);
        return approaches.stream()
                .map(ApproachDTO::fromEntity)
                .toList();
    }

    // Get recent approaches by user
    public List<ApproachDTO> getRecentApproachesByUser(String userId) {
        List<Approach> approaches = approachRepository.findTop10ByUser_IdOrderByUpdatedAtDesc(userId);
        return approaches.stream()
                .map(ApproachDTO::fromEntity)
                .toList();
    }

    // Get user's approach statistics
    public Map<String, Object> getUserApproachStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Total approaches
        long totalApproaches = approachRepository.countByUser_Id(userId);
        stats.put("totalApproaches", totalApproaches);
        
        // Total content size
        List<Approach> userApproaches = approachRepository.findByUser_Id(userId);
        int totalContentSize = userApproaches.stream()
                .mapToInt(Approach::getContentSize)
                .sum();
        stats.put("totalContentSize", totalContentSize);
        stats.put("totalContentSizeKB", totalContentSize / 1024.0);
        
        // Approaches by question
        Map<String, Integer> approachsByQuestion = new HashMap<>();
        userApproaches.forEach(approach -> {
            String questionId = approach.getQuestion().getId();
            approachsByQuestion.put(questionId, approachsByQuestion.getOrDefault(questionId, 0) + 1);
        });
        stats.put("approachesByQuestion", approachsByQuestion);
        
        return stats;
    }

    // Get size usage for a user on a specific question
    public Map<String, Object> getUserQuestionSizeUsage(String userId, String questionId) {
        Map<String, Object> usage = new HashMap<>();
        
        List<Approach> approaches = approachRepository.findByQuestion_IdAndUser_Id(questionId, userId);
        
        int totalUsed = approaches.stream()
                .mapToInt(Approach::getContentSize)
                .sum();
        
        int remaining = MAX_TOTAL_SIZE_PER_USER_PER_QUESTION - totalUsed;
        
        usage.put("totalUsed", totalUsed);
        usage.put("totalUsedKB", totalUsed / 1024.0);
        usage.put("remaining", Math.max(0, remaining));
        usage.put("remainingKB", Math.max(0, remaining) / 1024.0);
        usage.put("maxAllowed", MAX_TOTAL_SIZE_PER_USER_PER_QUESTION);
        usage.put("maxAllowedKB", MAX_TOTAL_SIZE_PER_USER_PER_QUESTION / 1024.0);
        usage.put("usagePercentage", (totalUsed * 100.0) / MAX_TOTAL_SIZE_PER_USER_PER_QUESTION);
        usage.put("approachCount", approaches.size());
        usage.put("maxApproaches", MAX_APPROACHES_PER_QUESTION);
        
        return usage;
    }

    // Delete all approaches for a question (used when question is deleted)
    public void deleteAllApproachesForQuestion(String questionId) {
        approachRepository.deleteByQuestion_Id(questionId);
    }

    // Delete all approaches by a user for a specific question
    public void deleteAllApproachesByUserForQuestion(String userId, String questionId) {
        approachRepository.deleteByQuestion_IdAndUser_Id(questionId, userId);
    }
}