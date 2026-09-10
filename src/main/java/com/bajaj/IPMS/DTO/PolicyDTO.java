package com.bajaj.IPMS.DTO;

import com.bajaj.IPMS.model.Policy;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyDTO {
    private Long id;
    private String policyNumber;
    private String policyType;
    private String policyStatus;
    private String customerCode;
    private String agentCode;
    private BigDecimal sumInsured;
    private BigDecimal premiumAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Long createdBy;

    public PolicyDTO(Policy policy){
        this.id = policy.getId();
        this.policyNumber = policy.getPolicyNumber();
        this.policyType = policy.getPolicyType();
        this.policyStatus = policy.getStatus();
        this.customerCode = policy.getCustomer().getCustomerCode();
        this.agentCode = policy.getAgent().getAgentCode();
        this.sumInsured = policy.getSumInsured();
        this.premiumAmount = policy.getPremiumAmount();
        this.startDate = policy.getStartDate();
        this.endDate = policy.getEndDate();
        this.description = policy.getDescription();
        this.createdBy = policy.getCreatedBy();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(String policyStatus) {
        this.policyStatus = policyStatus;
    }

    public BigDecimal getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(BigDecimal sumInsured) {
        this.sumInsured = sumInsured;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
