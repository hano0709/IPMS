package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Page<Customer> getAll(Pageable pageable){
        return customerRepository.findAll(pageable);
    }

    public Customer getCustomer(Long customerId){
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer Not Found"));
    }
}
