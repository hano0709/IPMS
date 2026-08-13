package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.PolicyAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyAuditLogRespository extends JpaRepository<PolicyAuditLog, Long> {
    PolicyAuditLog findByPolicyId(Long id);
}
