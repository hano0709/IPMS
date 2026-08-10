package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.service.AuthService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request){
        try {
            ResponseEntity<?> response = authService.login(request.get("email"), request.get("password"));
            return response;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken (@RequestBody Map<String, String> request){
        String refreshToken = request.get("refreshToken");
        return authService.refreshToken(refreshToken);
    }

    //{ "accessToken": "eyJhbGci...", "refreshToken": "d8f3a1...", "tokenType": "Bearer", "expiresIn": 86400,
    //"role": "AGENT" }

}
