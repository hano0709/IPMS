package com.bajaj.IPMS.security;

import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.model.Policy;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.repository.PolicyRepository;
import com.bajaj.IPMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PolicySecurity {

    @Autowired
    UserService userService;

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    CustomerRepository customerRepository;

    public boolean checkAuth(String policyNumber){
        User user = userService.getCurrUser();
        String role = user.getRole();
        Long id = user.getId();

        Policy policy = policyRepository.findByPolicyNumber(policyNumber);

        if ("ADMIN".equals(role) || "AGENT".equals(role)){
            return true;
        }

        Customer customer = customerRepository.findByUserId(id);

        if (policy.getCustomer().getId() == customer.getId()){
            return true;
        }

        return false;
    }
}
