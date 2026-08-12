package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.RefreshToken;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.RefreshTokenRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @InjectMocks
    AuthService authService;

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    @Test
    public void testRegister(){

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Strongpass1!");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("sdjfhskdf");
        when(userRepository.save(any())).thenReturn(new User());

        assertNotNull(authService.register(request));
    }

    @Test
    public void testLogin(){
        User user = new User();
        user.setRole("CUSTOMER");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(userRepository.save(any())).thenReturn(user);
        when(jwtUtil.generateToken(any())).thenReturn("dfsdkjhfksdf");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("sjdfjsdkfjs");
        when(refreshTokenRepository.save(any())).thenReturn(new RefreshToken());

        assertNotNull(authService.login("test@example.com", "strongPass1!"));
    }

    @Test
    public void testRefreshToken(){
        RefreshToken refreshToken1 = new RefreshToken();
        refreshToken1.setRevoked(true);

        RefreshToken refreshToken2 = new RefreshToken();
        refreshToken2.setRevoked(false);
        refreshToken2.setExpiry(Instant.now().plus(30, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByToken(any()))
                .thenReturn(null)
                .thenReturn(refreshToken1)
                .thenReturn(refreshToken2);

        when(jwtUtil.extractEmail(any())).thenReturn("test@example.com");
        when(jwtUtil.generateToken(any())).thenReturn("dfkjsdfdsfdsfsf");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));
        when(refreshTokenRepository.save(any())).thenReturn(new RefreshToken());
        when(jwtUtil.generateRefreshToken(any())).thenReturn("sdfsjdfdsjfsfd");

        ResponseEntity<?> response1 = authService.refreshToken("sdsdsdsddsd");
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        ResponseEntity<?> response2 = authService.refreshToken("dsdsdsdsdsdsd");
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());

        ResponseEntity<?> response3 = authService.refreshToken("sdsdsddsdsds");
        assertEquals(HttpStatus.OK, response3.getStatusCode());
    }

    @Test
    public void testLogout(){
        when(refreshTokenRepository.findByToken("sdfsdfsfdsfs")).thenReturn(new RefreshToken());
        when(refreshTokenRepository.save(any())).thenReturn(new RefreshToken());

        ResponseEntity<?> response1 = authService.logout("sdfsdfsfdsfs");
        assertEquals(HttpStatus.OK, response1.getStatusCode());
    }
}
