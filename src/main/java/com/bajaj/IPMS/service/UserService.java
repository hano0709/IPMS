package com.bajaj.IPMS.service;

import com.bajaj.IPMS.exception.ForbiddenException;
import com.bajaj.IPMS.exception.ResourceNotFoundException;
import com.bajaj.IPMS.exception.UnauthorizedException;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrUser (){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null) {
            throw new UnauthorizedException("User Not Authenticated");
        }

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        return user;
    }

    public User getUser(String email){
        User user = userRepository.findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        return user;
    }
}
