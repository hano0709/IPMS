package com.bajaj.IPMS.service;

import com.bajaj.IPMS.DTO.Request.CreateAgentRequest;
import com.bajaj.IPMS.DTO.Response.AgentDTO;
import com.bajaj.IPMS.exception.ForbiddenException;
import com.bajaj.IPMS.exception.ResourceNotFoundException;
import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.model.Policy;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.AgentRepository;
import com.bajaj.IPMS.repository.PolicyRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.security.AgentSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

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
    PolicyRepository policyRepository;

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

        CreateAgentRequest request = new CreateAgentRequest();
        request.setEmail("agent1@example.com");
        request.setPassword("Strongpass1!");
        request.setFullName("Agent1");
        request.setLicenseNumber("LIC-987654");

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
        assertThrows(ForbiddenException.class,() -> agentService.getAgent(1L));
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

    @Test
    public void testSearchByCode(){
        User user = new User();
        user.setId(1L);

        Agent agent = new Agent();
        agent.setAgentCode("AG123");
        agent.setUser(user);

        List<Agent> agentList = new ArrayList<>();
        agentList.add(agent);

        when(agentRepository.findByAgentCodeContainingIgnoreCase(any())).thenReturn(agentList);

        List<AgentDTO> result = agentService.searchByCode("AG123");

        assertEquals(1, result.size());
        assertEquals("AG123", result.get(0).getAgentCode());
    }

    @Test
    public void testGetPolicies(){
        User user = new User();
        user.setId(1L);

        Agent agent = new Agent();
        agent.setId(1L);
        agent.setUser(user);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);

        Policy policy = new Policy();
        policy.setAgent(agent);
        policy.setCustomer(customer);
        List<Policy> policies = new ArrayList<>();
        policies.add(policy);

        when(userService.getCurrUser()).thenReturn(user);
        when(agentRepository.findByUserId(any())).thenReturn(agent);
        when(policyRepository.findAllByAgentId(any())).thenReturn(policies);

        ResponseEntity<?> response = agentService.getPolicies();

        assertNotNull(response);
    }

    @Test
    public void testGetCurrAgent(){
        User user = new User();
        user.setId(1L);

        Agent agent = new Agent();
        agent.setId(1L);
        agent.setUser(user);

        when(userService.getCurrUser()).thenReturn(user);
        when(agentRepository.findByUserId(any())).thenReturn(agent);

        ResponseEntity<?> response = agentService.getCurrAgent();

        assertNotNull(response);
    }
}
