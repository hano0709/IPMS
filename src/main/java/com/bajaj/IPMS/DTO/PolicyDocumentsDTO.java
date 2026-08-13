package com.bajaj.IPMS.DTO;

import com.bajaj.IPMS.model.PolicyDocuments;

public class PolicyDocumentsDTO {

    private Long id;
    private Long policyId;
    private String fileName;
    private String filePath;
    private Long uploadedBy;

    public PolicyDocumentsDTO(PolicyDocuments policyDocuments){
        this.id = policyDocuments.getId();
        this.policyId = policyDocuments.getPolicy().getId();
        this.fileName = policyDocuments.getFileName();
        this.filePath = policyDocuments.getFilePath();
        this.uploadedBy = policyDocuments.getUploadedBy();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
