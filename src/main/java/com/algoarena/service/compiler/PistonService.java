// src/main/java/com/algoarena/service/compiler/PistonService.java
package com.algoarena.service.compiler;

import com.algoarena.config.AppConfig;
import com.algoarena.dto.compiler.ExecutionRequest;
import com.algoarena.dto.compiler.ExecutionResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;

@Service
public class PistonService {

    @Autowired
    private AppConfig appConfig;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PistonService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Execute code using Piston API
    public ExecutionResponse executeCode(ExecutionRequest request) {
        try {
            String pistonUrl = appConfig.getPiston().getApiUrl() + "/execute";
            
            // Prepare request body for Piston API
            Map<String, Object> pistonRequest = new HashMap<>();
            pistonRequest.put("language", request.getLanguage());
            pistonRequest.put("version", request.getVersion());
            
            // Prepare files array
            List<Map<String, String>> files = new ArrayList<>();
            Map<String, String> mainFile = new HashMap<>();
            mainFile.put("content", request.getCode());
            files.add(mainFile);
            
            // Add additional files if provided
            if (request.getFiles() != null) {
                for (ExecutionRequest.FileContent file : request.getFiles()) {
                    Map<String, String> additionalFile = new HashMap<>();
                    additionalFile.put("name", file.getName());
                    additionalFile.put("content", file.getContent());
                    files.add(additionalFile);
                }
            }
            
            pistonRequest.put("files", files);
            
            // Add stdin if provided
            if (request.getStdin() != null && !request.getStdin().isEmpty()) {
                pistonRequest.put("stdin", request.getStdin());
            }
            
            // Add args if provided
            if (request.getArgs() != null && !request.getArgs().isEmpty()) {
                pistonRequest.put("args", request.getArgs());
            }

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(pistonRequest, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(pistonUrl, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseExecutionResponse(response.getBody());
            } else {
                throw new RuntimeException("Piston API returned status: " + response.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            throw new RuntimeException("Failed to connect to Piston API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Code execution failed: " + e.getMessage());
        }
    }

    // Get available runtimes from Piston API
    public List<Map<String, Object>> getRuntimes() {
        try {
            String runtimesUrl = appConfig.getPiston().getApiUrl() + "/runtimes";
            
            ResponseEntity<String> response = restTemplate.getForEntity(runtimesUrl, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
                return objectMapper.readValue(response.getBody(), typeRef);
            } else {
                throw new RuntimeException("Failed to fetch runtimes from Piston API");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get runtimes: " + e.getMessage());
        }
    }

    // Get supported languages (extracted from runtimes)
    public List<String> getSupportedLanguages() {
        try {
            List<Map<String, Object>> runtimes = getRuntimes();
            Set<String> languages = new HashSet<>();
            
            for (Map<String, Object> runtime : runtimes) {
                String language = (String) runtime.get("language");
                if (language != null) {
                    languages.add(language);
                }
            }
            
            return new ArrayList<>(languages);
        } catch (Exception e) {
            // Fallback to common languages if API fails
            return Arrays.asList(
                "javascript", "python", "java", "cpp", "c", "csharp", 
                "go", "rust", "kotlin", "typescript", "php", "ruby"
            );
        }
    }

    // Check if Piston API is healthy
    public boolean isHealthy() {
        try {
            String runtimesUrl = appConfig.getPiston().getApiUrl() + "/runtimes";
            ResponseEntity<String> response = restTemplate.getForEntity(runtimesUrl, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    // FIXED: Parse Piston API response to our ExecutionResponse DTO
    @SuppressWarnings("unchecked")
    private ExecutionResponse parseExecutionResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, 
                new TypeReference<Map<String, Object>>() {});
            
            ExecutionResponse executionResponse = new ExecutionResponse();
            executionResponse.setLanguage((String) responseMap.get("language"));
            executionResponse.setVersion((String) responseMap.get("version"));
            
            // FIXED: Parse run results with proper casting
            Object runObj = responseMap.get("run");
            if (runObj instanceof Map) {
                Map<String, Object> runMap = (Map<String, Object>) runObj;
                ExecutionResponse.RunResult runResult = new ExecutionResponse.RunResult();
                runResult.setStdout((String) runMap.get("stdout"));
                runResult.setStderr((String) runMap.get("stderr"));
                runResult.setCode(getIntegerValue(runMap.get("code")));
                runResult.setSignal((String) runMap.get("signal"));
                runResult.setOutput((String) runMap.get("output"));
                executionResponse.setRun(runResult);
            }
            
            // FIXED: Parse compile results (for compiled languages) with proper casting
            Object compileObj = responseMap.get("compile");
            if (compileObj instanceof Map) {
                Map<String, Object> compileMap = (Map<String, Object>) compileObj;
                ExecutionResponse.CompileResult compileResult = new ExecutionResponse.CompileResult();
                compileResult.setStdout((String) compileMap.get("stdout"));
                compileResult.setStderr((String) compileMap.get("stderr"));
                compileResult.setCode(getIntegerValue(compileMap.get("code")));
                compileResult.setOutput((String) compileMap.get("output"));
                executionResponse.setCompile(compileResult);
            }
            
            return executionResponse;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse execution response: " + e.getMessage());
        }
    }

    // Helper method to safely convert Object to Integer
    private int getIntegerValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            return 0; // Default value
        }
    }

    // Get language version for a specific language
    public String getLanguageVersion(String language) {
        try {
            List<Map<String, Object>> runtimes = getRuntimes();
            
            for (Map<String, Object> runtime : runtimes) {
                if (language.equals(runtime.get("language"))) {
                    return (String) runtime.get("version");
                }
            }
            
            // Fallback versions for common languages
            Map<String, String> fallbackVersions = Map.of(
                "javascript", "18.15.0",
                "python", "3.10.0",
                "java", "15.0.2",
                "cpp", "10.2.0",
                "c", "10.2.0"
            );
            
            return fallbackVersions.getOrDefault(language, "latest");
            
        } catch (Exception e) {
            return "latest"; // Fallback
        }
    }

    // Get runtime info for a specific language
    public Map<String, Object> getLanguageInfo(String language) {
        try {
            List<Map<String, Object>> runtimes = getRuntimes();
            
            return runtimes.stream()
                    .filter(runtime -> language.equals(runtime.get("language")))
                    .findFirst()
                    .orElse(new HashMap<>());
                    
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    // Test connection to Piston API
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            List<Map<String, Object>> runtimes = getRuntimes();
            long endTime = System.currentTimeMillis();
            
            result.put("status", "success");
            result.put("responseTime", endTime - startTime);
            result.put("runtimesCount", runtimes.size());
            result.put("apiUrl", appConfig.getPiston().getApiUrl());
            
        } catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
            result.put("apiUrl", appConfig.getPiston().getApiUrl());
        }
        
        return result;
    }
}