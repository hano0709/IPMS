package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Policy findByPolicyNumber(String policyNumber);

    List<Policy> findAllByCustomerId(Long customerId);

    @Query("SELECT p from Policy p WHERE p.endDate BETWEEN :today AND :expiry")
    List<Policy> findAllByExpiringSoon(@Param("today") LocalDate today,
                                       @Param("expiry") LocalDate expiry);
}
