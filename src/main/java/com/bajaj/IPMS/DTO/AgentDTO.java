package com.bajaj.IPMS.DTO;

import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

public class AgentDTO {

    private Long id;
    private String agentCode;
    private Long userId;
    private String fullName;
    private String licenseNumber;
    private boolean isActive;
    private Long createdBy;

    public AgentDTO(Agent agent) {
        this.id = agent.getId();
        this.agentCode = agent.getAgentCode();
        this.userId = agent.getUser().getId();
        this.fullName = agent.getFullName();
        this.licenseNumber = agent.getLicenseNumber();
        this.isActive = agent.isActive();
        this.createdBy = agent.getCreatedBy();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
