package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.security.CustomerSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    CustomerSecurity customerSecurity;

    public Page<Customer> getAll(Pageable pageable){
        return customerRepository.findAll(pageable);
    }

    public Customer getCustomer(Long customerId){
        if(customerSecurity.checkAuth(customerId)) {
            return customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer Not Found"));
        } else {
            throw new IllegalArgumentException("No Authorisation");
        }
    }
}
