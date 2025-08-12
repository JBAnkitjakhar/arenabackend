// src/main/java/com/algoarena/dto/compiler/ExecutionRequest.java
package com.algoarena.dto.compiler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ExecutionRequest {

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Version is required")
    private String version;

    @NotBlank(message = "Code is required")
    @Size(max = 50000, message = "Code must not exceed 50,000 characters")
    private String code;

    private String stdin; // Input for the program
    private List<String> args; // Command line arguments
    private List<FileContent> files; // Additional files

    // Inner class for file content
    public static class FileContent {
        private String name;
        private String content;

        // Constructors
        public FileContent() {}

        public FileContent(String name, String content) {
            this.name = name;
            this.content = content;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Constructors
    public ExecutionRequest() {}

    public ExecutionRequest(String language, String version, String code) {
        this.language = language;
        this.version = version;
        this.code = code;
    }

    // Getters and Setters
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<FileContent> getFiles() {
        return files;
    }

    public void setFiles(List<FileContent> files) {
        this.files = files;
    }
}