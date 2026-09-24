package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.DTO.Request.LoginRequest;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController (AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    //DEBUG: Later make register only for ADMIN
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        User user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        ResponseEntity<?> response = authService.login(request.getEmail(), request.getPassword());
        return response;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken (@RequestBody Map<String, String> request){
        String refreshToken = request.get("refreshToken");
        return authService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request){
        String refreshToken = request.get("refreshToken");
        return authService.logout(refreshToken);
    }

}
