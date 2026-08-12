package com.bajaj.IPMS.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String agentCode;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String fullName;
    private String licenseNumber;
    private boolean isActive;
    private Long createdBy;

    public Long getId() {
        return id;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public User getUser() {
        return user;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
