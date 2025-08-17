// src/main/java/com/algoarena/controller/compiler/CompilerController.java
package com.algoarena.controller.compiler;

import com.algoarena.dto.compiler.ExecutionRequest;
import com.algoarena.dto.compiler.ExecutionResponse;
import com.algoarena.service.compiler.PistonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/compiler")
@PreAuthorize("isAuthenticated()")
public class CompilerController {

    @Autowired
    private PistonService pistonService;

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeCode(@Valid @RequestBody ExecutionRequest request) {
        try {
            ExecutionResponse result = pistonService.executeCode(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Code execution failed");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/runtimes")
    public ResponseEntity<Map<String, Object>> getRuntimes() {
        try {
            List<Map<String, Object>> runtimes = pistonService.getRuntimes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", runtimes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch runtimes");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        try {
            List<String> languages = pistonService.getSupportedLanguages();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", languages);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch supported languages");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = pistonService.testConnection();
        
        if ("success".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(503).body(result);
        }
    }
}

 