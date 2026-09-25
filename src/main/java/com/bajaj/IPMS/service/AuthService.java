package com.bajaj.IPMS.service;

import com.bajaj.IPMS.exception.DuplicateResourceException;
import com.bajaj.IPMS.exception.InvalidRequestException;
import com.bajaj.IPMS.exception.ResourceNotFoundException;
import com.bajaj.IPMS.exception.UnauthorizedException;
import com.bajaj.IPMS.model.RefreshToken;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.RefreshTokenRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.util.JwtUtil;
import com.bajaj.IPMS.util.PasswordValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
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

        log.info("Registration attempt for email: {}", request.getEmail());

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            log.warn("Registration Failed. Email already exists: {}", request.getEmail());

            throw new DuplicateResourceException("Email already in use");
        }

        if(!PasswordValidator.isValid(request.getPassword())){
            log.warn("Registration Failed. Invalid Password format");
            throw new InvalidRequestException("Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (request.getRole() != null && !request.getRole().isBlank()){
            user.setRole(request.getRole());
        } else {
            user.setRole("CUSTOMER");
        }

        User savedUser =  userRepository.save(user);

        log.info("User registered successfully. Email: {}, Role: {}", savedUser.getEmail(), savedUser.getRole());

        return savedUser;
    }

    public ResponseEntity<?> login(String email, String password){
        log.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed. User not found for email: {}", email);
                    return new UnauthorizedException("Invalid email or password");
                });;

        int loginAttempts = user.getFailedAttempts();

        if(loginAttempts >= 5){
            log.warn("Account locked for user: {}", email);
            throw new UnauthorizedException("Account is locked Out");
        }

        if(!passwordEncoder.matches(password, user.getPasswordHash())){
            user.setFailedAttempts(loginAttempts + 1);
            userRepository.save(user);
            log.warn("Invalid password for user: {}. Failed attempts: {}", email, loginAttempts + 1);
            throw new UnauthorizedException("Invalid email or password");
        }

        loginAttempts = 0;
        user.setFailedAttempts(loginAttempts);
        user.setLastLogin(Instant.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", email);

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshTokenValue = jwtUtil.generateRefreshToken(user.getEmail());

        log.debug("Generated JWT and refresh token for user: {}", email);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiry(Instant.now().plus(28, ChronoUnit.DAYS));
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

        if(refreshToken == null){
            throw new ResourceNotFoundException("Token not Found");
        }

        Instant refreshTokenExpiry = refreshToken.getExpiry();
        if(refreshToken.isRevoked() || Instant.now().isAfter(refreshTokenExpiry)){
            throw new  UnauthorizedException("Invalid or expired refresh token");
        }

        String email = jwtUtil.extractEmail(refreshTokenValue);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        String newAccessToken = jwtUtil.generateToken(email, user.getRole());

        refreshToken.setUpdatedAt(Instant.now());
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newRefreshTokenValue = jwtUtil.generateRefreshToken(email);
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setExpiry(Instant.now().plus(28, ChronoUnit.DAYS));
        newRefreshToken.setRevoked(false);
        newRefreshToken.setCreatedAt(Instant.now());
        newRefreshToken.setUpdatedAt(Instant.now());
        newRefreshToken.setCreatedBy(user.getId());

        refreshTokenRepository.save(newRefreshToken);

        return ResponseEntity.ok().body(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshTokenValue
        ));
    }

    public ResponseEntity<?> logout (String refreshTokenValue){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue);
        if (refreshToken ==null){
            log.warn("Logout failed. Refresh token not found.");
            throw new ResourceNotFoundException("Token not Found");
        }

        log.info("Logging out user");

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok("User has been logged out");
    }
}
