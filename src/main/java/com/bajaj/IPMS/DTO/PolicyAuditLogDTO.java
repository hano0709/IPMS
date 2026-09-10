package com.bajaj.IPMS.DTO;

import com.bajaj.IPMS.model.Policy;
import com.bajaj.IPMS.model.PolicyAuditLog;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

public class PolicyAuditLogDTO {

    private Long id;
    private String policyNumber;
    private String previousStatus;
    private String newStatus;
    private Long changedBy;
    private String remarks;
    private Long createdBy;
    private OffsetDateTime changedAt;
    private OffsetDateTime createdAt;

    public PolicyAuditLogDTO(PolicyAuditLog policyAuditLog){
        this.id = policyAuditLog.getId();
        this.policyNumber = policyAuditLog.getPolicy().getPolicyNumber();
        this.previousStatus = policyAuditLog.getPreviousStatus();
        this.newStatus = policyAuditLog.getNewStatus();
        this.changedBy = policyAuditLog.getChangedBy();
        this.remarks = policyAuditLog.getRemarks();
        this.createdBy = policyAuditLog.getCreatedBy();
        this.changedAt = policyAuditLog.getChangedAt();
        this.createdAt = policyAuditLog.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Long changedBy) {
        this.changedBy = changedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
