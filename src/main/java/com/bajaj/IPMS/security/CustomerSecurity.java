package com.bajaj.IPMS.security;

import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CustomerSecurity {

    UserRepository userRepository;

    public CustomerSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean checkAuth(Long customerId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null) return false;

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User Not Found"));

        Long id = user.getId();
        String role = user.getRole();
        if("AGENT".equals(role) || "ADMIN".equals(role) || customerId.equals(id)){
            return true;
        }

        return false;
    }
}
