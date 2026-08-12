package com.bajaj.IPMS.security;

import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.service.UserService;
import com.bajaj.IPMS.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CustomerSecurity {

    UserService userService;

    public CustomerSecurity(UserService userService) {
        this.userService = userService;
    }

    public boolean checkAuth(Long customerId){

        User user = userService.getCurrUser();
        Long id = user.getId();
        String role = user.getRole();
        if("AGENT".equals(role) || "ADMIN".equals(role) || customerId.equals(id)){
            return true;
        }

        return false;
    }
}
