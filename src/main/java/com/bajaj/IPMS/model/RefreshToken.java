package com.bajaj.IPMS.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String token;

    private Instant expiry;

    private boolean isRevoked;

    private Instant createdAt;

    private Instant updatedAt;

    private Long createdBy;

    public void setId(long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public Instant getExpiry() {
        return expiry;
    }
}
