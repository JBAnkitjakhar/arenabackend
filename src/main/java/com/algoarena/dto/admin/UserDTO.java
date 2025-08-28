// src/main/java/com/algoarena/dto/admin/UserDTO.java
package com.algoarena.dto.admin;

import com.algoarena.model.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private String id;
    private String name;
    private String email;
    private String image;
    private UserRole role;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private boolean isPrimarySuperAdmin;

    // Constructors
    public UserDTO() {}

    public UserDTO(String id, String name, String email, String image, UserRole role, 
                   LocalDateTime createdAt, LocalDateTime updatedAt, boolean isPrimarySuperAdmin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.image = image;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isPrimarySuperAdmin = isPrimarySuperAdmin;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPrimarySuperAdmin() {
        return isPrimarySuperAdmin;
    }

    public void setIsPrimarySuperAdmin(boolean isPrimarySuperAdmin) {
        this.isPrimarySuperAdmin = isPrimarySuperAdmin;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", isPrimarySuperAdmin=" + isPrimarySuperAdmin +
                '}';
    }
}