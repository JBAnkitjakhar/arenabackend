// src/main/java/com/algoarena/model/QuestionLevel.java
package com.algoarena.model;

public enum QuestionLevel {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    private final String value;

    QuestionLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QuestionLevel fromString(String value) {
        for (QuestionLevel level : QuestionLevel.values()) {
            if (level.value.equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown question level: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}