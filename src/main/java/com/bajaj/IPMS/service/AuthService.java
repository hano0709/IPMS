package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.util.JwtUtil;
import com.bajaj.IPMS.util.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    PasswordValidator passwordValidator;

    public User register(RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email already in use");
        }

        if(!PasswordValidator.isValid(request.getPassword())){
            throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setRole("CUSTOMER");

        return userRepository.save(user);
    }

    public String login(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if(!passwordEncoder.matches(password, user.getPasswordHash())){
            throw new IllegalArgumentException("Invalid email or password");
        }

        return JwtUtil.generateToken(user.getEmail());
    }
}
