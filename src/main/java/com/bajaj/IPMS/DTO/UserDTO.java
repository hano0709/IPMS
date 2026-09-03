package com.bajaj.IPMS.DTO;

import com.bajaj.IPMS.model.User;
import jakarta.persistence.Column;

import java.time.Instant;

public class UserDTO {
    private long id;
    private String email;
    private int failedAttempts;
    private String role;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.failedAttempts = user.getFailedAttempts();
        this.role = user.getRole();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
