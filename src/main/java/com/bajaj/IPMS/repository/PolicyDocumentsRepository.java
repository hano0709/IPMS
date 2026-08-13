package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.PolicyDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyDocumentsRepository extends JpaRepository<PolicyDocuments, Long> {
}
