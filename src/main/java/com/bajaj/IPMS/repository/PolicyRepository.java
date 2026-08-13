package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Policy findByPolicyNumber(String policyNumber);

    List<Policy> findAllByCustomerId(Long customerId);
}
