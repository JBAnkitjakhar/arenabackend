// src/main/java/com/algoarena/dto/auth/AuthResponse.java
package com.algoarena.dto.auth;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserInfo user;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, String refreshToken, UserInfo user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}