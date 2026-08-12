package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.AgentRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.security.AgentSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTests {

    @InjectMocks
    AgentService agentService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    AuthService authService;

    @Mock
    AgentSecurity agentSecurity;

    @Mock
    AgentRepository agentRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @Test
    public void testCreateAgent(){
        User mockUser = new User();
        mockUser.setEmail("agent1@example.com");
        mockUser.setPasswordHash(passwordEncoder.encode("Strongpass1!"));
        mockUser.setRole("AGENT");
        when(authService.register(any())).thenReturn(mockUser);

        when(agentRepository.count()).thenReturn(0L);

        User mockAdmin = new User();
        mockAdmin.setId(2);
        when(userService.getCurrUser()).thenReturn(mockAdmin);

        when(agentRepository.save(any())).thenReturn(new Agent());

        Map<String, String> request = new HashMap<>();
        request.put("email", "agent1@example.com");
        request.put("password", "Strongpass1!");
        request.put("fullName", "Agent1");
        request.put("licenseNumber", "LIC-987654");

        assertNotNull(agentService.createAgent(request));
    }

    @Test
    public void testGetAgent(){
        Agent agent = new Agent();
        when(agentSecurity.checkAuth(any()))
                .thenReturn(true)
                .thenReturn(false);

        when(agentRepository.findById(any())).thenReturn(Optional.of(agent));

        assertNotNull(agentService.getAgent(1L));
        assertThrows(IllegalArgumentException.class,() -> agentService.getAgent(1L));
    }

    @Test
    public void testDeleteAgent(){
        Agent agent = new Agent();
        agent.setId(1L);

        when(agentRepository.findByAgentCode("sdfdsfsdf")).thenReturn(agent);
        doNothing().when(agentRepository).delete(agent);
        doNothing().when(userRepository).deleteById(1L);

        assertNotNull(agentService.deleteAgent("sdfdsfsdf"));
    }
}
