package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByCustomerCode(String customerCode);

    Customer findByUserId(Long id);
}
