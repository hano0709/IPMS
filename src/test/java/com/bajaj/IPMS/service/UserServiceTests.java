package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Test
    public void testGetCurrUser(){
        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("test@example.com");

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        assertNotNull(userService.getCurrUser());
    }

    @Test
    public void testGetUser() {
        when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new IllegalArgumentException())
                .thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> userService.getUser("test@example.com"));
        assertNotNull(userService.getUser("test@example.com"));
    }
}
