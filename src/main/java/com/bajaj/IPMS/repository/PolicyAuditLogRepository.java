package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.PolicyAuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyAuditLogRepository extends JpaRepository<PolicyAuditLog, Long> {
    List<PolicyAuditLog> findAllByPolicyId(Long id);

    @Query("SELECT p FROM PolicyAuditLog p ORDER BY p.changedAt DESC")
    List<PolicyAuditLog> findLast10Changes(Pageable pageble);
}
