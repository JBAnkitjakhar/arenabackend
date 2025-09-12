// src/main/java/com/algoarena/service/dsa/SolutionService.java
package com.algoarena.service.dsa;

import com.algoarena.dto.dsa.SolutionDTO;
import com.algoarena.model.Solution;
import com.algoarena.model.Question;
import com.algoarena.model.User;
import com.algoarena.repository.SolutionRepository;
import com.algoarena.repository.QuestionRepository;
import com.algoarena.service.file.VisualizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SolutionService {

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private VisualizerService visualizerService;

    // Get solution by ID
    public SolutionDTO getSolutionById(String id) {
        Solution solution = solutionRepository.findById(id).orElse(null);
        return solution != null ? SolutionDTO.fromEntity(solution) : null;
    }

    // Get all solutions with pagination (Admin only)
    public Page<SolutionDTO> getAllSolutions(Pageable pageable) {
        Page<Solution> solutions = solutionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return solutions.map(SolutionDTO::fromEntity);
    }

    // Get solutions by question
    public List<SolutionDTO> getSolutionsByQuestion(String questionId) {
        List<Solution> solutions = solutionRepository.findByQuestion_IdOrderByCreatedAtAsc(questionId);
        return solutions.stream()
                .map(SolutionDTO::fromEntity)
                .toList();
    }

    // Create new solution for a question
    public SolutionDTO createSolution(String questionId, SolutionDTO solutionDTO, User createdBy) {
        // Find the question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Solution solution = new Solution();
        solution.setQuestion(question);
        solution.setContent(solutionDTO.getContent());
        
        // ENHANCED: Handle both drive and YouTube links
        solution.setDriveLink(validateAndCleanDriveLink(solutionDTO.getDriveLink()));
        solution.setYoutubeLink(validateAndCleanYoutubeLink(solutionDTO.getYoutubeLink()));
        
        solution.setImageUrls(solutionDTO.getImageUrls());
        solution.setVisualizerFileIds(solutionDTO.getVisualizerFileIds());
        solution.setCreatedBy(createdBy);

        // Convert and set code snippet
        if (solutionDTO.getCodeSnippet() != null) {
            Solution.CodeSnippet codeSnippet = new Solution.CodeSnippet(
                    solutionDTO.getCodeSnippet().getLanguage(),
                    solutionDTO.getCodeSnippet().getCode(),
                    solutionDTO.getCodeSnippet().getDescription()
            );
            solution.setCodeSnippet(codeSnippet);
        }

        Solution savedSolution = solutionRepository.save(solution);
        return SolutionDTO.fromEntity(savedSolution);
    }

    // Update solution
    public SolutionDTO updateSolution(String id, SolutionDTO solutionDTO) {
        Solution solution = solutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solution not found"));

        solution.setContent(solutionDTO.getContent());
        
        // ENHANCED: Update both links
        solution.setDriveLink(validateAndCleanDriveLink(solutionDTO.getDriveLink()));
        solution.setYoutubeLink(validateAndCleanYoutubeLink(solutionDTO.getYoutubeLink()));
        
        solution.setImageUrls(solutionDTO.getImageUrls());
        solution.setVisualizerFileIds(solutionDTO.getVisualizerFileIds());

        // Update code snippet
        if (solutionDTO.getCodeSnippet() != null) {
            Solution.CodeSnippet codeSnippet = new Solution.CodeSnippet(
                    solutionDTO.getCodeSnippet().getLanguage(),
                    solutionDTO.getCodeSnippet().getCode(),
                    solutionDTO.getCodeSnippet().getDescription()
            );
            solution.setCodeSnippet(codeSnippet);
        } else {
            solution.setCodeSnippet(null);
        }

        Solution updatedSolution = solutionRepository.save(solution);
        return SolutionDTO.fromEntity(updatedSolution);
    }

    // Delete solution
    public void deleteSolution(String id) {
        if (!solutionRepository.existsById(id)) {
            throw new RuntimeException("Solution not found");
        }

        // STEP 1: Delete all associated visualizer HTML files first
        try {
            visualizerService.deleteAllVisualizersForSolution(id);
        } catch (Exception e) {
            System.err.println("Failed to clean up visualizer files for solution " + id + ": " + e.getMessage());
            // Continue with solution deletion even if file cleanup fails
        }

        // STEP 2: Delete the solution document
        solutionRepository.deleteById(id);
        // System.out.println("Successfully deleted solution: " + id);
    }

    // Check if solution exists
    public boolean existsById(String id) {
        return solutionRepository.existsById(id);
    }

    // Count solutions for a question
    public long countSolutionsByQuestion(String questionId) {
        return solutionRepository.countByQuestion_Id(questionId);
    }

    // Get solutions by creator
    public Page<SolutionDTO> getSolutionsByCreator(String creatorId, Pageable pageable) {
        Page<Solution> solutions = solutionRepository.findByCreatedBy_IdOrderByCreatedAtDesc(creatorId, pageable);
        return solutions.map(SolutionDTO::fromEntity);
    }

    // Get solutions with visualizers
    public List<SolutionDTO> getSolutionsWithVisualizers() {
        List<Solution> solutions = solutionRepository.findSolutionsWithVisualizers();
        return solutions.stream()
                .map(SolutionDTO::fromEntity)
                .toList();
    }

    // Get solutions with images
    public List<SolutionDTO> getSolutionsWithImages() {
        List<Solution> solutions = solutionRepository.findSolutionsWithImages();
        return solutions.stream()
                .map(SolutionDTO::fromEntity)
                .toList();
    }

    // NEW: Get solutions with YouTube videos
    public List<SolutionDTO> getSolutionsWithYoutubeVideos() {
        List<Solution> solutions = solutionRepository.findSolutionsWithYoutubeVideos();
        return solutions.stream()
                .map(SolutionDTO::fromEntity)
                .toList();
    }

    // Add image to solution
    public SolutionDTO addImageToSolution(String solutionId, String imageUrl) {
        Solution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new RuntimeException("Solution not found"));

        if (solution.getImageUrls() == null) {
            solution.setImageUrls(List.of(imageUrl));
        } else {
            // Check limit (max 10 images per solution)
            if (solution.getImageUrls().size() >= 10) {
                throw new RuntimeException("Maximum number of images (10) reached for this solution");
            }
            
            var updatedUrls = new java.util.ArrayList<>(solution.getImageUrls());
            updatedUrls.add(imageUrl);
            solution.setImageUrls(updatedUrls);
        }

        Solution updatedSolution = solutionRepository.save(solution);
        return SolutionDTO.fromEntity(updatedSolution);
    }

    // Remove image from solution
    public SolutionDTO removeImageFromSolution(String solutionId, String imageUrl) {
        Solution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new RuntimeException("Solution not found"));

        if (solution.getImageUrls() != null) {
            var updatedUrls = new java.util.ArrayList<>(solution.getImageUrls());
            updatedUrls.remove(imageUrl);
            solution.setImageUrls(updatedUrls.isEmpty() ? null : updatedUrls);
        }

        Solution updatedSolution = solutionRepository.save(solution);
        return SolutionDTO.fromEntity(updatedSolution);
    }

    // Add visualizer to solution
    public SolutionDTO addVisualizerToSolution(String solutionId, String visualizerFileId) {
        Solution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new RuntimeException("Solution not found"));

        if (solution.getVisualizerFileIds() == null) {
            solution.setVisualizerFileIds(List.of(visualizerFileId));
        } else {
            // Check limit (max 2 visualizers per solution)
            if (solution.getVisualizerFileIds().size() >= 2) {
                throw new RuntimeException("Maximum number of visualizers (2) reached for this solution");
            }
            
            var updatedFileIds = new java.util.ArrayList<>(solution.getVisualizerFileIds());
            updatedFileIds.add(visualizerFileId);
            solution.setVisualizerFileIds(updatedFileIds);
        }

        Solution updatedSolution = solutionRepository.save(solution);
        return SolutionDTO.fromEntity(updatedSolution);
    }

    // Remove visualizer from solution
    public SolutionDTO removeVisualizerFromSolution(String solutionId, String visualizerFileId) {
        Solution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new RuntimeException("Solution not found"));

        if (solution.getVisualizerFileIds() != null) {
            var updatedFileIds = new java.util.ArrayList<>(solution.getVisualizerFileIds());
            updatedFileIds.remove(visualizerFileId);
            solution.setVisualizerFileIds(updatedFileIds.isEmpty() ? null : updatedFileIds);
        }

        Solution updatedSolution = solutionRepository.save(solution);
        return SolutionDTO.fromEntity(updatedSolution);
    }

    // ENHANCED: Private helper methods for link validation

    /**
     * Validate and clean Google Drive link
     */
    private String validateAndCleanDriveLink(String driveLink) {
        if (driveLink == null || driveLink.trim().isEmpty()) {
            return null;
        }
        
        String cleanLink = driveLink.trim();
        
        // Validate Google Drive URL
        if (!cleanLink.contains("drive.google.com") && !cleanLink.contains("docs.google.com")) {
            throw new IllegalArgumentException("Invalid Google Drive link. Must be a valid Google Drive URL.");
        }
        
        // Ensure HTTPS
        if (!cleanLink.startsWith("http://") && !cleanLink.startsWith("https://")) {
            cleanLink = "https://" + cleanLink;
        }
        
        return cleanLink;
    }

    /**
     * Validate and clean YouTube link
     */
    private String validateAndCleanYoutubeLink(String youtubeLink) {
        if (youtubeLink == null || youtubeLink.trim().isEmpty()) {
            return null;
        }
        
        String cleanLink = youtubeLink.trim();
        
        // Validate YouTube URL
        if (!cleanLink.contains("youtube.com") && !cleanLink.contains("youtu.be")) {
            throw new IllegalArgumentException("Invalid YouTube link. Must be a valid YouTube URL.");
        }
        
        // Ensure HTTPS
        if (!cleanLink.startsWith("http://") && !cleanLink.startsWith("https://")) {
            cleanLink = "https://" + cleanLink;
        }
        
        return cleanLink;
    }
}