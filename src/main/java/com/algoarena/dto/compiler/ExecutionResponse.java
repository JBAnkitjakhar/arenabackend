// src/main/java/com/algoarena/dto/compiler/ExecutionResponse.java
package com.algoarena.dto.compiler;

public class ExecutionResponse {

    private String language;
    private String version;
    private RunResult run;
    private CompileResult compile; // For compiled languages

    // Inner class for run results
    public static class RunResult {
        private String stdout;
        private String stderr;
        private int code; // Exit code
        private String signal; // Signal if terminated
        private String output; // Combined stdout + stderr

        // Constructors
        public RunResult() {}

        // Getters and Setters
        public String getStdout() { return stdout; }
        public void setStdout(String stdout) { this.stdout = stdout; }
        public String getStderr() { return stderr; }
        public void setStderr(String stderr) { this.stderr = stderr; }
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
    }

    // Inner class for compile results
    public static class CompileResult {
        private String stdout;
        private String stderr;
        private int code;
        private String output;

        // Constructors
        public CompileResult() {}

        // Getters and Setters
        public String getStdout() { return stdout; }
        public void setStdout(String stdout) { this.stdout = stdout; }
        public String getStderr() { return stderr; }
        public void setStderr(String stderr) { this.stderr = stderr; }
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
    }

    // Constructors
    public ExecutionResponse() {}

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

    public RunResult getRun() {
        return run;
    }

    public void setRun(RunResult run) {
        this.run = run;
    }

    public CompileResult getCompile() {
        return compile;
    }

    public void setCompile(CompileResult compile) {
        this.compile = compile;
    }

    // Helper methods
    public boolean hasCompileError() {
        return compile != null && compile.getCode() != 0;
    }

    public boolean hasRuntimeError() {
        return run != null && run.getCode() != 0;
    }

    public boolean isSuccessful() {
        return !hasCompileError() && !hasRuntimeError();
    }

    public String getOutput() {
        if (run != null && run.getOutput() != null) {
            return run.getOutput();
        }
        if (run != null) {
            StringBuilder output = new StringBuilder();
            if (run.getStdout() != null) {
                output.append(run.getStdout());
            }
            if (run.getStderr() != null) {
                if (output.length() > 0) output.append("\n");
                output.append(run.getStderr());
            }
            return output.toString();
        }
        return "";
    }

    public String getErrorMessage() {
        if (hasCompileError() && compile != null) {
            return compile.getStderr() != null ? compile.getStderr() : "Compilation failed";
        }
        if (hasRuntimeError() && run != null) {
            return run.getStderr() != null ? run.getStderr() : "Runtime error";
        }
        return null;
    }
}