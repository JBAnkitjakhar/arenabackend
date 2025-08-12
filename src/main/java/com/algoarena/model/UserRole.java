// src/main/java/com/algoarena/model/UserRole.java
package com.algoarena.model;

public enum UserRole {
    USER("user"),
    ADMIN("admin"),
    SUPERADMIN("superadmin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromString(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}