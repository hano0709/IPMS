package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.RefreshToken;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.RefreshTokenRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.util.JwtUtil;
import com.bajaj.IPMS.util.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtil jwtUtil;

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

    public ResponseEntity<?> login(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        int loginAttempts = user.getFailedAttempts();

        if(loginAttempts >= 5){
            throw new IllegalArgumentException("Account is locked Out");
        }

        if(!passwordEncoder.matches(password, user.getPasswordHash())){
            user.setFailedAttempts(loginAttempts + 1);
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid email or password");
        }

        loginAttempts = 0;
        user.setFailedAttempts(loginAttempts);
        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshTokenValue = jwtUtil.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiry(Instant.now().plus(1, ChronoUnit.MINUTES));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setUpdatedAt(Instant.now());
        refreshToken.setCreatedBy(user.getId());

        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken" , accessToken,
                "refreshToken" , refreshTokenValue,
                "tokenType", "Bearer",
                "expiresIn", 86400000,
                "role", user.getRole()
                ));
    }

    public ResponseEntity<?> refreshToken(String refreshTokenValue){
        RefreshToken refreshToken =  refreshTokenRepository.findByToken(refreshTokenValue);

        Instant refreshTokenExpiry = refreshToken.getExpiry();
        if(refreshToken == null || refreshToken.isRevoked() || Instant.now().isAfter(refreshTokenExpiry)){
            return ResponseEntity.badRequest().body(Map.of("Error", "Invalid or expired refresh token"));
        }

        String email = jwtUtil.extractEmail(refreshTokenValue);
        String newAccessToken = jwtUtil.generateRefreshToken(email);

        refreshToken.setUpdatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok().body(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", refreshTokenValue
        ));
    }
}
