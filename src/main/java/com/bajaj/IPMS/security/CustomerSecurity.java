package com.bajaj.IPMS.security;

import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.service.UserService;
import com.bajaj.IPMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CustomerSecurity {

    @Autowired
    UserService userService;

    @Autowired
    CustomerRepository customerRepository;

    public CustomerSecurity(UserService userService) {
        this.userService = userService;
    }

    public boolean checkAuth(Long customerId){

        User user = userService.getCurrUser();
        String role = user.getRole();
        if("AGENT".equals(role) || "ADMIN".equals(role)){
            return true;
        }

        Customer customer = customerRepository.findByUserId(user.getId());
        if (customer != null && customerId.equals(customer.getId())){
            return true;
        }
        return false;
    }
}
